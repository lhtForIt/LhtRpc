package com.lht.lhtrpc.demo.provider;

import com.lht.lhtrpc.core.api.RpcRequest;
import com.lht.lhtrpc.core.api.RpcResponse;
import com.lht.lhtrpc.core.config.ProviderConfig;
import com.lht.lhtrpc.core.provider.ProviderInvoker;
import com.lht.lhtrpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Import({ProviderConfig.class})
public class LhtrpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(LhtrpcDemoProviderApplication.class, args);
    }


    @Autowired
    private ProviderInvoker providerInvoker;

    @Autowired
    private UserService userService;

    //利用http+json实现序列化
    @RequestMapping("/")
    public RpcResponse invoke(@RequestBody RpcRequest request){
        return providerInvoker.invokeRequest(request);
    }

    @RequestMapping("/port")
    public RpcResponse<String> setPorts(@RequestParam("ports") String ports) {
        userService.setPorts(ports);
        RpcResponse<String> response = new RpcResponse();
        response.setStatus(true);
        response.setData("OK:" + ports);
        return response;
    }

    @Bean
    ApplicationRunner providerRun(){
        return x->{
            RpcRequest request = new RpcRequest();
            request.setService("com.lht.lhtrpc.demo.api.UserService");
            request.setMethodSign("getName@1_int");
            request.setArgs(new Object[]{500});

            RpcResponse response = invoke(request);
            System.out.println("return: "+response.getData());

            RpcRequest request1 = new RpcRequest();
            request1.setService("com.lht.lhtrpc.demo.api.UserService");
            request1.setMethodSign("getName@1_float");
            request1.setArgs(new Object[]{500F});

            RpcResponse response1 = invoke(request1);
            System.out.println("return: "+response1.getData());

        };
    }


}
