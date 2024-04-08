package com.lht.lhtrpc.core.config;

import lombok.Data;

/**
 * @author Leo
 * @date 2024/04/08
 */
@Data
public class OkHttpConfig {

    private String connectTimeout;
    private String readTimeout;
    private String writeTimeout;

}