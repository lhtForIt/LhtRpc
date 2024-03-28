package com.lht.lhtrpc.core.consumer;

import com.lht.lhtrpc.core.api.*;
import com.lht.lhtrpc.core.meta.InstanceMeta;
import com.lht.lhtrpc.core.utils.MethodUtils;
import com.lht.lhtrpc.core.utils.TypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 消费端动态代理处理类
 * @author Leo
 * @date 2024/03/11
 */
@Slf4j
public class LhtInvocationHandler implements InvocationHandler {


    private Class<?> service;
    private List<InstanceMeta> providers;

    private RpcContext context;
    private HttpInvoker httpInvoker;

    public LhtInvocationHandler(Class<?> service, RpcContext context, List<InstanceMeta> providers, HttpInvoker httpInvoker) {
        this.service = service;
        this.context = context;
        this.providers = providers;
        this.httpInvoker = httpInvoker;
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

        //这里用lambdm表达式不能返回，所有用foreach
        for (Filter filter : context.getFilters()) {
            Object response=filter.prefilter(rpcRequest);
            if (response != null) {
                log.debug(filter.getClass().getName() + "===> prefilter: " + response);
                return response;
            }
        }

        List<InstanceMeta> nodes = context.getRouter().route(providers);
        InstanceMeta node = context.getLoadBalancer().choose(nodes);
        String url = node.toUrl();
        log.debug("loadBalancer.choose(urls) ==> " + url);

        RpcResponse rpcResponse = httpInvoker.post(rpcRequest, url);
        Object result = castToResult(method, rpcResponse);

        for (Filter filter : context.getFilters()) {
            Object filterResult = filter.postfilter(rpcRequest, rpcResponse, result);
            if (filterResult != null) {
                return filterResult;
            }
        }

        return result;
    }

    @Nullable
    private static Object castToResult(Method method, RpcResponse rpcResponse) throws Exception {
        //这里如果不转，返回的其实是一个jsonObject对象，但是服务端调用返回的需要是具体的对象，所以需要进行转换(序列化和反序列化？)
        if (rpcResponse.isStatus()) {
            return TypeUtils.buildResponse(method, rpcResponse);
        } else {
            //异常不能直接返回，会类转换失败，直接抛出去就好，抛的时候可以控制，是所有堆栈信息都返回去，还是只返回主要信息，这里只返回主要信息
            Exception exception = rpcResponse.getEx();
            if (exception instanceof LhtRpcException ex) {
                throw ex;
            }
            throw new LhtRpcException(exception, LhtRpcException.UnKnowEx);
        }
    }
}
