package com.lht.lhtrpc.core.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Leo
 * @date 2024/03/07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse<T> {

    private boolean status;//状态
    private T data;//数据

}
