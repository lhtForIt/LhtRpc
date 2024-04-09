package com.lht.lhtrpc.demo.consumer;

import com.lht.lhtrpc.core.test.TestZkServer;
import com.lht.lhtrpc.demo.provider.LhtrpcDemoProviderApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(classes = {LhtrpcDemoConsumerApplication.class})
class LhtrpcDemoConsumerApplicationTests {
	static ApplicationContext context1;
	static ApplicationContext context2;

	static TestZkServer zkServer = new TestZkServer();

	@BeforeAll
	static void init() {
		System.out.println(" ====================================== ");
		System.out.println(" ====================================== ");
		System.out.println(" =============     ZK2182    ========== ");
		System.out.println(" ====================================== ");
		System.out.println(" ====================================== ");
		zkServer.start();
		System.out.println(" ====================================== ");
		System.out.println(" ====================================== ");
		System.out.println(" =============      P8094    ========== ");
		System.out.println(" ====================================== ");
		System.out.println(" ====================================== ");
		context1 = SpringApplication.run(LhtrpcDemoProviderApplication.class,
				"--server.port=8094",
				"--lhtrpc.zk.server=localhost:2182",
				"--lhtrpc.app.env=test",
				"--logging.level.com.lht.lhtrpc=info",
				"--lhtrpc.provider.metas.dc=bj",
				"--lhtrpc.provider.metas.gray=false",
				"--lhtrpc.provider.metas.unit=B001",
				"--lhtrpc.provider.packages=com.lht.lhtrpc.demo.provider"
		);
		System.out.println(" ====================================== ");
		System.out.println(" ====================================== ");
		System.out.println(" =============      P8095    ========== ");
		System.out.println(" ====================================== ");
		System.out.println(" ====================================== ");
		context2 = SpringApplication.run(LhtrpcDemoProviderApplication.class,
				"--server.port=8095",
				"--lhtrpc.zk.server=localhost:2182",
				"--lhtrpc.app.env=test",
				"--logging.level.com.lht.lhtrpc=info",
				"--lhtrpc.provider.metas.dc=bj",
				"--lhtrpc.provider.metas.gray=false",
				"--lhtrpc.provider.metas.unit=B002",
				"--lhtrpc.provider.packages=com.lht.lhtrpc.demo.provider"
		);
	}

	@Test
	void contextLoads() {
		System.out.println(" ===> LhtrpcDemoConsumerApplicationTests  .... ");
	}

	@AfterAll
	static void destory() {
		SpringApplication.exit(context1, () -> 1);
		SpringApplication.exit(context2, () -> 1);
		zkServer.stop();
	}
}
