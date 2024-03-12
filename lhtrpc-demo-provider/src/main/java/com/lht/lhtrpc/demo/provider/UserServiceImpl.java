package com.lht.lhtrpc.demo.provider;

import com.lht.lhtrpc.core.annotation.LhtProvider;
import com.lht.lhtrpc.demo.api.User;
import com.lht.lhtrpc.demo.api.UserService;
import org.springframework.stereotype.Component;

/**
 * @author Leo
 * @date 2024/03/07
 */
@Component
@LhtProvider
public class UserServiceImpl implements UserService {
    @Override
    public User findById(int id) {
        if (id == 300) {
            throw new RuntimeException("产生异常了--------");
        }
        return new User(id, "Lht" + System.currentTimeMillis());
    }

    @Override
    public int getId(int id) {
        return id;
    }

    @Override
    public String getName(int id) {
        return "leo111";
    }
}
