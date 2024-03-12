package com.lht.lhtrpc.core.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lht.lhtrpc.core.api.RpcRequest;
import com.lht.lhtrpc.core.api.RpcResponse;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @author Leo
 * @date 2024/03/11
 */
public class LhtInvocationHandler implements InvocationHandler {

    private final static MediaType MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private Class<?> service;

    public LhtInvocationHandler(Class<?> service) {
        this.service = service;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (Object.class.equals(method.getDeclaringClass())) {
            return null;
        }
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(service.getCanonicalName());
        rpcRequest.setMethod(method.getName());
        rpcRequest.setArgs(args);

        RpcResponse rpcResponse = post(rpcRequest);

        //这里如果不转，返回的其实是一个jsonObject对象，但是服务端调用返回的需要是具体的对象，所以需要进行转换(序列化和反序列化？)
        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            if (data instanceof JSONObject) {
                JSONObject rpcResponseData = (JSONObject) data;
                Object javaObject = rpcResponseData.toJavaObject(method.getReturnType());
                return javaObject;
            } else {
                return data;
            }
        } else {
            //异常不能直接返回，会类转换失败，直接抛出去就好，抛的时候可以控制，是所有堆栈信息都返回去，还是只返回主要信息，这里只返回主要信息
            throw rpcResponse.getEx();
        }
    }

    OkHttpClient client = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(16,60,TimeUnit.SECONDS))
            .readTimeout(3, TimeUnit.SECONDS)
            .writeTimeout(3,TimeUnit.SECONDS)
            .connectTimeout(3,TimeUnit.SECONDS)
            .build();

    private RpcResponse post(RpcRequest rpcRequest) {

        String requestJson = JSON.toJSONString(rpcRequest);
        System.out.println("requestJson = " + requestJson);
        Request request = new Request.Builder()
                .url("http://localhost:8080/")
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
