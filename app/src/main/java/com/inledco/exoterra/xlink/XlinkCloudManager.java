package com.inledco.exoterra.xlink;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.inledco.exoterra.AppConstants;
import com.inledco.exoterra.bean.Home2;
import com.inledco.exoterra.bean.HomeProperty;
import com.inledco.exoterra.bean.ImportDeviceResponse;
import com.inledco.exoterra.bean.QueryDeviceResponse;
import com.inledco.exoterra.manager.OKHttpManager;
import com.inledco.exoterra.util.DeviceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.xlink.restful.XLinkCallback;
import cn.xlink.restful.XLinkRestful;
import cn.xlink.restful.XLinkRestfulEnum;
import cn.xlink.restful.XLinkRestfulError;
import cn.xlink.restful.api.CommonQuery;
import cn.xlink.restful.api.app.DeviceApi;
import cn.xlink.restful.api.app.HomeApi;
import cn.xlink.restful.api.app.PushApi;
import cn.xlink.restful.api.app.UserApi;
import cn.xlink.restful.api.app.UserAuthApi;
import cn.xlink.restful.api.app.UserMessageApi;
import cn.xlink.sdk.common.ByteUtil;
import cn.xlink.sdk.core.constant.CoreConstant;
import cn.xlink.sdk.core.error.XLinkErrorCodeHelper;
import cn.xlink.sdk.core.java.local.XLinkLocalSendTriggerUpgradeTask;
import cn.xlink.sdk.core.model.XLinkDataPoint;
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
import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class XlinkCloudManager {
    private static final String TAG = "XlinkCloudManager";

    private String mCorpId;
    private final Retrofit mRetrofit;

    private XlinkCloudManager() {
        mRetrofit = new Retrofit.Builder().baseUrl(XLinkRestful.getBaseRetrofit().baseUrl())
                                          .client(XLinkRestful.getApiHttpClient().newBuilder().build())
                                          .addConverterFactory(ScalarsConverterFactory.create())
                                          .addConverterFactory(GsonConverterFactory.create())
                                          .build();
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

    public void modifyNickname(final String nickname, final XlinkRequestCallback<String> callback) {
        int userid = XLinkUserManager.getInstance().getUid();
        UserApi.UserRequest request = new UserApi.UserRequest();
        request.nickname = nickname;
        XLinkRestful.getApplicationApi()
                    .updateUserInfo(userid, request)
                    .enqueue(callback);
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

    public void getUserInfo(XlinkRequestCallback<UserApi.UserInfoResponse> callback) {
        final int userid = XLinkUserManager.getInstance().getUid();
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
//                    callback.onError(throwable.getMsg());
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

    public void unsubscribeDevice(final int devid) {
        final int userid = XLinkUserManager.getInstance().getUid();
        DeviceApi.UnSubscribeRequest request = new DeviceApi.UnSubscribeRequest();
        request.deviceId = devid;
        try {
            Response<String> response = XLinkRestful.getApplicationApi().unSubscribeDevice(userid, request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unsubscribeDevice(@NonNull XDevice device, XLinkTaskListener<String> listener) {
        XLinkRemoveDeviceTask task = XLinkRemoveDeviceTask.newBuilder()
                                                          .setXDevice(device)
                                                          .setListener(listener)
                                                          .build();
        XLinkSDK.startTask(task);
    }

    public XLinkScanDeviceTask createScanDeviceTask(@NonNull final String pid, final int timeout, final int retryInterval, final XLinkScanDeviceListener listener) {
        XLinkScanDeviceTask task = XLinkScanDeviceTask.newBuilder()
                                                      .setProductIds(pid)
                                                      .setTotalTimeout(timeout)
                                                      .setRetryInterval(retryInterval)
                                                      .setScanDeviceListener(listener)
                                                      .build();
        return task;
    }

    public XLinkScanDeviceTask createScanDeviceTask(@NonNull final List<String> pids, final int timeout, final int retryInterval, final XLinkScanDeviceListener listener) {
        XLinkScanDeviceTask task = XLinkScanDeviceTask.newBuilder()
                                                      .setProductIds(pids)
                                                      .setTotalTimeout(timeout)
                                                      .setRetryInterval(retryInterval)
                                                      .setScanDeviceListener(listener)
                                                      .build();
        return task;
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

    public void scanDevice(@NonNull List<String> pids, final int timeout, final int retryInterval, final XLinkScanDeviceListener listener) {
        XLinkScanDeviceTask task = XLinkScanDeviceTask.newBuilder()
                                                      .setProductIds(pids)
                                                      .setTotalTimeout(timeout)
                                                      .setRetryInterval(retryInterval)
                                                      .setScanDeviceListener(listener)
                                                      .build();
        XLinkSDK.startTask(task);
    }

    public XlinkResult<DeviceApi.DeviceResponse> getDeviceInfo(String pid, int devid) {
        try {
            Response<DeviceApi.DeviceResponse> response = XLinkRestful.getApplicationApi()
                                                                      .getDeviceInfo(pid, devid)
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
        } catch (IOException e) {
            e.printStackTrace();
            return new XlinkResult<>(e.getMessage());
        }
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
                                                                          .setForceRefresh(true)
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

    public void setDeviceProperty(@NonNull final String pid, final int devid, @NonNull final Map<String, Object> request, final XlinkRequestCallback<String> callback) {
        XLinkRestful.getApplicationApi()
                    .setDeviceProperty(pid, devid, request)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void setDeviceProperty(@NonNull final XDevice device, Map<String, Object> request, XlinkRequestCallback<String> callback) {
        setDeviceProperty(device.getProductId(), device.getDeviceId(), request, callback);
    }

    public String getDeviceProperty(@NonNull final String pid, final int devid) {
        try {
            Response<String> response = XLinkRestful.getApplicationApi()
                                                    .getDeviceProperty(pid, devid)
                                                    .execute();
            if (response.isSuccessful()) {
                return response.body();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getDeviceProperty(@NonNull final XDevice device) {
        return getDeviceProperty(device.getProductId(), device.getDeviceId());
    }

    public void getDeviceProperty(@NonNull final String pid, final int devid, final XlinkRequestCallback<String> callback) {
        XLinkRestful.getApplicationApi()
                    .getDeviceProperty(pid, devid)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void getDeviceProperty(@NonNull final XDevice device, XlinkRequestCallback<String> callback) {
        getDeviceProperty(device.getProductId(), device.getDeviceId(), callback);
    }
    public void checkUpgradeTask(final XDevice xDevice, XlinkTaskCallback<Boolean> callback) {
        XLinkLocalSendTriggerUpgradeTask task = XLinkLocalSendTriggerUpgradeTask.newBuilder()
                                                                                .setFirmwareType(CoreConstant.FIRMWARE_TYPE_WIFI)
                                                                                .setCoreDevice(xDevice)
                                                                                .setListener(callback)
                                                                                .build();
        XLinkSDK.startTask(task);
    }

//    public void sendUpgradeTask(final XDevice xDevice) {
//        XLinkLocalSendUpgradeTaskResultTask task = XLinkLocalSendUpgradeTaskResultTask.newBuilder()
//                                                                                      .setCoreDevice(xDevice)
//                                                                               l       .setCode(FirmwareUpgradeTaskResult.FIRMWARE_RESULT_CODE_CHECK_VERSION_SUCCESS).build()
//    }

    public void getNewsetVersion(@NonNull final XDevice xDevice, final XlinkRequestCallback<DeviceApi.DeviceNewestVersionResponse> callback) {
        DeviceApi.DeviceNewestVersionRequest request = new DeviceApi.DeviceNewestVersionRequest();
        request.productId = xDevice.getProductId();
        request.deviceId = xDevice.getDeviceId();
        try {
            int version = Integer.parseInt(xDevice.getFirmwareVersion());
            request.identify = (version & 0x01) == 0x01 ? 1 : 2;
            XLinkRestful.getApplicationApi()
                        .getDeviceNewestVersion(request)
                        .enqueue(callback);
            if (callback != null) {
                callback.onStart();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void upgradeDevice(@NonNull final XDevice xDevice, final XlinkRequestCallback<String> callback) {
        DeviceApi.UpgradeDeviceRequest request = new DeviceApi.UpgradeDeviceRequest();
        request.productId = xDevice.getProductId();
        request.deviceId = xDevice.getDeviceId();
        try {
            int version = Integer.parseInt(xDevice.getFirmwareVersion());
            request.identify = (version & 0x01) == 0x01 ? 1 : 2;
            XLinkRestful.getApplicationApi()
                        .upgradeDevice(request)
                        .enqueue(callback);
            if (callback != null) {
                callback.onStart();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void createHome(@NonNull final String homeName, final XlinkRequestCallback<HomeApi.HomeResponse> callback) {
        HomeApi.HomeRequest request = new HomeApi.HomeRequest();
        request.name = homeName;
        XLinkRestful.getApplicationApi()
                    .createHome(request)
                    .enqueue(callback);
    }

    public void setHomeProperty(final String homeid, final int zone, final int sunrise, final int sunset, final XlinkRequestCallback<String> callback) {
        if (zone < -720 || zone > 720) {
            return;
        }
        if (sunrise < 0 || sunrise > 1439) {
            return;
        }
        if (sunset < 0 || sunset > 1439) {
            return;
        }
        Map<String, HomeApi.HomeProperty> params = new HashMap<>();
        HomeApi.HomeProperty<String> zoneProperty = new HomeApi.HomeProperty<>();
        HomeApi.HomeProperty<String> sunriseProperty = new HomeApi.HomeProperty<>();
        HomeApi.HomeProperty<String> sunsetProperty = new HomeApi.HomeProperty<>();
        zoneProperty.name = "zone";
        zoneProperty.value = String.valueOf(zone);
        sunriseProperty.name = "sunrise";
        sunriseProperty.value = String.valueOf(sunrise);
        sunsetProperty.name = "sunset";
        sunsetProperty.value = String.valueOf(sunset);
        params.put("zone", zoneProperty);
        params.put("sunrise", sunriseProperty);
        params.put("sunset", sunsetProperty);
        XLinkRestful.getApplicationApi()
                    .setHomeProperty(homeid, params)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void setHomeRole(final String homeid, final int usrid, final XLinkRestfulEnum.HomeUserType role, final XlinkRequestCallback<String> callback) {
        HomeApi.HomeUserRequest request = new HomeApi.HomeUserRequest();
        request.role = role;
        XLinkRestful.getApplicationApi()
                    .updateHomeUser(homeid, usrid, request)
                    .enqueue(callback);
    }

    public XlinkResult<HomeProperty> getHomeProperty(final String homeid) {
        XlinkResult<HomeProperty> result = new XlinkResult<>();
        try {
            Response<Map<String, HomeApi.HomeProperty>> response = XLinkRestful.getApplicationApi()
                                                                               .getHomeProperty(homeid)
                                                                               .execute();
            if (response.isSuccessful()) {
                result.setSuccess(true);
                Map<String, HomeApi.HomeProperty> map = response.body();
                if (map != null && map.containsKey("zone") && map.containsKey("sunrise") && map.containsKey("sunset")) {
                    String rawZone = String.valueOf(map.get("zone").value);
                    String rawSunrise = String.valueOf(map.get("sunrise").value);
                    String rawSunset = String.valueOf(map.get("sunset").value);
                    try {
                        int zone = Integer.parseInt(rawZone);
                        int sunrise = Integer.parseInt(rawSunrise);
                        int sunset = Integer.parseInt(rawSunset);
                        HomeProperty property = new HomeProperty(zone, sunrise, sunset);
                        result.setResult(property);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                XLinkRestfulError.ErrorWrapper.Error error = XLinkRestfulError.parseError(response);
                if (error == null) {
                    error = new XLinkRestfulError.ErrorWrapper.Error(response.message(), response.code());
                }
                result.setError(XLinkErrorCodeHelper.getErrorCodeName(error.code));
            }
        } catch (IOException e) {
            e.printStackTrace();
            result.setError(e.getMessage());
        }
        return result;
    }

    public void getHomeProperty(final String homeid, final XlinkRequestCallback<HomeProperty> callback) {
        XLinkRestful.getApplicationApi().getHomeProperty(homeid).enqueue(new XlinkRequestCallback<Map<String, HomeApi.HomeProperty>>() {
            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }

            @Override
            public void onSuccess(Map<String, HomeApi.HomeProperty> map) {
                if (map.containsKey("zone") && map.containsKey("sunrise") && map.containsKey("sunset")) {
                    Object rawZone = map.get("zone").value;
                    Object rawSunrise = map.get("sunrise").value;
                    Object rawSunset = map.get("sunset").value;
                    if (rawZone instanceof String && rawSunrise instanceof String && rawSunset instanceof String) {
                        try {
                            int zone = Integer.parseInt((String) rawZone);
                            int sunrise = Integer.parseInt((String) rawSunrise);
                            int sunset = Integer.parseInt((String) rawSunset);
                            HomeProperty property = new HomeProperty(zone, sunrise, sunset);
                            if (callback != null) {
                                callback.onSuccess(property);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (callback != null) {
                                callback.onError(e.getMessage());
                            }
                        }
                    } else {
                        if (callback != null) {
                            callback.onError(null);
                        }
                    }
                }
            }
        });
        if (callback != null) {
            callback.onStart();
        }
    }

    public void createHomeWithRoom(@NonNull final String homeName, final XlinkRequestCallback<HomeApi.HomeResponse> callback) {
        final HomeApi.HomeRequest request = new HomeApi.HomeRequest();
        request.name = homeName;
        XLinkRestful.getApplicationApi()
                    .createHome(request)
                    .enqueue(new XlinkRequestCallback<HomeApi.HomeResponse>() {
                        @Override
                        public void onError(String error) {
                            if (callback != null) {
                                callback.onError(error);
                            }
                        }

                        @Override
                        public void onSuccess(HomeApi.HomeResponse homeResponse) {
                            createRoom(homeResponse.id, homeResponse.id, new XlinkRequestCallback<RoomApi.RoomResponse>() {
                                @Override
                                public void onError(String error) {
                                    if (callback != null) {
                                        callback.onError(error);
                                    }
                                }

                                @Override
                                public void onSuccess(RoomApi.RoomResponse roomResponse) {

                                }
                            });
                        }
                    });
    }

    public void renameHome(@NonNull final String homeid, @NonNull final String newName, final XlinkRequestCallback<String> callback) {
        HomeApi.HomeRequest request = new HomeApi.HomeRequest();
        request.name = newName;
        XLinkRestful.getApplicationApi()
                    .updateHome(homeid, request)
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

    public void getHomes(final XlinkRequestCallback<HomesExtendApi.HomesResponse> callback) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("user_id", XLinkUserManager.getInstance().getUid());
        requestMap.put("field", "room,zone");
        HomesExtendApi homesExtendApi = mRetrofit.create(HomesExtendApi.class);
        homesExtendApi.getHomes(requestMap)
                      .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public XlinkResult<HomeApi.HomeDevicesResponse> getHomeDeviceList(@NonNull final String homeid) {
        XlinkResult<HomeApi.HomeDevicesResponse> result = new XlinkResult<>();
        try {
            Response<HomeApi.HomeDevicesResponse> response = XLinkRestful.getApplicationApi()
                                                                         .getHomeDeviceList(homeid)
                                                                         .execute();
            if (response.isSuccessful()) {
                result.setSuccess(true);
                result.setResult(response.body());
            } else {
                XLinkRestfulError.ErrorWrapper.Error error = XLinkRestfulError.parseError(response);
                if (error == null) {
                    error = new XLinkRestfulError.ErrorWrapper.Error(response.message(), response.code());
                }
                result.setError(XLinkErrorCodeHelper.getErrorCodeName(error.code));
            }
        } catch (IOException e) {
            e.printStackTrace();
            result.setError(e.getMessage());
        }
        return result;
    }

    public void getHomeDeviceList(@NonNull final String homeid, final XlinkRequestCallback<HomeApi.HomeDevicesResponse> callback) {
        XLinkRestful.getApplicationApi()
                    .getHomeDeviceList(homeid)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

//    public void getHomeInfo(@NonNull final String homeid, final XlinkRequestCallback<Home2> callback) {
//        Map<String, Object> requestMap = new HashMap<>();
//        requestMap.put("user_id", XLinkUserManager.getInstance().getUid());
//        HomeExtendApi homeExtendApi = mRetrofit.create(HomeExtendApi.class);
//        homeExtendApi.getHomeInfo(homeid, requestMap)
//                     .enqueue(callback);
//        if (callback != null) {
//            callback.onStart();
//        }
//        HomeApi.HomeRequest request = new HomeApi.HomeRequest();
//    }

    public void getHomeInfo(@NonNull final String homeid, final XlinkRequestCallback<Home2> callback) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("user_id", XLinkUserManager.getInstance().getUid());
        HomeExtendApi homeExtendApi = mRetrofit.create(HomeExtendApi.class);
        homeExtendApi.getHomeInfo(homeid, requestMap)
                     .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public XlinkResult<String> addDeviceToHome(@NonNull final String homeid, final int deviceid) {
        XlinkResult<String> result = new XlinkResult<>();
        HomeApi.DeviceAddRequest request = new HomeApi.DeviceAddRequest();
        request.deviceId = deviceid;
        request.authority = XLinkRestfulEnum.DeviceAuthority.RW;
        request.subRole = XLinkRestfulEnum.DeviceSubscribeRole.USER;
        try {
            Response<String> response = XLinkRestful.getApplicationApi()
                                                    .addHomeDevice(homeid, request)
                                                    .execute();
            if (response.isSuccessful()) {
                result.setResult(response.body());
                result.setSuccess(true);
            } else {
                XLinkRestfulError.ErrorWrapper.Error error = XLinkRestfulError.parseError(response);
                if (error == null) {
                    error = new XLinkRestfulError.ErrorWrapper.Error(response.message(), response.code());
                }
                result.setError(XLinkErrorCodeHelper.getErrorCodeName(error.code));
            }
        } catch (IOException e) {
            e.printStackTrace();
            result.setError(e.getMessage());
        }
        return result;
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

    public void deleteDeviceFromHome(@NonNull final String homeid, final int deviceid) {
        try {
            XLinkRestful.getApplicationApi()
                        .deleteHomeDevice(homeid, deviceid)
                        .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteDeviceFromHome(@NonNull final String homeid, final int deviceid, final XlinkRequestCallback<String> callback) {
        XLinkRestful.getApplicationApi()
                    .deleteHomeDevice(homeid, deviceid)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void inviteHomeMember(@NonNull final String homeid, @NonNull final String account, final XlinkRequestCallback<HomeApi.UserInviteResponse> callback) {
        HomeApi.UserInviteRequest request = new HomeApi.UserInviteRequest();
        request.account = account;
        request.authority = XLinkRestfulEnum.DeviceAuthority.RW;
        request.mode = XLinkRestfulEnum.InvitationType.USER_ID;
        request.role = XLinkRestfulEnum.HomeUserType.USER;
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 2);
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
//        request.expireTime = df.format(calendar.getTime());
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

    public void deleteHomeUser(final String homeid, final int userid, final XlinkRequestCallback<String> callback) {
        XLinkRestful.getApplicationApi().deleteHomeUser(homeid, userid).enqueue(callback);
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

    public void deleteHome(@NonNull final String homeid) {
        try {
            XLinkRestful.getApplicationApi()
                        .deleteHome(homeid)
                        .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteHome(@NonNull final String homeid, final XlinkRequestCallback<String> callback) {
        XLinkRestful.getApplicationApi()
                    .deleteHome(homeid)
                    .enqueue(callback);
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

    public XlinkResult<RoomApi.RoomResponse> createRoom(@NonNull final String homeid, final String name) {
        XlinkResult<RoomApi.RoomResponse> result = new XlinkResult<>();
        RoomApi roomApi = mRetrofit.create(RoomApi.class);
        RoomApi.RoomRequest request = new RoomApi.RoomRequest();
        request.name = name;
        try {
            Response<RoomApi.RoomResponse> response = roomApi.postRoom(homeid, request).execute();
            if (response.isSuccessful()) {
                result.setResult(response.body());
                result.setSuccess(true);
            } else {
                XLinkRestfulError.ErrorWrapper.Error error = XLinkRestfulError.parseError(response);
                if (error == null) {
                    error = new XLinkRestfulError.ErrorWrapper.Error(response.message(), response.code());
                }
                result.setError(XLinkErrorCodeHelper.getErrorCodeName(error.code));
            }
        } catch (IOException e) {
            e.printStackTrace();
            result.setError(e.getMessage());
        }
        return result;
    }

    public void createRoom(@NonNull final String homeid, final String name, final XlinkRequestCallback<RoomApi.RoomResponse> callback) {
        RoomApi roomApi = mRetrofit.create(RoomApi.class);
        RoomApi.RoomRequest request = new RoomApi.RoomRequest();
        request.name = name;
        roomApi.postRoom(homeid, request)
               .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void renameRoom(@NonNull final String homeid, @NonNull final String roomid, final String name, final XlinkRequestCallback<RoomApi.RoomResponse> callback) {
        RoomApi roomApi = mRetrofit.create(RoomApi.class);
        RoomApi.RoomRequest request = new RoomApi.RoomRequest();
        request.name = name;
        roomApi.putRoom(homeid, roomid, request)
               .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void deleteRoom(@NonNull final String homeid, @NonNull final String roomid) {
        RoomApi roomApi = mRetrofit.create(RoomApi.class);
        try {
            roomApi.deleteRoom(homeid, roomid)
                   .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteRoom(@NonNull final String homeid, @NonNull final String roomid, final XlinkRequestCallback<String> callback) {
        RoomApi roomApi = mRetrofit.create(RoomApi.class);
        roomApi.deleteRoom(homeid, roomid)
               .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void getRoomInfo(@NonNull final String homeid, @NonNull final String roomid, final XlinkRequestCallback<RoomApi.RoomInfoResponse> callback) {
        RoomApi roomApi = mRetrofit.create(RoomApi.class);
        roomApi.getRoomInfo(homeid, roomid)
               .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public XlinkResult<String> addRoomDevice(@NonNull final String homeid, @NonNull final String roomid, final int device_id) {
        XlinkResult<String> result = new XlinkResult<>();
        RoomApi roomApi = mRetrofit.create(RoomApi.class);
        RoomApi.RoomDeviceRequest request = new RoomApi.RoomDeviceRequest();
        request.device_id = device_id;
        try {
            Response<String> response = roomApi.addRoomDevice(homeid, roomid, request).execute();
            if (response.isSuccessful()) {
                result.setResult(response.body());
                result.setSuccess(true);
            } else {
                XLinkRestfulError.ErrorWrapper.Error error = XLinkRestfulError.parseError(response);
                if (error == null) {
                    error = new XLinkRestfulError.ErrorWrapper.Error(response.message(), response.code());
                }
                result.setError(XLinkErrorCodeHelper.getErrorCodeName(error.code));
            }
        } catch (IOException e) {
            e.printStackTrace();
            result.setError(e.getMessage());
        }
        return result;
    }

    public void addRoomDevice(@NonNull final String homeid, @NonNull final String roomid, final int device_id, final XlinkRequestCallback<String> callback) {
        RoomApi roomApi = mRetrofit.create(RoomApi.class);
        RoomApi.RoomDeviceRequest request = new RoomApi.RoomDeviceRequest();
        request.device_id = device_id;
        roomApi.addRoomDevice(homeid, roomid, request)
               .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void removeRoomDevice(@NonNull final String homeid, @NonNull final String roomid, final int device_id, final XlinkRequestCallback<String> callback) {
        RoomApi roomApi = mRetrofit.create(RoomApi.class);
        RoomApi.RoomDeviceRequest request = new RoomApi.RoomDeviceRequest();
        request.device_id = device_id;
        roomApi.removeRoomDevice(homeid, roomid, request)
               .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void createZone(@NonNull final String homeid, @NonNull final String name, final XlinkRequestCallback<ZoneApi.ZoneResponse> callback) {
        ZoneApi zoneApi = mRetrofit.create(ZoneApi.class);
        ZoneApi.ZoneRequest request = new ZoneApi.ZoneRequest();
        request.name = name;
        zoneApi.postZone(homeid, request)
               .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void renameZone(@NonNull final String homeid, @NonNull final String zoneid, @NonNull final String name, final XlinkRequestCallback<ZoneApi.ZoneResponse> callback) {
        ZoneApi zoneApi = mRetrofit.create(ZoneApi.class);
        ZoneApi.ZoneRequest request = new ZoneApi.ZoneRequest();
        request.name = name;
        zoneApi.putZone(homeid, zoneid, request)
               .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void deleteZone(@NonNull final String homeid, @NonNull final String zoneid, final XlinkRequestCallback<String> callback) {
        ZoneApi zoneApi = mRetrofit.create(ZoneApi.class);
        zoneApi.deleteZone(homeid, zoneid)
               .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void getZoneInfo(@NonNull final String homeid, @NonNull final String zoneid, final XlinkRequestCallback<ZoneApi.ZoneInfoResponse> callback) {
        ZoneApi zoneApi = mRetrofit.create(ZoneApi.class);
        zoneApi.getzoneInfo(homeid, zoneid)
               .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public boolean addZoneRoom(@NonNull final String homeid, @NonNull final String zoneid, @NonNull final String roomid) {
        ZoneApi zoneApi = mRetrofit.create(ZoneApi.class);
        ZoneApi.ZoneRoomRequest request = new ZoneApi.ZoneRoomRequest();
        request.room_id = roomid;
        try {
            Response<String> response = zoneApi.addZoneRoom(homeid, zoneid, request).execute();
            return response.isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addZoneRoom(@NonNull final String homeid, @NonNull final String zoneid, @NonNull final String roomid, final XlinkRequestCallback<String> callback) {
        ZoneApi zoneApi = mRetrofit.create(ZoneApi.class);
        ZoneApi.ZoneRoomRequest request = new ZoneApi.ZoneRoomRequest();
        request.room_id = roomid;
        zoneApi.addZoneRoom(homeid, zoneid, request)
               .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public boolean removeZoneRoom(@NonNull final String homeid, @NonNull final String zoneid, @NonNull final String roomid) {
        ZoneApi zoneApi = mRetrofit.create(ZoneApi.class);
        ZoneApi.ZoneRoomRequest request = new ZoneApi.ZoneRoomRequest();
        request.room_id = roomid;
        try {
            Response<String> response = zoneApi.removeZoneRoom(homeid, zoneid, request).execute();
            return response.isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void removeZoneRoom(@NonNull final String homeid, @NonNull final String zoneid, @NonNull final String roomid, final XlinkRequestCallback<String> callback) {
        ZoneApi zoneApi = mRetrofit.create(ZoneApi.class);
        ZoneApi.ZoneRoomRequest request = new ZoneApi.ZoneRoomRequest();
        request.room_id = roomid;
        zoneApi.removeZoneRoom(homeid, zoneid, request)
               .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void registerFCMMessageService(@NonNull final String token, final XlinkRequestCallback<String> callback) {
        final int usrid = XLinkUserManager.getInstance().getUid();
        if (usrid == 0) {
            return;
        }
        PushApi.RegisterRequest request = new PushApi.RegisterRequest();
        request.appId = AppConstants.APPID;
        request.deviceToken = token;
        XLinkRestful.getApplicationApi()
                    .userRegisterGcm(usrid, request)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void unregisterFCMMessageService(final int usrid, final XlinkRequestCallback<String> callback) {
        if (usrid == 0) {
            return;
        }
        PushApi.UnregisterRequest request = new PushApi.UnregisterRequest();
        request.appId = AppConstants.APPID;
        XLinkRestful.getApplicationApi()
                    .userUnregisterGcm(usrid, request)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void registerAlipush(@NonNull final String deviceToken, XLinkRestfulEnum.PushMessageOpenType openType,
                                String openInput, final XlinkRequestCallback<String> callback) {
        UserMessageApi.RegisterAlipushRequest request = new UserMessageApi.RegisterAlipushRequest();
        request.appId = AppConstants.APPID;
        request.deviceToken = deviceToken;
        request.noticed = true;
        request.openType = openType;
        request.notifyType = XLinkRestfulEnum.PushMessageNotifyType.SOUND;
        if (openType == XLinkRestfulEnum.PushMessageOpenType.ACTIVITY) {
            request.activity = openInput;
        } else if (openType == XLinkRestfulEnum.PushMessageOpenType.URL) {
            request.openUrl = openInput;
        }

        XLinkRestful.getApplicationApi().postUserRegisterAlipush(XLinkUserManager.getInstance().getUid(), request).enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void unregisterAlipush(final int usrid, final XlinkRequestCallback<String> callback) {
        UserMessageApi.UnregisterAlipushRequest request = new UserMessageApi.UnregisterAlipushRequest();
        request.appId = AppConstants.APPID;

        XLinkRestful.getApplicationApi().postUserUnregisterAlipush(usrid, request).enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void getNotifyMessages(final XlinkRequestCallback<CommonQuery.Response<UserMessageApi.Message>> callback) {
        CommonQuery.Request request = new CommonQuery.Request();
        request.offset = 0;
        request.limit = 50;
        XLinkRestful.getApplicationApi()
                    .getUserMessageList(XLinkUserManager.getInstance().getUid(), request)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void getNotifyMessages(final int devid, final XlinkRequestCallback<CommonQuery.Response<UserMessageApi.Message>> callback) {
        CommonQuery.Request request = new CommonQuery.Request();
        request.offset = 0;
        request.limit = 50;
        request.query = new HashMap<>();
        request.query.put("from", new CommonQuery.Equal<>(devid));
        XLinkRestful.getApplicationApi()
                    .getUserMessageList(XLinkUserManager.getInstance().getUid(), request)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void setNotifyMessagesRead(@NonNull final String[] msgids, final XlinkRequestCallback<String> callback) {
        XLinkRestful.getApplicationApi()
                    .setMessageRead(XLinkUserManager.getInstance().getUid(), msgids)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    public void deleteNotifyMessages(@NonNull final String[] msgids, final XlinkRequestCallback<String> callback) {
        XLinkRestful.getApplicationApi()
                    .deleteMessage(XLinkUserManager.getInstance().getUid(), msgids)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    /**
     * 根据MAC地址查询单个设备 请求参数字符串, 返回mac及sn字段
     * @param mac
     * @return
     */
    private String getQueryDeviceJson(@NonNull final String mac) {
        JsonArray filter = new JsonArray();
        filter.add("mac");
        filter.add("sn");
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(mac);
        JsonObject condition = new JsonObject();
        condition.add("$in", jsonArray);        //{"$in":[""]}
        JsonObject field = new JsonObject();
        field.add("mac", condition);            //{"mac":{"$in":[""]}}
        JsonObject json = new JsonObject();
        json.addProperty("offset", 0);
        json.addProperty("limit", 1);
        json.add("filter", filter);
        json.add("query", field);
        return json.toString();
    }

    public void queryDevice(@NonNull final String pid, @NonNull final String mac, final OKHttpManager.HttpCallback<QueryDeviceResponse> callback) {
        final String url = String.format(AppConstants.XCP_QUERY_DEVICE_URL, pid);
        Headers headers = new Headers.Builder().add("Access-Token", AppConstants.XCP_ACCESS_TOKEN_ADMIN).build();
        String json = getQueryDeviceJson(mac);
        Log.e(TAG, "queryDevice: " + json);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public QueryDeviceResponse queryDeviceBlock(@NonNull final String pid, @NonNull final String mac) {
        final String url = String.format(AppConstants.XCP_QUERY_DEVICE_URL, pid);
        Headers headers = new Headers.Builder().add("Access-Token", AppConstants.XCP_ACCESS_TOKEN_ADMIN).build();
        String json = getQueryDeviceJson(mac);
        Log.e(TAG, "queryDevice: " + json);
        return OKHttpManager.getInstance().blockPost(url, headers, json, QueryDeviceResponse.class);
    }

    private String getImportDeviceJson(final String name, @NonNull final String mac, final String sn) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("mac", mac);
        if (!TextUtils.isEmpty(name)) {
            jsonObject.addProperty("name", name);
        }
        if (!TextUtils.isEmpty(sn)) {
            jsonObject.addProperty("sn", sn);
        }
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(jsonObject);
        return jsonArray.toString();
    }

    public void registerDevice(@NonNull final String pid, final String name, @NonNull final String mac, final String sn,
                               final OKHttpManager.HttpCallback<ImportDeviceResponse> callback) {
        final String url = String.format(AppConstants.XCP_IMPORT_DEVICE_URL, pid);
        Headers headers = new Headers.Builder().add("Access-Token", AppConstants.XCP_ACCESS_TOKEN_ADMIN).build();
        String json = getImportDeviceJson(name, mac, sn);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public ImportDeviceResponse registerDeviceBlock(@NonNull final String pid, final String name, @NonNull final String mac, final String sn) {
        final String url = String.format(AppConstants.XCP_IMPORT_DEVICE_URL, pid);
        Headers headers = new Headers.Builder().add("Access-Token", AppConstants.XCP_ACCESS_TOKEN_ADMIN).build();
        String json = getImportDeviceJson(name, mac, sn);
        return OKHttpManager.getInstance().blockPost(url, headers, json, ImportDeviceResponse.class);
    }

    public void getCurrentHomeId(final XlinkRequestCallback<String> callback) {
        XLinkRestful.getApplicationApi()
                    .getUserProperty(XLinkUserManager.getInstance().getUid(), XlinkConstants.CURRENT_HOME_ID)
                    .enqueue(new XlinkRequestCallback<String>() {
                        @Override
                        public void onError(String error) {
                            if (callback != null) {
                                callback.onError(error);
                            }
                        }

                        @Override
                        public void onSuccess(String s) {
                            String homeid = null;
                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                homeid = jsonObject.getString(XlinkConstants.CURRENT_HOME_ID);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (callback != null) {
                                callback.onSuccess(homeid);
                            }
                        }
                    });
        if (callback != null) {
            callback.onStart();
        }
    }

    public void setCurrentHomeId(final String homeid, final XlinkRequestCallback<String> callback) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(XlinkConstants.CURRENT_HOME_ID, homeid);
        XLinkRestful.getApplicationApi()
                    .setUserProperty(XLinkUserManager.getInstance().getUid(), jsonObject.toString())
                    .enqueue(new XlinkRequestCallback<String>() {
                        @Override
                        public void onError(String error) {
                            if (callback != null) {
                                callback.onError(error);
                            }
                        }

                        @Override
                        public void onSuccess(String s) {
                            if (callback != null) {
                                callback.onSuccess(homeid);
                            }
                        }
                    });
        if (callback != null) {
            callback.onStart();
        }
    }

    public void deleteCurrentHomeId(final XlinkRequestCallback<String> callback) {
        XLinkRestful.getApplicationApi()
                    .deleteUserProperty(XLinkUserManager.getInstance().getUid(), XlinkConstants.CURRENT_HOME_ID)
                    .enqueue(callback);
        if (callback != null) {
            callback.onStart();
        }
    }

    private static class LazyHolder {
        private static final XlinkCloudManager INSTANCE = new XlinkCloudManager();
    }
}
