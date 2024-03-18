package com.lht.lhtrpc.demo.api;

import java.util.List;
import java.util.Map;

/**
 * @author Leo
 * @date 2024/03/11
 */
public interface OrderService {

    Order findById(int id);

    List<String> findNames(int id);

    String getOrder(Map<String, String> map);

    Order getOrder(int id, Map<Integer, Order> map);

    String getString(Map<Integer, Order> map);

    String getString1(Map<String, Order> map);


}
