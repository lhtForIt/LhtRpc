package com.lht.lhtrpc.core.meta;

import lombok.Builder;
import lombok.Data;

/**
 * 描述服务元数据
 *
 * @author Leo
 * @date 2024/03/26
 */
@Data
@Builder
public class ServiceMeta {

    private String app;//应用名称
    private String namespace;//命名空间，做服务隔离
    private String env;//环境
    private String name;//服务名称
    private String version;//版本号


    public String toPath(){
        return String.format("%s_%s_%s_%s", app, namespace, env, name);
    }


}
