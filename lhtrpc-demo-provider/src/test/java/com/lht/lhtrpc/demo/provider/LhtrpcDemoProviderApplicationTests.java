package com.lht.lhtrpc.demo.provider;

import com.lht.lhtrpc.core.test.TestZkServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LhtrpcDemoProviderApplicationTests {

    static TestZkServer zkServer = new TestZkServer();

    @BeforeAll
    static void init() {
        zkServer.start();
    }

    @Test
    void contextLoads() {
        System.out.println(" ===> lhtrpcDemoProviderApplicationTests  .... ");
    }

    @AfterAll
    static void destory() {
        zkServer.stop();
    }

}
