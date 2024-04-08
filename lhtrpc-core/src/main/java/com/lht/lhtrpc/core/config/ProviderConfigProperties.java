package com.lht.lhtrpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * provider配置类
 *
 * @author Leo
 * @date 2024/04/08
 */
@Data
@Component
@ConfigurationProperties("lhtrpc.provider")
public class ProviderConfigProperties {

    //provider
    private Map<String, String> metas = new HashMap<>();

    private String packages;

}
