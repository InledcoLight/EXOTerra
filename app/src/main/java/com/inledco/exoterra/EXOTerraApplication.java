package com.inledco.exoterra;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.inledco.exoterra.base.BaseActivity;
import com.inledco.exoterra.bean.Device;
import com.inledco.exoterra.device.DeviceActivity;
import com.inledco.exoterra.event.DatapointChangedEvent;
import com.inledco.exoterra.event.DeviceStateChangedEvent;
import com.inledco.exoterra.event.HomeMemberChangedEvent;
import com.inledco.exoterra.event.SubscribeChangedEvent;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.manager.HomeManager;
import com.inledco.exoterra.manager.UserManager;
import com.inledco.exoterra.splash.SplashActivity;
import com.inledco.exoterra.util.DeviceUtil;
import com.inledco.exoterra.util.LogUtil;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkConstants;
import com.inledco.exoterra.xlink.XlinkDataPointAlertNotify;
import com.inledco.exoterra.xlink.XlinkRequestCallback;
import com.inledco.exoterra.xlink.XlinkTaskCallback;

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

    private NotificationManager mNotificationManager;

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
            createNotificationChannel(getString(R.string.notify_chnid_share), getString(R.string.notify_chn_share), NotificationManager.IMPORTANCE_HIGH);
            createNotificationChannel(getString(R.string.notify_chnid_share_receipt), getString(R.string.notify_chn_share_receipt), NotificationManager.IMPORTANCE_HIGH);
            createNotificationChannel(getString(R.string.notify_chnid_invite), getString(R.string.notify_chn_invite), NotificationManager.IMPORTANCE_HIGH);
            createNotificationChannel(getString(R.string.notify_chnid_home_member_changed), getString(R.string.notify_chn_home_member_changed), NotificationManager.IMPORTANCE_HIGH);
            createNotificationChannel(getString(R.string.notify_chnid_delete_home), getString(R.string.notify_chn_delete_home), NotificationManager.IMPORTANCE_HIGH);
            createNotificationChannel(getString(R.string.notify_chnid_alarm), getString(R.string.notify_chn_alarm), NotificationManager.IMPORTANCE_HIGH);
        }

        //alipush
