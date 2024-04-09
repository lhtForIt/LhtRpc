package com.lht.lhtrpc.core.annotation;

import com.lht.lhtrpc.core.config.ConsumerConfig;
import com.lht.lhtrpc.core.config.ProviderConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Leo
 * @date 2024/04/09
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
@Import({ProviderConfig.class, ConsumerConfig.class})
public @interface EnableLhtRpc {
}
