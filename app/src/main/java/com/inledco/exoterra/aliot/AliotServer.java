package com.inledco.exoterra.aliot;

import android.support.annotation.NonNull;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.inledco.exoterra.manager.OKHttpManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Headers;

public class AliotServer {

    private final String TAG = "AliotServer";

    private final String APPKEY = "a1yk0nvw5UI";

    private final String API_SERVER = "https://xx.xx.xx.xx:port";

    private final String USER_REGISTER = "/user_register";

    private final String USER_LOGIN = "/user_login";

    private final String MODIFY_USER_INFO = "/user_info/%1$s";

    private final String GET_USER_INFO = "/user_info/%1$s";

    private final String MODIFY_PASSWORD = "/user/password/modify";

    private final String RESET_PASSWORD = "/user/password/reset";

    private final String GET_SUB_DEVICES = "/user/%1$s/subscribe_devices";

    private final String SUBSCRIBE_DEVICE = "/user/%1$s/subscribe";

    private final String UNSUBSCRIBE_DEVICE = "/user/%1$s/unsubscribe";

    private final String MODIFY_DEVICE_INFO = "/product/%1$s/device/%2$s";

    private final String GET_DEVICE_INFO = "/product/%1$s/device/%2$s";

    private final String CREATE_GROUP = "/group";

    private final String MODIFY_GROUP_INFO = "/group/%1$s";

    private final String GET_GROUP_INFO = "/group/%1$s";

    private final String DELETE_GROUP = "/group/%1$s";

    private final String EXIT_GROUP = "/group/%1$s/user/%2$s";

    private final String GET_USER_GROUPS = "/groups?userid=%1$s";

    private final String GROUP_ADD_DEVICE = "/group/%1$s/add_device";

    private final String GROUP_REMOVE_DEVICE = "/group/%1$s/device";

    private final String GET_GROUP_DEVICES = "/group/%1$s/devices";

    private final String GROUP_INVITE_USER = "/group/%1$s/invite";

    private final String GROUP_REMOVE_USER = "/group/%1$s/user/%2$s";

    private final String GROUP_INVITE_CANCEL = "/group/%1$s/invite_cancel";

    private final String GROUP_INVITE_ACCEPT = "/group/%1$s/invite_accept";

    private final String GROUP_INVITE_DENY = "/group/%1$s/invite_deny";

    private final String INVITER_GET_LIST = "/groups/invite_list?inviter=%1$s";

    private final String INVITEE_GET_LIST = "/groups/invite_list?invitee=%1$s";

    private final String GET_FIRMWARE_LIST = "/product/%1$s/firmware_list";


    private String mUserid;
    private String mToken;

    private AliotServer() {

    }

