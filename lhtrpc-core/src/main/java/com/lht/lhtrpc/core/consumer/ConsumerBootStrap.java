package com.lht.lhtrpc.core.consumer;

import com.lht.lhtrpc.core.annotation.LhtConsumer;
import com.lht.lhtrpc.core.api.*;
import com.lht.lhtrpc.core.registry.Event;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Leo
 * @date 2024/03/10
 */
@Data
public class ConsumerBootStrap implements ApplicationContextAware, EnvironmentAware {


    private ApplicationContext applicationContext;

    private Environment environment;

    private Map<String, Object> stub = new HashMap<>();

    /**
     * 为什么provider直接用@PostConstruct就能初始化桩的集合，而consumer不能？
     * 因为provider其实只需要找到bean的类定义即可，只会用到类定义相关信息。
     *
     * 因为@PostConstruct时候
     * 这个bean可能没有属性设置完全，有可能被其他功能动态的设置或者代理，它可能不是完整的。
     *
     * 而consumer要用到的是完整初始化的xxxImpl，需要在整个容器完全初始化完之后才能被我们放到集合里面进行增强，
     * 所以需要用applicationRunner这种去进行桩的集合初始化。
     */
    public void start() {

        LoadBalancer loadBalancer = applicationContext.getBean(LoadBalancer.class);
        Router router = applicationContext.getBean(Router.class);
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);
        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);


//        String urls = environment.getProperty("lhtrpc.providers");
//        if (Strings.isEmpty(urls)) {
//            System.out.println("lhtrpc.providers is empty");
//        }
//        String[] providers = urls.split(",");


        String[] names = applicationContext.getBeanDefinitionNames();
        for (String beanName : names) {
            Object bean = applicationContext.getBean(beanName);
            List<Field> fields = findAnnotatedField(bean.getClass());
            fields.stream().forEach(d->{
                try {
                    Class<?> service = d.getType();
                    String serviceName = service.getCanonicalName();
                    Object consumer = stub.computeIfAbsent(serviceName, x -> createFromRegistry(service, context, rc));
                    d.setAccessible(true);
                    d.set(bean, consumer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private Object createFromRegistry(Class<?> service, RpcContext context, RegistryCenter rc) {
        String serviceName = service.getCanonicalName();
        List<String> providers = buildUrl4Zk(rc.fetchAll(serviceName));
        rc.subscribe(serviceName, data -> {
            providers.clear();
            providers.addAll(buildUrl4Zk(data.getData()));
        });
        return createConsumer(service, context, providers);
    }

    private List<String> buildUrl4Zk(List<String> zkNodes) {
        List<String> urls = zkNodes.stream().map(d -> "http://" + d.replace('_', ':')).collect(Collectors.toList());
        System.out.println("===> map to providers:");
        urls.forEach(System.out::println);
        return urls;
    }

    /**
     * 这里的类都会被cglib增强，那么如果直接用aClass.getDeclaredFields()去找增强的子类的字段，
     * 但是父类的字段是没有的，因此userService就是空，解决方法也很简单，直接循环着去找它的父类，拿到它
     * 父类所有的字段，那么userService就一定在里面。
     */
    private List<Field> findAnnotatedField(Class<?> aClass) {
        List<Field> res = new ArrayList<>();
        while (aClass != null) {
            Field[] fields = aClass.getDeclaredFields();
            for (Field f : fields) {
                if (f.isAnnotationPresent(LhtConsumer.class)) {
                    res.add(f);
                }
            }
            aClass = aClass.getSuperclass();
        }
        return res;
    }

    private Object createConsumer(Class<?> service, RpcContext rpcContext, List<String> providers) {
        return Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service}, new LhtInvocationHandler(service, rpcContext, providers));
    }


}
