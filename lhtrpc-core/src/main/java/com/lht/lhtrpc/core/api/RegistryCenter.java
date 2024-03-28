package com.lht.lhtrpc.core.api;

import com.lht.lhtrpc.core.meta.InstanceMeta;
import com.lht.lhtrpc.core.meta.ServiceMeta;
import com.lht.lhtrpc.core.registry.ChangedListener;

import java.util.List;

/**
 * Leo liang [lhtshent@gmail.com]
 * 2024/3/17 19:17
 */
public interface RegistryCenter {

    void start(); // p/c
    void stop(); // p/c

    //provider侧
    void register(ServiceMeta service, InstanceMeta instance); // p
    void unregister(ServiceMeta service, InstanceMeta instance); // p

    //consumer侧
    List<InstanceMeta> fetchAll(ServiceMeta service); // c
    void subscribe(ServiceMeta service, ChangedListener listener); // c

    class StaticRegistryCenter implements RegistryCenter {

        private List<InstanceMeta> providers;

        public StaticRegistryCenter(List<InstanceMeta> providers) {
            this.providers = providers;
        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

        @Override
        public void register(ServiceMeta service, InstanceMeta instance) {

        }

        @Override
        public void unregister(ServiceMeta service, InstanceMeta instance) {

        }

        @Override
        public List<InstanceMeta> fetchAll(ServiceMeta service) {
            return providers;
        }

        @Override
        public void subscribe(ServiceMeta service, ChangedListener listener) {

        }
    }


}
