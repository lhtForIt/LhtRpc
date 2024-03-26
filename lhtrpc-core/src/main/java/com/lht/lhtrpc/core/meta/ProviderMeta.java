package com.lht.lhtrpc.core.meta;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.lang.reflect.Method;

/**
 * 描述provider的映射关系元数据
 *
 * @author Leo
 * @date 2024/03/13
 */
@Data
@Builder
public class ProviderMeta {

    private Method method;
    private String methodSign;
    private Object serviceImpl;

}
