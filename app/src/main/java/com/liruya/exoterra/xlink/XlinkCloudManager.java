package com.liruya.exoterra.xlink;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;

import com.liruya.exoterra.util.DeviceUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.xlink.restful.XLinkCallback;
import cn.xlink.restful.XLinkRestful;
import cn.xlink.restful.XLinkRestfulEnum;
import cn.xlink.restful.XLinkRestfulError;
import cn.xlink.restful.api.app.DeviceApi;
import cn.xlink.restful.api.app.HomeApi;
import cn.xlink.restful.api.app.UserApi;
import cn.xlink.restful.api.app.UserAuthApi;
import cn.xlink.sdk.common.ByteUtil;
import cn.xlink.sdk.core.constant.CoreConstant;
import cn.xlink.sdk.core.error.XLinkErrorCodeHelper;
import cn.xlink.sdk.core.java.local.XLinkLocalSendTriggerUpgradeTask;
import cn.xlink.sdk.core.model.XLinkDataPoint;
import cn.xlink.sdk.task.TaskListener;
import cn.xlink.sdk.v5.listener.XLinkScanDeviceListener;
import cn.xlink.sdk.v5.listener.XLinkTaskListener;
import cn.xlink.sdk.v5.manager.XLinkUserManager;
import cn.xlink.sdk.v5.model.XDevice;
import cn.xlink.sdk.v5.module.connection.XLinkConnectDeviceTask;
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
import cn.xlink.sdk.v5.module.share.XLinkHandleShareDeviceTask;
import cn.xlink.sdk.v5.module.share.XLinkShareDeviceTask;
import cn.xlink.sdk.v5.module.subscription.XLinkAddDeviceTask;
import retrofit2.Call;
import retrofit2.Response;

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

