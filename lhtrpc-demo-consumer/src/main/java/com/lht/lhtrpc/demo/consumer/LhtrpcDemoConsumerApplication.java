package com.lht.lhtrpc.demo.consumer;

import com.lht.lhtrpc.core.annotation.LhtConsumer;
import com.lht.lhtrpc.core.consumer.ConsumerConfig;
import com.lht.lhtrpc.demo.api.Order;
import com.lht.lhtrpc.demo.api.OrderService;
import com.lht.lhtrpc.demo.api.User;
import com.lht.lhtrpc.demo.api.UserService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ConsumerConfig.class})
public class LhtrpcDemoConsumerApplication {

	@LhtConsumer
	private UserService userService;

	@LhtConsumer
	private OrderService orderService;

	public static void main(String[] args) {
		SpringApplication.run(LhtrpcDemoConsumerApplication.class, args);
	}


	@Bean
	public ApplicationRunner consumerRunner() {
		return x->{
			User user = userService.findById(200);
			System.out.println("return userService.findById(200)=" + user);


			Order order = orderService.findById(200);
			System.out.println("return orderService.findById(200)=" + order);


			//mock异常调用
//			User user1 = userService.findById(300);
//			System.out.println("return userService.findById(300)=" + user1);

			//被屏蔽
			String string = orderService.toString();
			System.out.println("return orderService.toString()=" + string);

			int id = userService.getId(10);
			System.out.println("return userService.getId(10)=" + id);

			String s = userService.getName(11);
			System.out.println("return userService.getName(11)=" + s);



		};
	}

}
