package com.lht.lhtrpc.core.config;

import com.lht.lhtrpc.core.api.*;
import com.lht.lhtrpc.core.cluster.GrayRouter;
import com.lht.lhtrpc.core.cluster.RandomRibonLoadBalancer;
import com.lht.lhtrpc.core.consumer.ConsumerBootStrap;
import com.lht.lhtrpc.core.filter.ParameterFilter;
import com.lht.lhtrpc.core.registry.zk.ZkRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
@Import({AppConfigProperties.class, ConsumerConfigProperties.class, ZkConfigProperties.class})
public class ConsumerConfig {

    @Autowired
    private AppConfigProperties appConfigProperties;

    @Autowired
    private ConsumerConfigProperties consumerConfigProperties;

    @Autowired
    private ZkConfigProperties zkConfigProperties;

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
        return new GrayRouter(consumerConfigProperties.getGrayRatio());
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
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public RegistryCenter registryCenterConsumer() {
        return new ZkRegistryCenter(zkConfigProperties.getZkServer(), zkConfigProperties.getZkRoot());
    }

    @Bean
    public RpcContext rpcContext(Router router, LoadBalancer loadBalancer, List<Filter> filters) {
        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);
        context.setFilters(filters);
        context.getParamerters().put("app.id", appConfigProperties.getId());
        context.getParamerters().put("app.namespace", appConfigProperties.getNamespace());
        context.getParamerters().put("app.env", appConfigProperties.getEnv());
        context.getParamerters().put("consumer.retry", consumerConfigProperties.getRetry());
        context.getParamerters().put("consumer.okhttp.connectTimeout", consumerConfigProperties.getOkhttp().getConnectTimeout());
        context.getParamerters().put("consumer.okhttp.readTimeout", consumerConfigProperties.getOkhttp().getReadTimeout());
        context.getParamerters().put("consumer.okhttp.writeTimeout", consumerConfigProperties.getOkhttp().getWriteTimeout());
        context.getParamerters().put("consumer.faultLimit", String.valueOf(consumerConfigProperties.getFaultLimit()));
        context.getParamerters().put("consumer.halfOpenInitialDelay", String.valueOf(consumerConfigProperties.getHalfOpenInitialDelay()));
        context.getParamerters().put("consumer.halfOpenDelay", String.valueOf(consumerConfigProperties.getHalfOpenDelay()));
        return context;
    }


}
