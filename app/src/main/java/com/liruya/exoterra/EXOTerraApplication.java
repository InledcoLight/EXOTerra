package com.liruya.exoterra;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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

import com.liruya.exoterra.base.BaseActivity;
import com.liruya.exoterra.bean.Device;
import com.liruya.exoterra.event.DatapointChangedEvent;
import com.liruya.exoterra.event.DeviceStateChangedEvent;
import com.liruya.exoterra.event.HomeChangedEvent;
import com.liruya.exoterra.event.HomeDeviceChangedEvent;
import com.liruya.exoterra.event.SubscribeChangedEvent;
import com.liruya.exoterra.manager.DeviceManager;
import com.liruya.exoterra.manager.UserManager;
import com.liruya.exoterra.splash.SplashActivity;
import com.liruya.exoterra.util.LogUtil;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkConstants;
import com.liruya.exoterra.xlink.XlinkRequestCallback;
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
            createNotificationChannel("share_receipt", "Share_Receipt", NotificationManager.IMPORTANCE_HIGH);
            createNotificationChannel("invite", "Invite", NotificationManager.IMPORTANCE_HIGH);
            createNotificationChannel("home_member_changed", "Home Member Changed", NotificationManager.IMPORTANCE_HIGH);
            createNotificationChannel("delete_home", "Delete Home", NotificationManager.IMPORTANCE_HIGH);
            createNotificationChannel("alarm", "Alarm", NotificationManager.IMPORTANCE_HIGH);
        }
    }

    private void init() {
        mXlinkDataListener = new XLinkDataListener() {
            @Override
            public void onDataPointUpdate(XDevice xDevice, List<XLinkDataPoint> list) {
                Device device = DeviceManager.getInstance().getDevice(xDevice);
                if (device != null) {
                    device.setDataPointList(list);
                    EventBus.getDefault().post(new DatapointChangedEvent(device.getDeviceTag()));
                }
            }
        };
        mXlinkUserListener = new XLinkUserListener() {
            @Override
            public void onUserLogout(LogoutReason logoutReason) {
                String rsn = null;
                switch (logoutReason) {
                    case USER_LOGOUT:
                        rsn = "User logout";
                        UserManager.clear(EXOTerraApplication.this);
                        DeviceManager.getInstance().clear();
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
            }
        };
        mXlinkCloudListener = new XLinkCloudListener() {
            @Override
            public void onCloudStateChanged(CloudConnectionState cloudConnectionState) {

            }

            @Override
            public void onEventNotify(EventNotify notify) {
                LogUtil.e(TAG, "onEventNotify: " + notify.toString());
                switch (notify.messageType) {
                    case EventNotify.MSG_TYPE_DEVICE_SHARE:
                        final EventNotifyHelper.DeviceShareNotify deviceShareNotify = EventNotifyHelper.parseDeviceShareNotify(notify.payload);
                        handleDeviceShareNotify(notify.fromId, deviceShareNotify);
                        break;
                    case EventNotify.MSG_TYPE_SUBSCRIPTION_CHANGE:
                        EventBus.getDefault().post(new SubscribeChangedEvent());
                        break;
                    case EventNotify.MSG_TYPE_DEVICE_PROP_CHANGE:
                        break;
                    case EventNotify.MSG_TYPE_ONLINE_STATE_CHANGE:
                        break;
                    case EventNotify.MSG_TYPE_HOME_INVITE:
                        final EventNotifyHelper.HomeMemberInvitedNotify homeNotify = EventNotifyHelper.parseNotifyEntityFromJson(notify.payload,
                                                                                                                                 EventNotifyHelper.HomeMemberInvitedNotify.class);
                        handleHomeShareNotify(homeNotify);
                        break;
                    case EventNotify.MSG_TYPE_HOME_MESSAGE_NOTIFY:
                        final EventNotifyHelper.HomeMessageNotify homeMsgNotify = EventNotifyHelper.parseNotifyEntityFromJson(notify.payload,
                                                                                                                              EventNotifyHelper.HomeMessageNotify.class);
                        handleHomeMessageNotify(homeMsgNotify);
                        break;
                    case EventNotify.MSG_TYPE_HOME_MEMBER_CHANGED:
                        final EventNotifyHelper.HomeMemberChangedNotify homeMemberChangedNotify = EventNotifyHelper.parseNotifyEntityFromJson(notify.payload,
                                                                                                                                              EventNotifyHelper.HomeMemberChangedNotify.class);
                        handleHomeMemberChangedNotify(homeMemberChangedNotify);
                        break;
                    case EventNotify.MSG_TYPE_HOME_DEVICE_CHANGED:
                        EventBus.getDefault().post(new HomeDeviceChangedEvent());
                        break;
                    case EventNotify.MSG_TYPE_DATA_POINT_CHANGED:
                        break;
                }
            }
        };
        mXlinkDeviceStateListener = new XLinkDeviceStateListener() {
            @Override
            public void onDeviceStateChanged(XDevice xDevice, XDevice.State state) {
                Device device = DeviceManager.getInstance().getDevice(xDevice);
                if (device != null) {
                    DeviceManager.getInstance().updateDevice(xDevice);
                    EventBus.getDefault().post(new DeviceStateChangedEvent(device.getDeviceTag(), state));
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
            public void onStart() {

            }

            @Override
            public void onError(String error) {
                Toast.makeText(mCurrentActivity.get(), error, Toast.LENGTH_SHORT)
                     .show();
            }

            @Override
            public void onSuccess(String s) {
                Toast.makeText(mCurrentActivity.get(), "Join home success.", Toast.LENGTH_SHORT)
                     .show();
                EventBus.getDefault().post(new HomeChangedEvent());
            }
        });
    }

    private void denyHomeInvite(@NonNull final String homeid, @NonNull final String inviteid) {
        if (mCurrentActivity == null || mCurrentActivity.get() == null) {
            return;
        }
        XlinkCloudManager.getInstance().denyHomeInvite(homeid, inviteid, new XlinkRequestCallback<String>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onError(String error) {
                Toast.makeText(mCurrentActivity.get(), error, Toast.LENGTH_SHORT)
                     .show();
            }

            @Override
            public void onSuccess(String s) {
                Toast.makeText(mCurrentActivity.get(), "Deny home share success.", Toast.LENGTH_SHORT)
                     .show();
                EventBus.getDefault().post(new HomeChangedEvent());
            }
        });
    }

    private void handleHomeMessageNotify(@NonNull final EventNotifyHelper.HomeMessageNotify notify) {
        Log.e(TAG, "handleHomeMessageNotify: " + notify.toString());
        if (notify.type.equalsIgnoreCase(EventNotifyHelper.HomeMessageNotify.TYPE_DELETE)) {
            showDeleteHomeMessage(notify);
            EventBus.getDefault().post(new HomeChangedEvent());
        }
    }

    private void handleHomeMemberChangedNotify(@NonNull final EventNotifyHelper.HomeMemberChangedNotify notify) {
        Log.e(TAG, "handleHomeMemberChangedNotify: " + notify.toString());
        if (notify.type.equalsIgnoreCase(EventNotifyHelper.HomeMemberChangedNotify.TYPE_ADD)) {
            showJoinHomeMessage(notify);
        } else if (notify.type.equalsIgnoreCase(EventNotifyHelper.HomeMemberChangedNotify.TYPE_REMOVE)) {
            showLeaveHomeMessage(notify);
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
//        Notification notification = builder.setContentTitle("Accpet Home Invite")
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
//        Notification notification = builder.setContentTitle("Deny Home Invite")
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
        Notification notification = builder.setContentTitle("Join Home")
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
        Notification notification = builder.setContentTitle("Leave Home")
                                           .setContentText("User " + notify.name + " leave home " + home + ".")
                                           .setWhen(System.currentTimeMillis())
                                           .setSmallIcon(R.drawable.ic_device_default_black_64dp)
                                           .setAutoCancel(true)
                                           .build();
        manager.notify(4, notification);
    }

    private void showDeleteHomeMessage(@NonNull final EventNotifyHelper.HomeMessageNotify notify) {
        String home = TextUtils.isEmpty(notify.home_name) ? notify.home_id : notify.home_name;
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "delete_home");
        Notification notification = builder.setContentTitle("Delete Home")
                                           .setContentText("Home " + home + " was deleted.")
                                           .setWhen(System.currentTimeMillis())
                                           .setSmallIcon(R.drawable.ic_device_default_black_64dp)
                                           .setAutoCancel(true)
                                           .build();
        manager.notify(5, notification);
    }

    private void relogin() {
        Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
