package com.lht.lhtrpc.core.registry.lht;

import com.alibaba.fastjson.JSON;
import com.lht.lhtrpc.core.consumer.HttpInvoker;
import com.lht.lhtrpc.core.meta.InstanceMeta;
import com.lht.lhtrpc.core.registry.Event;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Leo
 * @date 2024/04/28
 */
@Slf4j
public class LhtHealthChecker {

    ScheduledExecutorService consumerExecutor;
    ScheduledExecutorService providerExecutor;

    public void start() {
        log.info(" ====>>> [lhtRegistry]: health check start ....");
        consumerExecutor = Executors.newScheduledThreadPool(1);
        providerExecutor = Executors.newScheduledThreadPool(1);
    }

    public void providerCheck(CallBack callBack) {
        providerExecutor.scheduleWithFixedDelay(() -> {
            try {
                callBack.call();
            }catch (Exception e){
                log.error("[lhtRegistry]: provider health check error", e);
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public void consumerCheck(CallBack callBack) {
        consumerExecutor.scheduleWithFixedDelay(() -> {
            try {
                callBack.call();
            }catch (Exception e){
                log.error("[lhtRegistry]: consumer health check error", e);
            }
        }, 1, 5, TimeUnit.SECONDS);
    }

    public void stop() {
        log.info(" ====>>> [lhtRegistry]: stop health checker");
        gracefulShutdown(consumerExecutor);
        gracefulShutdown(providerExecutor);
    }

    private void gracefulShutdown(ScheduledExecutorService executorService) {
        executorService.shutdown();
        //优雅停止
        try {
            executorService.awaitTermination(1, TimeUnit.SECONDS);
            if (!executorService.isTerminated()) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            //do nothing
        }
    }

    public interface CallBack {
        void call() throws Exception;
    }



}
