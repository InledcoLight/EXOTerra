package com.inledco.exoterra.aliot;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public abstract class HttpCallback<T extends ApiResponse> implements Callback {
    private Type mType;

    public HttpCallback() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            mType = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        }
    }

    @Override
    public void onFailure(Call call, IOException e) {
        onError(e.getMessage());
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        String error = response.message();
        if (response.isSuccessful()) {
            String json = response.body().string();
            if (json != null ) {
                try {
                    T result = JSON.parseObject(json, mType);
                    if (result != null) {
//                        if (result.code == 0 && TextUtils.equals(result.msg, "success")) {
                        if (result.code == 0) {
                            onSuccess(result);
                            return;
                        } else {
                            error = result.msg;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("TAG", "onResponse: " + e.getMessage());
                }
            }
        }
        onError(error);
    }

    public abstract void onError(String error);

    public abstract void onSuccess(T result);
}
