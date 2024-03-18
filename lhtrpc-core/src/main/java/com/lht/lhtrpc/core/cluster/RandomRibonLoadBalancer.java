package com.lht.lhtrpc.core.cluster;

import com.lht.lhtrpc.core.api.LoadBalancer;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Leo liang [lhtshent@gmail.com]
 * 2024/3/17 18:26
 */
public class RandomRibonLoadBalancer<T> implements LoadBalancer<T> {

    private AtomicInteger index = new AtomicInteger();
    @Override
    public T choose(List<T> providers) {
        if (CollectionUtils.isEmpty(providers)) return null;
        //index.incrementAndGet() & Integer.MAX_VALUE 是为了避免加太多次溢出
        return providers.get((index.getAndIncrement() & Integer.MAX_VALUE) % providers.size());
    }

}
