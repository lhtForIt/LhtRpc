package com.lht.lhtrpc.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lht.lhtrpc.core.annotation.LhtConsumer;
import org.springframework.core.annotation.Order;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * @author Leo
 * @date 2024/03/13
 */
public class MethodUtils {

    public static String buildMethodSign(Method method) {
        StringBuilder sb = new StringBuilder(method.getName() + "@");
        sb.append(method.getParameterCount());//这玩意貌似可以不要？
        Arrays.stream(method.getParameterTypes()).forEach(d -> sb.append("_" + d.getCanonicalName()));
        return sb.toString();
    }



    /**
     * 这里的类都会被cglib增强，那么如果直接用aClass.getDeclaredFields()去找增强的子类的字段，
     * 但是父类的字段是没有的，因此userService就是空，解决方法也很简单，直接循环着去找它的父类，拿到它
     * 父类所有的字段，那么userService就一定在里面。
     */
    public static List<Field> findAnnotatedField(Class<?> aClass, Class<? extends Annotation> annotationClass) {
        List<Field> res = new ArrayList<>();
        while (aClass != null) {
            Field[] fields = aClass.getDeclaredFields();
            for (Field f : fields) {
                if (f.isAnnotationPresent(annotationClass)) {
                    res.add(f);
                }
            }
            aClass = aClass.getSuperclass();
        }
        return res;
    }

    public static List<MethodUtils> test(Map<Integer,String> map) {
        return null;
    }

    public static void main(String[] args) throws NoSuchFieldException, ClassNotFoundException {

        Method[] methods = MethodUtils.class.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals("test")) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                Type[] genericParameterTypes = method.getGenericParameterTypes();
                System.out.println("参数类型==========>");
                for (Type genericParameterType : genericParameterTypes) {
                    if (genericParameterType instanceof ParameterizedType parameterizedType) {
                        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                        for (Type actualTypeArgument : actualTypeArguments) {
                            System.out.println("actualTypeArgument类型为" + actualTypeArgument.getTypeName());
                        }
                    }
                }
                System.out.println("返回值类型===========>");
                Type genericReturnType = method.getGenericReturnType();
                if (genericReturnType instanceof ParameterizedType type) {
                    Type[] actualTypeArguments = type.getActualTypeArguments();
                    for (Type actualTypeArgument : actualTypeArguments) {
                        System.out.println("genericReturnType类型为" + actualTypeArgument.getTypeName());
                    }
                }
                System.out.println("parameterTypes: " + parameterTypes);
            }
        }

    }

}
