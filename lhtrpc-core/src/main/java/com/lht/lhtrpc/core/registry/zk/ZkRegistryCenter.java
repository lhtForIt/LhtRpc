package com.lht.lhtrpc.core.registry.zk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lht.lhtrpc.core.registry.ChangedListener;
import com.lht.lhtrpc.core.api.RpcException;
import com.lht.lhtrpc.core.api.RegistryCenter;
import com.lht.lhtrpc.core.meta.InstanceMeta;
import com.lht.lhtrpc.core.meta.ServiceMeta;
import com.lht.lhtrpc.core.registry.Event;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Leo liang [lhtshent@gmail.com]
 * 2024/3/17 19:40
 */
@Slf4j
public class ZkRegistryCenter implements RegistryCenter {

    private CuratorFramework client;

    private String servers;

    private String root;

    private List<TreeCache> caches = new ArrayList<>();

    public ZkRegistryCenter(String servers, String root) {
        this.servers = servers;
        this.root = root;
    }

    @Override
    public void start() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(this.servers)
                .namespace(this.root)
                .retryPolicy(retryPolicy)
                .build();
        log.info("===> zk client starting to server[" + this.servers + "/" + this.root + "].");
        client.start();
    }

    @Override
    public void stop() {
        log.info(" ===> zk tree cache closed.");
        caches.forEach(TreeCache::close);
        log.info("===> zk client stopped.");
        client.close();
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.toPath();
        try {
            //创建服务持久化节点
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, service.toMetas().getBytes());
            }
            //创建实例的临时性节点
            String instancePath = servicePath + "/" + instance.toPath();
            log.info("===> register to zk " + "/" + service.toPath() + " /" + instance);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, instance.toMetas().getBytes());
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.toPath();
        try {
            //判断服务是否存在
            if (client.checkExists().forPath(servicePath) == null) {
                return;
            }
            //删除实例节点
            String instancePath = servicePath + "/" + instance.toPath();
            log.info("===> unregister to zk " + "/" + service.toPath() + " /" + instance);
            client.delete().quietly().forPath(instancePath);//quietly如果没有节点则不抛出异常
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        String servicePath = "/" + service.toPath();
        try {
            //获取所有子节点
            List<String> nodes = client.getChildren().forPath(servicePath);
            log.info("===> fetch all to zk " + servicePath);
            nodes.forEach(System.out::println);
            return mapInstance(nodes, servicePath);
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    private List<InstanceMeta> mapInstance(List<String> nodes,String servicePath) {
        return nodes.stream().map(d -> {
            String[] s = d.split("_");
            InstanceMeta instance = InstanceMeta.http(s[0], Integer.valueOf(s[1]));
            log.debug(" instance : {}", instance.toUrl());
            String nodePath = servicePath + "/" + d;
            byte[] bytes;
            try {
                bytes = client.getData().forPath(nodePath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Map<String, Object> params = JSON.parseObject(new String(bytes));
            params.forEach((k,v)->{
                log.debug("{} -> {}", k, v);
                instance.getParameters().put(k, v == null ? null : v.toString());
            });
            return instance;
        }).collect(Collectors.toList());
    }

    @SneakyThrows
    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        final TreeCache cache = TreeCache.newBuilder(client, "/" + service.toPath())
                .setCacheData(true).setMaxDepth(2).build();
        cache.getListenable().addListener((curator, event) -> {
            log.info("zk subscribe event:" + event);
            List<InstanceMeta> nodes = fetchAll(service);
            listener.fire(new Event(nodes));
        });
        cache.start();
        caches.add(cache);
    }


}
