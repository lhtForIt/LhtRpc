package com.lht.lhtrpc.demo.provider;

import com.lht.lhtrpc.core.annotation.LhtProvider;
import com.lht.lhtrpc.demo.api.Order;
import com.lht.lhtrpc.demo.api.OrderService;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Leo
 * @date 2024/03/11
 */
@Component
@LhtProvider
public class OrderServiceImpl implements OrderService {
    @Override
    public Order findById(int id) {
        return new Order(id, id * 3 + 2);
    }

    @Override
    public List<String> findNames(int id) {
        return Arrays.asList("leo","lht","liang");
    }

    @Override
    public String getOrder(Map<String, String> map) {
        return map.values().stream().collect(Collectors.joining(","));
    }

    @Override
    public Order getOrder(int id, Map<Integer, Order> map) {
        return map.get(id);
    }

    @Override
    public String getString(Map<Integer, Order> map) {
//        return map.values().stream().findFirst().get().toString();
        return "123";
    }

    @Override
    public String getString1(Map<String, Order> map) {
        return map.values().stream().findFirst().get().toString();
    }

}
