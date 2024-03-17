package com.lht.lhtrpc.core.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

/**
 * @author Leo
 * @date 2024/03/07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest {
    private String service;//接口
    private String methodSign;//方法(这种遇到同名方法参数不同就不能处理，后续是构造方法签名，name@arg1_arg2这种）
    private Object[] args;//参数(我一开始想的是直接拿方法和参数判断，但是不行，参数可以传null，这种就拿不到参数类型) 这里其实理解很有问题，完全不需要用到参数，直接用method拿到方法名和参数类型然后对比即可，这样即使传空也不会有影响，因为就用不到args
}
