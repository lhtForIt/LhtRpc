package com.lht.lhtrpc.demo.provider;

import com.lht.lhtrpc.core.annotation.LhtProvider;
import com.lht.lhtrpc.core.api.RpcResponse;
import com.lht.lhtrpc.demo.api.User;
import com.lht.lhtrpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Leo
 * @date 2024/03/07
 */
@Component
@LhtProvider
public class UserServiceImpl implements UserService {

    @Autowired
    private Environment environment;

    @Override
    public User findById(int id) {
        if (id == 300) {
            throw new RuntimeException("产生异常了--------");
        }
        String port = environment.getProperty("server.port");
        return new User(id, "Lht-V5-" + port + "_" + System.currentTimeMillis());
    }

    @Override
    public int getId(int id) {
        return id;
    }

    @Override
    public String getName(int id) {
        return "leo111";
    }

    @Override
    public String getName(float f) {
        return "leo111f";
    }

    @Override
    public long getId(long id) {
        return id;
    }

    @Override
    public int getId(User user) {
        return user.getId();
    }

    @Override
    public long[] getIds() {
        return new long[]{2L, 3L, 4};
    }

    @Override
    public int[] getIds(int[] ids) {
        return new int[]{1, 2, 3};
    }

    @Override
    public Map<String, User> getMap(Map<String, User> map) {
//        return Map.of("1", new User(1, "leo111"));
        return map;
    }

    @Override
    public List<User> getList(List<User> list) {
        return list;
    }

    @Override
    public User find(int timeout) {
        String port = environment.getProperty("server.port");
        if (Arrays.stream(ports.split(",")).anyMatch(p -> p.equals(port))) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return new User(123, "Lht-" + port + "_" + System.currentTimeMillis());
    }

    private String ports = "8081,8094";
    @Override
    public void setPorts(String ports) {
        this.ports = ports;
    }


}
