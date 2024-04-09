package com.lht.lhtrpc.core.transport;

import com.lht.lhtrpc.core.api.RpcRequest;
import com.lht.lhtrpc.core.api.RpcResponse;
import com.lht.lhtrpc.core.provider.ProviderInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Leo
 * @date 2024/04/09
 */
@RestController
public class SpringBootTransport {

    @Autowired
    private ProviderInvoker providerInvoker;

    //利用http+json实现序列化
    @RequestMapping("/lhtrpc")
    public RpcResponse invoke(@RequestBody RpcRequest request){
        return providerInvoker.invokeRequest(request);
    }



}
