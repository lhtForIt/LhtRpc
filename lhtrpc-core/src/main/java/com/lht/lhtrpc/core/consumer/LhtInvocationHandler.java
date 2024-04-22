package com.lht.lhtrpc.core.consumer;

import com.lht.lhtrpc.core.api.*;
import com.lht.lhtrpc.core.consumer.http.OkHttpInvoker;
import com.lht.lhtrpc.core.governance.SlidingTimeWindow;
import com.lht.lhtrpc.core.meta.InstanceMeta;
import com.lht.lhtrpc.core.utils.MethodUtils;
import com.lht.lhtrpc.core.utils.TypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 消费端动态代理处理类
 *
 * @author Leo
 * @date 2024/03/11
 */
@Slf4j
public class LhtInvocationHandler implements InvocationHandler {


    private Class<?> service;
    private final List<InstanceMeta> providers;
    private List<InstanceMeta> isolateProviders = new ArrayList<>();
    private final List<InstanceMeta> halfOpenProviders = new ArrayList<>();
    private RpcContext context;
    private HttpInvoker httpInvoker;

    private Map<String, SlidingTimeWindow> windows = new HashMap<>();

    private ScheduledExecutorService executor;

    public LhtInvocationHandler(Class<?> service, RpcContext context, List<InstanceMeta> providers) {
        this.service = service;
        this.context = context;
        this.providers = providers;
        int readTimeout = context.getConsumerProperties().getOkhttp().getReadTimeout();
        int writeTimeout = context.getConsumerProperties().getOkhttp().getWriteTimeout();
        int connectTimeout = context.getConsumerProperties().getOkhttp().getConnectTimeout();
        int halfOpenInitialDelay = context.getConsumerProperties().getHalfOpenInitialDelay();
        int halfOpenDelay = context.getConsumerProperties().getHalfOpenDelay();
        this.httpInvoker = new OkHttpInvoker(readTimeout, writeTimeout, connectTimeout);
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(this::halfOpen, halfOpenInitialDelay, halfOpenDelay, TimeUnit.SECONDS);
    }

    private void halfOpen() {
        log.debug("========> half open isolateProviders: {}", isolateProviders);
        halfOpenProviders.clear();
        log.debug("========> after half open clear halfOpenProviders={}, isolateProviders={} ", halfOpenProviders, isolateProviders);
        halfOpenProviders.addAll(isolateProviders);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (Object.class.equals(method.getDeclaringClass())) {
            return null;
        }
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(service.getCanonicalName());
        rpcRequest.setMethodSign(MethodUtils.buildMethodSign(method));
        //map如果Key不是string json格式化会有问题，另一边解析不了
        Object[] newArg = TypeUtils.initMapKey(args);
        rpcRequest.setArgs(newArg);


        //超时重试，没配置默认不重试
        int retries = context.getConsumerProperties().getRetry();
        int faultLimit = context.getConsumerProperties().getFaultLimit();

        while (retries-- > 0) {

            log.debug("========> retries:" + retries);

            try {
                //这里用lambda表达式不能返回，所有用foreach
                for (Filter filter : context.getFilters()) {
                    Object response = filter.prefilter(rpcRequest);
                    if (response != null) {
                        log.debug(filter.getClass().getName() + "===> prefilter: " + response);
                        return response;
                    }
                }

                InstanceMeta instance = null;

                synchronized (halfOpenProviders) {
                    log.debug("halfOpenProviders={}", halfOpenProviders);
                    if (halfOpenProviders.isEmpty()) {
                        List<InstanceMeta> nodes = context.getRouter().route(providers);
                        instance = context.getLoadBalancer().choose(nodes);
                        log.debug("loadBalancer.choose(urls) ==> {}", instance);
                    } else {
                        instance = halfOpenProviders.remove(0);
                        log.debug("check alive instance ===> {}", instance);
                    }
                }


                RpcResponse rpcResponse = null;
                Object result = null;
                String url = instance.toUrl();
                try {
                    rpcResponse = httpInvoker.post(rpcRequest, url);
                    result = castToResult(method, rpcResponse);
                } catch (Exception e) {
                    synchronized (windows) {
                        tryIsolate(url, instance, faultLimit);
                    }
                    throw e;
                }

                synchronized (providers) {
                    if (!providers.contains(instance)) {
                        isolateProviders.remove(instance);
                        providers.add(instance);
                        log.debug("instance {} is recovered, isolateProviders={}, providers={}", instance, isolateProviders, providers);
                    }
                }


                for (Filter filter : context.getFilters()) {
                    Object filterResult = filter.postfilter(rpcRequest, rpcResponse, result);
                    if (filterResult != null) {
                        return filterResult;
                    }
                }

                return result;
            } catch (RuntimeException e) {
                if (!(e.getCause() instanceof SocketTimeoutException)) {
                    throw new RpcException(e);
                }
            }
        }

        return null;

    }

    private void tryIsolate(String url, InstanceMeta instance, int faultLimit) {
        // 故障的规则统计和隔离,
        // 每一次异常，记录一次，统计30s的异常数
        // 用一个环形数组统计一定时间的异常数，这里一个数组下标对应1s，默认30s，后面会拿到sum的值就是30s总的异常数
        SlidingTimeWindow window = windows.computeIfAbsent(url, t -> new SlidingTimeWindow());
        window.record(System.currentTimeMillis());
        log.debug("instance {} in window with {}", url, window.getSum());

        //发生10次，就做故障隔离
        //如果多次调用，30s内超过10次，这里isolate同一节点会被隔离多次，需要判断没在isolate里面才添加，或者用set
        if (window.getSum() >= faultLimit) {
            isolate(instance);
        }
    }

    private void isolate(InstanceMeta instance) {
        log.debug("==> isolate instance {}", instance);
        //没有这个元素直接返回，避免重复隔离
        if (!providers.remove(instance)) return;
        log.debug("==> providers = {}", providers);
        isolateProviders.add(instance);
        log.debug("==> isolateProviders = {}", isolateProviders);
    }

    @Nullable
    private static Object castToResult(Method method, RpcResponse rpcResponse) throws Exception {
        if (rpcResponse.isStatus()) {
            return TypeUtils.buildResponse(method, rpcResponse);
        } else {
            RpcException exception = rpcResponse.getEx();
            if (exception != null) {
                log.debug("response error {}", exception);
                throw exception;
            }
            throw null;
        }
    }
}
