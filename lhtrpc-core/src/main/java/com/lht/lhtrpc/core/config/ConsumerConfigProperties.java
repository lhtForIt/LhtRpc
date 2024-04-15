package com.lht.lhtrpc.core.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * consumer配置类
 *
 * @author Leo
 * @date 2024/04/08
 */
@Data
@Component
@ConfigurationProperties("lhtrpc.consumer")
public class ConsumerConfigProperties {

    //consumer
    private int grayRatio;

    private String retry;

    private OkHttpConfig okhttp;

    private int faultLimit = 10;

    private int halfOpenInitialDelay = 10_000;

    private int halfOpenDelay = 60_000;

}