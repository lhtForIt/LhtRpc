package com.lht.lhtrpc.core.consumer.http;

import com.alibaba.fastjson.JSON;
import com.lht.lhtrpc.core.api.RpcRequest;
import com.lht.lhtrpc.core.api.RpcResponse;
import com.lht.lhtrpc.core.consumer.HttpInvoker;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Leo
 * @date 2024/03/26
 */
@Slf4j
public class OkHttpInvoker implements HttpInvoker {

    private final static MediaType MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private OkHttpClient client;

    public OkHttpInvoker(int readTimeout,int writeTimeout,int connectTimeout) {
        this.client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(16,60, TimeUnit.SECONDS))
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(writeTimeout,TimeUnit.MILLISECONDS)
                .connectTimeout(connectTimeout,TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public RpcResponse post(RpcRequest rpcRequest,String url) {

        String requestJson = JSON.toJSONString(rpcRequest);
        log.debug("requestJson = " + requestJson);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestJson, MEDIA_TYPE))
                .build();
        try {
            String responseJson = client.newCall(request).execute().body().string();
            log.debug("responseJson = " + responseJson);
            RpcResponse rpcResponse = JSON.parseObject(responseJson, RpcResponse.class);
            return rpcResponse;
        } catch (IOException e) {
//            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
