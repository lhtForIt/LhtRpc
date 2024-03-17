package com.lht.lhtrpc.core.cluster;

import com.lht.lhtrpc.core.api.LoadBalancer;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Random;

/**
 * Leo liang [lhtshent@gmail.com]
 * 2024/3/17 18:26
 */
public class RandomLoadBalancer<T> implements LoadBalancer<T> {

    private Random random = new Random();

    @Override
    public T choose(List<T> providers) {
        if (CollectionUtils.isEmpty(providers)) return null;
        return providers.get(random.nextInt(providers.size()));
    }
}
