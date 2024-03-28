package com.lht.lhtrpc.core.utils;

import lombok.SneakyThrows;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * @author Leo
 * @date 2024/03/27
 */
public class MockUtils {



    public static Object mock(Class<?> clazz)  {

        if (clazz == null) {
            return null;
        }

        if (clazz.equals(Integer.class) || clazz.equals(Integer.TYPE)) {
            return 1;
        } else if (clazz.equals(Long.class) || clazz.equals(Long.TYPE)) {
            return 10000L;
        } else if (clazz.equals(Double.class) || clazz.equals(Double.TYPE)) {
            return 2d;
        } else if (clazz.equals(Float.class) || clazz.equals(Float.TYPE)) {
            return 4F;
        } else if (clazz.equals(Boolean.class) || clazz.equals(Boolean.TYPE)) {
            return true;
        } else if (clazz.equals(Short.class) || clazz.equals(Short.TYPE)) {
            return Short.valueOf("3");
        } else if (clazz.equals(Character.class) || clazz.equals(Character.TYPE)) {
            return 'a';
        } else if (clazz.equals(Byte.class) || clazz.equals(Byte.TYPE)) {
            return Byte.valueOf("8");
        } else if (clazz.equals(String.class)) {
            return "this_is_a_mock_string";
        }

        return castToObj(clazz);

    }

    /**
     * 只能创建基本类型，数组和list不行。
     * @param clazz
     * @return
     */
    @SneakyThrows
    private static Object castToObj(Class<?> clazz) {
        Object instance = clazz.getDeclaredConstructor().newInstance();
        Arrays.stream(clazz.getDeclaredFields()).forEach(d->{
            try {
                d.setAccessible(true);
                Class<?> type = d.getType();
                d.set(instance,mock(type));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return instance;
    }

    public static void main(String[] args) {
        System.out.println(mock(UserDto.class));
    }

    public static class UserDto{
        private int a;
        private String b;

        @Override
        public String toString() {
            return a + "," + b;
        }
    }

}
