package com.lht.lhtrpc.core.provider;

import com.lht.lhtrpc.core.api.RpcContext;
import com.lht.lhtrpc.core.api.RpcException;
import com.lht.lhtrpc.core.api.RpcRequest;
import com.lht.lhtrpc.core.api.RpcResponse;
import com.lht.lhtrpc.core.config.ConsumerProperties;
import com.lht.lhtrpc.core.config.ProviderProperties;
import com.lht.lhtrpc.core.governance.SlidingTimeWindow;
import com.lht.lhtrpc.core.meta.ProviderMeta;
import com.lht.lhtrpc.core.utils.TypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Leo
 * @date 2024/03/26
 */
@Slf4j
public class ProviderInvoker {

    private MultiValueMap<String, ProviderMeta> skeleton;
    private final ConcurrentHashMap<String, SlidingTimeWindow> windows = new ConcurrentHashMap<>();
    private final ProviderProperties providerProperties;

    public ProviderInvoker(ProviderBootStrap providerBootStrap) {
        this.skeleton = providerBootStrap.getSkeleton();
        this.providerProperties = providerBootStrap.getProviderProperties();
    }

    public RpcResponse invokeRequest(RpcRequest request) {
        log.debug("service值为：" + request.getService());
        List<ProviderMeta> providerMetas = skeleton.get(request.getService());
        Map<String, String> parameters = request.getParameters();
        if (!parameters.isEmpty()) {
            RpcContext.ContextParameters.get().putAll(parameters);
        }
        RpcResponse rpcResponse = new RpcResponse();
        String service = request.getService();

        try {
            tryTc(service);
            ProviderMeta providerMeta = findProviderMeta(request, providerMetas);
            if (providerMeta == null) {
                rpcResponse.setEx(new RpcException("没有找到对应的服务"));
                return rpcResponse;
            }
            Method method = providerMeta.getMethod();
            Object bean = providerMeta.getServiceImpl();
            Object[] newArgs = processArgs(request.getArgs(), method);
            Object result = method.invoke(bean, newArgs);
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            rpcResponse.setEx(new RpcException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            rpcResponse.setEx(new RpcException(e.getMessage()));
        }
        return rpcResponse;
    }

    private void tryTc(String service) throws RpcException{
        SlidingTimeWindow window = windows.computeIfAbsent(service, k -> new SlidingTimeWindow());
        int trafficControl = Integer.parseInt(providerProperties.getMetas().getOrDefault("tc", "20"));;
        if (window.calcSum() > trafficControl) {
            System.out.println(window);
            throw new RpcException("service " + service + " invoked in 30s/[" +
                    window.getSum() + "] larger than tpsLimit = " + trafficControl, RpcException.ExceedLimitEx);
        }
        window.record(System.currentTimeMillis());
        log.debug("service {} in window with {}", service, window.getSum());
    }

    private Object[] processArgs(Object[] args, Method method) {
        if (args == null || args.length == 0) {
            return args;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] newArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            TypeUtils.buildNewArgs(args, method, parameterTypes, i, newArgs);
        }
        return newArgs;
    }



    private ProviderMeta findProviderMeta(RpcRequest request, List<ProviderMeta> providerMetas) {
        Optional<ProviderMeta> meta = providerMetas.stream().filter(d -> d.getMethodSign().equals(request.getMethodSign())).findFirst();
        return meta.orElse(null);
    }


}
