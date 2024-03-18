package com.lht.lhtrpc.core.consumer;

import com.alibaba.fastjson.JSON;
import com.lht.lhtrpc.core.api.*;
import com.lht.lhtrpc.core.utils.MethodUtils;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
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
        Object[] newArg = null;
        if (args == null || args.length == 0) {
            newArg = args;
        } else {
            newArg = initMapKey(args);
        }
        rpcRequest.setArgs(newArg);


        List<String> urls = context.getRouter().route(providers);
        String url = (String) context.getLoadBalancer().choose(urls);
        System.out.println("loadBalancer.choose(urls) ==> " + url);

        RpcResponse rpcResponse = post(rpcRequest, url);

        //这里如果不转，返回的其实是一个jsonObject对象，但是服务端调用返回的需要是具体的对象，所以需要进行转换(序列化和反序列化？)
        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            if (data == null) {
                return null;
            }
            if (Map.class.isAssignableFrom(data.getClass())) {
                Map map = new HashMap();
                Type genericReturnType = method.getGenericReturnType();
                if (genericReturnType instanceof ParameterizedType parameterizedType) {
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    Class<?> keyType = (Class<?>) actualTypeArguments[0];
                    Class<?> valueType = (Class<?>) actualTypeArguments[1];
                    ((Map) data).entrySet().stream().forEach(entry -> {
                        Map.Entry e = (Map.Entry) entry;
                        Object key=MethodUtils.convertType(e.getKey(), keyType);
                        Object value=MethodUtils.convertType(e.getValue(), valueType);
                        map.put(key,value);
                    });
                    return map;
                }
            } else if (List.class.isAssignableFrom(data.getClass()) && !data.getClass().isArray()) {
            //如果list里面是实体类(User)，会当成一个map，这时候需要转，否则一旦获取user对象操作就会出错
                Type genericReturnType = method.getGenericReturnType();
                if (genericReturnType instanceof ParameterizedType parameterizedType) {
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    Class<?> valueType = (Class<?>) actualTypeArguments[0];
                    List list = (List) data;
                    List newList = new ArrayList();
                    list.forEach(e -> {
                        Object value=MethodUtils.convertType(e, valueType);
                        newList.add(value);
                    });
                    return newList;
                }
            }
            return MethodUtils.convertType(data, method.getReturnType());
        } else {
            //异常不能直接返回，会类转换失败，直接抛出去就好，抛的时候可以控制，是所有堆栈信息都返回去，还是只返回主要信息，这里只返回主要信息
            throw rpcResponse.getEx();
        }
    }

    @NotNull
    private static Object[] initMapKey(Object[] args) {
        Object[] newArg;
        newArg= new Object[args.length];
        for (int i = 0; i < newArg.length; i++) {
            if (args[i] instanceof Map map) {
                if (!CollectionUtils.isEmpty(map)) {
                    Map<Object, Object> newMap = new HashMap<>();
                    map.forEach((key, value) -> {
                        newMap.put(String.valueOf(key), value);
                    });
                    newArg[i] = newMap;
                    continue;
                }
            }
            newArg[i] = args[i];
        }
        return newArg;
    }


    public static void main(String[] args) throws NoSuchMethodException {
        System.out.println(MethodUtils.buildMethodSign(Temp.class.getMethod("getId",new Class[]{long.class})));
        System.out.println(MethodUtils.buildMethodSign(Temp.class.getMethod("getId",new Class[]{P.class})));
        System.out.println(MethodUtils.buildMethodSign(Temp.class.getMethod("tt", new Class[]{int.class, String.class})));
        System.out.println(MethodUtils.buildMethodSign(Temp.class.getMethod("tt", new Class[]{String.class, int.class})));
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
