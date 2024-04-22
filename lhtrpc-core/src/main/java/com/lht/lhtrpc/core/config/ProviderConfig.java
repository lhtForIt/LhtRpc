package com.lht.lhtrpc.core.config;

import com.lht.lhtrpc.core.api.RegistryCenter;
import com.lht.lhtrpc.core.provider.ProviderBootStrap;
import com.lht.lhtrpc.core.provider.ProviderInvoker;
import com.lht.lhtrpc.core.registry.zk.ZkRegistryCenter;
import com.lht.lhtrpc.core.transport.SpringBootTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

/**
 * @author Leo
 * @date 2024/03/07
 */
@Configuration
@Slf4j
@Import({AppConfigProperties.class, ProviderProperties.class, ZkConfigProperties.class, SpringBootTransport.class})
public class ProviderConfig {


    @Value("${server.port:8080}")
    private String port;

    @Autowired
    private AppConfigProperties appConfigProperties;

    @Autowired
    private ProviderProperties providerProperties;

    @Autowired
    private ZkConfigProperties zkConfigProperties;

    @Bean
    public ProviderBootStrap providerBootStrap() {
        return new ProviderBootStrap(port, appConfigProperties, providerProperties);
    }


    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "apollo.bootstrap", value = "enabled")
    ApolloChangedListener provider_apolloChangedListener() {
        return new ApolloChangedListener();
    }

    @Bean
    public ProviderInvoker providerInvoker(ProviderBootStrap providerBootStrap) {
        return new ProviderInvoker(providerBootStrap);
    }

    /**
     * 在服务提供者启动和关闭时调用注册中心的启动和关闭方法，这样让两者联系起来，而不是各干各的
     */
    @Bean //(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public RegistryCenter registryCenterProvider() {
        return new ZkRegistryCenter(zkConfigProperties.getZkServer(), zkConfigProperties.getZkRoot());
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner providersBootStrapRunner(ProviderBootStrap providerBootStrap) {
        return x -> {
            log.info("----providersBootStrapRunner start----");
            providerBootStrap.start();
            log.info("----providersBootStrapRunner end----");
        };
    }


}
