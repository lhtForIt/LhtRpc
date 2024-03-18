package com.lht.lhtrpc.demo.api;

import java.util.List;
import java.util.Map;

/**
 * @author Leo
 * @date 2024/03/07
 */
public interface UserService {

    User findById(int id);

    int getId(int id);

    String getName(int id);


    String getName(float f);

    long getId(long id);

    int getId(User user);

    long[] getIds();

    int[] getIds(int[] ids);

    Map<String, User> getMap(Map<String, User> map);

    List<User> getList(List<User> list);






}
