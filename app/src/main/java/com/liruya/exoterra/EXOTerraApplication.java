package com.liruya.exoterra;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.liruya.base.BaseActivity;
import com.liruya.exoterra.bean.Device;
import com.liruya.exoterra.event.DatapointChangedEvent;
import com.liruya.exoterra.event.DeviceStateChangedEvent;
import com.liruya.exoterra.event.SubscribeChangedEvent;
import com.liruya.exoterra.manager.DeviceManager;
import com.liruya.exoterra.manager.UserManager;
import com.liruya.exoterra.util.LogUtil;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkConstants;
import com.liruya.exoterra.xlink.XlinkTaskCallback;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.List;

import cn.xlink.sdk.common.Loggable;
import cn.xlink.sdk.core.java.model.local.FirmwareReportUpgradeResult;
import cn.xlink.sdk.core.java.model.local.FirmwareReportVersion;
import cn.xlink.sdk.core.java.model.local.FirmwareUpgradeTaskRequest;
import cn.xlink.sdk.core.model.XLinkDataPoint;
import cn.xlink.sdk.core.model.XLinkDeviceEvent;
import cn.xlink.sdk.v5.listener.XLinkCloudListener;
import cn.xlink.sdk.v5.listener.XLinkDataListener;
import cn.xlink.sdk.v5.listener.XLinkDeviceEventListener;
import cn.xlink.sdk.v5.listener.XLinkDeviceStateListener;
import cn.xlink.sdk.v5.listener.XLinkUserListener;
import cn.xlink.sdk.v5.manager.CloudConnectionState;
import cn.xlink.sdk.v5.manager.XLinkSendDataPolicy;
import cn.xlink.sdk.v5.model.EventNotify;
import cn.xlink.sdk.v5.model.XDevice;
import cn.xlink.sdk.v5.module.main.XLinkConfig;
import cn.xlink.sdk.v5.module.notify.EventNotifyHelper;
import cn.xlink.wrapper.XLinkAndroidSDK;

public class EXOTerraApplication extends Application {
    private static final String TAG = "EXOTerraApplication";

    private WeakReference<BaseActivity> mCurrentActivity;

