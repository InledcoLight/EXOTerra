package com.inledco.exoterra.scan;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.inledco.exoterra.aliot.KeyValue;
import com.inledco.exoterra.udptcp.UdpClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalClient {
    private final String TAG = "LocalClient";

    private final UdpClient mClient;

    private LocalClient() {
        mClient = new UdpClient();
    }

    public static LocalClient getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void init(final UdpClient.Listener listener) {
        mClient.setListener(new UdpClient.Listener() {
            @Override
            public void onError(String error) {
                if (listener != null) {
                    listener.onError(error);
                }
            }

            @Override
            public void onReceive(String ip, int port, byte[] bytes) {
                if (listener != null) {
                    listener.onReceive(ip, port, bytes);
                }
            }
        });
        mClient.start();
    }

    public void deinit() {
        mClient.stop();
    }

    public boolean isInited() {
        return mClient.isListening();
    }

    public void send(String ip, int port, String payload) {
        if (!isInited()) {
            return;
        }
        if (TextUtils.isEmpty(payload)) {
            return;
        }
        mClient.send(ip, port, payload);
    }

    public void setProperty(String ip, int port, KeyValue... keyValues) {
        if (!isInited()) {
            return;
        }
        if (keyValues == null || keyValues.length == 0) {
            return;
        }
        Map<String, Object> params = new HashMap<>();
        for (KeyValue attr : keyValues) {
            if (attr == null) {
                return;
            }
            params.put(attr.getAttrKey(), attr.getAttrValue());
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("params", params);
        mClient.send(ip, port, JSON.toJSONString(payload));
    }

    public void setProperty(String ip, int port, List<KeyValue> keyValues) {
        if (!isInited()) {
            return;
        }
        if (keyValues == null || keyValues.size() == 0) {
            return;
        }
        Map<String, Object> params = new HashMap<>();
        for (KeyValue attr : keyValues) {
            if (attr == null) {
                return;
            }
            params.put(attr.getAttrKey(), attr.getAttrValue());
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("params", params);
        mClient.send(ip, port, JSON.toJSONString(payload));
    }

    public void getProperty(String ip, int port, String... attrKeys) {
        if (!isInited()) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("params", attrKeys);
        Log.e(TAG, "getProperty: " + JSON.toJSONString(payload));
        mClient.send(ip, port, JSON.toJSONString(payload));
    }

    public void getAllProperties(String ip, int port) {
        getProperty(ip, port);
    }

    private static class LazyHolder {
        private static final LocalClient INSTANCE = new LocalClient();
    }
}
