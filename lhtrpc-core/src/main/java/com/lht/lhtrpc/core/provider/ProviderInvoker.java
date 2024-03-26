package com.lht.lhtrpc.core.provider;

import com.lht.lhtrpc.core.api.RpcRequest;
import com.lht.lhtrpc.core.api.RpcResponse;
import com.lht.lhtrpc.core.meta.ProviderMeta;
import com.lht.lhtrpc.core.utils.MethodUtils;
import com.lht.lhtrpc.core.utils.TypeUtils;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author Leo
 * @date 2024/03/26
 */
public class ProviderInvoker {

    private MultiValueMap<String, ProviderMeta> skeleton;

    public ProviderInvoker(ProviderBootStrap providerBootStrap) {
        this.skeleton = providerBootStrap.getSkeleton();
    }

    public RpcResponse invokeRequest(RpcRequest request) {
        System.out.println("service值为：" + request.getService());
        List<ProviderMeta> providerMetas = skeleton.get(request.getService());
        RpcResponse rpcResponse = new RpcResponse();
        try {
            ProviderMeta providerMeta = findProviderMeta(request, providerMetas);
            if (providerMeta == null) {
                rpcResponse.setEx(new RuntimeException("没有找到对应的服务"));
                return rpcResponse;
            }
            Method method = providerMeta.getMethod();
            Object bean = providerMeta.getServiceImpl();
            //args是一个object数组，它在进行序列化的时候可能会丢失类型，反序列化会转为最适合的类型，比如是13L这种long型会转成int型，这时候需要转换一下
            // 参数里map有引用类型，如果不转就会返回Null，但是list的引用类型就不会，不知道为啥？看来map还必须要转一下
            // (debug看了下，我map是一个map<Integer,User>，
            // 序列化的时候会自动转成string，如果这个时候不转，那么就会报错，因为map.get("1")一定拿不到东西，但是map的类型转了key就变成integer了
            // 这时候就没问题了。如果你是string就没有问题，因为key本来就是string,也不用你转，感觉还是挺坑的啊)
            // 你返回的不是order就不需要转，因为linkedHashMap不会出现转换问题
            // 只要你在整个方法实现里面没有任何和实体类型相关的操作，你map不转不会有任何问题，比如你直接返回传入的map，那么你转不转都没一点问题(相当于有个炸弹，只要你不点燃就不会爆炸)
            Object[] newArgs = processArgs(request.getArgs(), method);
            Object result = method.invoke(bean, newArgs);
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            rpcResponse.setEx(new RuntimeException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            rpcResponse.setEx(new RuntimeException(e.getMessage()));
        }
        return rpcResponse;
    }

    private Object[] processArgs(Object[] args, Method method) {
        if (args == null || args.length == 0) {
            return args;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] newArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            TypeUtils.buildNewArgs(args, method, parameterTypes, i, newArgs);
        }
        return newArgs;
    }



    private ProviderMeta findProviderMeta(RpcRequest request, List<ProviderMeta> providerMetas) {
        Optional<ProviderMeta> meta = providerMetas.stream().filter(d -> d.getMethodSign().equals(request.getMethodSign())).findFirst();
        return meta.orElse(null);
    }


}
