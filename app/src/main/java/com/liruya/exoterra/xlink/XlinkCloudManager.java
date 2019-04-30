package com.liruya.exoterra.xlink;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.xlink.restful.XLinkCallback;
import cn.xlink.restful.XLinkRestful;
import cn.xlink.restful.XLinkRestfulEnum;
import cn.xlink.restful.XLinkRestfulError;
import cn.xlink.restful.api.app.DeviceApi;
import cn.xlink.restful.api.app.UserApi;
import cn.xlink.restful.api.app.UserAuthApi;
import cn.xlink.sdk.common.ByteUtil;
import cn.xlink.sdk.core.constant.CoreConstant;
import cn.xlink.sdk.core.error.XLinkErrorCodeHelper;
import cn.xlink.sdk.core.error.XLinkErrorCodes;
import cn.xlink.sdk.core.java.local.XLinkLocalSendTriggerUpgradeTask;
import cn.xlink.sdk.core.model.XLinkDataPoint;
import cn.xlink.sdk.task.TaskListener;
import cn.xlink.sdk.v5.listener.XLinkScanDeviceListener;
import cn.xlink.sdk.v5.listener.XLinkTaskListener;
import cn.xlink.sdk.v5.manager.XLinkUserManager;
import cn.xlink.sdk.v5.model.XDevice;
import cn.xlink.sdk.v5.module.connection.XLinkScanDeviceTask;
import cn.xlink.sdk.v5.module.datapoint.XLinkGetDataPointMetaInfoTask;
import cn.xlink.sdk.v5.module.datapoint.XLinkGetDataPointTask;
import cn.xlink.sdk.v5.module.datapoint.XLinkProbeDataPointTask;
import cn.xlink.sdk.v5.module.datapoint.XLinkSetDataPointTask;
import cn.xlink.sdk.v5.module.http.XLinkGetDataPointTemplateTask;
import cn.xlink.sdk.v5.module.http.XLinkRefreshTokenTask;
import cn.xlink.sdk.v5.module.http.XLinkRemoveDeviceTask;
import cn.xlink.sdk.v5.module.http.XLinkSyncDeviceListTask;
import cn.xlink.sdk.v5.module.http.XLinkUserAuthorizeTask;
import cn.xlink.sdk.v5.module.main.XLinkSDK;
import cn.xlink.sdk.v5.module.notify.EventNotifyHelper;
import cn.xlink.sdk.v5.module.share.XLinkHandleShareDeviceTask;
import cn.xlink.sdk.v5.module.share.XLinkShareDeviceTask;
import cn.xlink.sdk.v5.module.subscription.XLinkAddDeviceTask;
import retrofit2.Call;

public class XlinkCloudManager {
    private static final String TAG = "XlinkCloudManager";

    private String mCorpId;

    private XlinkCloudManager() {

    }

    public static XlinkCloudManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void init(@NonNull String corp_id) {
        mCorpId = corp_id;
    }

    public void login(String email, String password, int timeout, XLinkTaskListener<UserAuthApi.UserAuthResponse> listener) {
        XLinkUserAuthorizeTask task = XLinkUserAuthorizeTask.newBuilder()
                                                            .setCorpId(mCorpId)
                                                            .setEmail(email, password)
                                                            .setTimeout(timeout)
                                                            .setListener(listener)
                                                            .build();
        XLinkSDK.startTask(task);
    }

