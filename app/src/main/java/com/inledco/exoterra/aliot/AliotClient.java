package com.inledco.exoterra.aliot;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.alink.dm.api.DeviceInfo;
import com.aliyun.alink.dm.api.IoTApiClientConfig;
import com.aliyun.alink.h2.api.CompletableListener;
import com.aliyun.alink.h2.entity.Http2Request;
import com.aliyun.alink.h2.stream.api.CompletableDataListener;
import com.aliyun.alink.h2.stream.api.IStreamSender;
import com.aliyun.alink.linkkit.api.ILinkKitConnectListener;
import com.aliyun.alink.linkkit.api.IoTMqttClientConfig;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linkkit.api.LinkKitInitParams;
import com.aliyun.alink.linksdk.channel.core.persistent.mqtt.MqttConfigure;
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
import com.aliyun.alink.linksdk.tools.AError;
import com.aliyun.alink.linksdk.tools.ALog;
import com.inledco.exoterra.AppConfig;
import com.inledco.exoterra.AppConstants;
import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.bean.AttrKey;
import com.inledco.exoterra.aliot.bean.CotaConfigRequest;
import com.inledco.exoterra.aliot.bean.DeleteLabelRequest;
import com.inledco.exoterra.aliot.bean.DeviceLabel;
import com.inledco.exoterra.aliot.bean.InviteAction;
import com.inledco.exoterra.aliot.bean.InviteMessage;
import com.inledco.exoterra.aliot.bean.UpdateLabelRequest;
import com.inledco.exoterra.event.DeviceStatusChangedEvent;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.manager.GroupManager;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
     * {productKey, region}
     */
    private final String IOTH2_EDNPOINT_FMT = "https://%1$s.iot-as-http2.%2$s.aliyuncs.com:443";
    private final String IOTH2_ENDPOINT;

    /**
     * {productKey}  {region}
     */
    private final String MQTT_DOMAIN_FMT = "%1$s.iot-as-mqtt.%2$s.aliyuncs.com:1883";
    private final String MQTT_DOMAIN;

//    private final String REGION = "us-west-1";
//    private final String APP_KEY = "a3jdKlLMaEn";
//    private final String APP_SECRET = "AAWBJNSJkaxi3nvh";
    private final String REGION;
    private final String APP_KEY;
    private final String APP_SECRET;

    private final String CODE_SUCCESS = "200";
    private final String KEY_DEVICE_SECRET = "deviceSecret";

    /**
     * publish {APP_KEY, userid}
     */

    private final String propertySetFormat = "/%1$s/%2$s/user/property/set";

    private final String propertyGetFormat = "/%1$s/%2$s/user/property/get";

    private final String fotaUpgradeFormat = "/%1$s/%2$s/user/fota/upgrade";

    private final String sntpRequestFormat = "/ext/ntp/%1$s/%2$s/request";

    private final String inviterFormat = "/%1$s/%2$s/user/group/inviter";

    private final String inviteeFormat = "/%1$s/%2$s/user/group/invitee";
    /*********************************************************************************************/


    /**
     * subscribe {APP_KEY, userid(deviceName)}
     */
    private final String customFormat = "/%1$s/%2$s/user/*";

    private final String propertyResponseFormat = "/%1$s/%2$s/user/property/response";

    private final String deviceStatusFormat = "/%1$s/%2$s/user/status";

    private final String fotaProgressFormat = "/%1$s/%2$s/user/fota/progress";

    private final String inviteListenFormat = "/%1$s/%2$s/user/group/listen";

    private final String sntpResponseFormat = "/ext/ntp/%1$s/%2$s/response";
    /*********************************************************************************************/

