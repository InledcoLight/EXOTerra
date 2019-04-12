package com.liruya.exoterra;

import android.app.Application;

import com.liruya.exoterra.bean.Device;
import com.liruya.exoterra.event.DatapointChangedEvent;
import com.liruya.exoterra.event.DeviceStateChangedEvent;
import com.liruya.exoterra.manager.DeviceManager;
import com.liruya.exoterra.manager.UserManager;
import com.liruya.exoterra.util.LogUtil;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkConstants;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import cn.xlink.sdk.common.Loggable;
import cn.xlink.sdk.core.model.XLinkDataPoint;
import cn.xlink.sdk.v5.listener.XLinkCloudListener;
import cn.xlink.sdk.v5.listener.XLinkDataListener;
import cn.xlink.sdk.v5.listener.XLinkDeviceStateListener;
import cn.xlink.sdk.v5.listener.XLinkUserListener;
import cn.xlink.sdk.v5.manager.CloudConnectionState;
import cn.xlink.sdk.v5.manager.XLinkSendDataPolicy;
import cn.xlink.sdk.v5.model.EventNotify;
import cn.xlink.sdk.v5.model.XDevice;
import cn.xlink.sdk.v5.module.main.XLinkConfig;
import cn.xlink.wrapper.XLinkAndroidSDK;

public class EXOTerraApplication extends Application {
    private static final String TAG = "EXOTerraApplication";

    private XLinkDataListener mXlinkDataListener;
    private XLinkUserListener mXlinkUserListener;
    private XLinkCloudListener mXlinkCloudListener;
    private XLinkDeviceStateListener mXlinkDeviceStateListener;

    @Override
    public void onCreate() {
        super.onCreate();

        init();
    }

    private void init() {
        mXlinkDataListener = new XLinkDataListener() {
            @Override
            public void onDataPointUpdate(XDevice xDevice, List<XLinkDataPoint> list) {
                Device device = DeviceManager.getInstance().getDevice(xDevice);
                if (device != null) {
                    for (XLinkDataPoint dp : list) {
                        device.setDataPoint(dp);
                    }
                }
                EventBus.getDefault().post(new DatapointChangedEvent(xDevice.getDeviceTag()));
                LogUtil.e(TAG, "onDataPointUpdate: ");
            }
        };
        mXlinkUserListener = new XLinkUserListener() {
            @Override
            public void onUserLogout(LogoutReason logoutReason) {
                switch (logoutReason) {
                    case USER_LOGOUT:
                        UserManager.clear(EXOTerraApplication.this);
                        break;
                    case SINGLE_SIGN_KICK_OFF:

                        break;
                    case TOKEN_EXPIRED:

                        break;
                }
            }
        };
        mXlinkCloudListener = new XLinkCloudListener() {
            @Override
            public void onCloudStateChanged(CloudConnectionState cloudConnectionState) {

            }

            @Override
            public void onEventNotify(EventNotify eventNotify) {
                LogUtil.e(TAG, "onEventNotify: " + eventNotify.toString());
            }
        };
        mXlinkDeviceStateListener = new XLinkDeviceStateListener() {
            @Override
            public void onDeviceStateChanged(XDevice xDevice, XDevice.State state) {
                EventBus.getDefault().post(new DeviceStateChangedEvent(xDevice.getDeviceTag()));
            }

            @Override
            public void onDeviceChanged(XDevice xDevice, XDevice.Event event) {
            }
        };

        XLinkConfig config = XLinkConfig.newBuilder()
                                        .setApiServer(XlinkConstants.HOST_API_FORMAL, XlinkConstants.PORT_API_FORMAL)
                                        .setCloudServer(XlinkConstants.HOST_CM_FORMAL, XlinkConstants.PORT_CM_FORMAL)
                                        .setEnableSSL(true)
                                        .setLocalNetworkAutoConnection(false)
                                        .setLogConfig(XLinkAndroidSDK.defaultLogConfig(this).setDebugLevel(Loggable.ERROR))
                                        .setSendDataPolicy(XLinkSendDataPolicy.AUTO)
                                        .setDebug(false)
                                        .setDebugMqtt(false)
                                        .setDebugGateway(false)
                                        .setAutoDumpCrash(false)
                                        .setXLinkCloudListener(mXlinkCloudListener)
                                        .setDataListener(mXlinkDataListener)
                                        .setUserListener(mXlinkUserListener)
                                        .setDeviceStateListener(mXlinkDeviceStateListener)
                                        .build();
        XLinkAndroidSDK.init(config);

        XlinkCloudManager.getInstance().init(XlinkConstants.CORPORATION_ID);
    }
}