    private XLinkDataListener mXlinkDataListener;
    private XLinkUserListener mXlinkUserListener;
    private XLinkCloudListener mXlinkCloudListener;
    private XLinkDeviceStateListener mXlinkDeviceStateListener;
    private XLinkDeviceEventListener mXLinkDeviceEventListener;

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                if (activity instanceof BaseActivity) {
                    mCurrentActivity = new WeakReference<>((BaseActivity) activity);
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                if (activity.equals(mCurrentActivity.get())) {
                    mCurrentActivity = new WeakReference<>(null);
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

        init();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("share", "Share", NotificationManager.IMPORTANCE_HIGH);
            createNotificationChannel("share_receipt", "Share_Receipt", NotificationManager.IMPORTANCE_DEFAULT);
            createNotificationChannel("alarm", "Alarm", NotificationManager.IMPORTANCE_HIGH);
        }
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
                switch (eventNotify.messageType) {
                    case EventNotify.MSG_TYPE_DEVICE_SHARE:
                        final EventNotifyHelper.DeviceShareNotify notify = EventNotifyHelper.parseDeviceShareNotify(eventNotify.payload);
                        handleDeviceShareNotify(eventNotify.fromId, notify);
                        break;
                    case EventNotify.MSG_TYPE_SUBSCRIPTION_CHANGE:
                        EventBus.getDefault().post(new SubscribeChangedEvent());
                        break;
                    case EventNotify.MSG_TYPE_DEVICE_PROP_CHANGE:
                        break;
                    case EventNotify.MSG_TYPE_ONLINE_STATE_CHANGE:
                        break;
                    case EventNotify.MSG_TYPE_HOME_INVITE:
                        break;
                    case EventNotify.MSG_TYPE_HOME_MESSAGE_NOTIFY:
                        break;
                    case EventNotify.MSG_TYPE_DATA_POINT_CHANGED:
                        break;
                }
            }
        };
        mXlinkDeviceStateListener = new XLinkDeviceStateListener() {
            @Override
            public void onDeviceStateChanged(XDevice xDevice, XDevice.State state) {
                EventBus.getDefault().post(new DeviceStateChangedEvent(xDevice.getDeviceTag()));
            }

            @Override
            public void onDeviceChanged(XDevice xDevice, XDevice.Event event) {
                Log.e(TAG, "onDeviceChanged: " + event.name() );
            }
        };
        mXLinkDeviceEventListener = new XLinkDeviceEventListener() {
            @Override
            public void onDeviceEventNotify(XDevice xDevice, List<XLinkDeviceEvent> list, int from) {
                if (list == null || list.size() == 0) {
                    return;
                }
                XLinkDeviceEvent event = list.get(0);
                switch (event.type) {
                    case XLinkDeviceEvent.TYPE_FIRMWARE_CHECK_UPGRADE_TASK:
                        FirmwareUpgradeTaskRequest request = event.parseFrame2DeviceEvent(FirmwareUpgradeTaskRequest.class);
                        Log.e(TAG, "onDeviceEventNotify: " + request.identifyCode + " " + request.firmwareType + " " + request.currentVersion);
                        break;
                    case XLinkDeviceEvent.TYPE_FIRMWARE_REPORT_UPGRADE_RESULT:
                        FirmwareReportUpgradeResult result = event.parseFrame2DeviceEvent(FirmwareReportUpgradeResult.class);
                        Log.e(TAG, "onDeviceEventNotify: " + result.code + "\n"
                                                                + result.currentVersion + "\n"
                                                                + result.firmwareType + "\n"
                                                                + result.identifyCode + "\n"
                                                                + result.mod + "\n"
                                                                + result.originalVersion + "\n"
                                                                + result.taskId + "\n"
                                                                + result.taskIdLen);
                        break;
                    case XLinkDeviceEvent.TYPE_FIRMWARE_REPORT_VERSION:
                        FirmwareReportVersion version = event.parseFrame2DeviceEvent(FirmwareReportVersion.class);
                        Log.e(TAG, "onDeviceEventNotify: " + version.firmwareCount + " " + version.firmwareFrames.toString());
                        break;
                }
            }
        };

        XLinkConfig config = XLinkConfig.newBuilder()
                                        .setApiServer(XlinkConstants.HOST_API_FORMAL, XlinkConstants.PORT_API_FORMAL)
                                        .setCloudServer(XlinkConstants.HOST_CM_FORMAL, XlinkConstants.PORT_CM_FORMAL)
                                        .setEnableSSL(true)
                                        .setLocalNetworkAutoConnection(false)
                                        .setLogConfig(XLinkAndroidSDK.defaultLogConfig(this).setDebugLevel(Loggable.ERROR).setEnableLogFile(false))
                                        .setSendDataPolicy(XLinkSendDataPolicy.AUTO)
                                        .setDebug(false)
                                        .setDebugMqtt(false)
                                        .setDebugGateway(false)
                                        .setAutoDumpCrash(false)
                                        .setXLinkCloudListener(mXlinkCloudListener)
                                        .setDataListener(mXlinkDataListener)
                                        .setUserListener(mXlinkUserListener)
                                        .setDeviceStateListener(mXlinkDeviceStateListener)
                                        .setEventListener(mXLinkDeviceEventListener)
                                        .build();
        XLinkAndroidSDK.init(config);

        XlinkCloudManager.getInstance().init(XlinkConstants.CORPORATION_ID);
    }

    private void handleDeviceShareNotify(int fromId, @NonNull EventNotifyHelper.DeviceShareNotify notify) {
        switch (notify.type) {
            case EventNotifyHelper.DeviceShareNotify.TYPE_RECV_SHARE:
                showReceiveShareDialog(fromId, notify);
                break;
            case EventNotifyHelper.DeviceShareNotify.TYPE_ACCEPT_SHARE:
                showDeviceShareAccpetMessage(fromId, notify);
                break;
            case EventNotifyHelper.DeviceShareNotify.TYPE_CANCEL_SHARE:
                Toast.makeText(this, "User(" + fromId + ") cancel the device share." , Toast.LENGTH_SHORT)
                     .show();
                break;
            case EventNotifyHelper.DeviceShareNotify.TYPE_DENY_SHARE:
                showDeviceShareDenyMessage(fromId, notify);
                break;
        }
    }

    private void showReceiveShareDialog(int fromId, @NonNull final EventNotifyHelper.DeviceShareNotify notify) {
        if (mCurrentActivity.get() == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mCurrentActivity.get());
        builder.setTitle(R.string.receive_device_share)
               .setMessage("User(" + fromId + ") share with you the device(device id: " + notify.device_id + ").")
               .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       acceptDeviceShare(notify);
                   }
               })
               .setNegativeButton(R.string.deny, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       denyDeviceShare(notify);
                   }
               })
               .setNeutralButton(R.string.later, null)
               .show();
    }

    private void acceptDeviceShare(@NonNull EventNotifyHelper.DeviceShareNotify notify) {
        if (mCurrentActivity.get() == null) {
            return;
        }
        XlinkCloudManager.getInstance().acceptShareDevice(notify, new XlinkTaskCallback<String>() {
            @Override
            public void onError(String error) {
                Toast.makeText(mCurrentActivity.get(), error, Toast.LENGTH_SHORT)
                     .show();
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(String s) {
                Toast.makeText(mCurrentActivity.get(), "Accept device share success.", Toast.LENGTH_SHORT)
                     .show();
                EventBus.getDefault().post(new SubscribeChangedEvent());
            }
        });
    }

    private void denyDeviceShare(@NonNull EventNotifyHelper.DeviceShareNotify notify) {
        if (mCurrentActivity.get() == null) {
            return;
        }
        XlinkCloudManager.getInstance().denyShareDevice(notify, new XlinkTaskCallback<String>() {
            @Override
            public void onError(String error) {
                Toast.makeText(mCurrentActivity.get(), error, Toast.LENGTH_SHORT)
                     .show();
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(String s) {
                Toast.makeText(mCurrentActivity.get(), "Deny device share success.", Toast.LENGTH_SHORT)
                     .show();
            }
        });
    }

    @RequiresApi (api = Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
    }

    private void showDeviceShareAccpetMessage(int fromId, @NonNull EventNotifyHelper.DeviceShareNotify notify) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mCurrentActivity.get(), "share");
        Notification notification = builder.setContentTitle("Accpet Device Share")
                                           .setContentText("User(" + fromId + ") accept the device(" + notify.device_id+ ") your share.")
                                           .setWhen(System.currentTimeMillis())
                                           .setSmallIcon(R.drawable.ic_device_default_black_64dp)
                                           .setAutoCancel(true)
                                           .build();
        manager.notify(1, notification);
    }

    private void showDeviceShareDenyMessage(int fromId, @NonNull EventNotifyHelper.DeviceShareNotify notify) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "share");
        Notification notification = builder.setContentTitle("Deny Device Share")
                                           .setContentText("User(" + fromId + ") deny the device(" + notify.device_id+ ") your share.")
                                           .setWhen(System.currentTimeMillis())
                                           .setSmallIcon(R.drawable.ic_device_default_black_64dp)
                                           .setAutoCancel(true)
                                           .build();
        manager.notify(2, notification);
    }
}
