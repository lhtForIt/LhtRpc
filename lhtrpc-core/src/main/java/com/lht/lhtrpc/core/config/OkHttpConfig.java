package com.lht.lhtrpc.core.config;

import lombok.Data;

/**
 * @author Leo
 * @date 2024/04/08
 */
@Data
public class OkHttpConfig {

    private int connectTimeout = 1000;
    private int readTimeout = 1000;
    private int writeTimeout = 1000;

}