    public static AliotServer getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void test() {
        String url = "https://api2.xlink.cn/v2/user_auth";
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .build();
        Map<String, String> map = new HashMap<>();
        map.put("corp_id", "100fa8b8584bdc00");
        map.put("email", "cylary1218@gmail.com");
        map.put("password", "a2bjsscc");

        final String request = JSON.toJSONString(map);
        OKHttpManager.getInstance().post(url, headers, request, new HttpCallback<UserApi.TestResponse>() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onError(int code, String msg) {
                Log.e(TAG, "onError: " + code + " " + msg);
            }

            @Override
            public void onSuccess(UserApi.TestResponse result) {
                Log.e(TAG, "onSuccess: " + JSON.toJSONString(result));
            }
        });
    }

    public void init(@NonNull final String userid, @NonNull final String token) {
        mUserid = userid;
        mToken = token;
    }

    public void register(final String email, final String password, final String veryfycode, final String nickname) {
        String url = API_SERVER + USER_REGISTER;
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .build();
        UserApi.UserRegisterRequest request = new UserApi.UserRegisterRequest();
        request.corpid = APPKEY;
        request.email = email;
        request.password = password;
        request.verifycode = veryfycode;
        request.nickname = nickname;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, null);
    }

    public void login(final String email, final String password, HttpCallback<UserApi.UserLoginResponse> callback) {
        String url = API_SERVER + USER_LOGIN;
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .build();
        UserApi.UserLoginRequest request = new UserApi.UserLoginRequest();
        request.corpid = APPKEY;
        request.email = email;
        request.password = password;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public void getUserInfo(final String userid, final String token, HttpCallback<UserApi.UserInfoResponse> callback) {
        String url = API_SERVER + String.format(GET_USER_INFO, userid);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void modifyUserNickname(final String userid, final String token, final String nickname) {
        String url = API_SERVER + String.format(MODIFY_USER_INFO, userid);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        UserApi.UserInfoRequest request = new UserApi.UserInfoRequest();
        request.nickname = nickname;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().put(url, headers, json, null);
    }

    public void modifyUserInfo(final String userid, final String token, final UserApi.UserInfoRequest request) {
        String url = API_SERVER + String.format(MODIFY_USER_INFO, userid);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().put(url, headers, json, null);
    }

    public void modifyPassword(final String token, final String old_psw, final String new_psw) {
        String url = API_SERVER + MODIFY_PASSWORD;
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        UserApi.ModifyPasswordRequest request = new UserApi.ModifyPasswordRequest();
        request.old_password = old_psw;
        request.new_password = new_psw;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().put(url, headers, json, null);
    }

    public void resetPassword(final String email, final String verifycode, final String new_psw) {
        String url = API_SERVER + RESET_PASSWORD;
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .build();
        UserApi.ResetPasswordRequest request = new UserApi.ResetPasswordRequest();
        request.corpid = APPKEY;
        request.verifycode = verifycode;
        request.new_password = new_psw;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().put(url, headers, json, null);
    }

    public void getSubscribeDevices(final String token, HttpCallback<UserApi.UserSubscribedDevicesResponse> callback) {
        String url = API_SERVER + GET_SUB_DEVICES;
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void subscribeDevice(final String userid, final String token, final String pkey, final String dname, final String mac,
                                HttpCallback<UserApi.SubscribeDeviceResponse> callback) {
        String url = API_SERVER + String.format(SUBSCRIBE_DEVICE, userid);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        UserApi.SubscribeDeviceRequest request = new UserApi.SubscribeDeviceRequest();
        request.product_key = pkey;
        request.device_name = dname;
        request.mac = mac;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public void unsubscribeDevice(final String userid, final String token, final String pkey, final String dname, final String mac) {
        String url = API_SERVER + String.format(UNSUBSCRIBE_DEVICE, userid);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        UserApi.UnsubscribeDeviceRequest request = new UserApi.UnsubscribeDeviceRequest();
        request.product_key = pkey;
        request.device_name = dname;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, null);
    }

    public void modifyDeviceName(final String token, final String pkey, final String dname, final String name) {
        String url = API_SERVER + String.format(MODIFY_DEVICE_INFO, pkey, dname);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        UserApi.DeviceInfoRequest request = new UserApi.DeviceInfoRequest();
        request.name = name;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, null);
    }

    public void modifyDeviceInfo(final String token, final String pkey, final String dname, final UserApi.DeviceInfoRequest request) {
        String url = API_SERVER + String.format(MODIFY_DEVICE_INFO, pkey, dname);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().put(url, headers, json, null);
    }

    public void getDeviceInfo(final String token, final String pkey, final String dname, HttpCallback<UserApi.DeviceInfoResponse> callback) {
        String url = API_SERVER + String.format(GET_DEVICE_INFO, pkey, dname);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void createGroup(final String token, final String name, HttpCallback<UserApi.GroupResponse> callback) {
        String url = API_SERVER + CREATE_GROUP;
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        UserApi.GroupRequest request = new UserApi.GroupRequest();
        request.name = name;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public void createGroup(final String token, final UserApi.GroupRequest request, HttpCallback<UserApi.GroupResponse> callback) {
        String url = API_SERVER + CREATE_GROUP;
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public void modifyGroupName(final String token, final String groupid, final String name, HttpCallback<UserApi.GroupResponse> callback) {
        String url = API_SERVER + String.format(MODIFY_GROUP_INFO, groupid);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        UserApi.GroupRequest request = new UserApi.GroupRequest();
        request.name = name;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().put(url, headers, json, callback);
    }

    public void modifyGroupInfo(final String token, final String groupid, final UserApi.GroupRequest request, HttpCallback<UserApi.GroupResponse> callback) {
        String url = API_SERVER + String.format(MODIFY_GROUP_INFO, groupid);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().put(url, headers, json, callback);
    }

    public void getGroupInfo(final String token, final String groupid, HttpCallback<UserApi.GroupResponse> callback) {
        String url = API_SERVER + String.format(GET_GROUP_INFO, groupid);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void deleteGroup(final String token, final String groupid) {
        String url = API_SERVER + String.format(DELETE_GROUP, groupid);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        OKHttpManager.getInstance().delete(url, headers, (String) null, null);
    }

    public void exitGroup(final String token, final String groupid, final String userid) {
        String url = API_SERVER + String.format(EXIT_GROUP, groupid, userid);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        OKHttpManager.getInstance().delete(url, headers, (String) null, null);
    }

    public void getGroups(final String token, final String userid, HttpCallback<UserApi.GroupsResponse> callback) {
        String url = API_SERVER + String.format(GET_USER_GROUPS, userid);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void addDeviceToGroup(final String token, final String groupid, final String pkey, final String dname) {
        String url = API_SERVER + String.format(GROUP_ADD_DEVICE, groupid);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        UserApi.GroupAddDeviceRequest request = new UserApi.GroupAddDeviceRequest();
        request.product_key = pkey;
        request.device_name = dname;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, null);
    }

    public void removeDeviceFromGroup(final String token, final String groupid, final String pkey, final String dname) {
        String url = API_SERVER + String.format(GROUP_REMOVE_DEVICE, groupid);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        UserApi.GroupRemoveDeviceRequest request = new UserApi.GroupRemoveDeviceRequest();
        request.product_key = pkey;
        request.device_name = dname;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().delete(url, headers, json, null);
    }

    public void getGroupDevices(final String token, final String groupid, HttpCallback<UserApi.GroupDevicesResponse> callback) {
        String url = API_SERVER + String.format(GET_GROUP_DEVICES, groupid);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void inviteUserToGroup(final String token, final String groupid, final String email, HttpCallback<UserApi.GroupInviteResponse> callback) {
        String url = API_SERVER + String.format(GROUP_INVITE_USER, groupid);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        UserApi.GroupInviteRequest request = new UserApi.GroupInviteRequest();
        request.email = email;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public void removeUserFromGroup(final String token, final String groupid, final String userid) {
        String url = API_SERVER + String.format(GROUP_REMOVE_USER, groupid, userid);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        OKHttpManager.getInstance().delete(url, headers, (String) null, null);
    }

    public void cancelInvite(final String token, final String groupid, final String invite_id) {
        String url = API_SERVER + String.format(GROUP_INVITE_CANCEL, groupid);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        UserApi.InviteActionRequest request = new UserApi.InviteActionRequest();
        request.invite_id = invite_id;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, null);
    }

    public void acceptInvite(final String token, final String groupid, final String invite_id) {
        String url = API_SERVER + String.format(GROUP_INVITE_ACCEPT, groupid);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        UserApi.InviteActionRequest request = new UserApi.InviteActionRequest();
        request.invite_id = invite_id;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, null);
    }

    public void denyInvite(final String token, final String groupid, final String invite_id) {
        String url = API_SERVER + String.format(GROUP_INVITE_DENY, groupid);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        UserApi.InviteActionRequest request = new UserApi.InviteActionRequest();
        request.invite_id = invite_id;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, null);
    }

    public void getInviterList(final String token, final String userid, HttpCallback<UserApi.InviteListResponse> callback) {
        String url = API_SERVER + String.format(INVITER_GET_LIST, userid);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void getInviteeList(final String token, final String userid, HttpCallback<UserApi.InviteListResponse> callback) {
        String url = API_SERVER + String.format(INVITEE_GET_LIST, userid);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void getFirmwareList(final String token, final String pkey, HttpCallback<UserApi.FirmwaresResponse> callback) {
        String url = API_SERVER + String.format(GET_FIRMWARE_LIST, pkey);
        Headers headers = new Headers.Builder().add("Content-Type", "application/json")
                                               .add("Token", token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    private static class LazyHolder {
        private static final AliotServer INSTANCE = new AliotServer();
    }
}