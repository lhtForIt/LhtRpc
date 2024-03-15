package com.lht.lhtrpc.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.core.annotation.Order;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static Object convertType(Object sourceType, Class<?> targetType) {

        if (sourceType == null) return null;

        Class<?> sourceTypeClass = sourceType.getClass();

        //转换类型是被转换类型子类直接返回(targetType是sourceTypeClass的超类或接口)
        if (targetType.isAssignableFrom(sourceTypeClass)) return sourceType;

        //对象
        if (sourceType instanceof Map jsonObj) {
            JSONObject jsonObject = new JSONObject(jsonObj);
            return jsonObject.toJavaObject(targetType);
//            return JSON.parseObject(JSON.toJSONString(jsonObj), targetType);
        }

        //数组
        if (targetType.isArray()) {
            if (sourceType instanceof List list) {
                sourceType = list.toArray();
            }
            int length = Array.getLength(sourceType);
            Class<?> componentType = targetType.getComponentType();
            Object arr = Array.newInstance(componentType, length);
            for (int i = 0; i < length; i++) {
                Array.set(arr, i, Array.get(sourceType, i));
            }
            return arr;
        }


        if (targetType.equals(Integer.class) || targetType.equals(Integer.TYPE)) {
            return Integer.valueOf(sourceType.toString());
        } else if (targetType.equals(Long.class) || targetType.equals(Long.TYPE)) {
            return Long.valueOf(sourceType.toString());
        } else if (targetType.equals(Double.class) || targetType.equals(Double.TYPE)) {
            return Double.valueOf(sourceType.toString());
        } else if (targetType.equals(Float.class) || targetType.equals(Float.TYPE)) {
            return Float.valueOf(sourceType.toString());
        } else if (targetType.equals(Boolean.class) || targetType.equals(Boolean.TYPE)) {
            return Boolean.valueOf(sourceType.toString());
        } else if (targetType.equals(Short.class) || targetType.equals(Short.TYPE)) {
            return Short.valueOf(sourceType.toString());
        } else if (targetType.equals(Character.class) || targetType.equals(Character.TYPE)) {
            return Character.valueOf(sourceType.toString().toCharArray()[0]);
        } else if (targetType.equals(Byte.class) || targetType.equals(Byte.TYPE)) {
            return Byte.valueOf(sourceType.toString());
        }

        return sourceType;

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
