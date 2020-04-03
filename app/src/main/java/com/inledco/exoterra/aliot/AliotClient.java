package com.inledco.exoterra.aliot;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.alink.dm.api.DeviceInfo;
import com.aliyun.alink.dm.api.IoTApiClientConfig;
import com.aliyun.alink.linkkit.api.ILinkKitConnectListener;
import com.aliyun.alink.linkkit.api.IoTMqttClientConfig;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linkkit.api.LinkKitInitParams;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttPublishRequest;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttSubscribeRequest;
import com.aliyun.alink.linksdk.cmp.core.base.AMessage;
import com.aliyun.alink.linksdk.cmp.core.base.ARequest;
import com.aliyun.alink.linksdk.cmp.core.base.AResponse;
import com.aliyun.alink.linksdk.cmp.core.base.ConnectState;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectNotifyListener;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSendListener;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSubscribeListener;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectUnscribeListener;
import com.aliyun.alink.linksdk.tmp.device.payload.ValueWrapper;
import com.aliyun.alink.linksdk.tools.AError;
import com.inledco.exoterra.manager.DeviceManager;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AliotClient {

    private final String TAG = "AliotClient";

    private final String APP_PRODUCT_KEY = "a1yk0nvw5UI";

    /**
     * publish {userid(deviceName), product}
     */
    private final String topicPropertySet = "/a1yk0nvw5UI/%1$s/user/%2$s/property/set";

    /**
     * publish {userid(deviceName), product}
     */
    private final String topicPropertyGet = "/a1yk0nvw5UI/%1$s/user/%2$s/property/get";

    /**
     * subscribe {userid(deviceName)}
     */
    private final String topicPropertyResponse = "/a1yk0nvw5UI/%1$s/user/property/response";

    /**
     * subscribe {userid(deviceName)}
     */
    private final String topicPropertyStatus = "/a1yk0nvw5UI/%1$s/user/status";

    private String mUserid;

    private String mToken;

    private boolean initialized;

    private final Map<String, String> productMap = new HashMap<>();

    private Set<String> subTopics = new HashSet<>();

    private final IConnectNotifyListener mNotifyListener = new IConnectNotifyListener() {
        @Override
        public void onNotify(String s, String s1, AMessage aMessage) {
            String payload = new String((byte[]) aMessage.getData());
            Log.e(TAG, "onNotify: " + s + " " + s1 + " " + payload);
            if (TextUtils.equals(s1, String.format(topicPropertyResponse, mUserid))) {          // 设备属性上报
                try {
                    ADevice adev = JSON.parseObject(payload, ADevice.class);
                    if (adev != null) {
                        if (DeviceManager.getInstance().contains(adev.getTag())) {
                            Device device = DeviceManager.getInstance().getDevice(adev.getTag());
                            device.updateProperties(adev.getItems());
                            EventBus.getDefault().post(adev);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (TextUtils.equals(s1, String.format(topicPropertyStatus, mUserid))) {     // 设备状态变化
                try {
                    StatusReponse status = JSON.parseObject(payload, StatusReponse.class);
                    if (status != null) {
                        EventBus.getDefault().post(status);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public boolean shouldHandle(String s, String s1) {
            Log.e(TAG, "shouldHandle: " + s + " " + s1);
            return subTopics.contains(s1);
        }

        @Override
        public void onConnectStateChange(String s, ConnectState connectState) {
            Log.e(TAG, "onConnectStateChange: " + s + " " + connectState.name());
        }
    };

    private AliotClient() {
        productMap.put("Test", "a1layga4ANI");
        productMap.put("ExoTerraSocket", "a1MUlQSvB8a");
        productMap.put("ExoTerraMonsoon", "a1iLPCJMw7s");
    }

    public static AliotClient getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void init(Context context, final String userid, final String token) {
        DeviceInfo devInfo = new DeviceInfo();
        devInfo.productKey = APP_PRODUCT_KEY;
        devInfo.deviceName = userid;
        devInfo.deviceSecret = token;

        IoTMqttClientConfig clientConfig = new IoTMqttClientConfig(APP_PRODUCT_KEY, userid, token);
        IoTApiClientConfig connectConfig = new IoTApiClientConfig();

        Map<String, ValueWrapper> propertyValues = new HashMap<>();

        LinkKitInitParams params = new LinkKitInitParams();
        params.deviceInfo = devInfo;
        params.propertyValues = propertyValues;
        params.mqttClientConfig = clientConfig;

        LinkKit.getInstance().init(context, params, new ILinkKitConnectListener() {
            @Override
            public void onError(AError error) {
                Log.e(TAG, "onError: " + JSONObject.toJSONString(error));
            }

            @Override
            public void onInitDone(Object o) {
                Log.e(TAG, "onInitDone: ");

                LinkKit.getInstance().registerOnPushListener(mNotifyListener);

                subscribeTopic(String.format(topicPropertyResponse, userid));
                subscribeTopic(String.format(topicPropertyStatus, userid));

                mUserid = userid;
                mToken = token;
                initialized = true;
            }
        });
    }

    public void deinit() {
        unsubscribeAllTopics();
        LinkKit.getInstance().unRegisterOnPushListener(mNotifyListener);
        LinkKit.getInstance().deinit();
        initialized = false;
        mUserid = null;
        mToken = null;
    }

    private void subscribeTopic(String topic) {
        final MqttSubscribeRequest request = new MqttSubscribeRequest();
        request.topic = topic;
        request.isSubscribe = true;
        LinkKit.getInstance().subscribe(request, new IConnectSubscribeListener() {
            @Override
            public void onSuccess() {
                subTopics.add(request.topic);
            }

            @Override
            public void onFailure(AError aError) {

            }
        });
    }

    private void unsubscribeTopic(String topic) {
        MqttSubscribeRequest request = new MqttSubscribeRequest();
        request.topic = topic;
        request.isSubscribe = false;
        LinkKit.getInstance().unsubscribe(request, new IConnectUnscribeListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(AError aError) {

            }
        });
    }

    private void unsubscribeAllTopics() {
        for (String topic : subTopics) {
            MqttSubscribeRequest request = new MqttSubscribeRequest();
            request.topic = topic;
            request.isSubscribe = false;
            LinkKit.getInstance().unsubscribe(request, new IConnectUnscribeListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(AError aError) {

                }
            });
        }
        subTopics.clear();
    }

    /**
     * 设置设备属性
     * @param product       产品名称
     * @param dname         设备ID
     * @param keyValues     属性键值对
     */
    public void setProperty(String product, String dname, KeyValue... keyValues) {
        if (!initialized || TextUtils.isEmpty(mUserid)) {
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
        payload.put("deviceName", dname);
        payload.put("params", params);
        MqttPublishRequest request = new MqttPublishRequest();
        request.topic = String.format(topicPropertySet, mUserid, product);
        request.payloadObj = JSON.toJSONString(payload);
        request.qos = 0;
        request.isRPC = false;
        Log.e(TAG, "setProperty: " + request.topic);
        Log.e(TAG, "setProperty: " + JSON.toJSONString(payload));
        LinkKit.getInstance().publish(request, new IConnectSendListener() {
            @Override
            public void onResponse(ARequest aRequest, AResponse aResponse) {
                Log.e(TAG, "onResponse: " + JSON.toJSONString(aResponse));
            }

            @Override
            public void onFailure(ARequest aRequest, AError aError) {
                Log.e(TAG, "onFailure: " + JSON.toJSONString(aError));
            }
        });
    }

    public void setProperty(String product, String dname, List<KeyValue> keyValues) {
        if (!initialized || TextUtils.isEmpty(mUserid)) {
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
        payload.put("deviceName", dname);
        payload.put("params", params);
        MqttPublishRequest request = new MqttPublishRequest();
        request.topic = String.format(topicPropertySet, mUserid, product);
        request.payloadObj = JSON.toJSONString(payload);
        request.qos = 0;
        request.isRPC = false;
        Log.e(TAG, "setProperty: " + request.topic);
        Log.e(TAG, "setProperty: " + JSON.toJSONString(payload));
        LinkKit.getInstance().publish(request, new IConnectSendListener() {
            @Override
            public void onResponse(ARequest aRequest, AResponse aResponse) {
                Log.e(TAG, "onResponse: " + JSON.toJSONString(aResponse));
            }

            @Override
            public void onFailure(ARequest aRequest, AError aError) {
                Log.e(TAG, "onFailure: " + JSON.toJSONString(aError));
            }
        });
    }

    /**
     * 获取设备属性
     * @param product       产品名称
     * @param dname         设备ID
     * @param attrKeys      属性名称
     */
    public void getProperty(String product, String dname, String... attrKeys) {
        if (!initialized || TextUtils.isEmpty(mUserid)) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("deviceName", dname);
        payload.put("params", attrKeys);
        MqttPublishRequest request = new MqttPublishRequest();
        request.topic = String.format(topicPropertyGet, mUserid, product);
        request.payloadObj = JSON.toJSONString(payload);
        request.qos = 0;
        request.isRPC = false;
        Log.e(TAG, "getProperty: " + request.topic);
        Log.e(TAG, "getProperty: " + JSON.toJSONString(payload));
        LinkKit.getInstance().publish(request, new IConnectSendListener() {
            @Override
            public void onResponse(ARequest aRequest, AResponse aResponse) {
                Log.e(TAG, "onResponse: " + JSON.toJSONString(aResponse));
            }

            @Override
            public void onFailure(ARequest aRequest, AError aError) {
                Log.e(TAG, "onFailure: " + JSON.toJSONString(aError));
            }
        });
    }

    public void getAllProperties(String product, String dname) {
        getProperty(product, dname);
    }

    private static class LazyHolder {
        private static final AliotClient INSTANCE = new AliotClient();
    }
}