//        PushServiceFactory.initLineChart(this);
//        final CloudPushService pushService = PushServiceFactory.getCloudPushService();
//        pushService.register(this, new CommonCallback() {
//            @Override
//            public void onSuccess(String s) {
//                Log.e(TAG, "onSuccess: registerAlipush - " + s);
//            }
//
//            @Override
//            public void onFailed(String s, String s1) {
//                Log.e(TAG, "onFailed: registerAlipush - " + s + " " + s1);
//            }
//        });
//        GcmRegister.register(this, AppConstants.ALIPUSH_SENDID, AppConstants.ALIPUSH_APPID);
    }

    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    private void init() {
        mXlinkDataListener = new XLinkDataListener() {
            @Override
            public void onDataPointUpdate(XDevice xDevice, List<XLinkDataPoint> list) {
                if (list == null || list.size() == 0) {
                    return;
                }
                Device device = DeviceManager.getInstance().getDevice(xDevice);
                if (device != null) {
                    for (XLinkDataPoint dp : list) {
                        device.setDataPoint(dp);
                    }
                    EventBus.getDefault().post(new DatapointChangedEvent(device.getDeviceTag()));
                }
            }
        };
        mXlinkUserListener = new XLinkUserListener() {
            @Override
            public void onUserLogout(LogoutReason logoutReason) {
                int usrid = UserManager.getUserId(EXOTerraApplication.this);
                Log.e(TAG, "onUserLogout: " + usrid);
                XlinkCloudManager.getInstance().unregisterFCMMessageService(usrid, new XlinkRequestCallback<String>() {
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "onError: " + error);
                    }

                    @Override
                    public void onSuccess(String s) {
                        Log.e(TAG, "onSuccess: " + s);
                    }
                });
                String rsn = null;
                switch (logoutReason) {
                    case USER_LOGOUT:
                        rsn = "User logout";
                        UserManager.clear(EXOTerraApplication.this);
                        break;
                    case SINGLE_SIGN_KICK_OFF:
                        rsn = "Kick off user";
                        UserManager.removeUserId(EXOTerraApplication.this);
                        relogin();
                        break;
                    case TOKEN_EXPIRED:
                        rsn = "Token expired";
                        UserManager.removeRefreshToken(EXOTerraApplication.this);
                        relogin();
                        break;
                }
                final String reason = rsn;
                mCurrentActivity.get().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mCurrentActivity.get(), reason, Toast.LENGTH_SHORT)
                             .show();
                    }
                });
                DeviceManager.getInstance().clear();
            }
        };
        mXlinkCloudListener = new XLinkCloudListener() {
            @Override
            public void onCloudStateChanged(CloudConnectionState cloudConnectionState) {

            }

            @Override
            public void onEventNotify(EventNotify notify) {
                LogUtil.e(TAG, "onEventNotify: " + notify.toString());
                int devid = notify.fromId;
                switch (notify.messageType) {
                    case EventNotify.MSG_TYPE_DATA_POINT_CHANGED:       //1
                        final EventNotifyHelper.DataPointChangedNotify dataPointChangedNotify = EventNotifyHelper.parseDataPointChangedNotify(notify.payload);
                        Log.e(TAG, "onEventNotify: dataPointChanged - " + dataPointChangedNotify.toString());
                        break;
                    case EventNotify.MSG_TYPE_DATA_POINT_ALERT:         //2
//                        short var2 = ByteUtil.byteToShort(notify.payload);
//                        String var3 = StringUtil.getStringEmptyDefault(notify.payload, 2, var2);
//                        Log.e(TAG, "onEventNotify: " + var3);
                        // 自定义数据端点报警通知 仅支持单个数据端点
                        XlinkDataPointAlertNotify dataPointAlertNotify = EventNotifyHelper.parseNotifyEntityFromJson(notify.payload, XlinkDataPointAlertNotify.class);
                        //  xlink sdk 自带解码数据端点报警通知 功能异常  (控制台可设置多个数据端点共同触发报警)
//                                                final EventNotifyHelper.DataPointAlertNotify dataPointAlertNotify = EventNotifyHelper.parseDataPointAlertNotify(notify.payload);
                        Log.e(TAG, "onEventNotify: datapointAlert - " + dataPointAlertNotify.toString());
                        showAlarmNotification(devid, dataPointAlertNotify);
                        break;
                    case EventNotify.MSG_TYPE_DEVICE_SHARE:             //3
                        final EventNotifyHelper.DeviceShareNotify deviceShareNotify = EventNotifyHelper.parseDeviceShareNotify(notify.payload);
                        Log.e(TAG, "onEventNotify: deviceShare - " + deviceShareNotify.toString());
                        handleDeviceShareNotify(notify.fromId, deviceShareNotify);
                        break;
                    case EventNotify.MSG_TYPE_PUSH_MSG:                 //4
                        final EventNotifyHelper.PushMsgNotify pushMsgNotify = EventNotifyHelper.parsePushMsgNotify(notify.payload);
                        Log.e(TAG, "onEventNotify: pushMsg - " + pushMsgNotify.toString());
                        break;
                    case EventNotify.MSG_TYPE_DEVICE_PROP_CHANGE:       //5
                        final EventNotifyHelper.DevicePropChangeNotify devicePropChangeNotify = EventNotifyHelper.parseDevicePropChangeNotify(notify.payload);
                        Log.e(TAG, "onEventNotify: devicePropChanged - " + devicePropChangeNotify.toString());
//                        EventBus.getDefault().post(new DevicePropertyChangedEvent(devicePropChangeNotify.));
                        break;
                    case EventNotify.MSG_TYPE_SUBSCRIPTION_CHANGE:      //6
                        final EventNotifyHelper.SubscriptionChangeNotify subscriptionChangeNotify = EventNotifyHelper.parseSubscriptionChangeNotify(notify.payload);
                        Log.e(TAG, "onEventNotify: subscribeChanged - " + subscriptionChangeNotify.toString());
                        DeviceManager.getInstance().syncSubcribeDevices(null);
//                        EventBus.getDefault().post(new SubscribeChangedEvent());
                        break;
                    case EventNotify.MSG_TYPE_ONLINE_STATE_CHANGE:      //7
                        final EventNotifyHelper.OnlineStateChangeNotify onlineStateChangeNotify = EventNotifyHelper.parseOnlineStateChangeNotify(notify.payload);
                        Log.e(TAG, "onEventNotify: onlineStateChange - " + onlineStateChangeNotify.toString());
                        break;
                    case EventNotify.MSG_TYPE_ONLINE_STATE_ALERT:       //8
                        final EventNotifyHelper.OnlineStateAlertNotify onlineStateAlertNotify = EventNotifyHelper.parseOnlineStateAlertNotify(notify.payload);
                        Log.e(TAG, "onEventNotify: onlineStateAlert - " + onlineStateAlertNotify.toString());
                        showOnlineStateAlarmNotification(onlineStateAlertNotify);
                        break;
                    case EventNotify.MSG_TYPE_HOME_MESSAGE_NOTIFY:      //9
                        final EventNotifyHelper.HomeMessageNotify homeMsgNotify = EventNotifyHelper.parseNotifyEntityFromJson(notify.payload,
                                                                                                                              EventNotifyHelper.HomeMessageNotify.class);
                        Log.e(TAG, "onEventNotify: homeMsg - " + homeMsgNotify.toString());
                        handleHomeMessageNotify(homeMsgNotify);
                        break;
                    case EventNotify.MSG_TYPE_HOME_INVITE:              //10
                        final EventNotifyHelper.HomeMemberInvitedNotify homeNotify = EventNotifyHelper.parseNotifyEntityFromJson(notify.payload,
                                                                                                                                 EventNotifyHelper.HomeMemberInvitedNotify.class);
                        Log.e(TAG, "onEventNotify: homeInvite - " + homeNotify.toString());
                        handleHomeShareNotify(homeNotify);
                        break;
                    case EventNotify.MSG_TYPE_HOME_DEVICE_PERMISSION_CHANGED:   //11
                        final EventNotifyHelper.HomeDevicePermissionChangedNotify permissionChangedNotify = EventNotifyHelper.parseNotifyEntityFromJson(notify.payload,
                                                                                                                                                        EventNotifyHelper.HomeDevicePermissionChangedNotify.class);
                        Log.e(TAG, "onEventNotify: devicePermissionChanged - " + permissionChangedNotify.toString());
                        break;
                    case EventNotify.MSG_TYPE_HOME_MEMBER_CHANGED:      //12
                        final EventNotifyHelper.HomeMemberChangedNotify homeMemberChangedNotify = EventNotifyHelper.parseNotifyEntityFromJson(notify.payload,
                                                                                                                                              EventNotifyHelper.HomeMemberChangedNotify.class);
                        Log.e(TAG, "onEventNotify: homeMemberChanged - " + homeMemberChangedNotify.toString());
                        handleHomeMemberChangedNotify(homeMemberChangedNotify);
                        break;
                    case EventNotify.MSG_TYPE_HOME_DEVICE_CHANGED:      //13
                        final EventNotifyHelper.HomeDeviceChangedNotify homeDeviceChangedNotify = EventNotifyHelper.parseNotifyEntityFromJson(notify.payload,
                                                                                                                                              EventNotifyHelper.HomeDeviceChangedNotify.class);
                        Log.e(TAG, "onEventNotify: homeDeviceChanged - " + homeDeviceChangedNotify.toString());
                        HomeManager.getInstance().refreshHomeDevices(homeDeviceChangedNotify.home_id);
                        break;
                }
            }
        };
        mXlinkDeviceStateListener = new XLinkDeviceStateListener() {
            @Override
            public void onDeviceStateChanged(XDevice xDevice, XDevice.State state) {
                Log.e(TAG, "onDeviceStateChanged: " + state);
                Device device = DeviceManager.getInstance().getDevice(xDevice);
                if (device != null) {
                    device.setXDevice(xDevice);
                    EventBus.getDefault().post(new DeviceStateChangedEvent(device.getDeviceTag(), state));
                    DeviceManager.getInstance().refreshDevice(device);
                }
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
                                        .setLogConfig(XLinkAndroidSDK.defaultLogConfig(this).setDebugLevel(Loggable.DEBUG).setEnableLogFile(true))
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
                showReceiveDeviceShareDialog(fromId, notify);
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

    private void showReceiveDeviceShareDialog(int fromId, @NonNull final EventNotifyHelper.DeviceShareNotify notify) {
        if (mCurrentActivity == null || mCurrentActivity.get() == null) {
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
               .setCancelable(false)
               .show();
    }

    private void acceptDeviceShare(@NonNull EventNotifyHelper.DeviceShareNotify notify) {
        if (mCurrentActivity == null || mCurrentActivity.get() == null) {
            return;
        }
        XlinkCloudManager.getInstance().acceptShareDevice(notify.invite_code, new XlinkTaskCallback<String>() {
            @Override
            public void onError(String error) {
                Toast.makeText(mCurrentActivity.get(), error, Toast.LENGTH_SHORT)
                     .show();
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
        if (mCurrentActivity == null || mCurrentActivity.get() == null) {
            return;
        }
        XlinkCloudManager.getInstance().denyShareDevice(notify.invite_code, new XlinkTaskCallback<String>() {
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

    private void handleHomeShareNotify(@NonNull final EventNotifyHelper.HomeMemberInvitedNotify notify) {
        Log.e(TAG, "handleHomeShareNotify: " + notify.toString());
        if (notify.opt.equalsIgnoreCase(EventNotifyHelper.HomeMemberInvitedNotify.OPERATION_INVITED)) {
            showReceiveHomeShareDialog(notify);
        }
//        else if (notify.opt.equalsIgnoreCase(EventNotifyHelper.HomeMemberInvitedNotify.OPERATION_DENY)) {
//            showHomeInviteDenyMessage(notify);
//        } else if (notify.opt.equalsIgnoreCase(EventNotifyHelper.HomeMemberInvitedNotify.OPERATION_ACCEPT)) {
//            showHomeInviteAccpetMessage(notify);
//        }
    }

    private void showReceiveHomeShareDialog(@NonNull final EventNotifyHelper.HomeMemberInvitedNotify notify) {
        if (mCurrentActivity == null || mCurrentActivity.get() == null) {
            return;
        }
        String from = TextUtils.isEmpty(notify.from_name) ? String.valueOf(notify.from_id) : notify.from_name;
        String home = TextUtils.isEmpty(notify.home_name) ? notify.home_id : notify.home_name;
        final String homeid = notify.home_id;
        final String inviteid = notify.invite_id;
        AlertDialog.Builder builder = new AlertDialog.Builder(mCurrentActivity.get());
        builder.setTitle(R.string.receive_home_share)
               .setMessage("User " + from + " invite you join home " + home + ".")
               .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       acceptHomeInvite(homeid, inviteid);
                   }
               })
               .setNegativeButton(R.string.deny, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       denyHomeInvite(homeid, inviteid);
                   }
               })
               .setNeutralButton(R.string.later, null)
               .setCancelable(false)
               .show();
    }

    private void acceptHomeInvite(@NonNull final String homeid, @NonNull final String inviteid) {
        if (mCurrentActivity == null || mCurrentActivity.get() == null) {
            return;
        }
        XlinkCloudManager.getInstance().acceptHomeInvite(homeid, inviteid, new XlinkRequestCallback<String>() {
            @Override
            public void onError(String error) {
                Toast.makeText(mCurrentActivity.get(), error, Toast.LENGTH_SHORT)
                     .show();
            }

            @Override
            public void onSuccess(String s) {
                Toast.makeText(mCurrentActivity.get(), "Join home success.", Toast.LENGTH_SHORT)
                     .show();
//                EventBus.getDefault().post(new HomeChangedEvent());
                HomeManager.getInstance().refreshHomeList(null);
            }
        });
    }

    private void denyHomeInvite(@NonNull final String homeid, @NonNull final String inviteid) {
        if (mCurrentActivity == null || mCurrentActivity.get() == null) {
            return;
        }
        XlinkCloudManager.getInstance().denyHomeInvite(homeid, inviteid, new XlinkRequestCallback<String>() {
            @Override
            public void onError(String error) {
                Toast.makeText(mCurrentActivity.get(), error, Toast.LENGTH_SHORT)
                     .show();
            }

            @Override
            public void onSuccess(String s) {
                Toast.makeText(mCurrentActivity.get(), "Deny home share success.", Toast.LENGTH_SHORT)
                     .show();
//                EventBus.getDefault().post(new HomeChangedEvent());
                HomeManager.getInstance().refreshHomeList(null);
            }
        });
    }

    private void handleHomeMessageNotify(@NonNull final EventNotifyHelper.HomeMessageNotify notify) {
        Log.e(TAG, "handleHomeMessageNotify: " + notify.toString());
        if (notify.type.equalsIgnoreCase(EventNotifyHelper.HomeMessageNotify.TYPE_DELETE)) {
            showDeleteHomeMessage(notify);
//            EventBus.getDefault().post(new HomeChangedEvent());
            HomeManager.getInstance().refreshHomeList(null);
        }
    }

    private void handleHomeMemberChangedNotify(@NonNull final EventNotifyHelper.HomeMemberChangedNotify notify) {
        Log.e(TAG, "handleHomeMemberChangedNotify: " + notify.toString());
        if (notify.type.equalsIgnoreCase(EventNotifyHelper.HomeMemberChangedNotify.TYPE_ADD)) {
            showJoinHomeMessage(notify);
            EventBus.getDefault().post(new HomeMemberChangedEvent(notify.home_id));
        } else if (notify.type.equalsIgnoreCase(EventNotifyHelper.HomeMemberChangedNotify.TYPE_REMOVE)) {
            showLeaveHomeMessage(notify);
            EventBus.getDefault().post(new HomeMemberChangedEvent(notify.home_id));
//            EventBus.getDefault().post(new HomeChangedEvent());
            HomeManager.getInstance().refreshHomeList(null);
        }
    }

    @RequiresApi (api = Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
    }

    private void showDeviceShareAccpetMessage(int fromId, @NonNull EventNotifyHelper.DeviceShareNotify notify) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mCurrentActivity.get(), "share_receipt");
        Notification notification = builder.setContentTitle("Accpet Device Share")
                                           .setContentText("User(" + fromId + ") accept the device(" + notify.device_id + ") you share.")
                                           .setWhen(System.currentTimeMillis())
                                           .setSmallIcon(R.drawable.ic_device_default_black_64dp)
                                           .setAutoCancel(true)
                                           .build();
        manager.notify(1, notification);
    }

    private void showDeviceShareDenyMessage(int fromId, @NonNull EventNotifyHelper.DeviceShareNotify notify) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "share_receipt");
        Notification notification = builder.setContentTitle("Deny Device Share")
                                           .setContentText("User(" + fromId + ") deny the device(" + notify.device_id + ") you share.")
                                           .setWhen(System.currentTimeMillis())
                                           .setSmallIcon(R.drawable.ic_device_default_black_64dp)
                                           .setAutoCancel(true)
                                           .build();
        manager.notify(2, notification);
    }

