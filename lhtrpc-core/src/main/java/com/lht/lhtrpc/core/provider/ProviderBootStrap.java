package com.lht.lhtrpc.core.provider;

import com.lht.lhtrpc.core.annotation.LhtProvider;
import com.lht.lhtrpc.core.api.RegistryCenter;
import com.lht.lhtrpc.core.meta.ProviderMeta;
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

    private String instance;

    @Value("${server.port}")
    private String port;

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
        instance = ip + "_" + port;
        rc.start();
        skeleton.keySet().forEach(this::registerService);
    }

    @PreDestroy
    public void stop() {
        skeleton.keySet().forEach(this::unregisterService);
        rc.stop();
    }

    private void unregisterService(String service) {
        rc.unregister(service, instance);
    }

    private void registerService(String service) {
        rc.register(service, instance);
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
        ProviderMeta meta = new ProviderMeta();
        meta.setMethod(m);
        meta.setServiceImpl(bean);
        meta.setMethodSign(MethodUtils.buildMethodSign(m));
        System.out.println("创建provider: " + meta);
        skeleton.add(anInterface.getCanonicalName(), meta);
    }


}
