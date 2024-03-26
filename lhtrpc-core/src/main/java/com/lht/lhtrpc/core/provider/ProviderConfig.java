package com.lht.lhtrpc.core.provider;

import com.lht.lhtrpc.core.api.RegistryCenter;
import com.lht.lhtrpc.core.consumer.ConsumerBootStrap;
import com.lht.lhtrpc.core.registry.ZkRegistryCenter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * @author Leo
 * @date 2024/03/07
 */
@Configuration
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
    public RegistryCenter registryCenterProvider() {return new ZkRegistryCenter();}

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
