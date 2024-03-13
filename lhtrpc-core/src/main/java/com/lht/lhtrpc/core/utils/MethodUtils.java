package com.lht.lhtrpc.core.utils;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Method;

/**
 * @author Leo
 * @date 2024/03/13
 */
public class MethodUtils {

    public static String buildMethodSign(Method method, Class<?> service) {
        StringBuilder sb = new StringBuilder(service.getCanonicalName() + "@");
        sb.append(method.getName() + "@");
        sb.append(method.getParameterCount());//这玩意貌似可以不要？
        Class<?>[] parameters = method.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            sb.append("_" + parameters[i].getCanonicalName());
        }
        return sb.toString();
    }


    public static Object convertType(Object requestType, String returnType) throws ClassNotFoundException {
        if (!requestType.getClass().getCanonicalName().equals(returnType)) {
            if (returnType.equals("java.lang.String")) {
                return requestType.toString();
            } else if (returnType.equals("int") || returnType.equals("java.lang.Integer")) {
                return Integer.valueOf(requestType.toString());
            } else if (returnType.equals("long") || returnType.equals("java.lang.Long")) {
                return Long.valueOf(requestType.toString());
            } else if (returnType.equals("double") || returnType.equals("java.lang.Double")) {
                return Double.valueOf(requestType.toString());
            } else if (returnType.equals("float") || returnType.equals("java.lang.Float")) {
                return Float.valueOf(requestType.toString());
            } else if (returnType.equals("boolean") || returnType.equals("java.lang.Boolean")) {
                return Boolean.valueOf(requestType.toString());
            } else if (returnType.equals("char") || returnType.equals("java.lang.Character")) {
                return Character.valueOf((Character) requestType);
            } else if (returnType.equals("byte") || returnType.equals("java.lang.Byte")) {
                return Byte.valueOf(requestType.toString());
            } else if (returnType.equals("short") || returnType.equals("java.lang.Short")) {
                return Short.valueOf(requestType.toString());
            } else {
                return JSON.parseObject(JSON.toJSONString(requestType), Class.forName(returnType));
            }
        }
        return requestType;
    }


}
