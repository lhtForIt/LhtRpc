package com.lht.lhtrpc.core.consumer;

import com.lht.lhtrpc.core.api.RpcContext;
import com.lht.lhtrpc.core.api.RpcRequest;
import com.lht.lhtrpc.core.api.RpcResponse;
import com.lht.lhtrpc.core.utils.MethodUtils;
import com.lht.lhtrpc.core.utils.TypeUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 消费端动态代理处理类
 * @author Leo
 * @date 2024/03/11
 */
public class LhtInvocationHandler implements InvocationHandler {


    private Class<?> service;
    private List<String> providers;

    private RpcContext context;
    private HttpInvoker httpInvoker;

    public LhtInvocationHandler(Class<?> service, RpcContext context, List<String> providers, HttpInvoker httpInvoker) {
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

        List<String> urls = context.getRouter().route(providers);
        String url = (String) context.getLoadBalancer().choose(urls);
        System.out.println("loadBalancer.choose(urls) ==> " + url);

        RpcResponse rpcResponse = httpInvoker.post(rpcRequest, url);
        //这里如果不转，返回的其实是一个jsonObject对象，但是服务端调用返回的需要是具体的对象，所以需要进行转换(序列化和反序列化？)
        if (rpcResponse.isStatus()) {
            return TypeUtils.buildResponse(method, rpcResponse);
        } else {
            //异常不能直接返回，会类转换失败，直接抛出去就好，抛的时候可以控制，是所有堆栈信息都返回去，还是只返回主要信息，这里只返回主要信息
            throw rpcResponse.getEx();
        }
    }
}
