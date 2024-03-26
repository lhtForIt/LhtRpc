package com.lht.lhtrpc.core.utils;

import com.alibaba.fastjson.JSONObject;
import com.lht.lhtrpc.core.api.RpcResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Leo
 * @date 2024/03/26
 */
public class TypeUtils {

    @Nullable
    public static Object buildResponse(Method method, RpcResponse rpcResponse) {
        Object data = rpcResponse.getData();
        if (data == null) {
            return null;
        }
        if (Map.class.isAssignableFrom(data.getClass())) {
            Map map = new HashMap();
            Type genericReturnType = method.getGenericReturnType();
            if (genericReturnType instanceof ParameterizedType parameterizedType) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                Class<?> keyType = (Class<?>) actualTypeArguments[0];
                Class<?> valueType = (Class<?>) actualTypeArguments[1];
                ((Map) data).entrySet().stream().forEach(entry -> {
                    Map.Entry e = (Map.Entry) entry;
                    Object key = convertType(e.getKey(), keyType);
                    Object value = convertType(e.getValue(), valueType);
                    map.put(key, value);
                });
                return map;
            }
        } else if (List.class.isAssignableFrom(data.getClass()) && !data.getClass().isArray()) {
            //如果list里面是实体类(User)，会当成一个map，这时候需要转，否则一旦获取user对象操作就会出错
            Type genericReturnType = method.getGenericReturnType();
            if (genericReturnType instanceof ParameterizedType parameterizedType) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                Class<?> valueType = (Class<?>) actualTypeArguments[0];
                List list = (List) data;
                List newList = new ArrayList();
                list.forEach(e -> {
                    Object value = convertType(e, valueType);
                    newList.add(value);
                });
                return newList;
            }
        }
        return convertType(data, method.getReturnType());
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


    public static void buildNewArgs(Object[] args, Method method, Class<?>[] parameterTypes, int i, Object[] newArgs) {
        if (Map.class.isAssignableFrom(parameterTypes[i])) {
            Map map = new HashMap();
            Type[] genericParameterTypes = method.getGenericParameterTypes();
            if (genericParameterTypes[i] instanceof ParameterizedType parameterizedType) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                Class<?> keyType = (Class<?>) actualTypeArguments[0];
                Class<?> valueType = (Class<?>) actualTypeArguments[1];
                ((Map) args[i]).entrySet().stream().forEach(e -> {
                    Map.Entry entry = (Map.Entry) e;
                    Object key = TypeUtils.convertType(entry.getKey(), keyType);
                    Object value = TypeUtils.convertType(entry.getValue(), valueType);
                    map.put(key, value);
                });
                newArgs[i] = map;
            } else {
                newArgs[i] = args[i];
            }
        } else if (List.class.isAssignableFrom(parameterTypes[i])) {
            List list = new ArrayList();
            Type[] genericParameterTypes = method.getGenericParameterTypes();
            if (genericParameterTypes[i] instanceof ParameterizedType parameterizedType) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                Class<?> type = (Class<?>) actualTypeArguments[0];
                ((List) args[i]).stream().forEach(d -> list.add(TypeUtils.convertType(d, type)));
                newArgs[i] = list;
            } else {
                newArgs[i] = args[i];
            }
        } else {
            newArgs[i] = TypeUtils.convertType(args[i], parameterTypes[i]);
        }
    }

    @NotNull
    public static Object[] initMapKey(Object[] args) {
        if (args == null || args.length == 0) return args;
        Object[] newArg;
        newArg= new Object[args.length];
        for (int i = 0; i < newArg.length; i++) {
            if (args[i] instanceof Map map) {
                if (!CollectionUtils.isEmpty(map)) {
                    Map<Object, Object> newMap = new HashMap<>();
                    map.forEach((key, value) -> {
                        newMap.put(String.valueOf(key), value);
                    });
                    newArg[i] = newMap;
                    continue;
                }
            }
            newArg[i] = args[i];
        }
        return newArg;
    }

}