//    private final String SERVER_FILE_PATH = "http://47.89.235.158:8086/imgs/";

    private static final int IDLE = 0;
    private static final int INITING = 1;
    private static final int INITED = 2;
    private static final int DEINITING = 3;
    @IntDef({IDLE, INITING, INITED, DEINITING})
    public @interface STATE {}

    private WeakReference<Context> mWeakContext;

    private final ExecutorService mExecutorService;
    private final byte[] lock;

    private int msgid;

    private String mUserid;

    @STATE
    private volatile int state;

    private volatile boolean subscribing;
    private volatile boolean unsubscribing;

    private volatile boolean synchronizedTime;

    private boolean registered;

    private boolean connected;

    private long mTimeOffset;

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
                device.setRequestId(result.getRequestId());
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

    private final SubscribeParser<InviteMessage> inviteListenParser = new SubscribeParser<InviteMessage>(inviteListenFormat) {
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
                case REMOVE:
                    if (TextUtils.equals(mUserid, result.getInvitee())) {

                    }
                    break;
                case DELETE:
                    if (TextUtils.equals(mUserid, result.getInvitee())) {

                    }
                    break;
                case ACCEPT:
                    if (TextUtils.equals(mUserid, result.getInviter())) {
                        GroupManager.getInstance().getGroups();
                        showAcceptInviteMessage(result);
                    }
                    break;
                case DENY:
                    if (TextUtils.equals(mUserid, result.getInviter())) {
                        showDenyInviteMessage(result);
                    }
                    break;
                case EXIT:
                    if (TextUtils.equals(mUserid, result.getInviter())) {

                    }
                    break;
            }
        }
    };

    private final SubscribeParser<UserApi.SntpResponse> sntpParser = new SubscribeParser<UserApi.SntpResponse>(sntpResponseFormat) {
        @Override
        public void onParse(UserApi.SntpResponse result) {
            long deviceRecvTime = System.currentTimeMillis();
            try {
                long deviceSendTime = Long.parseLong(result.deviceSendTime);
                long serverRecvTime = Long.parseLong(result.serverRecvTime);
                long serverSendTime = Long.parseLong(result.serverSendTime);
                mTimeOffset = (serverSendTime + serverRecvTime - deviceRecvTime - deviceSendTime) / 2;
                synchronizedTime = true;
                Log.e(TAG, "onParse: timeoffset- " + mTimeOffset);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private final SubscribeParser<UserApi.FotaProgress> fotaProgressParser = new SubscribeParser<UserApi.FotaProgress>(fotaProgressFormat) {
        @Override
        public void onParse(UserApi.FotaProgress result) {
            Log.e(TAG, "onParse: " + JSON.toJSONString(result));
            EventBus.getDefault().post(result);
        }
    };

    private final SubscribeParser[] subscribeParsers = new SubscribeParser[] {
        responseParser,
        statusParser, inviteListenParser,
        sntpParser,
        fotaProgressParser
    };

    private final IConnectNotifyListener mNotifyListener = new IConnectNotifyListener() {
        @Override
        public void onNotify(String connectId, String topic, AMessage aMessage) {
            String payload = new String((byte[]) aMessage.getData());
            Log.e(TAG, "onNotify: " + connectId + " " + topic + " " + payload);
            for (SubscribeParser parser : subscribeParsers) {
                if (TextUtils.equals(topic, parser.getTopic(APP_KEY, mUserid))) {
                    parser.parse(payload);
                    return;
                }
            }
        }

        @Override
        public boolean shouldHandle(String connectId, String topic) {
            Log.e(TAG, "shouldHandle: " + connectId + " " + topic);
            return true;
        }

        @Override
        public void onConnectStateChange(String connectId, ConnectState connectState) {
            Log.e(TAG, "onConnectStateChange: " + connectId + " " + connectState);
            connected = (connectState == ConnectState.CONNECTED);
            if (connected) {
                subscribeAllTopics();
                if (!synchronizedTime) {
                    syncTime();
                }
            }
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

    public static AliotClient getInstance() {
        return LazyHolder.INSTANCE;
    }

    private AliotClient () {
        REGION = AppConfig.getString("region");
        APP_KEY = AppConfig.getString("appKey");
        APP_SECRET = AppConfig.getString("appSecret");
        IOTAUTH_DOMAIN = String.format(IOTAUTH_DOMAIN_FMT, REGION);
        IOT_DOMAIN = String.format(IOT_DOMAIN_FMT, REGION);
        IOTH2_ENDPOINT = String.format(IOTH2_EDNPOINT_FMT, APP_KEY, REGION);
        MQTT_DOMAIN = String.format(MQTT_DOMAIN_FMT, APP_KEY, REGION);

        state = IDLE;
        mExecutorService = Executors.newCachedThreadPool();
        lock = new byte[0];

        ALog.setLevel(ALog.LEVEL_DEBUG);
    }

    private void delay(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void init(@NonNull final Context context, final String userid, final String secret, final ILinkListener calback) {
        Log.e(TAG, "INIT: " + state);
        state = INITING;
        DeviceInfo devInfo = new DeviceInfo();
        devInfo.productKey = APP_KEY;
        devInfo.productSecret = APP_SECRET;
        devInfo.deviceName = userid;
        devInfo.deviceSecret = secret;

        MqttConfigure.clientId = userid;
        MqttConfigure.setKeepAliveInterval(120);
        IoTMqttClientConfig clientConfig = new IoTMqttClientConfig(APP_KEY, userid, secret);
        clientConfig.channelHost = MQTT_DOMAIN;
        clientConfig.isCheckChannelRootCrt = false;
        clientConfig.secureMode = MqttConfigure.MQTT_SECURE_MODE_TCP;

        IoTApiClientConfig connectConfig = new IoTApiClientConfig();
        connectConfig.domain = IOT_DOMAIN;

        //        Map<String, ValueWrapper> propertyValues = new HashMap<>();
        //
        //        IoTH2Config ioTH2Config = new IoTH2Config();
        //        ioTH2Config.clientId = userid;
        //        ioTH2Config.endPoint = IOTH2_ENDPOINT;

        LinkKitInitParams params = new LinkKitInitParams();
        params.deviceInfo = devInfo;
        params.mqttClientConfig = clientConfig;
        params.connectConfig = connectConfig;
        //        params.propertyValues = propertyValues;
        //        params.iotH2InitParams = ioTH2Config;

        Log.e(TAG, "init: LinkKit SDK Version - " + LinkKit.getInstance().getSDKVersion());
        Log.e(TAG, "init: " + JSON.toJSONString(params));
        final long time = System.currentTimeMillis();
        LinkKit.getInstance().init(context, params, new ILinkKitConnectListener() {
            @Override
            public void onError(AError error) {
                Log.e(TAG, "INIT onError: " + JSONObject.toJSONString(error));
                if (calback != null) {
                    calback.onInitError(error);
                }
                state = IDLE;
            }

            @Override
            public void onInitDone(final Object o) {
                Log.e(TAG, "INIT onInitDone: " + (System.currentTimeMillis() - time));

                mWeakContext = new WeakReference<>(context);
                IntentFilter filter = new IntentFilter(AppConstants.GROUP_INVITE);
                mWeakContext.get().registerReceiver(mGroupInviteReceiver, filter);
                registered = true;

                mUserid = userid;

                if (calback != null) {
                    calback.onInitDone();
                }
                state = INITED;
            }
        });
        LinkKit.getInstance().registerOnPushListener(mNotifyListener);
        while (state == INITING);
        if (state == IDLE) {
            delay(50);
            deinit();
        }
    }

    public void start(@NonNull final Context context, final String userid, final String secret, final ILinkListener calback) {
        Log.e(TAG, "start: " + state);
        if (calback != null) {
            calback.onStart();
        }
        if (state == INITING) {
            return;
        }
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    if (state == DEINITING) {
                        while (state != IDLE)
                            ;
                    } else if (state == INITED) {
                        deinit();
                    }
                    init(context, userid, secret, calback);
                }
            }
        });
    }

    private void deinit() {
        Log.e(TAG, "INIT deinit: " + state);
        state = DEINITING;
//        unsubscribeAllTopics();
        LinkKit.getInstance().unRegisterOnPushListener(mNotifyListener);
        LinkKit.getInstance().deinit();
        if (mWeakContext != null) {
            if (mWeakContext.get() != null && registered) {
                mWeakContext.get().unregisterReceiver(mGroupInviteReceiver);
                registered = false;
            }
            mWeakContext.clear();
            mWeakContext = null;
        }
        connected = false;
        mUserid = null;
        synchronizedTime = false;
        delay(100);
        state = IDLE;
        Log.e(TAG, "INIT: deinit done");
    }

    public void stop() {
        Log.e(TAG, "stop: " + state);
        if (state != INITED) {
            return;
        }
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    deinit();
                }
            }
        });
    }

    public boolean isInited() {
        return (state == INITED);
    }

    public boolean isConnected() {
        return connected;
    }

    public long getTimeOffset() {
        return mTimeOffset;
    }

    private void subscribeTopic(final String topic, final IConnectSubscribeListener callback) {
        final MqttSubscribeRequest request = new MqttSubscribeRequest();
        request.topic = topic;
        request.isSubscribe = true;
        request.qos = 0;
        LinkKit.getInstance().subscribe(request, callback);
    }

    private void subscribeTopic(final String topic) {
//        subscribing = true;
        final MqttSubscribeRequest request = new MqttSubscribeRequest();
        request.topic = topic;
        request.isSubscribe = true;
        request.qos = 0;
        LinkKit.getInstance().subscribe(request, new IConnectSubscribeListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "onSuccess: subscribe " + topic);
                subTopics.add(request.topic);
                subscribing = false;
            }

            @Override
            public void onFailure(AError aError) {
                Log.e(TAG, "onFailure: subscribe " + topic + " " + JSON.toJSONString(aError));
                subscribing = false;
            }
        });
