package com.lht.lhtrpc.core.registry.lht;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lht.lhtrpc.core.api.RegistryCenter;
import com.lht.lhtrpc.core.consumer.HttpInvoker;
import com.lht.lhtrpc.core.meta.InstanceMeta;
import com.lht.lhtrpc.core.meta.ServiceMeta;
import com.lht.lhtrpc.core.registry.ChangedListener;
import com.lht.lhtrpc.core.registry.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Leo
 * @date 2024/04/24
 */
@Slf4j
public class LhtRegistryCenter implements RegistryCenter {

    @Value("${lhtregistry.servers}")
    private String servers;

    Map<String, Long> VERSIONS = new HashMap<>();

    MultiValueMap<InstanceMeta, String> RENEWS = new LinkedMultiValueMap<>();

    LhtHealthChecker healthChecker = new LhtHealthChecker();
    @Override
    public void start() {
        log.info(" ====>>> [lhtRegistry]: start with server: {}", servers);
        healthChecker.start();
        healthChecker.providerCheck(()->{
            RENEWS.forEach((k, v) -> {
                String services = v.stream().collect(Collectors.joining(","));
                Long time = HttpInvoker.httpPost(JSON.toJSONString(k), servers + "/renews?services=" + services, Long.class);
                log.debug(" ====>>> [lhtRegistry]: renew instance {} server {} for renew with time {}", k, services, time);
            });
        });
    }

    @Override
    public void stop() {
        log.info(" ====>>> [lhtRegistry]: stop with server: {}", servers);
        healthChecker.stop();
    }



    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        log.info(" ====>>> [lhtRegistry]: registry instance {} for {}", instance, service);
        HttpInvoker.httpPost(JSON.toJSONString(instance), servers + "/register?service=" + service.toPath(), String.class);
        RENEWS.add(instance, service.toPath());
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        log.info(" ====>>> [lhtRegistry]: unRegistry instance {} for {}", instance, service);
        String path = service.toPath();
        HttpInvoker.httpPost(JSON.toJSONString(instance), servers + "/unregister?service=" + path, String.class);
        List<String> services = RENEWS.computeIfAbsent(instance, k -> new ArrayList<>());
        services.removeIf(serviceString -> serviceString.equals(path));
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        log.info(" ====>>> [lhtRegistry]: find all instance for {}", service);
        List<InstanceMeta> instanceList = HttpInvoker.httpGet(servers + "/findAll?service=" + service.toPath(), new TypeReference<List<InstanceMeta>>() {});
        log.info(" ====>>> [lhtRegistry]: find all instance : {}", instanceList);
        return instanceList;
    }

    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        healthChecker.consumerCheck(() -> {
            long version = VERSIONS.getOrDefault(service.toPath(), -1L);
            Long serverVersion = HttpInvoker.httpGet(servers + "/version?service=" + service.toPath(), Long.class);
            log.info(" ====>>> [lhtRegistry]: version = {}, serverVersion = {}", version, serverVersion);
            if (version < serverVersion) {
                List<InstanceMeta> instanceMetas = fetchAll(service);
                listener.fire(new Event(instanceMetas));
                VERSIONS.put(service.toPath(), serverVersion);
            }
        });
    }
}