    public void register(final String email, String nickname, String password, final XlinkRequestCallback<UserAuthApi.EmailRegisterResponse> callback) {
        UserAuthApi.EmailRegisterRequest request = new UserAuthApi.EmailRegisterRequest();
        request.corpId = mCorpId;
        request.email = email;
        request.nickname = nickname;
        request.password = password;
        request.localLang = XLinkRestfulEnum.LocalLang.EN_US;
        request.source = XLinkRestfulEnum.UserSource.ANDROID;
        final Call<UserAuthApi.EmailRegisterResponse> call = XLinkRestful.getApplicationApi()
                                                                         .registEmailAccount(request);
        call.enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void refreshToken(int userid, String authorize, String refresh_token, XLinkTaskListener<UserApi.TokenRefreshResponse> listener) {
        XLinkRefreshTokenTask task = XLinkRefreshTokenTask.newBuilder()
                                                          .setUserId(userid)
                                                          .setAuthString(authorize)
                                                          .setRefreshToken(refresh_token)
                                                          .setListener(listener)
                                                          .build();
        XLinkSDK.startTask(task);
    }

    public void modifyPassword(String oldPassword, String newPassword, final XlinkRequestCallback<String> callback) {
        UserApi.PasswordResetRequest request = new UserApi.PasswordResetRequest();
        request.oldPassword = oldPassword;
        request.newPassword = newPassword;
        Call<String> call = XLinkRestful.getApplicationApi().updatePassword(request);
        call.enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void requestEmailFoundbackPasswordVerifyCode(String email, final XlinkRequestCallback<String> callback) {
        UserAuthApi.EmailPasswordForgotRequest request = new UserAuthApi.EmailPasswordForgotRequest();
        request.corpId = mCorpId;
        request.email = email;
        request.localLang = XLinkRestfulEnum.LocalLang.EN_US;
        Call<String> call = XLinkRestful.getApplicationApi().sendEmailPasswordFound(request);
        call.enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void foundbackPassword(String email, String password, String verifycode, final XlinkRequestCallback<String> callback) {
        UserAuthApi.PasswordFoundBackRequest request = new UserAuthApi.PasswordFoundBackRequest();
        request.corpId = mCorpId;
        request.email = email;
        request.newPassword = password;
        request.verifycode = verifycode;
        Call<String> call = XLinkRestful.getApplicationApi().sendPasswordReset(request);
        call.enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void getUserInfo(final int userid, XlinkRequestCallback<UserApi.UserInfoResponse> callback) {
        Call<UserApi.UserInfoResponse> call = XLinkRestful.getApplicationApi()
                                                              .getUserInfo(userid);
        call.enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void registerDevice(final XDevice device, String name, final IXlinkRegisterDeviceCallback callback) {
        DeviceApi.RegisterDeviceRequest request = new DeviceApi.RegisterDeviceRequest();
        request.productId = device.getProductId();
        request.mac = device.getMacAddress();
        request.firmwareMod = "0";                  //wifi:0  gprs:1, 如果不设置无法手动升级
        if (!TextUtils.isEmpty(name)) {
            request.name = name;
        }
        Call<DeviceApi.RegisterDeviceResponse> call = XLinkRestful.getApplicationApi()
                                                                  .userRegisterDevice(XLinkUserManager.getInstance().getUid(), request);
        call.enqueue(new XLinkCallback<DeviceApi.RegisterDeviceResponse>() {
            @Override
            public void onHttpError(Throwable throwable) {
                if (callback != null) {
                    callback.onError(throwable.getMessage());
                }
            }

            @Override
            public void onApiError(XLinkRestfulError.ErrorWrapper.Error error) {
                if (callback != null) {
                    if (error.code == XLinkErrorCodes.ERROR_API_DEVICE_MAC_ADDRESS_EXISTS) {
                        callback.onDeviceAlreadyExists(device);
                    } else {
                        callback.onError(XLinkErrorCodeHelper.getErrorCodeName(error.code));
                    }
                }
            }

            @Override
            public void onSuccess(DeviceApi.RegisterDeviceResponse response) {
                Log.e(TAG, "onSuccess: " + response.accessKey );
                device.setDeviceId(response.deviceId);
                if (callback != null) {
                    callback.onSuccess(device);
                }
            }
        });
        if (callback != null) {
            callback.onStart();
        }
    }

    public void registerDevice(final XDevice device, final IXlinkRegisterDeviceCallback callback) {
        registerDevice(device, null, callback);
    }

    public void subscribeDevice(final XDevice device, byte[] pincode, int timeout, XLinkTaskListener<XDevice> listener) {
        XLinkAddDeviceTask task = XLinkAddDeviceTask.newBuilder()
                                                    .setXDevice(device)
                                                    .setConnectLocal(false)
                                                    .setNeedSubscription(true)
                                                    .setPinCode(pincode)
                                                    .setTotalTimeout(timeout)
                                                    .setListener(listener)
                                                    .build();
        XLinkSDK.startTask(task);
    }

    public void subscribeDeviceBySn(@NonNull String pid, @NonNull String sn, XlinkRequestCallback<DeviceApi.SnSubscribeResponse> callback) {
        DeviceApi.SnSubscribeRequest request = new DeviceApi.SnSubscribeRequest();
        request.productId = pid;
        request.sn = sn;
        XLinkRestful.getApplicationApi()
                    .snSubscribeDevice(XLinkUserManager.getInstance().getUid(), request)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void subscribeDeviceByQr(@NonNull String code, XlinkRequestCallback<DeviceApi.QRCodeSubscribeResponse> callback) {
        DeviceApi.QRCodeSubscribeRequest request = new DeviceApi.QRCodeSubscribeRequest();
        request.qrCode = new String(Base64.decode(code.getBytes(), Base64.DEFAULT));
        XLinkRestful.getApplicationApi()
                    .qrCodeSubscribeDevice(XLinkUserManager.getInstance().getUid(), request)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void unsubscribeDevice(@NonNull XDevice device, XLinkTaskListener<String> listener) {
        //setUserId??
        XLinkRemoveDeviceTask task = XLinkRemoveDeviceTask.newBuilder()
                                                          .setXDevice(device)
                                                          .setListener(listener)
                                                          .build();
        XLinkSDK.startTask(task);
    }

    public void addLocalDevice(@NonNull final XDevice device, byte[] pincode, int timeout, XLinkTaskListener<XDevice> listener) {
        XLinkAddDeviceTask task = XLinkAddDeviceTask.newBuilder()
                                                    .setXDevice(device)
                                                    .setConnectLocal(false)
                                                    .setNeedSubscription(false)
                                                    .setPinCode(pincode)
                                                    .setTotalTimeout(timeout)
                                                    .setListener(listener)
                                                    .build();
        XLinkSDK.startTask(task);
    }

    public void scanDevice(String pid, int timeout, int retryInterval, XLinkScanDeviceListener listener) {
        XLinkScanDeviceTask task = XLinkScanDeviceTask.newBuilder()
                                                      .setProductIds(pid)
                                                      .setTotalTimeout(timeout)
                                                      .setRetryInterval(retryInterval)
                                                      .setScanDeviceListener(listener)
                                                      .build();
        XLinkSDK.startTask(task);
    }

    public void scanDevice(List<String> pid, int timeout, int retryInterval, XLinkScanDeviceListener listener) {
        XLinkScanDeviceTask task = XLinkScanDeviceTask.newBuilder()
                                                      .setProductIds(pid)
                                                      .setTotalTimeout(timeout)
                                                      .setRetryInterval(retryInterval)
                                                      .setScanDeviceListener(listener)
                                                      .build();
        XLinkSDK.startTask(task);
    }

    public void renameDevice(String pid, int devid, String newName, XlinkRequestCallback<DeviceApi.DeviceResponse> callback) {
        DeviceApi.DeviceRequest request = new DeviceApi.DeviceRequest();
        request.name = newName;
        Call<DeviceApi.DeviceResponse> call = XLinkRestful.getApplicationApi().updateDeviceInfo(pid, devid, request);
        call.enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void syncSubscribedDevices(XLinkTaskListener<List<XDevice>> listener) {
        XLinkSyncDeviceListTask task = XLinkSyncDeviceListTask.newBuilder()
                                                              .setListener(listener)
                                                              .build();
        XLinkSDK.startTask(task);
    }

    public void getDeviceInfo(@NonNull final XDevice xDevice, XlinkRequestCallback<DeviceApi.DeviceResponse> callback) {
        Call<DeviceApi.DeviceResponse> call = XLinkRestful.getApplicationApi()
                                                                .getDeviceInfo(xDevice.getProductId(), xDevice.getDeviceId());
        call.enqueue(callback);
    }

    public void getDeviceMetaDatapoints(@NonNull XDevice device, XLinkTaskListener<List<XLinkDataPoint>> listener) {
        XLinkGetDataPointMetaInfoTask task = XLinkGetDataPointMetaInfoTask.newBuilder()
                                                                          .setProductId(device.getProductId())
                                                                          .setListener(listener)
                                                                          .build();
        XLinkSDK.startTask(task);
    }

    public void getDeviceTemplateDatapoints(@NonNull XDevice device, XLinkTaskListener<List<DeviceApi.DataPointsResponse>> listener) {
        XLinkGetDataPointTemplateTask task = XLinkGetDataPointTemplateTask.newBuilder()
                                                                          .setProductId(device.getProductId())
                                                                          .setListener(listener)
                                                                          .build();
        XLinkSDK.startTask(task);
    }

    public void getDeviceDatapoints(@NonNull XDevice device, XLinkTaskListener<List<XLinkDataPoint>> listener) {
        XLinkGetDataPointTask task = XLinkGetDataPointTask.newBuilder()
                                                          .setXDevice(device)
                                                          .setListener(listener)
                                                          .build();
        XLinkSDK.startTask(task);
    }

    public void setDeviceDatapoints(@NonNull XDevice device, final List<XLinkDataPoint> datapoints, XLinkTaskListener<XDevice> listener) {
        XLinkSetDataPointTask task = XLinkSetDataPointTask.newBuilder()
                                                          .setXDevice(device)
                                                          .setDataPoints(datapoints)
                                                          .setListener(listener)
                                                          .build();
        XLinkSDK.startTask(task);
    }

    public void setDeviceDatapoints(@NonNull XDevice device, final List<XLinkDataPoint> datapoints, int timeout, XLinkTaskListener<XDevice> listener) {
        XLinkSetDataPointTask task = XLinkSetDataPointTask.newBuilder()
                                                          .setXDevice(device)
                                                          .setDataPoints(datapoints)
                                                          .setListener(listener)
                                                          .setTotalTimeout(timeout)
                                                          .build();
        XLinkSDK.startTask(task);
    }

    public void setApplicationSetDatapoints(@NonNull final XDevice device, @NonNull final List<XLinkDataPoint> dps, final XlinkRequestCallback<List<XLinkDataPoint>> callback) {
        final List<DeviceApi.DeviceDataPointRequest.Command> commands = new ArrayList<>();
        for (XLinkDataPoint dp : dps) {
            if (dp.getSource() != XLinkRestfulEnum.DataPointSource.APPLICATION_SET.getValue()) {
                return;
            }
            DeviceApi.DeviceDataPointRequest.Command cmd = new DeviceApi.DeviceDataPointRequest.Command();
            cmd.index = dp.getIndex();
            switch (dp.getType()) {
                case BOOL:
                    cmd.value = String.valueOf(dp.getAsByte());
                    break;
                case BYTE:
                case SHORT:
                case USHORT:
                case INT:
                case UINT:
                case LONG:
                case ULONG:
                case FLOAT:
                case DOUBLE:
                    cmd.value = String.valueOf(dp.getValue());
                    break;
                case STRING:
                    cmd.value = dp.getValue();
                    break;
                case BYTE_ARRAY:
                    cmd.value = ByteUtil.bytesToHex((byte[]) dp.getValue());
                    break;
            }
            commands.add(cmd);
        }
        DeviceApi.DeviceDataPointRequest request = new DeviceApi.DeviceDataPointRequest();
        request.source = XLinkRestfulEnum.DataPointSource.APPLICATION_SET;
        request.command = commands;
        Call<String> call = XLinkRestful.getApplicationApi()
                                        .setDeviceDataPoint(device.getDeviceId(), request);
        call.enqueue(new XlinkRequestCallback<String>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }

            @Override
            public void onSuccess(String s) {
                if (callback != null) {
                    callback.onSuccess(dps);
                }
            }
        });
        if (callback != null) {
            callback.onStart();
        }
    }

//    public void setDeviceDatapoints(final XDevice device, List<DeviceApi.DeviceDataPointRequest.Command> commands, final XlinkRequestCallback<String> callback) {
//        DeviceApi.DeviceDataPointRequest request = new DeviceApi.DeviceDataPointRequest();
//        request.source = XLinkRestfulEnum.DataPointSource.APPLICATION_SET;
//        request.command = commands;
//        Call<String> call = XLinkRestful.getApplicationApi()
//                                        .setDeviceDataPoint(device.getDeviceId(), request);
//        call.enqueue(callback);
//        if (callback != null) {
//            callback.onStart();
//        }
//    }

    public void probeDevice(XDevice device, @NonNull Collection<Integer> indexs, XLinkTaskListener<List<XLinkDataPoint>> listener) {
        XLinkProbeDataPointTask task = XLinkProbeDataPointTask.newBuilder()
                                                              .setXDevice(device)
                                                              .setIndexs(indexs)
                                                              .setListener(listener)
                                                              .build();
        XLinkSDK.startTask(task);
    }

    public void shareDevice(XDevice device, String account, int expired, XLinkTaskListener<DeviceApi.ShareDeviceResponse> listener) {
        XLinkShareDeviceTask task = XLinkShareDeviceTask.newBuilder()
                                                        .setXDevice(device)
                                                        .setMode(XLinkRestfulEnum.ShareMode.ACCOUNT)
                                                        .setAccount(account)
                                                        .setExpired(expired)
                                                        .setListener(listener)
                                                        .build();
        XLinkSDK.startTask(task);
    }

    public void denyShareDevice(EventNotifyHelper.DeviceShareNotify notify, XLinkTaskListener<String> listener) {
        XLinkHandleShareDeviceTask task = XLinkHandleShareDeviceTask.newBuilder()
                                                                    .setAction(XLinkHandleShareDeviceTask.Action.DENY)
                                                                    .setInviteCode(notify.invite_code)
                                                                    .setUid(XLinkUserManager.getInstance()
                                                                                            .getUid())
                                                                    .setListener(listener)
                                                                    .build();
        XLinkSDK.startTask(task);
    }

    public void acceptShareDevice(EventNotifyHelper.DeviceShareNotify notify, XLinkTaskListener<String> listener) {
        XLinkHandleShareDeviceTask task = XLinkHandleShareDeviceTask.newBuilder()
                                                                    .setAction(XLinkHandleShareDeviceTask.Action.ACCEPT)
                                                                    .setInviteCode(notify.invite_code)
                                                                    .setUid(XLinkUserManager.getInstance()
                                                                                            .getUid())
                                                                    .setListener(listener)
                                                                    .build();
        XLinkSDK.startTask(task);
    }

    public void cancelShareDevice(DeviceApi.ShareDeviceItem item, XLinkTaskListener<String> listener) {
        XLinkHandleShareDeviceTask task = XLinkHandleShareDeviceTask.newBuilder()
                                                                    .setAction(XLinkHandleShareDeviceTask.Action.CANCEL)
                                                                    .setInviteCode(item.inviteCode)
                                                                    .setUid(XLinkUserManager.getInstance()
                                                                                            .getUid())
                                                                    .setListener(listener)
                                                                    .build();
        XLinkSDK.startTask(task);
    }

    public void deleteShareDevice(DeviceApi.ShareDeviceItem item, XLinkTaskListener<String> listener) {
        XLinkHandleShareDeviceTask task = XLinkHandleShareDeviceTask.newBuilder()
                                                                    .setAction(XLinkHandleShareDeviceTask.Action.DELETE)
                                                                    .setInviteCode(item.inviteCode)
                                                                    .setUid(XLinkUserManager.getInstance()
                                                                                            .getUid())
                                                                    .setListener(listener)
                                                                    .build();
        XLinkSDK.startTask(task);
    }

    public void getDeviceLocation(XDevice xDevice, final XlinkRequestCallback<DeviceApi.DeviceGeographyResponse> callback) {
        final Call<DeviceApi.DeviceGeographyResponse> call = XLinkRestful.getApplicationApi()
                                                                         .getDeviceGeography(xDevice.getProductId(), xDevice.getDeviceId());
        call.enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void checkUpgradeTask(final XDevice xDevice, TaskListener<Boolean> listener) {
        XLinkLocalSendTriggerUpgradeTask task = XLinkLocalSendTriggerUpgradeTask.newBuilder()
                                                                                .setFirmwareType(CoreConstant.FIRMWARE_TYPE_WIFI)
                                                                                .setCoreDevice(xDevice)
                                                                                .setListener(listener)
                                                                                .build();
        XLinkSDK.startTask(task);
    }

//    public void sendUpgradeTask(final XDevice xDevice) {
//        XLinkLocalSendUpgradeTaskResultTask task = XLinkLocalSendUpgradeTaskResultTask.newBuilder()
//                                                                                      .setCoreDevice(xDevice)
//                                                                                      .setCode(FirmwareUpgradeTaskResult.FIRMWARE_RESULT_CODE_CHECK_VERSION_SUCCESS).build()
//    }

    public void getNewsetVersion(@NonNull final XDevice xDevice, final XlinkRequestCallback<DeviceApi.DeviceNewestVersionResponse> callback) {
        DeviceApi.DeviceNewestVersionRequest request = new DeviceApi.DeviceNewestVersionRequest();
        request.productId = xDevice.getProductId();
        request.deviceId = xDevice.getDeviceId();
        Call<DeviceApi.DeviceNewestVersionResponse> call = XLinkRestful.getApplicationApi().getDeviceNewestVersion(request);
        call.enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void upgradeDevice(@NonNull final XDevice xDevice, final XlinkRequestCallback<String> callback) {
        DeviceApi.UpgradeDeviceRequest request = new DeviceApi.UpgradeDeviceRequest();
        request.productId = xDevice.getProductId();
        request.deviceId = xDevice.getDeviceId();
        Call<String> call = XLinkRestful.getApplicationApi().upgradeDevice(request);
        call.enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    private static class LazyHolder {
        private static final XlinkCloudManager INSTANCE = new XlinkCloudManager();
    }
}