//        while (subscribing);
    }

    private void subscribeAllTopics() {
        subscribeTopic(String.format(propertyResponseFormat, APP_KEY, mUserid));
        subscribeTopic(String.format(deviceStatusFormat, APP_KEY, mUserid));
        subscribeTopic(String.format(fotaProgressFormat, APP_KEY, mUserid));
        subscribeTopic(String.format(inviteListenFormat, APP_KEY, mUserid));
//        subThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                delay(200);
//                subscribeTopic(String.format(propertyResponseFormat, APP_KEY, mUserid));
//                delay(20);
//                subscribeTopic(String.format(deviceStatusFormat, APP_KEY, mUserid));
//                delay(20);
//                subscribeTopic(String.format(fotaProgressFormat, APP_KEY, mUserid));
//                delay(20);
//                subscribeTopic(String.format(inviteListenFormat, APP_KEY, mUserid));
//            }
//        });
//        subThread.start();
    }

    private void unsubscribeTopic(String topic) {
        unsubscribing = true;
        MqttSubscribeRequest request = new MqttSubscribeRequest();
        request.topic = topic;
        request.isSubscribe = false;
        request.qos = 0;
        LinkKit.getInstance().unsubscribe(request, new IConnectUnscribeListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "onSuccess: unsub ");
                unsubscribing = false;
            }

            @Override
            public void onFailure(AError aError) {
                Log.e(TAG, "onFailure: unsub " + JSON.toJSONString(aError));
                unsubscribing = false;
            }
        });
        while (unsubscribing);
    }

    private void unsubscribeAllTopics() {
        while (subscribing);
        Set<String> topics = new HashSet<>(subTopics);
        for (final String topic : topics) {
            unsubscribeTopic(topic);
            subTopics.remove(topic);
        }

//        unsubscribeTopic(String.format(propertyResponseFormat, APP_KEY, mUserid));
//        unsubscribeTopic(String.format(deviceStatusFormat, APP_KEY, mUserid));
//        unsubscribeTopic(String.format(fotaProgressFormat, APP_KEY, mUserid));
//        unsubscribeTopic(String.format(inviteListenFormat, APP_KEY, mUserid));
    }

    private void publish(@NonNull final String topic, int qos, @NonNull final String payload) {
        MqttPublishRequest request = new MqttPublishRequest();
        request.topic = topic;
        request.payloadObj = payload;
        if (qos != 1) {
            request.qos = 0;
        }
        request.isRPC = false;
        Log.e(TAG, "publish: " + topic);
        Log.e(TAG, "publish: " + payload);
        LinkKit.getInstance().publish(request, mPublishListener);
    }

    public void updateLabel(DeviceLabel... labels) {
        if (!isInited() || labels == null || labels.length == 0) {
            return;
        }
        msgid++;
        UpdateLabelRequest request = new UpdateLabelRequest(msgid);
        for (DeviceLabel l : labels) {
            request.params.add(l);
        }
        LinkKit.getInstance().getDeviceLabel().labelUpdate(request, new IConnectSendListener() {
            @Override
            public void onResponse(ARequest aRequest, AResponse aResponse) {
                Log.e(TAG, "onResponse: label " + JSON.toJSONString(aResponse));
            }

            @Override
            public void onFailure(ARequest aRequest, AError aError) {
                Log.e(TAG, "onFailure: label " + JSON.toJSONString(aError));
            }
        });
    }

    public void deleteLabel(AttrKey... attrKeys) {
        if (!isInited() || attrKeys == null || attrKeys.length == 0) {
            return;
        }
        msgid++;
        DeleteLabelRequest requet = new DeleteLabelRequest(msgid);
        for (AttrKey key : attrKeys) {
            requet.params.add(key);
        }
        LinkKit.getInstance().getDeviceLabel().labelDelete(requet, new IConnectSendListener() {
            @Override
            public void onResponse(ARequest aRequest, AResponse aResponse) {
                Log.e(TAG, "onResponse: label " + JSON.toJSONString(aResponse));
            }

            @Override
            public void onFailure(ARequest aRequest, AError aError) {
                Log.e(TAG, "onFailure: label " + JSON.toJSONString(aError));
            }
        });
    }

    public void getCotaConfig() {
        if (!isInited()) {
            return;
        }
        msgid++;
        CotaConfigRequest request = new CotaConfigRequest(msgid);
        LinkKit.getInstance().getDeviceCOTA().COTAGet(request, new IConnectSendListener() {
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

    public void uploadFile(final String uploadFile) {
        final IStreamSender client = LinkKit.getInstance().getH2StreamClient();
        final CompletableListener disconnCallback = new CompletableListener() {
            @Override
            public void complete(Object o) {
                Log.e(TAG, "complete: disconnect");
            }

            @Override
            public void completeExceptionally(Throwable throwable) {
                Log.e(TAG, "completeExceptionally: diconnect - " + throwable.getMessage());
            }
        };
        final CompletableDataListener uploadCallback = new CompletableDataListener() {
            @Override
            public void callBack(String s) {
                Log.e(TAG, "callBack: upload - " + s);
            }

            @Override
            public void complete(Object o) {
                Log.e(TAG, "complete: upload");
                client.disconnect(disconnCallback);
            }

            @Override
            public void completeExceptionally(Throwable throwable) {
                Log.e(TAG, "completeExceptionally: upload - " + throwable.getMessage());
                client.disconnect(disconnCallback);
            }
        };
        CompletableListener connCallback = new CompletableListener() {
            @Override
            public void complete(Object o) {
                Log.e(TAG, "complete: connect");
                final Http2Request request = new Http2Request();
                //OSS上存储的文件名。文件名校验规则正则表达式为[a-zA-Z][a-zA-Z0-9_.]*。
                request.getHeaders().add("x-file-name", "fileName");
                //是否覆盖同名文件。0(不覆盖), 1(覆盖)。默认为0。如果文件已存在，并指定了不默认覆盖，则创建流失败
                request.getHeaders().add("x-file-overwrite", "1");
                //文件类型，不指定则由OSS自动指定。
                // request.getHeaders().add("x-file-content-type", "jpg");

                String serviceName = "/c/iot/sys/thing/file/upload";
                // 注意替换成真实上传文件的路径
                String filePath = uploadFile;
                client.uploadFile(serviceName, request, filePath, uploadCallback);
            }

            @Override
            public void completeExceptionally(Throwable throwable) {
                Log.e(TAG, "completeExceptionally: connect - " + throwable.getMessage());
            }
        };
        client.connect(connCallback);
    }

    public void syncTime() {
        String topic = String.format(sntpRequestFormat, APP_KEY, mUserid);
        UserApi.SntpRequet requet = new UserApi.SntpRequet();
        requet.deviceSendTime = String.valueOf(System.currentTimeMillis());
        publish(topic, 1, JSON.toJSONString(requet));
    }

//    /**
//     * 设置设备属性
//     * @param pkey          产品Key
//     * @param dname         设备ID
//     * @param keyValues     属性键值对
//     */
//    public void setProperty(String pkey, String dname, KeyValue... keyValues) {
//        if (!inited || TextUtils.isEmpty(mUserid)) {
//            return;
//        }
//        if (keyValues == null || keyValues.length == 0) {
//            return;
//        }
//        Map<String, Object> params = new HashMap<>();
//        for (KeyValue attr : keyValues) {
//            if (attr == null) {
//                return;
//            }
//            params.put(attr.getAttrKey(), attr.getAttrValue());
//        }
//        Map<String, Object> payload = new HashMap<>();
//        payload.put("productKey", pkey);
//        payload.put("deviceName", dname);
//        payload.put("params", params);
//        String topic = String.format(propertySetFormat, APP_KEY, mUserid);
//        publish(topic, JSON.toJSONString(payload));
//    }
//
//    public void setProperty(String pkey, String dname, List<KeyValue> keyValues) {
//        if (!inited || TextUtils.isEmpty(mUserid)) {
//            return;
//        }
//        if (keyValues == null || keyValues.size() == 0) {
//            return;
//        }
//        Map<String, Object> params = new HashMap<>();
//        for (KeyValue attr : keyValues) {
//            if (attr == null) {
//                return;
//            }
//            params.put(attr.getAttrKey(), attr.getAttrValue());
//        }
//        Map<String, Object> payload = new HashMap<>();
//        payload.put("productKey", pkey);
//        payload.put("deviceName", dname);
//        payload.put("params", params);
//        String topic = String.format(propertySetFormat, APP_KEY, mUserid);
//        publish(topic, JSON.toJSONString(payload));
//    }

//    /**
//     * 获取设备属性
//     * @param pkey          产品Key
//     * @param dname         设备ID
//     * @param attrKeys      属性名称
//     */
//    public void getProperty(String pkey, String dname, String... attrKeys) {
//        if (!inited || TextUtils.isEmpty(mUserid)) {
//            return;
//        }
//        Map<String, Object> payload = new HashMap<>();
//        payload.put("productKey", pkey);
//        payload.put("deviceName", dname);
//        payload.put("params", attrKeys);
//        String topic = String.format(propertyGetFormat, APP_KEY, mUserid);
//        publish(topic, JSON.toJSONString(payload));
//    }

//    public void getAllProperties(String pkey, String dname) {
//        getProperty(pkey, dname);
//    }

//    public void upgradeFirmware(String pkey, String dname, int version, String url) {
//        if (!inited || TextUtils.isEmpty(mUserid)) {
//            return;
//        }
//        UserApi.FirmwareInfo info = new UserApi.FirmwareInfo();
//        info.version = String.valueOf(version);
//        info.url = SERVER_FILE_PATH + url;
//        Map<String, Object> payload = new HashMap<>();
//        payload.put("productKey", pkey);
//        payload.put("deviceName", dname);
//        payload.put("message", "success");
//        payload.put("data", info);
//        String topic = String.format(fotaUpgradeFormat, APP_KEY, mUserid);
//        publish(topic, JSON.toJSONString(payload));
//    }

//    private void sendGroupMessage(@NonNull InviteAction action, final String inviter, final String invitee, final String invite_id, final String groupid, final String groupname) {
//        if (!inited || TextUtils.isEmpty(mUserid)) {
//            return;
//        }
//        String topic;
//        switch (action) {
//            case INVITE:
//            case CANCEL:
//            case REMOVE:
//            case DELETE:
//                topic = String.format(inviterFormat, APP_KEY, inviter);
//                break;
//            case ACCEPT:
//            case DENY:
//            case EXIT:
//                topic = String.format(inviteeFormat, APP_KEY, invitee);
//                break;
//            default:
//                return;
//        }
//        InviteMessage message = new InviteMessage();
//        message.setAction(action.getAction());
//        message.setInviter(inviter);
//        message.setInvitee(invitee);
//        message.setInvite_id(invite_id);
//        message.setGroupid(groupid);
//        message.setGroupname(groupname);
//        publish(topic, JSON.toJSONString(message));
//    }

//    public void invite(final String invitee, final String invite_id, final String groupid, final String groupname) {
//        sendGroupMessage(InviteAction.INVITE, mUserid, invitee, invite_id, groupid, groupname);
//    }

//    public void inviteCancel(final String invitee, final String invite_id, final String groupid, final String groupname) {
//        sendGroupMessage(InviteAction.CANCEL, mUserid, invitee, invite_id, groupid, groupname);
//    }

//    public void inviteAccept(final String inviter, final String invite_id, final String groupid, final String groupname) {
//        sendGroupMessage(InviteAction.ACCEPT, inviter, mUserid, invite_id, groupid, groupname);
//    }
//
//    public void inviteDeny(final String inviter, final String invite_id, final String groupid, final String groupname) {
//        sendGroupMessage(InviteAction.INVITE, inviter, mUserid, invite_id, groupid, groupname);
//    }

//    public void removeUser(final String invitee, final String groupid, final String groupname) {
//        sendGroupMessage(InviteAction.REMOVE, mUserid, invitee, null, groupid, groupname);
//    }
//
//    public void exitGroup(final String inviter, final String groupid, final String groupname) {
//        sendGroupMessage(InviteAction.EXIT, inviter, mUserid, null, groupid, groupname);
//    }
//
//    public void deleteGroup(final String invitee, final String groupid, final String groupname) {
//        sendGroupMessage(InviteAction.DELETE, mUserid, invitee, null, groupid, groupname);
//    }

    private void showReceiveGroupInviteMessage(@NonNull final InviteMessage message) {
        if (mWeakContext == null || mWeakContext.get() == null) {
            return;
        }
        String inviter = message.getInviter();
        String groupname = message.getGroupname();
        final String inviteid = message.getInvite_id();

        Intent acceptIntent = new Intent(AppConstants.GROUP_INVITE);
        message.setAction(InviteAction.ACCEPT.getAction());
        acceptIntent.putExtra("InviteMessage", JSON.toJSONString(message));
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(mWeakContext.get(), 0, acceptIntent, PendingIntent.FLAG_ONE_SHOT);

        Intent denyIntent = new Intent(AppConstants.GROUP_INVITE);
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
            if (TextUtils.equals(intent.getAction(), AppConstants.GROUP_INVITE)) {
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
                                    AliotServer.getInstance().inviteAccept(inviter, inviteid, groupid, groupname);
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
                                    AliotServer.getInstance().inviteDeny(inviter, inviteid, groupid, groupname);
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

//    public void dynamicRegister(final Context context, final String userid) {
//        final DeviceInfo devInfo = new DeviceInfo();
//        devInfo.productKey = APP_KEY;
//        devInfo.productSecret = APP_SECRET;
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
//        public String getTopic(String APP_KEY, String userid) {
//            return String.format(topicFormat, APP_KEY, userid);
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
