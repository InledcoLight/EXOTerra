package com.liruya.exoterra.xlink;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.xlink.restful.XLinkRestful;
import cn.xlink.restful.XLinkRestfulEnum;
import cn.xlink.restful.api.app.DeviceApi;
import cn.xlink.restful.api.app.UserApi;
import cn.xlink.restful.api.app.UserAuthApi;
import cn.xlink.sdk.core.error.XLinkErrorCodeHelper;
import cn.xlink.sdk.core.error.XLinkErrorCodes;
import cn.xlink.sdk.core.model.XLinkDataPoint;
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
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
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

//    public void registerDevice(XDevice device, String name, final IXlinkRequestCallback<DeviceApi.RegisterDeviceResponse> callback) {
//        DeviceApi.RegisterDeviceRequest request = new DeviceApi.RegisterDeviceRequest();
//        request.accessKey = XLinkUserManager.getInstance().getAccessToken();
//        request.productId = device.getProductId();
//        request.mac = device.getMacAddress();
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
//                        callback.onSuccess(null);
//                    } else {
//                        callback.onError(XLinkErrorCodeHelper.getErrorCodeName(error.code));
//                    }
//                }
//            }
//
//            @Override
//            public void onSuccess(DeviceApi.RegisterDeviceResponse registerDeviceResponse) {
//                if (callback != null) {
//                    callback.onSuccess(registerDeviceResponse);
//                }
//            }
//        });
//        if (callback != null) {
//            callback.onStart();
//        }
//    }

    public void registerDevice(XDevice device, String name, final IXlinkRequestCallback<String> callback) {
        Map<String, String> params = new HashMap<>();
        params.put( "product_id", device.getProductId() );
        params.put( "mac", device.getMacAddress() );
        if (!TextUtils.isEmpty(name)) {
            params.put("name", name);
        }
        String json = new JSONObject(params ).toString();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        final Request request = new Request.Builder()
                                .url( "https://api2.xlink.cn/v2/user/" + XLinkUserManager.getInstance().getUid() + "/register_device" )
                                .header( "Access-Token", XLinkUserManager.getInstance().getAccessToken())
                                .post( requestBody )
                                .build();
        XLinkRestful.getApiHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    if (callback != null) {
                        callback.onSuccess(response.body().string());
                    }
                } else {
                    String s = response.body().string();
                    try {
                        JSONObject object = new JSONObject(s);
                        if (object != null && object.has("error")) {
                            JSONObject error = object.getJSONObject("error");
                            if (error != null && error.has("code") && error.has("msg")) {
                                int code = error.getInt("code");
                                if (callback != null) {
                                    if (code == XLinkErrorCodes.ERROR_API_DEVICE_MAC_ADDRESS_EXISTS) {
                                        callback.onSuccess(s);
                                    } else {
                                        callback.onError(XLinkErrorCodeHelper.getErrorCodeName(code));
                                    }
                                }
                                return;
                            }
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (callback != null) {
                        callback.onError(s);
                    }
                }
            }
        });
        if (callback != null) {
            callback.onStart();
        }
    }

    public void subscribeDevice(final XDevice device, byte[] pincode, int timeout, XLinkTaskListener<XDevice> listener) {
        XLinkAddDeviceTask task = XLinkAddDeviceTask.newBuilder()
                                                    .setXDevice(device)
                                                    .setConnectLocal(false)
                                                    .setClearCache(true)
                                                    .setNeedSubscription(true)
                                                    .setPinCode(pincode)
                                                    .setTimeout(timeout)
                                                    .setListener(listener)
                                                    .build();
        XLinkSDK.startTask(task);
    }

    public void unsubscribeDevice(XDevice device, XLinkTaskListener<String> listener) {
        //setUserId??
        XLinkRemoveDeviceTask task = XLinkRemoveDeviceTask.newBuilder()
                                                          .setXDevice(device)
                                                          .setListener(listener)
                                                          .build();
        XLinkSDK.startTask(task);
    }

    public void addLocalDevice(final XDevice device, byte[] pincode, int timeout, XLinkTaskListener<XDevice> listener) {
        XLinkAddDeviceTask task = XLinkAddDeviceTask.newBuilder()
                                                    .setXDevice(device)
                                                    .setConnectLocal(false)
                                                    .setClearCache(true)
                                                    .setNeedSubscription(false)
                                                    .setPinCode(pincode)
                                                    .setTimeout(timeout)
                                                    .setListener(listener)
                                                    .build();
        XLinkSDK.startTask(task);
    }

    public void scanDevice(List<String> pid, int timeout, XLinkScanDeviceListener listener) {
        XLinkScanDeviceTask task = XLinkScanDeviceTask.newBuilder()
                                                      .setTotalTimeout(timeout)
                                                      .setProductIds(pid)
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

    public void getDeviceMetaDatapoints(XDevice device, XLinkTaskListener<List<XLinkDataPoint>> listener) {
        XLinkGetDataPointMetaInfoTask task = XLinkGetDataPointMetaInfoTask.newBuilder()
                                                                          .setProductId(device.getProductId())
                                                                          .setListener(listener)
                                                                          .build();
        XLinkSDK.startTask(task);
    }

    public void getDeviceTemplateDatapoints(XDevice device, XLinkTaskListener<List<DeviceApi.DataPointsResponse>> listener) {
        XLinkGetDataPointTemplateTask task = XLinkGetDataPointTemplateTask.newBuilder()
                                                                          .setProductId(device.getProductId())
                                                                          .setListener(listener)
                                                                          .build();
        XLinkSDK.startTask(task);
    }

    public void getDeviceDatapoints(XDevice device, XLinkTaskListener<List<XLinkDataPoint>> listener) {
        XLinkGetDataPointTask task = XLinkGetDataPointTask.newBuilder()
                                                          .setXDevice(device)
                                                          .setListener(listener)
                                                          .build();
        XLinkSDK.startTask(task);
    }

    public void setDeviceDatapoints(XDevice device, List<XLinkDataPoint> datapoints, XLinkTaskListener<XDevice> listener) {
        XLinkSetDataPointTask task = XLinkSetDataPointTask.newBuilder()
                                                          .setXDevice(device)
                                                          .setDataPoints(datapoints)
                                                          .setListener(listener)
                                                          .build();
        XLinkSDK.startTask(task);
    }

    public void setDeviceDatapoints(final XDevice device, List<DeviceApi.DeviceDataPointRequest.Command> commands, final XlinkRequestCallback<String> callback) {
        DeviceApi.DeviceDataPointRequest request = new DeviceApi.DeviceDataPointRequest();
        request.source = XLinkRestfulEnum.DataPointSource.APPLICATION_SET;
        request.command = commands;
        Call<String> call = XLinkRestful.getApplicationApi()
                                        .setDeviceDataPoint(device.getDeviceId(), request);
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

    private static class LazyHolder {
        private static final XlinkCloudManager INSTANCE = new XlinkCloudManager();
    }
}
