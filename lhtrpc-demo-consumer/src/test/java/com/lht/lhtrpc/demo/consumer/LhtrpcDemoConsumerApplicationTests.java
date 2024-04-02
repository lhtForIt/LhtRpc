package com.lht.lhtrpc.demo.consumer;

import com.lht.lhtrpc.core.test.TestZkServer;
import com.lht.lhtrpc.demo.provider.LhtrpcDemoProviderApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * 这里要测试provider端需要将Provider里面所有的xxximpl实现都放到provider的包下，这样才能扫描到放到桩里，否则会报错
 * 这样写两端依赖是否太强了？实际应该是直接mock provider端调用就行
 */
@SpringBootTest(classes = {LhtrpcDemoConsumerApplication.class})
class LhtrpcDemoConsumerApplicationTests {
	static ApplicationContext context;

	static TestZkServer zkServer = new TestZkServer();

	@BeforeAll
	static void init() {
		System.out.println(" ====================================== ");
		System.out.println(" ====================================== ");
		System.out.println(" ====================================== ");
		System.out.println(" ====================================== ");
		System.out.println(" ====================================== ");
		System.out.println(" ====================================== ");

		zkServer.start();
		context = SpringApplication.run(LhtrpcDemoProviderApplication.class,
				"--server.port=8094", "--lhtrpc.zkServer=localhost:2182",
				"--logging.level.com.lht.lhtrpc=info");
	}

	@Test
	void contextLoads() {
		System.out.println(" ===> aaaa  .... ");
	}

	@AfterAll
	static void destory() {
		SpringApplication.exit(context, () -> 1);
		zkServer.stop();
	}
}
