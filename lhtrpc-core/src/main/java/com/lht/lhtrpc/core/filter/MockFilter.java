package com.lht.lhtrpc.core.filter;

import com.lht.lhtrpc.core.api.Filter;
import com.lht.lhtrpc.core.api.RpcRequest;
import com.lht.lhtrpc.core.api.RpcResponse;
import com.lht.lhtrpc.core.utils.MethodUtils;
import com.lht.lhtrpc.core.utils.MockUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Leo
 * @date 2024/03/27
 */
public class MockFilter implements Filter {
    @Override
    public Object prefilter(RpcRequest request) {
        try {
            Class<?> service = Class.forName(request.getService());
            Method method = findMethod(service, request.getMethodSign());
            return MockUtils.mock(method.getReturnType());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Method findMethod(Class<?> service, String methodSign) {
        Method[] methods = service.getDeclaredMethods();
        for (Method method : methods) {
            //这里直接过滤掉Object类的本地方法，后面直接找不到对应方法签名的provider
            if (Object.class.equals(method.getDeclaringClass())) {
                continue;
            }
            if (StringUtils.equals(MethodUtils.buildMethodSign(method), methodSign)) {
                return method;
            }
        }
        return null;
    }

    @Override
    public Object postfilter(RpcRequest request, RpcResponse response, Object result) {
        return null;
    }
}
