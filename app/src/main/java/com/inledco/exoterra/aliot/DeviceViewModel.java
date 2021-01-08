package com.inledco.exoterra.aliot;

import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.inledco.exoterra.aliot.bean.DeviceParams;
import com.inledco.exoterra.base.BaseViewModel;
import com.inledco.exoterra.manager.UserManager;
import com.inledco.exoterra.udptcp.UdpClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeviceViewModel<T extends Device> extends BaseViewModel<T> {

    private final boolean cloud = UserManager.getInstance().isAuthorized();

    private final int LOCAL_TIMEOUT = 500;

    private final int CLOUD_TIMEOUT = 1500;

    private final int DEVICE_PORT   = 8899;

    private final ExecutorService mExecutorService = Executors.newCachedThreadPool();
    private byte[] mLock = new byte[0];

    private volatile boolean localSuccess;

    private UdpClient mClient;
    private String mReceive;
    private volatile boolean wait_for_response;

    private volatile String msgid;

    private final CountDownTimer localTimer = new CountDownTimer(LOCAL_TIMEOUT, 50) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            wait_for_response = false;
        }
    };

    private final CountDownTimer heartbeatTimer = new CountDownTimer(60000, 30000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            if (mClient != null) {
                mClient.send("Heart beat!");
            }
            start();
        }
    };

    private final HttpCallback<UserApi.SetDevicePropertiesResponse> setPropCallback = new HttpCallback<UserApi.SetDevicePropertiesResponse>() {
        @Override
        public void onError(String error) {
            Log.e(TAG, "onError: " + error);
            msgid = "";
        }

        @Override
        public void onSuccess(UserApi.SetDevicePropertiesResponse result) {
            Log.e(TAG, "onSuccess0: " + result.data);
            msgid = result.data;
        }
    };

    private final HttpCallback<UserApi.PublishTopicResponse> getPropCallback = new HttpCallback<UserApi.PublishTopicResponse>() {
        @Override
        public void onError(String error) {
            Log.e(TAG, "onError: getProperty - " + error);
        }

        @Override
        public void onSuccess(UserApi.PublishTopicResponse result) {
            Log.e(TAG, "onSuccess: getProperty - " + result.data);
        }
    };

    private void init(final String remoteIp, final int remotePort) {
        mClient = new UdpClient(remoteIp, remotePort);
        mClient.setListener(new UdpClient.Listener() {
            @Override
            public void onError(String error) {

            }

            @Override
            public void onReceive(String ip, int port, byte[] bytes) {
                if (TextUtils.equals(remoteIp, ip) && remotePort == port) {
                    localTimer.cancel();
                    mReceive = new String(bytes);
                    Log.e(TAG, "onReceive: " + mReceive);
                    try {
                        DeviceParams params = JSON.parseObject(mReceive, DeviceParams.class);
                        if (params != null && TextUtils.equals(getData().getProductKey(), params.getProductKey()) && TextUtils.equals(getData().getDeviceName(), params.getDeviceName())) {
                            AliotClient.getInstance().ignoreMessage(getData().getProductKey(), getData().getDeviceName(), params.getId());
                            getData().updateValues(params.getParams());
                            postValue();
                            localSuccess = true;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    wait_for_response = false;
                }
            }
        });
        mClient.start();
        while (!mClient.isListening());
    }

    public void deinit() {
        synchronized (mLock) {
            heartbeatTimer.cancel();
            if (mClient != null) {
                mClient.stop();
                mClient = null;
            }
        }
    }

    private void sendLocal(String payload) {
        if (mClient == null) {
            getAllProperties();
            return;
        }
        heartbeatTimer.cancel();
        localSuccess = false;
        wait_for_response = true;
        mClient.send(payload);
        Log.e(TAG, "sendLocal: " + payload);
        localTimer.start();
        heartbeatTimer.start();
    }

    private String getPropertyPayload(Object params) {
        String pkey = getData().getProductKey();
        String dname = getData().getDeviceName();

        Map<String, Object> map = new HashMap<>();
        map.put("productKey", pkey);
        map.put("deviceName", dname);
        map.put("params", params);

        String payload = "{\"property\":" + JSON.toJSONString(map) + "}";
        return payload;
    }

    public void setProperty(KeyValue... attrs) {
        if (attrs == null || attrs.length == 0) {
            return;
        }
        mExecutorService.execute(() -> {
            synchronized (mLock) {
                boolean lastLocalResult = localSuccess;

                String pkey = getData().getProductKey();
                String dname = getData().getDeviceName();

                Map<String, Object> params = new HashMap<>();
                for (KeyValue attr : attrs) {
                    if (attr == null) {
                        return;
                    }
                    params.put(attr.getAttrKey(), attr.getAttrValue());
                }

                String json = getPropertyPayload(params);
                sendLocal(json);

                msgid = null;
                String items = JSON.toJSONString(params);
                if (cloud && !lastLocalResult) {
                    AliotServer.getInstance().setDeviceProperties(pkey, dname, items, setPropCallback);
                }

                while (wait_for_response);
                if (localSuccess) {
                    if (lastLocalResult || !cloud) {
                        return;
                    }
                } else {
                    if (!cloud) {
                        sendLocal(json);
                        while (wait_for_response);
                        return;
                    } else if (lastLocalResult) {
                        AliotServer.getInstance().setDeviceProperties(pkey, dname, items, setPropCallback);
                    }
                }

                while (msgid == null);
                if (!TextUtils.isEmpty(msgid)) {
                    if (localSuccess) {
                        AliotClient.getInstance().ignoreMessage(pkey, dname, msgid);
                        return;
                    }
                    long time = System.currentTimeMillis() + CLOUD_TIMEOUT;
                    while (System.currentTimeMillis() < time) {
                        if (TextUtils.equals(getData().getRequestId(), msgid)) {
                            return;
                        }
                    }
                    AliotServer.getInstance().getDeviceProperties(pkey, dname, new HttpCallback<UserApi.GetDevicePropertiesResponse>() {
                        @Override
                        public void onError(String error) {

                        }

                        @Override
                        public void onSuccess(UserApi.GetDevicePropertiesResponse result) {
                            if (TextUtils.equals(getData().getRequestId(), msgid)) {
                                return;
                            }
                            getData().updateProperties(result.data);
                            postValue();
                        }
                    });
                }
            }
        });
    }

    public void setProperty(List<KeyValue> attrs) {
        if (attrs == null || attrs.size() == 0) {
            return;
        }
        KeyValue[] array = new KeyValue[attrs.size()];
        attrs.toArray(array);
        setProperty(array);

//        mExecutorService.execute(() -> {
//            synchronized (mLock) {
//                boolean lastLocalResult = localSuccess;
//
//                Map<String, Object> params = new HashMap<>();
//                for (KeyValue attr : attrs) {
//                    if (attr == null) {
//                        return;
//                    }
//                    params.put(attr.getAttrKey(), attr.getAttrValue());
//                }
//
//                Map<String, Object> payload = new HashMap<>();
//                payload.put("params", params);
//                String json = JSON.toJSONString(payload);
//                sendLocal(json);
//
//                String pkey = getData().getProductKey();
//                String dname = getData().getDeviceName();
//                msgid = null;
//                String items = JSON.toJSONString(params);
//                if (cloud && !lastLocalResult) {
//                    AliotServer.getInstance().setDeviceProperties(pkey, dname, items, setPropCallback);
//                }
//
//                while (wait_for_response);
//                if (localSuccess) {
//                    if (lastLocalResult || !cloud) {
//                        return;
//                    }
//                } else {
//                    if (!cloud) {
//                        sendLocal(json);
//                        while (wait_for_response);
//                        return;
//                    } else if (lastLocalResult) {
//                        AliotServer.getInstance().setDeviceProperties(pkey, dname, items, setPropCallback);
//                    }
//                }
//
//                while (msgid == null);
//                if (!TextUtils.isEmpty(msgid)) {
//                    if (localSuccess) {
//                        AliotClient.getInstance().ignoreMessage(pkey, dname, msgid);
//                        return;
//                    }
//                    long time = System.currentTimeMillis() + CLOUD_TIMEOUT;
//                    while (System.currentTimeMillis() < time) {
//                        if (TextUtils.equals(getData().getRequestId(), msgid)) {
//                            return;
//                        }
//                    }
//                    AliotServer.getInstance().getDeviceProperties(pkey, dname, new HttpCallback<UserApi.GetDevicePropertiesResponse>() {
//                        @Override
//                        public void onError(String error) {
//
//                        }
//
//                        @Override
//                        public void onSuccess(UserApi.GetDevicePropertiesResponse result) {
//                            if (TextUtils.equals(getData().getRequestId(), msgid)) {
//                                return;
//                            }
//                            getData().updateProperties(result.data);
//                            postValue();
//                        }
//                    });
//                }
//            }
//        });
    }

    public void getProperty(String... keys) {
        if (keys == null || keys.length == 0) {
            return;
        }
        mExecutorService.execute(() -> {
            synchronized (mLock) {
                boolean lastLocalResult = localSuccess;
                String pkey = getData().getProductKey();
                String dname = getData().getDeviceName();

                String json = getPropertyPayload(keys);
                sendLocal(json);

                if (cloud && !lastLocalResult) {
                    AliotServer.getInstance().getDeviceProperties(pkey, dname, getPropCallback, keys);
                }

                while (wait_for_response);
                if (localSuccess) {
                    if (lastLocalResult || !cloud) {
                        return;
                    }
                } else {
                    if (!cloud) {
                        sendLocal(json);
                        while (wait_for_response);
                        return;
                    } else if (lastLocalResult) {
                        AliotServer.getInstance().getDeviceProperties(pkey, dname, getPropCallback, keys);
                    }
                }
            }
        });
    }

    public void getAllProperties() {
        mExecutorService.execute(() -> {
            synchronized (mLock) {
                String pkey = getData().getProductKey();
                String dname = getData().getDeviceName();
                String[] attrkeys = new String[0];
                String payload = getPropertyPayload(attrkeys);

                if (cloud) {
                    wait_for_response = true;
                    AliotServer.getInstance()
                               .getDeviceProperties(pkey, dname, new HttpCallback<UserApi.GetDevicePropertiesResponse>() {
                                   @Override
                                   public void onError(String error) {
                                       wait_for_response = false;
                                   }

                                   @Override
                                   public void onSuccess(UserApi.GetDevicePropertiesResponse result) {
                                       getData().updateProperties(result.data);
                                       postValue();
                                       wait_for_response = false;
                                   }
                               });
                    while (wait_for_response);
                    DeviceInfo devinfo = getData().getDeviceInfo();
                    if (devinfo != null) {
                        init(devinfo.getIp(), DEVICE_PORT);
                        sendLocal(payload);
                    }
                } else {
                    init(getData().getIp(), getData().getPort());
                    sendLocal(payload);
                    while (wait_for_response);
                    if (!localSuccess) {
                        sendLocal(payload);
                    }
                }
                while (wait_for_response);
                postValue();
            }
        });
    }

    public void upgradeFirmware(int version, String url, HttpCallback<UserApi.PublishTopicResponse> callback) {
        String pkey = getData().getProductKey();
        String dname = getData().getDeviceName();
        AliotServer.getInstance().upgradeFirmware(pkey, dname, version, url, callback);
    }

    public void setZone(int zone) {
        KeyValue attrZone = getData().setZone(zone);
        setProperty(attrZone);
    }

    public void setSunrise(int sunrise) {
        KeyValue attrSunrise = getData().setSunrise(sunrise);
        setProperty(attrSunrise);
    }

    public void setSunset(int sunset) {
        KeyValue attrSunset = getData().setSunset(sunset);
        setProperty(attrSunset);
    }
    public void setDaytime(int sunrise, int sunset) {
        KeyValue attrSunrise = getData().setSunrise(sunrise);
        KeyValue attrSunset = getData().setSunset(sunset);
        setProperty(attrSunrise, attrSunset);
    }

}
