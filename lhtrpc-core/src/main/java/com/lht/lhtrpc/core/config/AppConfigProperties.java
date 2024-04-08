package com.lht.lhtrpc.core.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * instance配置类
 *
 * @author Leo
 * @date 2024/04/08
 */
@Data
@Component
@ConfigurationProperties("lhtrpc.app")
public class AppConfigProperties {

    //instance

    private String id;

    private String namespace;

    private String env;


}