//    private void showHomeInviteAccpetMessage(@NonNull final EventNotifyHelper.HomeMemberInvitedNotify notify) {
//        String from = TextUtils.isEmpty(notify.from_name) ? String.valueOf(notify.from_id) : notify.from_name;
//        String home = TextUtils.isEmpty(notify.home_name) ? notify.home_id : notify.home_name;
//        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(mCurrentActivity.get(), "invite_receipt");
//        Notification notification = builder.setContentTitle("Accpet Home2 Invite")
//                                           .setContentText("User " + from + " join home " + home + " you invite.")
//                                           .setWhen(System.currentTimeMillis())
//                                           .setSmallIcon(R.drawable.ic_device_default_black_64dp)
//                                           .setAutoCancel(true)
//                                           .build();
//        manager.notify(3, notification);
//    }
//
//    private void showHomeInviteDenyMessage(@NonNull final EventNotifyHelper.HomeMemberInvitedNotify notify) {
//        String from = TextUtils.isEmpty(notify.from_name) ? String.valueOf(notify.from_id) : notify.from_name;
//        String home = TextUtils.isEmpty(notify.home_name) ? notify.home_id : notify.home_name;
//        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "invite_receipt");
//        Notification notification = builder.setContentTitle("Deny Home2 Invite")
//                                           .setContentText("User " + from + " deny home " + home + " you invite.")
//                                           .setWhen(System.currentTimeMillis())
//                                           .setSmallIcon(R.drawable.ic_device_default_black_64dp)
//                                           .setAutoCancel(true)
//                                           .build();
//        manager.notify(4, notification);
//    }

    private void showJoinHomeMessage(@NonNull final EventNotifyHelper.HomeMemberChangedNotify notify) {
        String home = TextUtils.isEmpty(notify.home_name) ? notify.home_id : notify.home_name;
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mCurrentActivity.get(), "home_member_changed");
        Notification notification = builder.setContentTitle("Join Habitat")
                                           .setContentText("User " + notify.name + " join home " + home + ".")
                                           .setWhen(System.currentTimeMillis())
                                           .setSmallIcon(R.drawable.ic_device_default_black_64dp)
                                           .setAutoCancel(true)
                                           .build();
        manager.notify(3, notification);
    }

    private void showLeaveHomeMessage(@NonNull final EventNotifyHelper.HomeMemberChangedNotify notify) {
        String home = TextUtils.isEmpty(notify.home_name) ? notify.home_id : notify.home_name;
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "home_member_changed");
        Notification notification = builder.setContentTitle("Leave Habitat")
                                           .setContentText("User " + notify.name + " leave home " + home + ".")
                                           .setWhen(System.currentTimeMillis())
                                           .setSmallIcon(R.drawable.ic_device_default_black_64dp)
                                           .setAutoCancel(true)
                                           .build();
        manager.notify(4, notification);
    }

    private void showDeleteHomeMessage(@NonNull final EventNotifyHelper.HomeMessageNotify notify) {
        if (getNotificationManager() != null) {
            String home = TextUtils.isEmpty(notify.home_name) ? notify.home_id : notify.home_name;
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.notify_chnid_delete_home));
            Notification notification = builder.setContentTitle("Delete Habitat")
                                               .setContentText("Habitat " + home + " was deleted.")
                                               .setWhen(System.currentTimeMillis())
                                               .setSmallIcon(R.drawable.ic_device_default_black_64dp)
                                               .setAutoCancel(true)
                                               .build();
            getNotificationManager().notify(5, notification);
        }
    }

    private void showOnlineStateAlarmNotification(EventNotifyHelper.OnlineStateAlertNotify notify) {
        if (getNotificationManager() != null) {
            Device device = DeviceManager.getInstance().getDeviceByDevid(notify.device_id);
            int icon = R.drawable.ic_device_default_white_64dp;
            if (device != null) {
                icon = DeviceUtil.getProductIcon(device.getXDevice().getProductId());
            }
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.notify_chnid_alarm));
            Notification notification = builder.setContentTitle(getString(R.string.notify_chn_alarm))
                                               .setContentText(notify.msg)
                                               .setStyle(new NotificationCompat.BigTextStyle().bigText(notify.msg))
                                               .setWhen(System.currentTimeMillis())
                                               .setSmallIcon(icon)
                                               .setAutoCancel(true)
                                               .build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            getNotificationManager().notify(notify.device_id, notification);
        }
    }

    private void showAlarmNotification(final int devid, XlinkDataPointAlertNotify notify) {
        if (getNotificationManager() != null) {
            Device device = DeviceManager.getInstance().getDeviceByDevid(devid);
            int icon = R.drawable.ic_device_default_white_64dp;
            if (device != null) {
                icon = DeviceUtil.getProductIcon(device.getXDevice().getProductId());
            }
            Intent intent = new Intent(this, DeviceActivity.class);
            intent.putExtra("device_tag", device.getDeviceTag());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, notify.getIndex(), intent, PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.notify_chnid_alarm));
            Notification notification = builder.setContentTitle(getString(R.string.notify_chn_alarm))
                                               .setContentText(notify.getMsg())
                                               .setStyle(new NotificationCompat.BigTextStyle().bigText(notify.getMsg()))
                                               .setWhen(System.currentTimeMillis())
                                               .setSmallIcon(icon)
                                               .setAutoCancel(true)
                                               .setContentIntent(pendingIntent)
                                               .build();
            getNotificationManager().notify(""+devid, notify.getIndex(), notification);
        }
    }

    private void relogin() {
        Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
