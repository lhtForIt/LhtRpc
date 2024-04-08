package com.lht.lhtrpc.core.filter;

import com.lht.lhtrpc.core.api.Filter;
import com.lht.lhtrpc.core.api.RpcContext;
import com.lht.lhtrpc.core.api.RpcRequest;
import com.lht.lhtrpc.core.api.RpcResponse;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * @author Leo
 * @date 2024/04/08
 */
public class ParameterFilter implements Filter {
    @Override
    public Object prefilter(RpcRequest request) {
        Map<String, String> params = RpcContext.ContextParameters.get();
        if (!CollectionUtils.isEmpty(params)) {
            request.getParameters().putAll(params);
        }
        return null;
    }

    @Override
    public Object postfilter(RpcRequest request, RpcResponse response, Object result) {
//        RpcContext.ContextParameters.get().clear(); //这种写法每次调用都会清空参数，下次需要重新传参
        return null;
    }
}
