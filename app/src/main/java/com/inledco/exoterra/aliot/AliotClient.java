package com.inledco.exoterra.aliot;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
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
import com.inledco.exoterra.AppConstants;
import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.bean.InviteAction;
import com.inledco.exoterra.aliot.bean.InviteMessage;
import com.inledco.exoterra.event.DeviceStatusChangedEvent;
import com.inledco.exoterra.manager.DeviceManager;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
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

    /**
     * publish {appkey, userid(deviceName), product}
     */
    private final String propertyGetFormat = "/%1$s/%2$s/user/%3$s/property/get";

    /**
     * subscribe {appkey, userid(deviceName)}
     */
    private final String propertyResponseFormat = "/%1$s/%2$s/user/property/response";

    /**
     * subscribe {appkey, userid(deviceName)}
     */
    private final String deviceStatusFormat = "/%1$s/%2$s/user/status";

    /**
     * publish {appkey, userid}
     */
    private final String inviteFormat = "/%1$s/%2$s/user/group/invite";

    /**
     * publish {appkey, userid}
     */
    private final String inviteReplyFormat = "/%1$s/%2$s/user/group/invite_reply";

    /**
     * subscribe {appkey, userid}
     */
    private final String inviteListenFormat = "/%1$s/%2$s/user/group/invite_listen";

    private WeakReference<Context> mWeakContext;

    private String mUserid;

    private boolean initialized;

    private boolean connected;

    private Set<String> subTopics = new HashSet<>();

    private final GroupInviteReceiver mGroupInviteReceiver = new GroupInviteReceiver();

    private final SubscribeParser<ADevice> responseParser = new SubscribeParser<ADevice>(propertyResponseFormat) {
        @Override
        public void onParse(ADevice result) {
            if (DeviceManager.getInstance().contains(result.getTag())) {
                Device device = DeviceManager.getInstance().getDevice(result.getTag());
                if (device.isOnline() == false) {
                    device.setOnline(true);
                    EventBus.getDefault().post(new DeviceStatusChangedEvent(result.getProductKey(), result.getDeviceName()));
                }
                device.updateProperties(result.getItems());
                EventBus.getDefault().post(result);
            }
        }
    };

    private final SubscribeParser<StatusReponse> statusParser = new SubscribeParser<StatusReponse>(deviceStatusFormat) {
        @Override
        public void onParse(StatusReponse result) {
            if (DeviceManager.getInstance().contains(result.getTag())) {
                Device device = DeviceManager.getInstance().getDevice(result.getTag());
                if (device.updateOnlineStatus(result)) {
                    EventBus.getDefault().post(new DeviceStatusChangedEvent(result.getProductKey(), result.getDeviceName()));
                }
            }
        }
    };

    private final SubscribeParser<InviteMessage> inviteParser = new SubscribeParser<InviteMessage>(inviteListenFormat) {
        @Override
        public void onParse(InviteMessage result) {
            InviteAction action = InviteAction.getInviteAction(result);
            if (action == null) {
                return;
            }
            switch (action) {
                case INVITE:
                    if (TextUtils.equals(mUserid, result.getInvitee())) {
                        showReceiveGroupInviteMessage(result);
                    }
                    break;
                case CANCEL:
                    if (TextUtils.equals(mUserid, result.getInvitee())) {

                    }
                    break;
                case ACCEPT:
                    if (TextUtils.equals(mUserid, result.getInviter())) {
                        showAcceptInviteMessage(result);
                    }
                    break;
                case DENY:
                    if (TextUtils.equals(mUserid, result.getInviter())) {
                        showDenyInviteMessage(result);
                    }
                    break;
            }
        }
    };

    private final SubscribeParser[] subscribeParsers = new SubscribeParser[] {responseParser, statusParser, inviteParser};

    private final IConnectNotifyListener mNotifyListener = new IConnectNotifyListener() {
        @Override
        public void onNotify(String s, String s1, AMessage aMessage) {
            String payload = new String((byte[]) aMessage.getData());
            Log.e(TAG, "onNotify: " + s + " " + s1 + " " + payload);
            for (SubscribeParser parser : subscribeParsers) {
                if (TextUtils.equals(s1, parser.getTopic(APP_PRODUCT_KEY, mUserid))) {
                    parser.parse(payload);
                    return;
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
            Log.e(TAG, "onConnectStateChange: " + s + " " + connectState);
            connected = (connectState == ConnectState.CONNECTED);
        }
    };

    private final IConnectSendListener mPublishListener = new IConnectSendListener() {
        @Override
        public void onResponse(ARequest aRequest, AResponse aResponse) {
            Log.e(TAG, "onResponse: " + JSON.toJSONString(aResponse));
        }

        @Override
        public void onFailure(ARequest aRequest, AError aError) {
            Log.e(TAG, "onFailure: " + JSON.toJSONString(aError));
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

    public void init(@NonNull final Context context, final String userid, final String secret) {
        DeviceInfo devInfo = new DeviceInfo();
        devInfo.productKey = APP_PRODUCT_KEY;
        devInfo.deviceName = userid;
        devInfo.deviceSecret = secret;

        IoTMqttClientConfig clientConfig = new IoTMqttClientConfig(APP_PRODUCT_KEY, userid, secret);
        // 慎用 设置 mqtt 请求域名, 默认 productKey+".iot-as-mqtt.cn-shanghai.aliyuncs.com:1883", 如果无具体的业务需求, 请不要设置
        clientConfig.channelHost = MQTT_DOMAIN;
        clientConfig.isCheckChannelRootCrt = false;
        clientConfig.secureMode = 3;
        IoTApiClientConfig connectConfig = new IoTApiClientConfig();
        connectConfig.domain = IOT_DOMAIN;

        Map<String, ValueWrapper> propertyValues = new HashMap<>();

        LinkKitInitParams params = new LinkKitInitParams();
        params.deviceInfo = devInfo;
        params.propertyValues = propertyValues;
        params.mqttClientConfig = clientConfig;
        params.connectConfig = connectConfig;

        Log.e(TAG, "init: " + JSON.toJSONString(devInfo));
        Log.e(TAG, "init: " + JSON.toJSONString(clientConfig));
        Log.e(TAG, "init: " + JSON.toJSONString(connectConfig));
        Log.e(TAG, "init: " + JSON.toJSONString(params));
        final long time = System.currentTimeMillis();
        LinkKit.getInstance().init(context, params, new ILinkKitConnectListener() {
            @Override
            public void onError(AError error) {
                Log.e(TAG, "onError: " + JSONObject.toJSONString(error));
            }

            @Override
            public void onInitDone(Object o) {
                Log.e(TAG, "onInitDone: " + (System.currentTimeMillis() - time));

                LinkKit.getInstance().registerOnPushListener(mNotifyListener);

                subscribeTopic(String.format(propertyResponseFormat, APP_PRODUCT_KEY, userid));
                subscribeTopic(String.format(deviceStatusFormat, APP_PRODUCT_KEY, userid));
                subscribeTopic(String.format(inviteListenFormat, APP_PRODUCT_KEY, userid));

                mWeakContext = new WeakReference<>(context);
                IntentFilter filter = new IntentFilter(AppConstants.HOME_INVITE);
                mWeakContext.get().registerReceiver(mGroupInviteReceiver, filter);

                mUserid = userid;
                initialized = true;
            }
        });
    }

    public void deinit() {
        unsubscribeAllTopics();
        LinkKit.getInstance().unRegisterOnPushListener(mNotifyListener);
        LinkKit.getInstance().deinit();
        if (mWeakContext != null && mWeakContext.get() != null) {
            mWeakContext.get().unregisterReceiver(mGroupInviteReceiver);
            mWeakContext.clear();
        }
        connected = false;
        initialized = false;
        mUserid = null;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isConnected() {
        return connected;
    }

    private void subscribeTopic(final String topic) {
        final MqttSubscribeRequest request = new MqttSubscribeRequest();
        request.topic = topic;
        request.isSubscribe = true;
        LinkKit.getInstance().subscribe(request, new IConnectSubscribeListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "onSuccess: subscribe " + topic);
                subTopics.add(request.topic);
            }

            @Override
            public void onFailure(AError aError) {
                Log.e(TAG, "onFailure: subscribe " + topic + " " + JSON.toJSONString(aError));
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
                    Log.e(TAG, "onSuccess: unsubscribe " + topic);
                    subTopics.remove(topic);
                }

                @Override
                public void onFailure(AError aError) {
                    Log.e(TAG, "onFailure: unsubscribe " + topic + " " + JSON.toJSONString(aError));
                }
            });
        }
    }

    private void publish(@NonNull final String topic, @NonNull final String payload) {
        MqttPublishRequest request = new MqttPublishRequest();
        request.topic = topic;
        request.payloadObj = payload;
        request.qos = 0;
        request.isRPC = false;
        Log.e(TAG, "publish: " + topic);
        Log.e(TAG, "publish: " + payload);
        LinkKit.getInstance().publish(request, mPublishListener);
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
        String topic = String.format(propertySetFormat, APP_PRODUCT_KEY, mUserid, product);
        publish(topic, JSON.toJSONString(payload));
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
        String topic = String.format(propertySetFormat, APP_PRODUCT_KEY, mUserid, product);
        publish(topic, JSON.toJSONString(payload));
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
        String topic = String.format(propertyGetFormat, APP_PRODUCT_KEY, mUserid, product);
        publish(topic, JSON.toJSONString(payload));
    }

    public void getAllProperties(String product, String dname) {
        getProperty(product, dname);
    }

    public void invite(final String invitee, final String invite_id, final String groupid, final String groupname) {
        if (!initialized || TextUtils.isEmpty(mUserid)) {
            return;
        }
        InviteMessage message = new InviteMessage();
        message.setAction(InviteAction.INVITE.getAction());
        message.setInviter(mUserid);
        message.setInvitee(invitee);
        message.setInvite_id(invite_id);
        message.setGroupid(groupid);
        message.setGroupname(groupname);
        String topic = String.format(inviteFormat, APP_PRODUCT_KEY, mUserid);
        publish(topic, JSON.toJSONString(message));
    }

    public void inviteCancel(final String invitee, final String invite_id, final String groupid, final String groupname) {
        if (!initialized || TextUtils.isEmpty(mUserid)) {
            return;
        }
        InviteMessage message = new InviteMessage();
        message.setAction(InviteAction.CANCEL.getAction());
        message.setInviter(mUserid);
        message.setInvitee(invitee);
        message.setInvite_id(invite_id);
        message.setGroupid(groupid);
        message.setGroupname(groupname);
        String topic = String.format(inviteFormat, APP_PRODUCT_KEY, mUserid);
        publish(topic, JSON.toJSONString(message));
    }

    public void inviteAccept(final String inviter, final String invite_id, final String groupid, final String groupname) {
        if (!initialized || TextUtils.isEmpty(mUserid)) {
            return;
        }
        InviteMessage message = new InviteMessage();
        message.setAction(InviteAction.ACCEPT.getAction());
        message.setInviter(inviter);
        message.setInvitee(mUserid);
        message.setInvite_id(invite_id);
        message.setGroupid(groupid);
        message.setGroupname(groupname);
        String topic = String.format(inviteReplyFormat, APP_PRODUCT_KEY, mUserid);
        publish(topic, JSON.toJSONString(message));
    }

    public void inviteDeny(final String inviter, final String invite_id, final String groupid, final String groupname) {
        if (!initialized || TextUtils.isEmpty(mUserid)) {
            return;
        }
        InviteMessage message = new InviteMessage();
        message.setAction(InviteAction.DENY.getAction());
        message.setInviter(inviter);
        message.setInvitee(mUserid);
        message.setInvite_id(invite_id);
        message.setGroupid(groupid);
        message.setGroupname(groupname);
        String topic = String.format(inviteReplyFormat, APP_PRODUCT_KEY, mUserid);
        publish(topic, JSON.toJSONString(message));
    }

    private void showReceiveGroupInviteMessage(@NonNull final InviteMessage message) {
        if (mWeakContext == null || mWeakContext.get() == null) {
            return;
        }
        String inviter = message.getInviter();
        String groupname = message.getGroupname();
        final String inviteid = message.getInvite_id();

        Intent acceptIntent = new Intent(AppConstants.HOME_INVITE);
        message.setAction(InviteAction.ACCEPT.getAction());
        acceptIntent.putExtra("InviteMessage", JSON.toJSONString(message));
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(mWeakContext.get(), 0, acceptIntent, PendingIntent.FLAG_ONE_SHOT);

        Intent denyIntent = new Intent(AppConstants.HOME_INVITE);
        message.setAction(InviteAction.DENY.getAction());
        denyIntent.putExtra("InviteMessage", JSON.toJSONString(message));
        PendingIntent denyPendingIntent = PendingIntent.getBroadcast(mWeakContext.get(), 0, denyIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mWeakContext.get(), getString(R.string.notify_chnid_invite));
        Notification notification = builder.setContentTitle(getString(R.string.habitat_invite_member))
                                           .setStyle(new NotificationCompat.BigTextStyle())
                                           .setContentText("User " + inviter + " invite you join habitat " + groupname + ".")
                                           .setWhen(System.currentTimeMillis())
                                           .setSmallIcon(R.drawable.ic_person_add_white_24dp)
                                           .setAutoCancel(true)
                                           .setOngoing(true)
                                           .addAction(R.drawable.ic_person_add_white_24dp, getString(R.string.accept), acceptPendingIntent)
                                           .addAction(R.drawable.ic_person_add_white_24dp, getString(R.string.deny), denyPendingIntent)
                                           .build();
        NotificationManager manager = (NotificationManager) mWeakContext.get().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(inviteid, 0, notification);
    }

    private void showAcceptInviteMessage(@NonNull final InviteMessage message) {
        if (mWeakContext == null || mWeakContext.get() == null) {
            return;
        }
        String inviteid = message.getInvite_id();
        String invitee = message.getInvitee();
        String groupname = message.getGroupname();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mWeakContext.get(), getString(R.string.notify_chnid_invite));
        Notification notification = builder.setContentTitle("Accpet Invite")
                                           .setStyle(new NotificationCompat.BigTextStyle())
                                           .setContentText("User " + invitee + " accept to join group " + groupname + ".")
                                           .setWhen(System.currentTimeMillis())
                                           .setSmallIcon(R.drawable.ic_person_add_white_24dp)
                                           .setAutoCancel(true)
                                           .build();
        NotificationManager manager = (NotificationManager) mWeakContext.get().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(inviteid, 0, notification);
    }

    private void showDenyInviteMessage(@NonNull final InviteMessage message) {
        if (mWeakContext == null || mWeakContext.get() == null) {
            return;
        }
        String inviteid = message.getInvite_id();
        String invitee = message.getInvitee();
        String groupname = message.getGroupname();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mWeakContext.get(), getString(R.string.notify_chnid_invite));
        Notification notification = builder.setContentTitle("Deny Invite")
                                           .setStyle(new NotificationCompat.BigTextStyle())
                                           .setContentText("User " + invitee + " refuse to join group " + groupname + ".")
                                           .setWhen(System.currentTimeMillis())
                                           .setSmallIcon(R.drawable.ic_person_add_white_24dp)
                                           .setAutoCancel(true)
                                           .build();
        NotificationManager manager = (NotificationManager) mWeakContext.get().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(inviteid, 0, notification);
    }

    private String getString(int resId) {
        if (mWeakContext == null || mWeakContext.get() == null) {
            return null;
        }
        return mWeakContext.get().getString(resId);
    }

    private static class LazyHolder {
        private static final AliotClient INSTANCE = new AliotClient();
    }

    public class GroupInviteReceiver extends BroadcastReceiver {
        private static final String TAG = "GroupInviteReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive: " + intent.getAction());
            if (TextUtils.equals(intent.getAction(), AppConstants.HOME_INVITE)) {
                final String message = intent.getStringExtra("InviteMessage");
                Log.e(TAG, "onReceive: " + message);
                try {
                    final InviteMessage inviteMessage = JSON.parseObject(message, InviteMessage.class);
                    if (inviteMessage == null) {
                        return;
                    }
                    final InviteAction action = InviteAction.getInviteAction(inviteMessage.getAction());
                    if (action == null) {
                        return;
                    }
                    final String inviter = inviteMessage.getInviter();
                    final String inviteid = inviteMessage.getInvite_id();
                    final String groupid = inviteMessage.getGroupid();
                    final String groupname = inviteMessage.getGroupname();
                    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    switch (action) {
                        case ACCEPT:
                            AliotServer.getInstance().acceptInvite(groupid, inviteid, new HttpCallback<UserApi.Response>() {
                                @Override
                                public void onError(String error) {
                                    Log.e(TAG, "onError: " + error);
                                }

                                @Override
                                public void onSuccess(UserApi.Response result) {
                                    EventBus.getDefault().post(inviteMessage);
                                    AliotClient.getInstance().inviteAccept(inviter, inviteid, groupid, groupname);
                                }
                            });
                            manager.cancel(inviteid, 0);
                            break;
                        case DENY:
                            AliotServer.getInstance().denyInvite(groupid, inviteid, new HttpCallback<UserApi.Response>() {
                                @Override
                                public void onError(String error) {
                                    Log.e(TAG, "onError: " + error);
                                }

                                @Override
                                public void onSuccess(UserApi.Response result) {
                                    AliotClient.getInstance().inviteDeny(inviter, inviteid, groupid, groupname);
                                }
                            });
                            manager.cancel(inviteid, 0);
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
//    public enum SubscribeEnum {
//        TOPIC_PROPERTY_RESPONSE(propertyResponseFormat) {
//            @Override
//            public Type getResultType() {
//                return ADevice.class;
//            }
//
//            @Override
//            public void parseReceive(String payload) {
//                ADevice adev = parse(payload);
//                if (adev != null) {
//                    if (DeviceManager.getInstance().contains(adev.getTag())) {
//                        Device device = DeviceManager.getInstance().getDevice(adev.getTag());
//                        device.updateProperties(adev.getItems());
//                        if (!device.isOnline()) {
//                            device.setOnline(true);
//                            EventBus.getDefault().post(new StatusReponse());
//                        }
//                        EventBus.getDefault().post(adev);
//                    }
//                }
//            }
//        },
//        TOPIC_DEVICE_STATUS(deviceStatusFormat) {
//            @Override
//            public Type getResultType() {
//                return StatusReponse.class;
//            }
//
//            @Override
//            public void parseReceive(String payload) {
//                StatusReponse status = parse(payload);
//                if (status != null) {
//                    if (DeviceManager.getInstance().contains(status.getTag())) {
//                        Device device = DeviceManager.getInstance().getDevice(status.getTag());
//                        if (device.updateOnlineStatus(status)) {
//                            EventBus.getDefault().post(new DeviceStatusChangedEvent(status.getProductKey(), status.getDeviceName()));
//                        }
//                    }
//                }
//            }
//        },
//        TOPIC_INVITE_LISTEN(inviteListenFormat) {
//            @Override
//            public Type getResultType() {
//                return InviteMessage.class;
//            }
//
//            @Override
//            public void parseReceive(String payload) {
//                InviteMessage msg = parse(payload);
//                if (msg != null) {
//                    InviteAction action = InviteAction.getInviteAction(msg);
//                    if (action == null) {
//                        return;
//                    }
//                    switch (action) {
//                        case INVITE:
//                            if (TextUtils.equals(mUserid, msg.getInvitee())) {
//
//                            }
//                            break;
//                        case CANCEL:
//                            if (TextUtils.equals(mUserid, msg.getInvitee())) {
//
//                            }
//                            break;
//                        case ACCEPT:
//                            if (TextUtils.equals(mUserid, msg.getInviter())) {
//
//                            }
//                            break;
//                        case DENY:
//                            if (TextUtils.equals(mUserid, msg.getInviter())) {
//
//                            }
//                            break;
//                    }
//                }
//            }
//        };
//
//        private final String topicFormat;
//
//        SubscribeEnum(String format) {
//            topicFormat = format;
//        }
//
//        public String getTopic(String appkey, String userid) {
//            return String.format(topicFormat, appkey, userid);
//        }
//
//        protected <T> T parse(String payload) {
//            try {
//                return JSON.parseObject(payload, getResultType());
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        public abstract Type getResultType();
//
//        public abstract void parseReceive(String payload);
//    }
}
