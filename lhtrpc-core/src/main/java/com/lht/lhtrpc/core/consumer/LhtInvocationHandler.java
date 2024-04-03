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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 消费端动态代理处理类
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
        int readTimeout = Integer.parseInt(context.getParamerters().getOrDefault("app.okhttp.readTimeout", "1000"));
        int writeTimeout = Integer.parseInt(context.getParamerters().getOrDefault("app.okhttp.writeTimeout", "1000"));
        int connectTimeout = Integer.parseInt(context.getParamerters().getOrDefault("app.okhttp.connectTimeout", "1000"));
        this.httpInvoker = new OkHttpInvoker(readTimeout, writeTimeout, connectTimeout);
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(this::halfOpen, 10, 60, TimeUnit.SECONDS);
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
        int retries = Integer.parseInt(context.getParamerters().getOrDefault("app.retry", "0"));

        while (retries-- > 0) {

            log.debug("========> retries:" + retries);

            try{
                //这里用lambdm表达式不能返回，所有用foreach
                for (Filter filter : context.getFilters()) {
                    Object response=filter.prefilter(rpcRequest);
                    if (response != null) {
                        log.debug(filter.getClass().getName() + "===> prefilter: " + response);
                        return response;
                    }
                }

                InstanceMeta instance = null;

                synchronized (halfOpenProviders){
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
                }catch(Exception e){
                    synchronized (windows){
                        tryIsolate(url, instance);
                    }
                    throw e;
                }

                synchronized (providers){
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
            }catch (RuntimeException e){
                if (!(e.getCause() instanceof SocketTimeoutException)) {
                    throw new RpcException(e);
                }
            }
        }

        return null;

    }

    private void tryIsolate(String url, InstanceMeta instance) {
        // 故障的规则统计和隔离,
        // 每一次异常，记录一次，统计30s的异常数
        // 用一个环形数组统计一定时间的异常数，这里一个数组下标对应1s，默认30s，后面会拿到sum的值就是30s总的异常数
        SlidingTimeWindow window = windows.computeIfAbsent(url, t -> new SlidingTimeWindow());
        window.record(System.currentTimeMillis());
        log.debug("instance {} in window with {}", url, window.getSum());

        //发生10次，就做故障隔离
        //如果多次调用，30s内超过10次，这里isolate同一节点会被隔离多次，需要判断没在isolate里面才添加，或者用set
        if (window.getSum()>=10) {
            isolate(instance);
        }
    }

    private void isolate(InstanceMeta instance) {
        log.debug("==> isolate instance {}", instance);
        providers.remove(instance);
        log.debug("==> providers = {}", providers);
//        if (!isolateProviders.contains(instance)) {
            isolateProviders.add(instance);
//        }
        log.debug("==> isolateProviders = {}", isolateProviders);
    }

    @Nullable
    private static Object castToResult(Method method, RpcResponse rpcResponse) throws Exception {
        //这里如果不转，返回的其实是一个jsonObject对象，但是服务端调用返回的需要是具体的对象，所以需要进行转换(序列化和反序列化？)
        if (rpcResponse.isStatus()) {
            return TypeUtils.buildResponse(method, rpcResponse);
        } else {
            //异常不能直接返回，会类转换失败，直接抛出去就好，抛的时候可以控制，是所有堆栈信息都返回去，还是只返回主要信息，这里只返回主要信息
            Exception exception = rpcResponse.getEx();
            if (exception instanceof RpcException ex) {
                throw ex;
            }
            throw new RpcException(exception, RpcException.UnKnowEx);
        }
    }
}
