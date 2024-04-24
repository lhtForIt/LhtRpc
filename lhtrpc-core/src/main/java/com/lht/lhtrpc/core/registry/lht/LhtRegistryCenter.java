package com.lht.lhtrpc.core.registry.lht;

import ch.qos.logback.classic.Logger;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lht.lhtrpc.core.api.RegistryCenter;
import com.lht.lhtrpc.core.consumer.HttpInvoker;
import com.lht.lhtrpc.core.consumer.http.OkHttpInvoker;
import com.lht.lhtrpc.core.meta.InstanceMeta;
import com.lht.lhtrpc.core.meta.ServiceMeta;
import com.lht.lhtrpc.core.registry.ChangedListener;
import com.lht.lhtrpc.core.registry.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Leo
 * @date 2024/04/24
 */
@Slf4j
public class LhtRegistryCenter implements RegistryCenter {

    @Value("${lht.registry.servers}")
    private String servers;

    @Override
    public void start() {
        log.info(" ====>>> [lhtRegistry]: start with server: {}", servers);
        executor = Executors.newScheduledThreadPool(1);
    }

    @Override
    public void stop() {
        log.info(" ====>>> [lhtRegistry]: stop with server: {}", servers);
        executor.shutdown();
        //优雅停止
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {

        }
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        log.info(" ====>>> [lhtRegistry]: registry instance {} for {}", instance, service);
        HttpInvoker.httpPost(JSON.toJSONString(instance), servers + "/registry?server=" + service.toPath(), String.class);
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        log.info(" ====>>> [lhtRegistry]: unRegistry instance {} for {}", instance, service);
        HttpInvoker.httpPost(JSON.toJSONString(instance), servers + "/unRegistry?server=" + service.toPath(), String.class);
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        log.info(" ====>>> [lhtRegistry]: find all instance for {}", service);
        List<InstanceMeta> instanceList = HttpInvoker.httpGet(servers + "/findAll?server=" + service.toPath(), new TypeReference<List<InstanceMeta>>() {});
        log.info(" ====>>> [lhtRegistry]: find all instance : {}", instanceList);
        return instanceList;
    }

    Map<String, Long> VERSIONS = new HashMap<>();
    ScheduledExecutorService executor;

    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        executor.scheduleWithFixedDelay(() -> {
            long version = VERSIONS.getOrDefault(service.toPath(), -1L);
            Long serverVersion = HttpInvoker.httpGet(servers + "/version?server=" + service.toPath(), Long.class);
            log.info(" ====>>> [lhtRegistry]: version = {}, serverVersion = {}", version, serverVersion);
            if (version < serverVersion) {
                List<InstanceMeta> instanceMetas = fetchAll(service);
                listener.fire(new Event(instanceMetas));
                VERSIONS.put(service.toPath(), serverVersion);
            }
        }, 1, 5, TimeUnit.SECONDS);
    }
}
