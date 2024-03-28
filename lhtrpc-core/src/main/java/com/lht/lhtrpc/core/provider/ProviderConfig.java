package com.lht.lhtrpc.core.provider;

import com.lht.lhtrpc.core.api.RegistryCenter;
import com.lht.lhtrpc.core.registry.zk.ZkRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author Leo
 * @date 2024/03/07
 */
@Configuration
@Slf4j
public class ProviderConfig {


    @Bean
    public ProviderBootStrap providerBootStrap() {
        return new ProviderBootStrap();
    }


    @Bean
    public ProviderInvoker providerInvoker(ProviderBootStrap providerBootStrap) {return new ProviderInvoker(providerBootStrap);}

    /**
     * 在服务提供者启动和关闭时调用注册中心的启动和关闭方法，这样让两者联系起来，而不是各干各的
     */
    @Bean //(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public RegistryCenter registryCenterProvider() {return new ZkRegistryCenter();}

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
