package com.lht.lhtrpc.core.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.lht.lhtrpc.core.api.RpcRequest;
import com.lht.lhtrpc.core.api.RpcResponse;
import com.lht.lhtrpc.core.utils.MethodUtils;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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
//        rpcRequest.setMethod(method);
        rpcRequest.setMethodSign(MethodUtils.buildMethodSign(method, service));
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
                return MethodUtils.convertType(data, method.getReturnType().getCanonicalName());
            }
        } else {
            //异常不能直接返回，会类转换失败，直接抛出去就好，抛的时候可以控制，是所有堆栈信息都返回去，还是只返回主要信息，这里只返回主要信息
            throw rpcResponse.getEx();
        }
    }



    public static void main(String[] args) throws NoSuchMethodException {
        System.out.println(MethodUtils.buildMethodSign(Temp.class.getMethod("getId",new Class[]{long.class}),Temp.class));
        System.out.println(MethodUtils.buildMethodSign(Temp.class.getMethod("getId",new Class[]{P.class}),Temp.class));
        System.out.println(MethodUtils.buildMethodSign(Temp.class.getMethod("tt", new Class[]{int.class, String.class}),Temp.class));
        System.out.println(MethodUtils.buildMethodSign(Temp.class.getMethod("tt", new Class[]{String.class, int.class}),Temp.class));
    }

    class Temp{
        public long getId(long id){
            return id;
        }

        public int getId(P p) {
            return p.id;
        }

        public int tt(int a, String b) {
            return 1;
        }

        public int tt(String b, int a) {
            return 1;
        }


    }

    class P{
        int id;
        String name;
    }

    OkHttpClient client = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(16,60,TimeUnit.SECONDS))
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300,TimeUnit.SECONDS)
            .connectTimeout(300,TimeUnit.SECONDS)
            .build();

    private RpcResponse post(RpcRequest rpcRequest) {

//        String requestJson = JSON.toJSONString(rpcRequest, SerializerFeature.IgnoreNonFieldGetter);
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
