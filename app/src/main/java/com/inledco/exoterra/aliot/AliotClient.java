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

    /**
     * {region}
     */
    private final String IOTAUTH_DOMAIN_FMT = "iot-auth.%1$s.aliyuncs.com";
    private final String IOTAUTH_DOMAIN;

    /**
     * {region}
     */
    private final String IOT_DOMAIN_FMT = "iot.%1$s.aliyuncs.com";
    private final String IOT_DOMAIN;

    /**
     * {productKey}  {region}
     */
    private final String MQTT_DOMAIN_FMT = "%1$s.iot-as-mqtt.%2$s.aliyuncs.com:1883";
    private final String MQTT_DOMAIN;

    private final String REGION = "us-west-1";

    private final String APP_PRODUCT_KEY = "a3jdKlLMaEn";
    private final String APP_PRODUCT_SECRET = "AAWBJNSJkaxi3nvh";

    private final String CODE_SUCCESS = "200";
    private final String KEY_DEVICE_SECRET = "deviceSecret";

    /**
     * publish {appkey, userid(deviceName), product}
     */
    private final String propertySetFormat = "/%1$s/%2$s/user/%3$s/property/set";
    private String topicPropertySet;

    /**
     * publish {appkey, userid(deviceName), product}
     */
    private final String propertyGetFormat = "/%1$s/%2$s/user/%3$s/property/get";
    private String topicPropertyGet;

    /**
     * subscribe {appkey, userid(deviceName)}
     */
    private final String propertyResponseFormat = "/%1$s/%2$s/user/property/response";
    private String topicPropertyResponse;

    /**
     * subscribe {appkey, userid(deviceName)}
     */
    private final String deviceStatusFormat = "/%1$s/%2$s/user/status";
    private String topicDeviceStatus;

    private String mUserid;

    private String mSecret;

    private boolean initialized;

    private Set<String> subTopics = new HashSet<>();

    private final IConnectNotifyListener mNotifyListener = new IConnectNotifyListener() {
        @Override
        public void onNotify(String s, String s1, AMessage aMessage) {
            String payload = new String((byte[]) aMessage.getData());
            Log.e(TAG, "onNotify: " + s + " " + s1 + " " + payload);
            if (TextUtils.equals(s1, String.format(propertyResponseFormat, APP_PRODUCT_KEY, mUserid))) {          // 设备属性上报
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
            } else if (TextUtils.equals(s1, String.format(deviceStatusFormat, APP_PRODUCT_KEY, mUserid))) {     // 设备状态变化
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
        IOTAUTH_DOMAIN = String.format(IOTAUTH_DOMAIN_FMT, REGION);
        IOT_DOMAIN = String.format(IOT_DOMAIN_FMT, REGION);
        MQTT_DOMAIN = String.format(MQTT_DOMAIN_FMT, APP_PRODUCT_KEY, REGION);
    }

    public static AliotClient getInstance() {
        return LazyHolder.INSTANCE;
    }

//    public void dynamicRegister(final Context context, final String userid) {
//        final DeviceInfo devInfo = new DeviceInfo();
//        devInfo.productKey = APP_PRODUCT_KEY;
//        devInfo.productSecret = APP_PRODUCT_SECRET;
//        devInfo.deviceName = userid;
//
//        LinkKitInitParams params = new LinkKitInitParams();
//        params.deviceInfo = devInfo;
//        HubApiRequest request = new HubApiRequest();
//        request.domain = IOTAUTH_DOMAIN;
//        request.path = "/auth/register/device";
//
//        LinkKit.getInstance().deviceRegister(context, params, request, new IConnectSendListener() {
//            @Override
//            public void onResponse(ARequest aRequest, AResponse aResponse) {
//                Log.e(TAG, "onResponse: " + JSON.toJSONString(aResponse));
//                if (aResponse == null || aResponse.data == null) {
//                    return;
//                }
//                String payload = aResponse.data.toString();
//                Type type = new TypeReference<ResponseModel<Map<String, String>>>(){}.getType();
//                try {
//                    ResponseModel<Map<String, String>> response = JSONObject.parseObject(payload, type);
//                    if (CODE_SUCCESS.equals(response.code) && response.data != null && response.data.containsKey(KEY_DEVICE_SECRET)) {
//                        String deviceSecret = response.data.get(KEY_DEVICE_SECRET);
//                        if (!TextUtils.isEmpty(deviceSecret)) {
//                            devInfo.deviceSecret = deviceSecret;
//                            UserPref.setSecret(context, deviceSecret);
//                        }
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(ARequest aRequest, AError aError) {
//                Log.e(TAG, "onFailure: " + JSON.toJSONString(aError));
//            }
//        });
//    }

    public void init(Context context, final String userid, final String secret) {
        DeviceInfo devInfo = new DeviceInfo();
        devInfo.productKey = APP_PRODUCT_KEY;
        devInfo.deviceName = userid;
        devInfo.deviceSecret = secret;

        IoTMqttClientConfig clientConfig = new IoTMqttClientConfig(APP_PRODUCT_KEY, userid, secret);
        // 慎用 设置 mqtt 请求域名, 默认 productKey+".iot-as-mqtt.cn-shanghai.aliyuncs.com:1883", 如果无具体的业务需求, 请不要设置
        clientConfig.channelHost = MQTT_DOMAIN;
        IoTApiClientConfig connectConfig = new IoTApiClientConfig();
        connectConfig.domain = IOT_DOMAIN;

        Map<String, ValueWrapper> propertyValues = new HashMap<>();

        LinkKitInitParams params = new LinkKitInitParams();
        params.deviceInfo = devInfo;
        params.propertyValues = propertyValues;
        params.mqttClientConfig = clientConfig;
        params.connectConfig = connectConfig;

        LinkKit.getInstance().init(context, params, new ILinkKitConnectListener() {
            @Override
            public void onError(AError error) {
                Log.e(TAG, "onError: " + JSONObject.toJSONString(error));
            }

            @Override
            public void onInitDone(Object o) {
                Log.e(TAG, "onInitDone: ");

                LinkKit.getInstance().registerOnPushListener(mNotifyListener);

                subscribeTopic(String.format(propertyResponseFormat, APP_PRODUCT_KEY, userid));
                subscribeTopic(String.format(deviceStatusFormat, APP_PRODUCT_KEY, userid));

                mUserid = userid;
                mSecret = secret;
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
        mSecret = null;
    }

    public boolean isInitialized() {
        return initialized;
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
        Set<String> topics = new HashSet<>(subTopics);
        for (final String topic : topics) {
            MqttSubscribeRequest request = new MqttSubscribeRequest();
            request.topic = topic;
            request.isSubscribe = false;
            LinkKit.getInstance().unsubscribe(request, new IConnectUnscribeListener() {
                @Override
                public void onSuccess() {
                    subTopics.remove(topic);
                }

                @Override
                public void onFailure(AError aError) {

                }
            });
        }
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
        request.topic = String.format(propertySetFormat, APP_PRODUCT_KEY, mUserid, product);
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
        request.topic = String.format(propertySetFormat, APP_PRODUCT_KEY, mUserid, product);
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
        request.topic = String.format(propertyGetFormat, APP_PRODUCT_KEY, mUserid, product);
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
