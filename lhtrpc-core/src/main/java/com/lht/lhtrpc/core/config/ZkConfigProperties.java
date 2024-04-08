package com.lht.lhtrpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Leo
 * @date 2024/04/08
 */
@Data
@Component
@ConfigurationProperties("lhtrpc.zk")
public class ZkConfigProperties {

    private String zkServer;
    private String zkRoot;

}
