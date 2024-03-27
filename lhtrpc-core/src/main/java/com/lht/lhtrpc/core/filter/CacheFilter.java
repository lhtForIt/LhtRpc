package com.lht.lhtrpc.core.filter;

import com.lht.lhtrpc.core.api.Filter;
import com.lht.lhtrpc.core.api.RpcRequest;
import com.lht.lhtrpc.core.api.RpcResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Leo
 * @date 2024/03/27
 */
public class CacheFilter implements Filter {

    private Map<String, Object> cache = new ConcurrentHashMap<>();

    @Override
    public Object prefilter(RpcRequest request) {
        return cache.get(request.toString());
    }

    @Override
    public Object postfilter(RpcRequest request, RpcResponse response,Object result) {
        cache.putIfAbsent(request.toString(), result);
        return result;
    }
}
