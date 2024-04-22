package com.lht.lhtrpc.demo.provider;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.lht.lhtrpc.core.api.RpcException;
import com.lht.lhtrpc.core.api.RpcRequest;
import com.lht.lhtrpc.core.api.RpcResponse;
import com.lht.lhtrpc.core.config.ApolloChangedListener;
import com.lht.lhtrpc.core.config.ProviderConfig;
import com.lht.lhtrpc.core.config.ProviderProperties;
import com.lht.lhtrpc.core.provider.ProviderInvoker;
import com.lht.lhtrpc.core.transport.SpringBootTransport;
import com.lht.lhtrpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.properties.ConfigurationPropertiesRebinder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableApolloConfig
@Import({ProviderConfig.class})
public class LhtrpcDemoProviderApplication {


    public static void main(String[] args) {
        SpringApplication.run(LhtrpcDemoProviderApplication.class, args);
    }

    @Autowired
    private UserService userService;

    @Autowired
    private SpringBootTransport springBootTransport;

    @Autowired
    private ProviderProperties providerProperties;

    @RequestMapping("/metas")
    public String meta() {
        System.out.println(System.identityHashCode(providerProperties.getMetas()));
        return providerProperties.getMetas().toString();
    }

    /**
     * 这块本来想移到core里面，但是它会依赖UserService，是在api里面，所以先只能放在服务端
     * @param ports
     * @return
     */
    @RequestMapping("/port")
    public RpcResponse<String> setPorts(@RequestParam("ports") String ports) {
        userService.setPorts(ports);
        RpcResponse<String> response = new RpcResponse();
        response.setStatus(true);
        response.setData("OK:" + ports);
        return response;
    }

    @Bean
    ApplicationRunner providerRun(ApplicationContext context){
        return x->{
            ConfigurationPropertiesRebinder rebinder = context.getBean(ConfigurationPropertiesRebinder.class);
            System.out.println(rebinder);
            testAll();
        };
    }

    private void testAll() {
        RpcRequest request = new RpcRequest();
        request.setService("com.lht.lhtrpc.demo.api.UserService");
        request.setMethodSign("getName@1_int");
        request.setArgs(new Object[]{500});

        RpcResponse response = springBootTransport.invoke(request);
        System.out.println("return: "+response.getData());

        RpcRequest request1 = new RpcRequest();
        request1.setService("com.lht.lhtrpc.demo.api.UserService");
        request1.setMethodSign("getName@1_float");
        request1.setArgs(new Object[]{500F});

        RpcResponse response1 = springBootTransport.invoke(request1);
        System.out.println("return: "+response1.getData());


        //限流测试
//        System.out.println("Provider >>===[复杂测试：测试流量并发控制]===");
//        for (int i = 0; i < 120; i++) {
//            try {
//                Thread.sleep(1000);
//                RpcResponse<Object> r = springBootTransport.invoke(request);
//                System.out.println(i + " ***>>> " +r.getData());
//            } catch (RpcException e) {
//                // ignore
//                System.out.println(i + " ***>>> " +e.getMessage() + " -> " + e.getErrCode());
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
    }


}
