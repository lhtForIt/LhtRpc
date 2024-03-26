package com.lht.lhtrpc.core.meta;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 描述实例的元数据
 *
 * @author Leo
 * @date 2024/03/26
 */
@Data
public class InstanceMeta {

    private String scheme;
    private String host;
    private int port;
    private String context = "";//path

    private boolean status;// 上下线状态 online or offline
    private Map<String, String> parameters;//表示哪个机房的

    public InstanceMeta(String scheme, String host, int port) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
    }

    public String toPath() {
        return String.format("%s_%d", host, port);
    }

    public String toUrl() {
        return String.format("%s://%s:%d/%s", scheme, host, port, context);
    }

    public static InstanceMeta http(String host, int port) {
        return new InstanceMeta("http", host, port);
    }

}