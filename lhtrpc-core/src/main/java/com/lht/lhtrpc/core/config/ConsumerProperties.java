package com.lht.lhtrpc.core.config;

import lombok.Data;
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
public class ConsumerProperties {

    //consumer
    private int grayRatio = 0;

    private int retry = 1;

    private OkHttpConfig okhttp;

    private int faultLimit = 10;

    private int halfOpenInitialDelay = 10_000;

    private int halfOpenDelay = 60_000;

}
