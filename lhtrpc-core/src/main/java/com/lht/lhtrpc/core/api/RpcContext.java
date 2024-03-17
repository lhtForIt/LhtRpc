package com.lht.lhtrpc.core.api;

import lombok.Data;

/**
 * Leo liang [lhtshent@gmail.com]
 * 2024/3/17 18:47
 *
 * 我们现在有router,loadbalance,filter,providers等，维护列表很长，后续可能还会加，
 * 不好辨认，传递链路也可能很长，这时候利用上下文模式，用一个统一的“容器”管理，
 * 后续只要传这个上下文就行
 *
 */
@Data
public class RpcContext {

    private Router router;
    private LoadBalancer loadBalancer;
    private Filter filter;


}
