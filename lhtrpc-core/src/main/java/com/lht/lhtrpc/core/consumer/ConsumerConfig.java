package com.lht.lhtrpc.core.consumer;

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


}
