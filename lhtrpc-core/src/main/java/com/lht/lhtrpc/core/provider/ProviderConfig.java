package com.lht.lhtrpc.core.provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Leo
 * @date 2024/03/07
 */
@Configuration
public class ProviderConfig {


    @Bean
    public ProviderBootStrap initProviderBootStrap() {
        return new ProviderBootStrap();
    }


}
