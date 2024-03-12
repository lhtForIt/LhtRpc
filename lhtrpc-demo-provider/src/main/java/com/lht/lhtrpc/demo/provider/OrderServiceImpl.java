package com.lht.lhtrpc.demo.provider;

import com.lht.lhtrpc.core.annotation.LhtProvider;
import com.lht.lhtrpc.demo.api.Order;
import com.lht.lhtrpc.demo.api.OrderService;
import org.springframework.stereotype.Component;

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
}
