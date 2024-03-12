package com.lht.lhtrpc.demo.provider;

import com.lht.lhtrpc.core.annotation.LhtProvider;
import com.lht.lhtrpc.core.api.RpcRequest;
import com.lht.lhtrpc.core.api.RpcResponse;
import com.lht.lhtrpc.core.provider.ProviderBootStrap;
import com.lht.lhtrpc.core.provider.ProviderConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

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
            request.setMethod("findById");
            request.setArgs(new Object[]{500});

            RpcResponse response = invoke(request);
            System.out.println("return: "+response.getData());
        };
    }


}
