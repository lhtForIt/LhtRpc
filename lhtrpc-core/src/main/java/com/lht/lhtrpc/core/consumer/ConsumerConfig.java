package com.lht.lhtrpc.core.consumer;

import com.lht.lhtrpc.core.api.LoadBalancer;
import com.lht.lhtrpc.core.api.Router;
import com.lht.lhtrpc.core.cluster.RandomLoadBalancer;
import com.lht.lhtrpc.core.cluster.RandomRibonLoadBalancer;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author Leo
 * @date 2024/03/10
 */
@Configuration
public class ConsumerConfig {

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
            System.out.println("----consumerBootStrapRunner start----");
            consumerBootStrap.start();
            System.out.println("----consumerBootStrapRunner end----");
        };
    }


    @Bean
    public LoadBalancer loadBalancer() {
        return new RandomRibonLoadBalancer();
    }

    @Bean
    public Router router() {
        return Router.Default;
    }



}
