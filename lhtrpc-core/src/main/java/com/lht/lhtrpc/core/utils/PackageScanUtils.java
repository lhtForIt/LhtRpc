package com.lht.lhtrpc.core.utils;

import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Leo
 * @date 2024/03/27
 */
public class PackageScanUtils {


    public static List<Class<?>> doScan(String packages,Class<? extends Annotation> annotationType) throws ClassNotFoundException {

        if (packages == null || packages.length() == 0) return null;
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(annotationType));
        List<Class<?>> result = new ArrayList<>();

        for (String basePackage : packages.split(",")) {
            Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
            for (BeanDefinition candidateComponent : candidateComponents) {
                System.out.println(candidateComponent.getBeanClassName());
                Class<?> bean = Class.forName(candidateComponent.getBeanClassName());
                result.add(bean);
//            String canonicalName = bean.getCanonicalName();
            }
        }
        return result;
    }

}
