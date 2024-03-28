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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
@RestController
@Import({ConsumerConfig.class})
public class LhtrpcDemoConsumerApplication {

	@LhtConsumer
	private UserService userService;

	@LhtConsumer
	private OrderService orderService;

	public static void main(String[] args) {
		SpringApplication.run(LhtrpcDemoConsumerApplication.class, args);
	}

	//网关
	@RequestMapping("/api")
	public User find(@RequestParam("id") int id){
		return userService.findById(id);
	}


	@RequestMapping("/time")
	public User findTime(@RequestParam("timeout") int timeout){
		return userService.find(timeout);
	}


	@Bean
	public ApplicationRunner consumerRunner() {
		return x->{

			long start = System.currentTimeMillis();
			User user = userService.find(800);
			System.out.println("userService.find(800) " + user);
			System.out.println("userService.find take " + (System.currentTimeMillis() - start) + "ms");

//			testAll();

		};
	}

	private void testAll() {
		User user = userService.findById(200);
		System.out.println("return userService.findById(200)=" + user);


		Order order = orderService.findById(200);
		System.out.println("return orderService.findById(200)=" + order);


		//mock异常调用
		try {
			User user4 = userService.findById(300);
			System.out.println("return userService.findById(300)=" + user4);
		}catch (Exception e){
			System.out.println("=====>exception: " + e.getMessage());
		}

		//被屏蔽
		String string = orderService.toString();
		System.out.println("return orderService.toString()=" + string);

		int id = userService.getId(10);
		System.out.println("return userService.getId(10)=" + id);

		String s = userService.getName(11);
		System.out.println("return userService.getName(11)=" + s);

		long l = userService.getId(13L);
		System.out.println("return userService.getId(13L)=" + l);

		int userId = userService.getId(new User(22, "leo"));
		System.out.println("return userService.getId(new User(22, \"leo\"))=" + userId);

		int[] id1 = userService.getIds(new int[]{1});
		System.out.println("return userService.getIds(new int[]{1})=> ");
		Arrays.stream(id1).forEach(i -> System.out.println(i));

		long[] ids = userService.getIds();
		System.out.println("return userService.getIds()=> ");
		Arrays.stream(ids).forEach(i -> System.out.println(i));

		String order1 = orderService.getOrder(Map.of("1", "2", "2", "3", "3", "4"));
		System.out.println("return orderService.getOrder(Map.of(\"1\", \"2\", \"2\", \"3\", \"3\", \"4\"))=" + order1);

		List<String> names = orderService.findNames(3);
		System.out.println("return orderService.findNames(3)=" + names.stream().collect(Collectors.joining(",")));

		Order order2 = orderService.getOrder(1, Map.of(1, new Order(1, 1.2d), 2, new Order(2, 2.2d)));
		System.out.println("return orderService.getOrder(1, Map.of(1, new Order(1, 1.2d), 2, new Order(2, 2.2d)))=" + order2);
//
		String string2 = orderService.getString(Map.of(1, new Order(1, 1.2d), 2, new Order(2, 2.2d)));
		System.out.println("return orderService.getString(Map.of(1, new Order(1, 1.2d), 2, new Order(2, 2.2d)))=" + string2);

		String string1 = orderService.getString1(Map.of("1", new Order(1, 1.2d), "2", new Order(2, 2.2d)));
		System.out.println("return orderService.getString1(Map.of(1, new Order(1, 1.2d), 2, new Order(2, 2.2d)))=" + string1);


		//作业
		Map<String, User> map = userService.getMap(Map.of("1", new User(1, "leo"), "2", new User(2, "lht")));
		User user2 = map.get("1");
		System.out.println("return userService.getMap(new HashMap<>())=" + map);
		System.out.println("return map里面的user=" + user2);

		List<User> list = userService.getList(Arrays.asList(new User(1, "leo"), new User(2, "lht")));
		User user1 = list.get(0);
		System.out.println("return userService.getList(Arrays.asList(new User(1, \"leo\"), new User(2, \"lht\")))=" + list);
		System.out.println("return list里面的user=" + user1);
	}

}
