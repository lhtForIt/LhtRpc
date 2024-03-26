package com.lht.lhtrpc.core.consumer;

import com.alibaba.fastjson.JSON;
import com.lht.lhtrpc.core.api.*;
import com.lht.lhtrpc.core.utils.MethodUtils;
import com.lht.lhtrpc.core.utils.TypeUtils;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Leo
 * @date 2024/03/11
 */
public class LhtInvocationHandler implements InvocationHandler {

    private final static MediaType MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private Class<?> service;
    private List<String> providers;

    private RpcContext context;

    public LhtInvocationHandler(Class<?> service, RpcContext context, List<String> providers) {
        this.service = service;
        this.context = context;
        this.providers = providers;
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

        RpcResponse rpcResponse = post(rpcRequest, url);
        //这里如果不转，返回的其实是一个jsonObject对象，但是服务端调用返回的需要是具体的对象，所以需要进行转换(序列化和反序列化？)
        if (rpcResponse.isStatus()) {
            return TypeUtils.buildResponse(method, rpcResponse);
        } else {
            //异常不能直接返回，会类转换失败，直接抛出去就好，抛的时候可以控制，是所有堆栈信息都返回去，还是只返回主要信息，这里只返回主要信息
            throw rpcResponse.getEx();
        }
    }

    OkHttpClient client = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(16,60,TimeUnit.SECONDS))
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300,TimeUnit.SECONDS)
            .connectTimeout(300,TimeUnit.SECONDS)
            .build();

    private RpcResponse post(RpcRequest rpcRequest,String url) {

        String requestJson = JSON.toJSONString(rpcRequest);
        System.out.println("requestJson = " + requestJson);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestJson, MEDIA_TYPE))
                .build();
        try {
            String responseJson = client.newCall(request).execute().body().string();
            System.out.println("responseJson = " + responseJson);
            RpcResponse rpcResponse = JSON.parseObject(responseJson, RpcResponse.class);
            return rpcResponse;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
