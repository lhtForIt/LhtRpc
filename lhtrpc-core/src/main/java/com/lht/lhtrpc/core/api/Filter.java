package com.lht.lhtrpc.core.api;

import java.util.List;

/**
 *
 * 过滤器
 *
 * Leo liang [lhtshent@gmail.com]
 * 2024/3/17 17:22
 *
 * 对访问进行过滤，前置处理和后置处理
 *
 */
public interface Filter<T> {

    Object prefilter(RpcRequest request);

    Object postfilter(RpcRequest request, RpcResponse response, Object result);

//    Filter next();

    Filter Default = new Filter() {
        @Override
        public Object prefilter(RpcRequest request) {
            return null;
        }

        @Override
        public Object postfilter(RpcRequest request, RpcResponse response, Object result) {
            return result;
        }
    };

}
