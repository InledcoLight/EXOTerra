package com.inledco.exoterra.aliot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class SubscribeParser<T> {
    private Type mType;
    private final String topicFormat;

    public SubscribeParser(String topicFormat) {
        this.topicFormat = topicFormat;
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            mType = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        }
    }

    public String getTopic(final String appkey, final String userid) {
        return String.format(topicFormat, appkey, userid);
    }

    public void parse(String payload) {
        try {
            T result = JSON.parseObject(payload, mType);
            if (result != null) {
                onParse(result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public abstract void onParse(T result);
}
