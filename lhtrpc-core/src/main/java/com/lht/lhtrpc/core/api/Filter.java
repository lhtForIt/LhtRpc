package com.lht.lhtrpc.core.api;

import java.util.List;

/**
 * Leo liang [lhtshent@gmail.com]
 * 2024/3/17 17:22
 *
 * 对访问进行过滤，前置处理和后置处理
 *
 */
public interface Filter<T> {

    List<T> filter(List<T> providers);

}
