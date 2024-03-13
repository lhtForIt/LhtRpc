package com.lht.lhtrpc.core.provider;

import com.alibaba.fastjson.JSON;
import com.lht.lhtrpc.core.annotation.LhtProvider;
import com.lht.lhtrpc.core.api.RpcRequest;
import com.lht.lhtrpc.core.api.RpcResponse;
import com.lht.lhtrpc.core.utils.MethodUtils;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Leo
 * @date 2024/03/07
 */
@Data
public class ProviderBootStrap implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private Map<String, Object> skeleton = new HashMap<>();

    private Map<String, Method> methodSignMap = new HashMap<>();

    @PostConstruct
    public void buildProviders() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(LhtProvider.class);
        providers.values().forEach(d->getInterface(d));
    }

    public RpcResponse invokeRequest(RpcRequest request) {
        String methodName = request.getMethodSign().split("@")[0];
//        if (methodName.equals("toString") || methodName.equals("hashCode")) {
//            return null;
//        }
        System.out.println("service值为：" + request.getService());
        Object bean = skeleton.get(request.getService());
        RpcResponse rpcResponse = new RpcResponse();
        try {
            Method method = findMethod(request, bean);
            if (Object.class.equals(method.getDeclaringClass())) {
                rpcResponse.setEx(new RuntimeException("Object method is not support!!"));
                return rpcResponse;
            }
            //args是一个object数组，它在进行序列化的时候可能会丢失类型，反序列化会转为最适合的类型，比如是13L这种long型会转成int型，这时候需要转换一下
            String[] s = request.getMethodSign().split("_");
            Object[] newArgs = new Object[request.getArgs().length];
            for (int i = 0; i < request.getArgs().length; i++) {
                newArgs[i] = MethodUtils.convertType(request.getArgs()[i], s[i + 1]);
            }
            Object result = method.invoke(bean, newArgs);
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
            rpcResponse.setEx(null);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            rpcResponse.setStatus(false);
            rpcResponse.setData(null);
            rpcResponse.setEx(new RuntimeException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            rpcResponse.setEx(new RuntimeException(e.getMessage()));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            rpcResponse.setEx(new RuntimeException(e.getMessage()));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            rpcResponse.setEx(new RuntimeException(e.getMessage()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            rpcResponse.setEx(new RuntimeException(e.getMessage()));
        }
        return rpcResponse;
    }



    /**
     * 这个有多种实现：
     * 一个是直接用方法名和参数类型找，但是注意基础类型有自动装箱拆箱的问题，需要特殊处理(参数如果传空处理不了)
     * 另一个就是遍历所有方法找到同名的方法
     *
     * tips:
     * 这里如果传的是method名这个string的话，用args去判断类型就会有问题，因为args可能传Null的，但是这里方法却判断不出来。
     * 可以构建方法签名(接口名+方法名+间隔符+参数个数+参数类型_参数类型)传到服务端，然后服务端在解析这个方法签名找到方法。
     *
     * 其实有一个更高效的方法，方法签名我们可以认为是对方法的一次编码，服务端在初始化的时候对每个方法也进行编码，每次只要对比两个字符串是否相等即可。
     * 如果字符串较长，比对效率不高，可以进行再一次编码，转成数字(前提是你的hash函数设计的比较好，碰撞概率较低)，这样比对会快很多。
     *
     */
    private Method findMethod(RpcRequest request, Object bean) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        //法一 利用封装类里面的type字段判断是否是基本类型
//        Class[] types = new Class[request.getArgs().length];
//        for (int i = 0; i < request.getArgs().length; i++) {
//            Class<?> aClass = request.getArgs()[i].getClass();
//            Field type = aClass.getDeclaredField("TYPE");
//            types[i] = type == null ? aClass : (Class) type.get(null);
//        }
//        Method method = bean.getClass().getDeclaredMethod(request.getMethod(), types);
//        return method;
        //法二
//        for(Method m:bean.getClass().getDeclaredMethods()){//不包含父类方法
//        for(Method m:bean.getClass().getMethods()){
//            if (m.getName().equals(request.getMethodSign())) {
//                return m;
//            }
//        }
//        return null;


        for (Method m : bean.getClass().getMethods()) {
            String methodSign = MethodUtils.buildMethodSign(m,bean.getClass().getInterfaces()[0]);
            Method method = methodSignMap.computeIfAbsent(methodSign, t -> m);
            if (request.getMethodSign().equals(methodSign)) {
                return method;
            }
        }

        return null;

    }


    /**
     * 默认只支持一个接口
     */
    private void getInterface(Object d) {
        Class<?> anInterface = d.getClass().getInterfaces()[0];
        System.out.println("放入provider: " + anInterface.getCanonicalName() + ",对象为：" + d.getClass().getCanonicalName());
        skeleton.put(anInterface.getCanonicalName(), d);
    }


}
