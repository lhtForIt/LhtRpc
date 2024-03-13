package com.lht.lhtrpc.demo.api;

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




}
