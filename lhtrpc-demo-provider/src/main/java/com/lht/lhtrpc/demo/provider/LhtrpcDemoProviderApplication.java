package com.lht.lhtrpc.demo.provider;

import com.lht.lhtrpc.core.api.RpcRequest;
import com.lht.lhtrpc.core.api.RpcResponse;
import com.lht.lhtrpc.core.provider.ProviderBootStrap;
import com.lht.lhtrpc.core.provider.ProviderConfig;
import com.lht.lhtrpc.core.utils.MethodUtils;
import com.lht.lhtrpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Import({ProviderConfig.class})
public class LhtrpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(LhtrpcDemoProviderApplication.class, args);
    }


    @Autowired
    private ProviderBootStrap providerBootStrap;
    //利用http+json实现序列化
    @RequestMapping("/")
    public RpcResponse invoke(@RequestBody RpcRequest request){
        return providerBootStrap.invokeRequest(request);
    }



    @Bean
    ApplicationRunner providerRun(){
        return x->{
            RpcRequest request = new RpcRequest();
            request.setService("com.lht.lhtrpc.demo.api.UserService");
//            request.setMethod(UserService.class.getMethod("findById", int.class));
            request.setMethodSign(MethodUtils.buildMethodSign(UserService.class.getMethod("findById", int.class), UserService.class));
            request.setArgs(new Object[]{500});

            RpcResponse response = invoke(request);
            System.out.println("return: "+response.getData());

            //模拟调用toString方法
            RpcRequest request1 = new RpcRequest();
            request1.setService("com.lht.lhtrpc.demo.api.UserService");
//            request.setMethod(UserService.class.getMethod("findById", int.class));
            request1.setMethodSign(MethodUtils.buildMethodSign(UserServiceImpl.class.getMethod("hashCode"), UserService.class));
            request1.setArgs(new Object[]{500});

            RpcResponse response1 = invoke(request1);
            System.out.println("return: "+response1.getData());

        };
    }


}
