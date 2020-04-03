package com.inledco.exoterra.aliot;

import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public abstract class HttpCallback<T> implements Callback {
    private Type mType;

    public HttpCallback() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            mType = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        }
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
            String json = response.body().string();
            if (json == null ) {
                onError(response.code(), null);
                return;
            }
            try {
                T result = JSON.parseObject(json, mType);
                if (result != null) {
                    onSuccess(result);
                } else {
                    onError(response.code(), null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                onError(response.code(), response.message());
            }
        } else {
            onError(response.code(), response.message());
        }
    }

    public abstract void onError(int code, String msg);

    public abstract void onSuccess(T result);
}
