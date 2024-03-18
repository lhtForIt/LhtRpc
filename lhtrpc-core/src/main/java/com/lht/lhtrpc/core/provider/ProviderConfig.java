package com.lht.lhtrpc.core.provider;

import com.lht.lhtrpc.core.api.RegistryCenter;
import com.lht.lhtrpc.core.consumer.ConsumerBootStrap;
import com.lht.lhtrpc.core.registry.ZkRegistryCenter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * @author Leo
 * @date 2024/03/07
 */
@Configuration
public class ProviderConfig {


    @Bean
    public ProviderBootStrap initProviderBootStrap() {
        return new ProviderBootStrap();
    }


    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter registryCenter() {return new ZkRegistryCenter();}

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner providersBootStrapRunner(ProviderBootStrap providerBootStrap) {
        return x -> {
            System.out.println("----providersBootStrapRunner start----");
            providerBootStrap.start();
            System.out.println("----providersBootStrapRunner end----");
        };
    }


}
