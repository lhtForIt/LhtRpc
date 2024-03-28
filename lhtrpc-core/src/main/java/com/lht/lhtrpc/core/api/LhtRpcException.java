package com.lht.lhtrpc.core.api;

import lombok.Data;

/**
 * @author Leo
 * @date 2024/03/28
 */
@Data
public class LhtRpcException extends RuntimeException {

    private String errCode;


    /**
     * 异常定义
     * X:技术类异常
     * Y:业务类异常
     * Z=>unknown,现在搞不清楚，等知道了之后在变成X或者Y
      */
    public static final String SocketTimeoutEx = "X001-"+"http_invoke_timeout";
    public static final String NoSuchMethod = "X002-"+"method_not_exists";
    public static final String UnKnowEx = "Z001-"+"unknown_exception";

    public LhtRpcException() {
    }

    public LhtRpcException(String message) {
        super(message);
    }

    public LhtRpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public LhtRpcException(Throwable cause) {
        super(cause);
    }

    public LhtRpcException(Throwable cause,String errCode) {
        super(cause);
        this.errCode = errCode;
    }
}
