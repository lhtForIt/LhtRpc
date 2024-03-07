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
        return new User(id, "Lht" + System.currentTimeMillis());
    }
}
