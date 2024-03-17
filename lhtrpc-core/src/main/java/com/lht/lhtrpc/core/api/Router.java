package com.lht.lhtrpc.core.api;

import java.util.List;

/**
 * Leo liang [lhtshent@gmail.com]
 * 2024/3/17 17:23
 *
 * 对访问的请求进行路由，从一个大的集合里面拿到一个符合条件的小的集合
 * 比如我选不同的机房，在初始化的时候对它们打tag，如果想访问某个机房，就根据tag进行过滤，从而
 * 实现容灾时跨机房访问
 *
 *
 */
public interface Router<T> {

    List<T> route(List<T> providers);

    Router Default= p -> p;


}
