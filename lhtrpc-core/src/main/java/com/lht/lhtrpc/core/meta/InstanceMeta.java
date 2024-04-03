package com.lht.lhtrpc.core.meta;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述实例的元数据
 *
 * @author Leo
 * @date 2024/03/26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstanceMeta {

    private String scheme;
    private String host;
    private int port;
    private String context;//path

    private boolean status;// 上下线状态 online or offline
    private Map<String, String> parameters = new HashMap<>();//表示哪个机房的，可以加各种参数

    public InstanceMeta(String scheme, String host, int port,String context) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public String toPath() {
        return String.format("%s_%d", host, port);
    }

    public String toUrl() {
        return String.format("%s://%s:%d/%s", scheme, host, port, context);
    }

    public static InstanceMeta http(String host, int port) {
        return new InstanceMeta("http", host, port,"");
    }

    public String toMetas() {
        return JSON.toJSONString(this.parameters);
    }
}
