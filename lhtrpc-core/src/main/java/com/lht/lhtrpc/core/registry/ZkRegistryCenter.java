package com.lht.lhtrpc.core.registry;

import com.lht.lhtrpc.core.api.ChangedListener;
import com.lht.lhtrpc.core.api.RegistryCenter;
import com.lht.lhtrpc.core.meta.InstanceMeta;
import com.lht.lhtrpc.core.meta.ServiceMeta;
import lombok.SneakyThrows;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Leo liang [lhtshent@gmail.com]
 * 2024/3/17 19:40
 */
public class ZkRegistryCenter implements RegistryCenter {

    @Value("${lhtrpc.zkServer}")
    private String servers;

    @Value("${lhtrpc.zkRoot}")
    private String root;

    private CuratorFramework client;

    @Override
    public void start() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(servers)
                .namespace(root)
                .retryPolicy(retryPolicy)
                .build();
        System.out.println("===> zk client starting to server[" + servers + "/" + root + "].");
        client.start();
    }

    @Override
    public void stop() {
        client.close();
        System.out.println("===> zk client stopped.");
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.toPath();
        try {
            //创建服务持久化节点
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
            }
            //创建实例的临时性节点
            String instancePath = servicePath + "/" + instance.toPath();
            System.out.println("===> register to zk " + "/" + service.toPath() + " /" + instance);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "provider".getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
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
            System.out.println("===> unregister to zk " + "/" + service.toPath() + " /" + instance);
            client.delete().quietly().forPath(instancePath);//quietly如果没有节点则不抛出异常
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        String servicePath = "/" + service.toPath();
        try {
            //获取所有子节点
            List<String> nodes = client.getChildren().forPath(servicePath);
            System.out.println("===> fetch all to zk " + servicePath);
            nodes.forEach(System.out::println);
            return mapInstance(nodes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<InstanceMeta> mapInstance(List<String> nodes) {
        return nodes.stream().map(d -> {
            String[] s = d.split("_");
            return new InstanceMeta("http", s[0], Integer.valueOf(s[1]));
        }).collect(Collectors.toList());
    }

    @SneakyThrows
    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        final TreeCache cache = TreeCache.newBuilder(client, "/" + service.toPath())
                .setCacheData(true).setMaxDepth(2).build();
        cache.getListenable().addListener((curator, event) -> {
            System.out.println("zk subscribe event:" + event);
            List<InstanceMeta> nodes = fetchAll(service);
            listener.fire(new Event(nodes));
        });
        cache.start();
    }


}
