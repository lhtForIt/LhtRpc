package com.lht.lhtrpc.core.provider;

import com.lht.lhtrpc.core.annotation.LhtProvider;
import com.lht.lhtrpc.core.api.RpcRequest;
import com.lht.lhtrpc.core.api.RpcResponse;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    @PostConstruct
    public void buildProviders() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(LhtProvider.class);
        providers.values().forEach(d->getInterface(d));
    }

    public RpcResponse invokeRequest(RpcRequest request) {
        System.out.println("service值为：" + request.getService());
        Object bean = skeleton.get(request.getService());
        try {
            Method method = findMethod(request, bean);
            Object result = method.invoke(bean, request.getArgs());
            return new RpcResponse(true, result);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 这个有多种实现：
     * 一个是直接用方法名和参数类型找，但是注意基础类型有自动装箱拆箱的问题，需要特殊处理(参数如果传空处理不了)
     * 另一个就是遍历所有方法找到同名的方法
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
        for(Method m:bean.getClass().getMethods()){
            if (m.getName().equals(request.getMethod())) {
                return m;
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
