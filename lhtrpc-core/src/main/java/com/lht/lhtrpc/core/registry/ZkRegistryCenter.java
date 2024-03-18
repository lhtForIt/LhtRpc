package com.lht.lhtrpc.core.registry;

import com.lht.lhtrpc.core.api.RegistryCenter;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;

/**
 * Leo liang [lhtshent@gmail.com]
 * 2024/3/17 19:40
 */
public class ZkRegistryCenter implements RegistryCenter {

    private CuratorFramework client;

    @Override
    public void start() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString("192.168.3.100:2181")
                .namespace("lhtrpc")
                .retryPolicy(retryPolicy)
                .build();
        client.start();
        System.out.println("===> zk client starting.");
    }

    @Override
    public void stop() {
        client.close();
        System.out.println("===> zk client stopped.");
    }

    @Override
    public void register(String service, String instance) {
        String servicePath = "/" + service;
        try{
            //创建服务持久化节点
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath,"service".getBytes());
            }
            //创建实例的临时性节点
            String instancePath = servicePath + "/" + instance;
            System.out.println("===> register to zk " + "service-" + service + " instance-" + instance);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath,"provider".getBytes());
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unregister(String service, String instance) {
        String servicePath = "/" + service;
        try{
            //判断服务是否存在
            if (client.checkExists().forPath(servicePath) == null) {
                return;
            }
            //删除实例节点
            String instancePath = servicePath + "/" + instance;
            System.out.println("===> unregister to zk " + "service-" + service + " instance-" + instance);
            client.delete().quietly().forPath(instancePath);//quietly如果没有节点则不抛出异常
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> fetchAll(String service) {
        return null;
    }
}