//    public void register(final String email, String nickname, String password, final XlinkRequestCallback<UserAuthApi.EmailRegisterResponse> callback) {
//        UserAuthApi.EmailRegisterRequest request = new UserAuthApi.EmailRegisterRequest();
//        request.corpId = mCorpId;
//        request.email = email;
//        if (!TextUtils.isEmpty(nickname)) {
//            request.nickname = nickname;
//        }
//        request.password = password;
//        request.localLang = XLinkRestfulEnum.LocalLang.EN_US;
//        request.source = XLinkRestfulEnum.UserSource.ANDROID;
//        XLinkRestful.getApplicationApi()
//                    .registEmailAccount(request)
//                    .enqueue(callback);
//        if (callback != null) {
//            callback.onStart();
//        }
//
//        UserAuthApi.EmailVerifyCodeRegisterRequest rq;
//        UserAuthApi.RegisterEmailVerifyCodeRequest req = new UserAuthApi.RegisterEmailVerifyCodeRequest();
//        req.corpId = mCorpId;
//        req.email = email;
//        req.localLang = XLinkRestfulEnum.LocalLang.EN_US;
//        XLinkRestful.getApplicationApi().registerEmailVerifyCode(req).enqueue(null);
//    }

    public void requestRegisterEmailVerifycode(@NonNull final String email, final XlinkRequestCallback<String> callback) {
        UserAuthApi.RegisterEmailVerifyCodeRequest request = new UserAuthApi.RegisterEmailVerifyCodeRequest();
        request.corpId = mCorpId;
        request.email = email;
        request.localLang = XLinkRestfulEnum.LocalLang.EN_US;
        XLinkRestful.getApplicationApi()
                    .registerEmailVerifyCode(request)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void registerEmailByVerifycode(@NonNull final String email, final String nickname, final String password, @NonNull final String verifycode,
                                          final XlinkRequestCallback<UserAuthApi.EmailVerifyCodeRegisterResponse> callback) {
        UserAuthApi.EmailVerifyCodeRegisterRequest request = new UserAuthApi.EmailVerifyCodeRegisterRequest();
        request.corpId = mCorpId;
        request.email = email;
        if (!TextUtils.isEmpty(nickname)) {
            request.nickname = nickname;
        }
        request.password = password;
        request.localLang = XLinkRestfulEnum.LocalLang.EN_US;
        request.source = XLinkRestfulEnum.UserSource.ANDROID;
        request.verifycode = verifycode;
        XLinkRestful.getApplicationApi()
                    .registerEmailVerifyCodeAccount(request)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void registerEmailByVerifycode(@NonNull final String email, final String password, @NonNull final String verifycode,
                                          final XlinkRequestCallback<UserAuthApi.EmailVerifyCodeRegisterResponse> callback) {
        registerEmailByVerifycode(email, null, password, verifycode, callback);
    }

    public void refreshToken(int userid, String authorize, String refresh_token, XLinkTaskListener<UserApi.TokenRefreshResponse> listener) {
        XLinkRefreshTokenTask task = XLinkRefreshTokenTask.newBuilder()
                                                          .setUserId(userid)
                                                          .setAuthString(authorize)
                                                          .setRefreshToken(refresh_token)
                                                          .setTimeout(5000)
                                                          .setListener(listener)
                                                          .build();
        XLinkSDK.startTask(task);
    }

    public void modifyPassword(String oldPassword, String newPassword, final XlinkRequestCallback<String> callback) {
        UserApi.PasswordResetRequest request = new UserApi.PasswordResetRequest();
        request.oldPassword = oldPassword;
        request.newPassword = newPassword;
        XLinkRestful.getApplicationApi()
                    .updatePassword(request)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void requestEmailFoundbackPasswordVerifyCode(String email, final XlinkRequestCallback<String> callback) {
        UserAuthApi.EmailPasswordForgotRequest request = new UserAuthApi.EmailPasswordForgotRequest();
        request.corpId = mCorpId;
        request.email = email;
        request.localLang = XLinkRestfulEnum.LocalLang.EN_US;
        XLinkRestful.getApplicationApi()
                    .sendEmailPasswordFound(request)
                    .enqueue(callback);
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
        XLinkRestful.getApplicationApi()
                    .sendPasswordReset(request)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void getUserInfo(final int userid, XlinkRequestCallback<UserApi.UserInfoResponse> callback) {
        XLinkRestful.getApplicationApi()
                    .getUserInfo(userid)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

//    public void registerDevice(final XDevice device, String name, final IXlinkRegisterDeviceCallback callback) {
//        DeviceApi.RegisterDeviceRequest request = new DeviceApi.RegisterDeviceRequest();
//        request.productId = device.getProductId();
//        request.mac = device.getMacAddress();
//        request.firmwareMod = "0";                  //wifi:0  gprs:1, 如果不设置无法手动升级
//        if (!TextUtils.isEmpty(name)) {
//            request.name = name;
//        }
//        Call<DeviceApi.RegisterDeviceResponse> call = XLinkRestful.getApplicationApi()
//                                                                  .userRegisterDevice(XLinkUserManager.getInstance().getUid(), request);
//        call.enqueue(new XLinkCallback<DeviceApi.RegisterDeviceResponse>() {
//            @Override
//            public void onHttpError(Throwable throwable) {
//                if (callback != null) {
//                    callback.onError(throwable.getMessage());
//                }
//            }
//
//            @Override
//            public void onApiError(XLinkRestfulError.ErrorWrapper.Error error) {
//                if (callback != null) {
//                    if (error.code == XLinkErrorCodes.ERROR_API_DEVICE_MAC_ADDRESS_EXISTS) {
//                        callback.onDeviceAlreadyExists(device);
//                    } else {
//                        callback.onError(XLinkErrorCodeHelper.getErrorCodeName(error.code));
//                    }
//                }
//            }
//
//            @Override
//            public void onSuccess(DeviceApi.RegisterDeviceResponse response) {
//                Log.e(TAG, "onSubscribeSuccess: " + response.accessKey );
//                device.setDeviceId(response.deviceId);
//                if (callback != null) {
//                    callback.onSuccess(device);
//                }
//            }
//        });
//        if (callback != null) {
//            callback.onStart();
//        }
//    }
//
//    public void registerDevice(final XDevice device, final IXlinkRegisterDeviceCallback callback) {
//        registerDevice(device, null, callback);
//    }

    public void addDevice(final XDevice device, int timeout, XLinkTaskListener<XDevice> listener) {
        XLinkAddDeviceTask task = XLinkAddDeviceTask.newBuilder()
                                                    .setXDevice(device)
                                                    .setConnectLocal(true)
                                                    .setNeedSubscription(false)
                                                    .setPinCode(null)
                                                    .setTotalTimeout(timeout)
                                                    .setListener(listener)
                                                    .build();
        XLinkSDK.startTask(task);
    }

    public void connectDevice(final XDevice device, XLinkTaskListener<XDevice> listener) {
        XLinkConnectDeviceTask task = XLinkConnectDeviceTask.newBuilder()
                                                            .setXDevice(device)
                                                            .setConnectionFlags(CoreConstant.FROM_LOCAL)
                                                            .setListener(listener)
                                                            .build();
        XLinkSDK.startTask(task);
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

    public XlinkResult<DeviceApi.SnSubscribeResponse> subscribeDeviceBySn(@NonNull String pid, @NonNull String sn) {
        DeviceApi.SnSubscribeRequest request = new DeviceApi.SnSubscribeRequest();
        request.productId = pid;
        request.sn = sn;
        try {
            Response<DeviceApi.SnSubscribeResponse> response = XLinkRestful.getApplicationApi()
                                                                           .snSubscribeDevice(XLinkUserManager.getInstance().getUid(), request)
                                                                           .execute();
            if (response.isSuccessful()) {
                return new XlinkResult<>(response.body());
            } else {
                XLinkRestfulError.ErrorWrapper.Error error = XLinkRestfulError.parseError(XLinkRestful.getBaseRetrofit(), response);
                if (error == null) {
                    error = new XLinkRestfulError.ErrorWrapper.Error(response.message(), response.code());
                }
                return new XlinkResult<>(XLinkErrorCodeHelper.getErrorCodeName(error.code));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return new XlinkResult<>(e.getMessage());
        }
    }

    public void subscribeDeviceBySn(@NonNull String pid, @NonNull String sn, final XlinkRequestCallback<DeviceApi.SnSubscribeResponse> callback) {
        DeviceApi.SnSubscribeRequest request = new DeviceApi.SnSubscribeRequest();
        request.productId = pid;
        request.sn = sn;
        XLinkRestful.getApplicationApi()
                    .snSubscribeDevice(XLinkUserManager.getInstance().getUid(), request)
                    .enqueue(new XLinkCallback<DeviceApi.SnSubscribeResponse>() {
                        @Override
                        public void onHttpError(Throwable throwable) {
                            if (callback != null) {
                                callback.onError(throwable.getMessage());
                            }
                        }

                        @Override
                        public void onApiError(XLinkRestfulError.ErrorWrapper.Error error) {
                            if (callback != null) {
        //                                if (error.code == XLinkErrorCodes.ERROR_API_USER_HAS_SUBSCRIBE_DEVICE) {
        //                                    callback.onSuccess(null);
        //                                } else {
                                    callback.onError(XLinkErrorCodeHelper.getErrorCodeName(error.code));
        //                                }
                            }
                        }

                        @Override
                        public void onSuccess(DeviceApi.SnSubscribeResponse response) {
                            if (callback != null) {
                                callback.onSuccess(response);
                            }
                        }
                    });
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

    public void scanDevice(@NonNull final String pid, final int timeout, final int retryInterval, final XLinkScanDeviceListener listener) {
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
        XLinkRestful.getApplicationApi()
                    .updateDeviceInfo(pid, devid, request)
                    .enqueue(callback);
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
        XLinkRestful.getApplicationApi()
                    .getDeviceInfo(xDevice.getProductId(), xDevice.getDeviceId())
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
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
                                                          .setTotalTimeout(timeout)
                                                          .setListener(listener)
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

    public void setDeviceDatapoints(@NonNull final int devid, List<DeviceApi.DeviceDataPointRequest.Command> commands, final XlinkRequestCallback<String> callback) {
        DeviceApi.DeviceDataPointRequest request = new DeviceApi.DeviceDataPointRequest();
        request.source = XLinkRestfulEnum.DataPointSource.DEVICE_POST;
        request.command = commands;
        request.writeBack = true;
        Call<String> call = XLinkRestful.getApplicationApi()
                                        .setDeviceDataPoint(devid, request);
        call.enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void probeDevice(XDevice device, @NonNull Collection<Integer> indexs, XLinkTaskListener<List<XLinkDataPoint>> listener) {
        XLinkProbeDataPointTask task = XLinkProbeDataPointTask.newBuilder()
                                                              .setXDevice(device)
                                                              .setIndexs(indexs)
                                                              .setListener(listener)
                                                              .build();
        XLinkSDK.startTask(task);
    }

    /**
     * 增加扩展字段，productID，name，
     * @param device
     * @param account
     * @param listener
     */
    public void shareDevice(@NonNull final XDevice device, String account, XLinkTaskListener<DeviceApi.ShareDeviceResponse> listener) {
        String pid = device.getProductId();
        String name = device.getDeviceName();
        if (TextUtils.isEmpty(name)) {
            name = DeviceUtil.getDefaultName(pid);
        }
        XLinkShareDeviceTask task = XLinkShareDeviceTask.newBuilder()
                                                        .setXDevice(device)
                                                        .setMode(XLinkRestfulEnum.ShareMode.ACCOUNT)
                                                        .setAccount(account)
                                                        .setExpired(24*3600)
                                                        .setExtendString(pid + "-" + name)
                                                        .setListener(listener)
                                                        .build();
        XLinkSDK.startTask(task);
    }

    public void denyShareDevice(@NonNull final String inviteCode, XLinkTaskListener<String> listener) {
        XLinkHandleShareDeviceTask task = XLinkHandleShareDeviceTask.newBuilder()
                                                                    .setAction(XLinkHandleShareDeviceTask.Action.DENY)
                                                                    .setInviteCode(inviteCode)
                                                                    .setUid(XLinkUserManager.getInstance()
                                                                                            .getUid())
                                                                    .setListener(listener)
                                                                    .build();
        XLinkSDK.startTask(task);
    }

    public void acceptShareDevice(@NonNull final String inviteCode, XLinkTaskListener<String> listener) {
        XLinkHandleShareDeviceTask task = XLinkHandleShareDeviceTask.newBuilder()
                                                                    .setAction(XLinkHandleShareDeviceTask.Action.ACCEPT)
                                                                    .setInviteCode(inviteCode)
                                                                    .setUid(XLinkUserManager.getInstance()
                                                                                            .getUid())
                                                                    .setListener(listener)
                                                                    .build();
        XLinkSDK.startTask(task);
    }

    public void cancelShareDevice(@NonNull final String inviteCode, XLinkTaskListener<String> listener) {
        XLinkHandleShareDeviceTask task = XLinkHandleShareDeviceTask.newBuilder()
                                                                    .setAction(XLinkHandleShareDeviceTask.Action.CANCEL)
                                                                    .setInviteCode(inviteCode)
                                                                    .setUid(XLinkUserManager.getInstance()
                                                                                            .getUid())
                                                                    .setListener(listener)
                                                                    .build();
        XLinkSDK.startTask(task);
    }

    public void deleteShareDevice(@NonNull final String inviteCode, XLinkTaskListener<String> listener) {
        XLinkHandleShareDeviceTask task = XLinkHandleShareDeviceTask.newBuilder()
                                                                    .setAction(XLinkHandleShareDeviceTask.Action.DELETE)
                                                                    .setInviteCode(inviteCode)
                                                                    .setUid(XLinkUserManager.getInstance()
                                                                                            .getUid())
                                                                    .setListener(listener)
                                                                    .build();
        XLinkSDK.startTask(task);
    }

    public void getDeviceShareList(final XlinkRequestCallback<List<DeviceApi.ShareDeviceItem>> callback) {
        XLinkRestful.getApplicationApi()
                    .getDeviceShareList()
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void getDeviceUserList(final int devid, final XlinkRequestCallback<DeviceApi.DeviceSubscribeUsersResponse> callback) {
        XLinkRestful.getApplicationApi()
                    .getDeviceSubscribeUserList(XLinkUserManager.getInstance().getUid(), devid)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void getDeviceLocation(@NonNull final String pid, @NonNull final int devid, final XlinkRequestCallback<DeviceApi.DeviceGeographyResponse> callback) {
        XLinkRestful.getApplicationApi()
                    .getDeviceGeography(pid, devid)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void getDeviceLocation(@NonNull final XDevice xDevice, final XlinkRequestCallback<DeviceApi.DeviceGeographyResponse> callback) {
        getDeviceLocation(xDevice.getProductId(), xDevice.getDeviceId(), callback);
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
        XLinkRestful.getApplicationApi()
                    .getDeviceNewestVersion(request)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void upgradeDevice(@NonNull final XDevice xDevice, final XlinkRequestCallback<String> callback) {
        DeviceApi.UpgradeDeviceRequest request = new DeviceApi.UpgradeDeviceRequest();
        request.productId = xDevice.getProductId();
        request.deviceId = xDevice.getDeviceId();
        XLinkRestful.getApplicationApi()
                    .upgradeDevice(request)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void createHome(@NonNull final String homeName, final XlinkRequestCallback<HomeApi.HomeResponse> callback) {
        HomeApi.HomeRequest request = new HomeApi.HomeRequest();
        request.name = homeName;
        XLinkRestful.getApplicationApi()
                    .createHome(request)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void getHomeList(final XlinkRequestCallback<HomeApi.HomesResponse> callback) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("user_id", XLinkUserManager.getInstance().getUid());
        XLinkRestful.getApplicationApi()
                    .getHomeList(requestMap)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void getHomeDeviceList(@NonNull final String homeid, final XlinkRequestCallback<HomeApi.HomeDevicesResponse> callback) {
        XLinkRestful.getApplicationApi()
                    .getHomeDeviceList(homeid)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void addDeviceToHome(@NonNull final String homeid, final int deviceid, final XlinkRequestCallback<String> callback) {
        HomeApi.DeviceAddRequest request = new HomeApi.DeviceAddRequest();
        request.deviceId = deviceid;
        request.authority = XLinkRestfulEnum.DeviceAuthority.RW;
        request.subRole = XLinkRestfulEnum.DeviceSubscribeRole.USER;
        XLinkRestful.getApplicationApi()
                    .addHomeDevice(homeid, request)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void removeDeviceFromHome(@NonNull final String homeid, final int deviceid, final XlinkRequestCallback<String> callback) {
        XLinkRestful.getApplicationApi()
                    .deleteHomeDevice(homeid, deviceid)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void shareHome(@NonNull final String homeid, @NonNull final String account, final XlinkRequestCallback<HomeApi.UserInviteResponse> callback) {
        HomeApi.UserInviteRequest request = new HomeApi.UserInviteRequest();
        request.account = account;
        request.authority = XLinkRestfulEnum.DeviceAuthority.RW;
        request.mode = XLinkRestfulEnum.InvitationType.USER_ID;
        request.role = XLinkRestfulEnum.HomeUserType.USER;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 2);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        request.expireTime = df.format(calendar.getTime());
        XLinkRestful.getApplicationApi()
                    .inviteHomeUser(homeid, request)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void acceptHomeInvite(@NonNull final String homeid, @NonNull final String inviteId, final XlinkRequestCallback<String> callback) {
        HomeApi.UserAcceptRequest request = new HomeApi.UserAcceptRequest();
        request.inviteId = inviteId;
        XLinkRestful.getApplicationApi()
                    .acceptHomeInvite(homeid, request)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void denyHomeInvite(@NonNull final String homeid, @NonNull final String inviteId, final XlinkRequestCallback<String> callback) {
        HomeApi.UserDenyRequest request = new HomeApi.UserDenyRequest();
        request.inviteId = inviteId;
        XLinkRestful.getApplicationApi()
                    .denyHomeInvite(homeid, request)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void deleteHome(@NonNull final HomeApi.HomesResponse.Home home, final XlinkRequestCallback<String> callback) {
        boolean isHomeAdmin = false;
        for (HomeApi.HomesResponse.Home.User user : home.userList) {
            if (user.userId == XLinkUserManager.getInstance().getUid()) {
                isHomeAdmin = (user.role == XLinkRestfulEnum.HomeUserType.ADMIN || user.role == XLinkRestfulEnum.HomeUserType.SUPER_ADMIN);
                break;
            }
        }
        if (isHomeAdmin) {
            XLinkRestful.getApplicationApi()
                        .deleteHome(home.id)
                        .enqueue(callback);
        } else {
            XLinkRestful.getApplicationApi()
                        .deleteHomeUser(home.id, XLinkUserManager.getInstance().getUid())
                        .enqueue(callback);
        }
        if (callback != null) {
            callback.onStart();
        }
    }

    public void getHomeInviteeList(final XlinkRequestCallback<HomeApi.InviteeListResponse> callback) {
        XLinkRestful.getApplicationApi()
                    .getHomeInviteeList(XLinkUserManager.getInstance().getUid())
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void getHomeInviterList(final XlinkRequestCallback<HomeApi.InviterListResponse> callback) {
        XLinkRestful.getApplicationApi()
                    .getHomeInviterList(XLinkUserManager.getInstance().getUid())
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public boolean isHomeAdmin(@NonNull final String homeid) {
        return false;
    }

    private static class LazyHolder {
        private static final XlinkCloudManager INSTANCE = new XlinkCloudManager();
    }
}
