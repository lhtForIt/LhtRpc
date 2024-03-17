package com.lht.lhtrpc.core.api;

import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Leo liang [lhtshent@gmail.com]
 * 2024/3/17 17:22
 * <p>
 * 为避免一个服务器的压力过大，会有一定的负载均衡策略，
 * 比如轮询、随机、权重轮询、一致性哈希等，都是在这里去实现
 *
 * 权重实现其实是按占比实现的，比如有两台机器8081和8082,
 * 8081,w=100,25次
 * 8082,w=300,75次
 *
 * 权重w是100和300，那么比重就是1:3，放到100次访问里面就是前者25次后者75次。那么实现就可以取一个0~100的随机数，
 * 0~99,r<25 8081 else 8082。 因为访问量拉大看，随机数是最平均的。
 *
 * AAWR-自适应
 * 根据每次访问的相映时间，动态给于权重，响应时间段的，权重就高，当然实际实现会比较复杂，比如如何评估权重等，
 *
 * avg*0.3+last*0.7=W* ~
 *
 */
public interface LoadBalancer<T> {

    T choose(List<T> providers);


    LoadBalancer Default = p -> CollectionUtils.isEmpty(p) ? null : p.get(0);

}
