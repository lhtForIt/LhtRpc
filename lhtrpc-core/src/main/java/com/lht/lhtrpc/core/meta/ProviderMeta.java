package com.lht.lhtrpc.core.meta;

import lombok.Data;
import lombok.ToString;

import java.lang.reflect.Method;

/**
 * @author Leo
 * @date 2024/03/13
 */
@Data
public class ProviderMeta {

    private Method method;
    private String methodSign;
    private Object serviceImpl;



}
