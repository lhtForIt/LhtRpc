package com.lht.lhtrpc.core.api;

import com.lht.lhtrpc.core.meta.InstanceMeta;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private Router<InstanceMeta> router;
    private LoadBalancer<InstanceMeta> loadBalancer;
    private List<Filter> filters;
    //动态传参，玩花活
    private Map<String, String> paramerters = new HashMap<>();
    // kkrpc.color = gray
    // kkrpc.gtrace_id
    // gw -> service1 ->  service2(跨线程传递) ...
    // http headers

}
