package com.inledco.exoterra.aliot;

import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.inledco.exoterra.base.BaseViewModel;
import com.inledco.exoterra.scan.LocalClient;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceViewModel<T extends Device> extends BaseViewModel<T> {

    protected boolean cloud;
    protected HttpCallback<UserApi.GetDevicePropertiesResponse> getAllPropertiesCallback;

    private String msgid;
    private final CountDownTimer setTimer = new CountDownTimer(1000, 100) {
        @Override
        public void onTick(long millisUntilFinished) {
            if (TextUtils.equals(msgid, getData().getRequestId())) {
                Log.e(TAG, "onTick: cancel " + millisUntilFinished);
                cancel();
            }
        }

        @Override
        public void onFinish() {
            if (TextUtils.equals(msgid, getData().getRequestId())) {
                Log.e(TAG, "onFinish: 0");
                return;
            }
            Log.e(TAG, "onFinish: 1");
            final String pkey = getData().getProductKey();
            final String dname = getData().getDeviceName();
            AliotServer.getInstance().getDeviceProperties(pkey, dname, new HttpCallback<UserApi.GetDevicePropertiesResponse>() {
                @Override
                public void onError(String error) {

                }

                @Override
                public void onSuccess(UserApi.GetDevicePropertiesResponse result) {
                    if (TextUtils.equals(getData().getRequestId(), msgid) == false) {
                        Log.e(TAG, "onSuccess: x");
                        getData().updateProperties(result.data);
                        postValue();
                        EventBus.getDefault().post(new ADevice(pkey, dname));
                    }
                }
            });
        }
    };

    private final CountDownTimer localTimer = new CountDownTimer(60000, 30000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            if (!cloud) {
                String ip = getData().getIp();
                int port = getData().getPort();
                LocalClient.getInstance().send(ip, port, "Heart beat!");
                sendHeartbeat();
            }
        }
    };

    public boolean isCloud() {
        return cloud;
    }

    public void setCloud(boolean cloud) {
        this.cloud = cloud;
    }

    public void setGetAllPropertiesCallback(HttpCallback<UserApi.GetDevicePropertiesResponse> callback) {
        getAllPropertiesCallback = callback;
    }

    public void setProperty(KeyValue... attrs) {
        if (attrs == null || attrs.length == 0) {
            return;
        }
        localTimer.cancel();
        if (cloud) {
            String pkey = getData().getProductKey();
            String dname = getData().getDeviceName();
            Map<String, Object> params = new HashMap<>();
            for (KeyValue attr : attrs) {
                if (attr == null) {
                    continue;
                }
                params.put(attr.getAttrKey(), attr.getAttrValue());
            }
            String items = JSON.toJSONString(params);
            Log.e(TAG, "setProperty: " + items);
            setTimer.cancel();
            AliotServer.getInstance().setDeviceProperties(pkey, dname, items, new HttpCallback<UserApi.SetDevicePropertiesResponse>() {
                @Override
                public void onError(String error) {
                    Log.e(TAG, "onError0: " + error);
                }

                @Override
                public void onSuccess(UserApi.SetDevicePropertiesResponse result) {
                    Log.e(TAG, "onSuccess0: " + result.data);
                    msgid = result.data;
                    setTimer.start();
                }
            });
        } else {
            String ip = getData().getIp();
            int port = getData().getPort();
            LocalClient.getInstance().setProperty(ip, port, attrs);
            localTimer.start();
        }
    }

    public void setProperty(List<KeyValue> attrs) {
        if (attrs == null || attrs.size() == 0) {
            return;
        }
        localTimer.cancel();
        if (cloud) {
            String pkey = getData().getProductKey();
            String dname = getData().getDeviceName();
            Map<String, Object> params = new HashMap<>();
            for (KeyValue attr : attrs) {
                if (attr == null) {
                    continue;
                }
                params.put(attr.getAttrKey(), attr.getAttrValue());
            }
            String items = JSON.toJSONString(params);
            setTimer.cancel();
            AliotServer.getInstance().setDeviceProperties(pkey, dname, items, new HttpCallback<UserApi.SetDevicePropertiesResponse>() {
                @Override
                public void onError(String error) {
                    Log.e(TAG, "onError1: " + error);
                }

                @Override
                public void onSuccess(UserApi.SetDevicePropertiesResponse result) {
                    Log.e(TAG, "onSuccess1: " + result.data);
                    msgid = result.data;
                    setTimer.start();
                }
            });
        } else {
            String ip = getData().getIp();
            int port = getData().getPort();
            LocalClient.getInstance().setProperty(ip, port, attrs);
            localTimer.start();
        }
    }

    public void getProperty(String... keys) {
        if (keys == null || keys.length == 0) {
            return;
        }
        localTimer.cancel();
        if (cloud) {
            String pkey = getData().getProductKey();
            String dname = getData().getDeviceName();
            AliotServer.getInstance().getDeviceProperties(pkey, dname, new HttpCallback<UserApi.PublishTopicResponse>() {
                @Override
                public void onError(String error) {
                    Log.e(TAG, "onError: getProperty - " + error);
                }

                @Override
                public void onSuccess(UserApi.PublishTopicResponse result) {
                    Log.e(TAG, "onSuccess: getProperty - " + result.data);
                }
            }, keys);
        } else {
            String ip = getData().getIp();
            int port = getData().getPort();
            LocalClient.getInstance().getProperty(ip, port, keys);
            localTimer.start();
        }
    }

    public void getAllProperties() {
        localTimer.cancel();
        if (cloud) {
            String pkey = getData().getProductKey();
            String dname = getData().getDeviceName();
            AliotServer.getInstance().getDeviceProperties(pkey, dname, new HttpCallback<UserApi.GetDevicePropertiesResponse>() {
                @Override
                public void onError(String error) {
                    if (getAllPropertiesCallback != null) {
                        getAllPropertiesCallback.onError(error);
                    }
                }

                @Override
                public void onSuccess(UserApi.GetDevicePropertiesResponse result) {
                    getData().updateProperties(result.data);
                    postValue();
                    if (getAllPropertiesCallback != null) {
                        getAllPropertiesCallback.onSuccess(result);
                    }
                }
            });
        } else {
            String ip = getData().getIp();
            int port = getData().getPort();
            LocalClient.getInstance().getProperty(ip, port);
            localTimer.start();
        }
    }

    private void sendHeartbeat() {
        localTimer.start();
    }

    public void disconnectLocal() {
        localTimer.cancel();
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
