package com.lht.lhtrpc.core.api;

import lombok.Data;

/**
 * @author Leo
 * @date 2024/03/28
 */
@Data
public class RpcException extends RuntimeException {

    private String errCode;


    /**
     * 异常定义
     * X:技术类异常
     * Y:业务类异常
     * Z=>unknown,现在搞不清楚，等知道了之后在变成X或者Y
      */
    public static final String SocketTimeoutEx = "X001-"+"http_invoke_timeout";
    public static final String NoSuchMethod = "X002-"+"method_not_exists";
    public static final String ExceedLimitEx  = "X003" + "-" + "tps_exceed_limit";
    public static final String UnKnowEx = "Z001-"+"unknown_exception";

    public RpcException() {
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

    public RpcException(Throwable cause, String errCode) {
        super(cause);
        this.errCode = errCode;
    }

    public RpcException(String message, String errCode) {
        super(message);
        this.errCode = errCode;
    }

}
