package com.lht.lhtrpc.core.consumer;

import com.lht.lhtrpc.core.api.RpcRequest;
import com.lht.lhtrpc.core.api.RpcResponse;

/**
 * @author Leo
 * @date 2024/03/26
 */
public interface HttpInvoker {

    RpcResponse<?> post(RpcRequest rpcRequest, String url);

}
