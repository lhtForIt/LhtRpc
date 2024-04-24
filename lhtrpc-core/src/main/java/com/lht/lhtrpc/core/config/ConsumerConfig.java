package com.lht.lhtrpc.core.config;

import com.alibaba.fastjson.JSON;
import com.lht.lhtrpc.core.api.*;
import com.lht.lhtrpc.core.cluster.GrayRouter;
import com.lht.lhtrpc.core.cluster.RandomRibonLoadBalancer;
import com.lht.lhtrpc.core.consumer.ConsumerBootStrap;
import com.lht.lhtrpc.core.filter.ParameterFilter;
import com.lht.lhtrpc.core.registry.lht.LhtRegistryCenter;
import com.lht.lhtrpc.core.registry.zk.ZkRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * @author Leo
 * @date 2024/03/10
 */
@Configuration
@Slf4j
@Import({AppConfigProperties.class, ConsumerProperties.class, ZkConfigProperties.class})
public class ConsumerConfig {

    @Autowired
    private AppConfigProperties appConfigProperties;

    @Autowired
    private ConsumerProperties consumerProperties;

    @Autowired
    private ZkConfigProperties zkConfigProperties;

    @Bean
    public ConsumerBootStrap initConsumerBootStrap() {
        return new ConsumerBootStrap();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "apollo.bootstrap", value = "enabled")
    ApolloChangedListener consumer_apolloChangedListener() {
        return new ApolloChangedListener();
    }

    /**
     * applicationRunner在所有bean都生成并初始化完成之后才会调用，这时候去初始化消费者
     */
    @Bean
    @Order(Integer.MIN_VALUE + 1)
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
        return new GrayRouter(consumerProperties.getGrayRatio());
    }

//    @Bean
//    public Filter filter() {return new CacheFilter();}
//    @Bean
//    public Filter filter() {return new MockFilter();}

    @Bean
    public Filter filter() {
        return Filter.Default;
    }


    @Bean
    public Filter parameterFilter() {
        return new ParameterFilter();
    }


    //注册中心自动启动和销毁通过initMethod和destroyMethod
//    @Bean(initMethod = "start", destroyMethod = "stop")
//    @ConditionalOnMissingBean
//    public RegistryCenter registryCenterConsumer() {
//        return new ZkRegistryCenter(zkConfigProperties.getZkServer(), zkConfigProperties.getZkRoot());
//    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public RegistryCenter registryCenterProvider() {
        return new LhtRegistryCenter();
    }

    @Bean
    @RefreshScope
    public RpcContext rpcContext(Router router, LoadBalancer loadBalancer, List<Filter> filters) {
        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);
        context.setFilters(filters);
        context.getParamerters().put("app.id", appConfigProperties.getId());
        context.getParamerters().put("app.namespace", appConfigProperties.getNamespace());
        context.getParamerters().put("app.env", appConfigProperties.getEnv());
        context.setConsumerProperties(consumerProperties);
        return context;
    }


}
