package com.lht.lhtrpc.core.consumer;

import com.lht.lhtrpc.core.api.Filter;
import com.lht.lhtrpc.core.api.LoadBalancer;
import com.lht.lhtrpc.core.api.RegistryCenter;
import com.lht.lhtrpc.core.api.Router;
import com.lht.lhtrpc.core.cluster.GrayRouter;
import com.lht.lhtrpc.core.cluster.RandomRibonLoadBalancer;
import com.lht.lhtrpc.core.registry.zk.ZkRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author Leo
 * @date 2024/03/10
 */
@Configuration
@Slf4j
public class ConsumerConfig {

    @Value("${lhtrpc.providers}")
    private String service;

    @Value("${app.grayRatio}")
    private int grayRatio;

    @Bean
    public ConsumerBootStrap initConsumerBootStrap() {
        return new ConsumerBootStrap();
    }


    /**
     * applicationRunner在所有bean都生成并初始化完成之后才会调用，这时候去初始化消费者
     */
    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner consumerBootStrapRunner(ConsumerBootStrap consumerBootStrap) {
        return x -> {
            log.info("----consumerBootStrapRunner start----");
            consumerBootStrap.start();
            log.info("----consumerBootStrapRunner end----");
        };
    }


    @Bean
    public LoadBalancer loadBalancer() {
        return new RandomRibonLoadBalancer();
    }

    @Bean
    public Router router() {
        return new GrayRouter(grayRatio);
    }

//    @Bean
//    public Filter filter() {return new CacheFilter();}
//    @Bean
//    public Filter filter() {return new MockFilter();}

    @Bean
    public Filter filter() {return Filter.Default;}


    //注册中心自动启动和销毁通过initMethod和destroyMethod
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public RegistryCenter registryCenterConsumer() {return new ZkRegistryCenter();}


}
