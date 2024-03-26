package com.lht.lhtrpc.core.provider;

import com.lht.lhtrpc.core.annotation.LhtProvider;
import com.lht.lhtrpc.core.api.RegistryCenter;
import com.lht.lhtrpc.core.meta.InstanceMeta;
import com.lht.lhtrpc.core.meta.ProviderMeta;
import com.lht.lhtrpc.core.meta.ServiceMeta;
import com.lht.lhtrpc.core.utils.MethodUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Leo
 * @date 2024/03/07
 */
@Data
public class ProviderBootStrap implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    //多值map，value其实是ProviderMeta的list
    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    private InstanceMeta instance;

    @Value("${server.port}")
    private String port;

    @Value("${app.id}")
    private String app;

    @Value("${app.namespace}")
    private String namespace;

    @Value("${app.env}")
    private String env;

    private RegistryCenter rc;


    @PostConstruct
    public void init() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(LhtProvider.class);
        rc = applicationContext.getBean(RegistryCenter.class);
        providers.keySet().forEach(d-> System.out.println(d));
        providers.values().forEach(d -> getInterface(d));
    }

    /**
     * spring容器在进行初始化的时候可能服务还不可用，只有等到所有bean都初始化完毕，才能进行服务注册
     * 对服务进行延迟暴露
     */
    @SneakyThrows
    public void start() {
        String ip= InetAddress.getLocalHost().getHostAddress();
        instance = InstanceMeta.http(ip, Integer.parseInt(port));
        rc.start();
        skeleton.keySet().forEach(this::registerService);
    }

    @PreDestroy
    public void stop() {
        skeleton.keySet().forEach(this::unregisterService);
        rc.stop();
    }

    private void unregisterService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(app)
                .namespace(namespace)
                .env(env)
                .name(service)
                .build();
        rc.unregister(serviceMeta, instance);
    }

    private void registerService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(app)
                .namespace(namespace)
                .env(env)
                .name(service)
                .build();
        rc.register(serviceMeta, instance);
    }

    /**
     * 默认只支持一个接口
     */
    private void getInterface(Object d) {
        Arrays.stream(d.getClass().getInterfaces()).forEach(anInterface -> {
            System.out.println("放入provider: " + anInterface.getCanonicalName() + ",对象为：" + d.getClass().getCanonicalName());
            Method[] methods = d.getClass().getDeclaredMethods();
            for (Method method : methods) {
                //这里直接过滤掉Object类的本地方法，后面直接找不到对应方法签名的provider
                if (Object.class.equals(method.getDeclaringClass())) {
                    continue;
                }
                createProvider(anInterface, d, method);
            }
        });

    }

    private void createProvider(Class<?> anInterface, Object bean, Method m) {
        ProviderMeta meta=ProviderMeta.builder().method(m).serviceImpl(bean).methodSign(MethodUtils.buildMethodSign(m)).build();
        System.out.println("创建provider: " + meta);
        skeleton.add(anInterface.getCanonicalName(), meta);
    }


}
