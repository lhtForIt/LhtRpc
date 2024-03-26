package com.lht.lhtrpc.core.consumer;

import com.lht.lhtrpc.core.annotation.LhtConsumer;
import com.lht.lhtrpc.core.api.LoadBalancer;
import com.lht.lhtrpc.core.api.RegistryCenter;
import com.lht.lhtrpc.core.api.Router;
import com.lht.lhtrpc.core.api.RpcContext;
import com.lht.lhtrpc.core.meta.InstanceMeta;
import com.lht.lhtrpc.core.meta.ServiceMeta;
import com.lht.lhtrpc.core.utils.MethodUtils;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消费者启动类
 * @author Leo
 * @date 2024/03/10
 */
@Data
public class ConsumerBootStrap implements ApplicationContextAware, EnvironmentAware {


    private ApplicationContext applicationContext;

    private Environment environment;

    private Map<String, Object> stub = new HashMap<>();

    @Autowired
    //根据某些策略配置httpInvoker实现类,可以从配置文件设置，如果没有设置默认使用OkHttpInvoker
    private HttpInvoker httpInvoker;

    @Value("${app.id}")
    private String app;

    @Value("${app.namespace}")
    private String namespace;

    @Value("${app.env}")
    private String env;

    /**
     * 为什么provider直接用@PostConstruct就能初始化桩的集合，而consumer不能？
     * 因为provider其实只需要找到bean的类定义即可，只会用到类定义相关信息。
     * <p>
     * 因为@PostConstruct时候
     * 这个bean可能没有属性设置完全，有可能被其他功能动态的设置或者代理，它可能不是完整的。
     * <p>
     * 而consumer要用到的是完整初始化的xxxImpl，需要在整个容器完全初始化完之后才能被我们放到集合里面进行增强，
     * 所以需要用applicationRunner这种去进行桩的集合初始化。
     */
    public void start() {

        LoadBalancer<InstanceMeta> loadBalancer = applicationContext.getBean(LoadBalancer.class);
        Router<InstanceMeta> router = applicationContext.getBean(Router.class);
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);
        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);

        String[] names = applicationContext.getBeanDefinitionNames();
        for (String beanName : names) {
            Object bean = applicationContext.getBean(beanName);
            List<Field> fields = MethodUtils.findAnnotatedField(bean.getClass(), LhtConsumer.class);
            fields.stream().forEach(d -> {
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
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(app)
                .namespace(namespace)
                .env(env)
                .name(serviceName)
                .build();
        List<InstanceMeta> providers = rc.fetchAll(serviceMeta);
        rc.subscribe(serviceMeta, data -> {
            providers.clear();
            providers.addAll(data.getData());
        });
        return createConsumer(service, context, providers);
    }

    private Object createConsumer(Class<?> service, RpcContext rpcContext, List<InstanceMeta> providers) {
        return Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service}, new LhtInvocationHandler(service, rpcContext, providers, httpInvoker));
    }


}
