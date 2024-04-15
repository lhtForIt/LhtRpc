package com.lht.lhtrpc.core.provider;

import com.lht.lhtrpc.core.annotation.LhtProvider;
import com.lht.lhtrpc.core.api.RegistryCenter;
import com.lht.lhtrpc.core.api.RpcContext;
import com.lht.lhtrpc.core.config.AppConfigProperties;
import com.lht.lhtrpc.core.config.ProviderConfigProperties;
import com.lht.lhtrpc.core.meta.InstanceMeta;
import com.lht.lhtrpc.core.meta.ProviderMeta;
import com.lht.lhtrpc.core.meta.ServiceMeta;
import com.lht.lhtrpc.core.utils.MethodUtils;
import com.lht.lhtrpc.core.utils.PackageScanUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Leo
 * @date 2024/03/07
 */
@Data
@Slf4j
public class ProviderBootStrap implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    //多值map，value其实是ProviderMeta的list
    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    private InstanceMeta instance;

    private RegistryCenter rc;

    private String port;
    private AppConfigProperties appConfigProperties;
    private ProviderConfigProperties providerConfigProperties;


    public ProviderBootStrap(String port, AppConfigProperties appConfigProperties, ProviderConfigProperties providerConfigProperties) {
        this.port = port;
        this.appConfigProperties = appConfigProperties;
        this.providerConfigProperties = providerConfigProperties;
    }

    @PostConstruct
    public void init() throws ClassNotFoundException {
//        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(LhtProvider.class);
        Map<String, Object> providers = new HashMap<>();
        List<Class<?>> provids = PackageScanUtils.doScan(providerConfigProperties.getPackages(), LhtProvider.class);
        //这里还是使用了@Component注解放到spring容器里，如果想要用一个注解自动将这些bean放到容器，可以自己把类拿到，然后自己动态注册到容器里
        //注意如果要自己手动注册到spring容器里，代码就不能写在这里，需要在Bean还没有实例化之前就注册到容器里，需要实现ImportBeanDefinitionRegistrar，
        //并将扫描代码放到那里面去
        provids.forEach(d -> providers.put(d.getCanonicalName(), applicationContext.getBean(d)));
        rc = applicationContext.getBean(RegistryCenter.class);
        providers.keySet().forEach(d -> log.info(d));
        providers.values().forEach(d -> getInterface(d));
    }

    /**
     * spring容器在进行初始化的时候可能服务还不可用，只有等到所有bean都初始化完毕，才能进行服务注册
     * 对服务进行延迟暴露
     */
    @SneakyThrows
    public void start() {
        String ip = InetAddress.getLocalHost().getHostAddress();
        instance = InstanceMeta.http(ip, Integer.parseInt(this.port));
        instance.getParameters().putAll(providerConfigProperties.getMetas());
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
                .app(appConfigProperties.getId())
                .namespace(appConfigProperties.getNamespace())
                .env(appConfigProperties.getEnv())
                .name(service)
                .build();
        rc.unregister(serviceMeta, instance);
    }

    private void registerService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(appConfigProperties.getId())
                .namespace(appConfigProperties.getNamespace())
                .env(appConfigProperties.getEnv())
                .name(service)
                .build();
        rc.register(serviceMeta, instance);
    }

    /**
     * 默认只支持一个接口
     */
    private void getInterface(Object d) {
        Arrays.stream(d.getClass().getInterfaces()).forEach(anInterface -> {
            log.info("放入provider: " + anInterface.getCanonicalName() + ",对象为：" + d.getClass().getCanonicalName());
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
        ProviderMeta meta = ProviderMeta.builder().method(m).serviceImpl(bean).methodSign(MethodUtils.buildMethodSign(m)).build();
        log.info("创建provider: " + meta);
        skeleton.add(anInterface.getCanonicalName(), meta);
    }


}
