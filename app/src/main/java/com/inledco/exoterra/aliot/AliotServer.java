package com.inledco.exoterra.aliot;

import android.support.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.inledco.exoterra.manager.OKHttpManager;

import okhttp3.Headers;

public class AliotServer {

    private final String TAG = "AliotServer";

    private final String APPKEY = "a3jdKlLMaEn";

    private final String API_SERVER = "http://47.89.235.158:8086";

    private final String KEY_CONTENT_TYPE = "Content-Type";
    private final String CONTENT_TYPE = "application/json";
    private final String KEY_AUTH = "Authorization";
    private final String AUTH_TYPE = "Bearer ";

    //  {email}
    private final String EMAIL_GET_VERIFYCODE = "/emailSend/%1$s";

    private final String USER_REGISTER = "/user_register";

    private final String USER_LOGIN = "/login?grant_type=app";

    private final String USER_LOGOUT = "/sys/logout";

    private final String MODIFY_USER_INFO = "/user_info/%1$s";

    //  {userid}
    private final String GET_USER_INFO = "/user_info/%1$s";

    private final String MODIFY_PASSWORD = "/user/password/modify";

    private final String RESET_PASSWORD = "/user/password/reset";

    //  {userid}
    private final String GET_SUB_DEVICES = "/user/%1$s/subscribe_devices";

    //  {userid}
    private final String SUBSCRIBE_DEVICE = "/user/%1$s/subscribe";

    //  {userid}
    private final String UNSUBSCRIBE_DEVICE = "/user/%1$s/unsubscribe";

    //  {productKey} {deviceName}
    private final String MODIFY_DEVICE_INFO = "/product/%1$s/device/%2$s";

    //  {productKey} {deviceName}
    private final String GET_DEVICE_INFO = "/product/%1$s/device/%2$s";

    private final String CREATE_GROUP = "/group";

    //  {groupid}
    private final String MODIFY_GROUP_INFO = "/group/%1$s";

    //  {groupid}
    private final String GET_GROUP_INFO = "/group/%1$s";

    //  {groupid}
    private final String DELETE_GROUP = "/group/%1$s";

    //  {groupid} {userid}
    private final String EXIT_GROUP = "/group/%1$s/user/%2$s";

    //  {userid}
    private final String GET_USER_GROUPS = "/groups?userid=%1$s";

    //  {groupid}
    private final String GROUP_ADD_DEVICE = "/group/%1$s/add_device";

    //  {groupid}
    private final String GROUP_REMOVE_DEVICE = "/group/%1$s/device";

    //  {groupid}
    private final String GET_GROUP_DEVICES = "/group/%1$s/devices";

    //  {groupid}
    private final String GROUP_INVITE_USER = "/group/%1$s/invite";

    //  {groupid} {userod}
    private final String GROUP_REMOVE_USER = "/group/%1$s/user/%2$s";

    //  {groupid}
    private final String GROUP_INVITE_CANCEL = "/group/%1$s/invite_cancel";

    //  {groupid}
    private final String GROUP_INVITE_ACCEPT = "/group/%1$s/invite_accept";

    //  {groupid}
    private final String GROUP_INVITE_DENY = "/group/%1$s/invite_deny";

    //  {userid}
    private final String INVITER_GET_LIST = "/groups/invite_list?inviter=%1$s";

    //  {userid}
    private final String INVITEE_GET_LIST = "/groups/invite_list?invitee=%1$s";

    //  {productKey}
    private final String GET_FIRMWARE_LIST = "/product/%1$s/firmware_list";

    private String mUserid;
    private String mToken;

    private AliotServer() {

    }

    public static AliotServer getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void init(@NonNull final String userid, @NonNull final String token) {
        mUserid = userid;
        mToken = token;
    }

    public void getEmailVerifycode(final String email, HttpCallback<UserApi.Response> callback) {
        String url = API_SERVER + String.format(EMAIL_GET_VERIFYCODE, email);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void register(final String email, final String password, final String verifycode, final String nickname, HttpCallback<UserApi.Response> callback) {
        String url = API_SERVER + USER_REGISTER;
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .build();
        UserApi.UserRegisterRequest request = new UserApi.UserRegisterRequest();
        request.corpid = APPKEY;
        request.productKey = APPKEY;
        request.email = email;
        request.password = password;
        request.verifycode = verifycode;
        request.nickname = nickname;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public UserApi.UserLoginResponse login(final String email, final String password) {
        String url = API_SERVER + USER_LOGIN;
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .build();
        UserApi.UserLoginRequest request = new UserApi.UserLoginRequest();
        request.principal = "password@" + email;
        request.credentials = password;
        String json = JSON.toJSONString(request);
        UserApi.UserLoginResponse response = OKHttpManager.getInstance().blockPost(url, headers, json, UserApi.UserLoginResponse.class);
        return response;
    }

    public void login(final String email, final String password, HttpCallback<UserApi.UserLoginResponse> callback) {
        String url = API_SERVER + USER_LOGIN;
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .build();
        UserApi.UserLoginRequest request = new UserApi.UserLoginRequest();
        request.principal = "password@" + email;
        request.credentials = password;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public void logout(final String token, HttpCallback<UserApi.Response> callback) {
        String url = API_SERVER + USER_LOGOUT;
        Headers headers = new Headers.Builder().add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().post(url, headers, "", callback);
    }

    public UserApi.GetUserInfoResponse getUserInfo(final String userid, final String token) {
        String url = API_SERVER + String.format(GET_USER_INFO, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.GetUserInfoResponse response = OKHttpManager.getInstance().blockGet(url, headers, UserApi.GetUserInfoResponse.class);
        return response;
    }

    public void getUserInfo(final String userid, final String token, HttpCallback<UserApi.GetUserInfoResponse> callback) {
        String url = API_SERVER + String.format(GET_USER_INFO, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void modifyUserNickname(final String userid, final String token, final String nickname, HttpCallback<UserApi.Response> callback) {
        String url = API_SERVER + String.format(MODIFY_USER_INFO, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.SetUserInfoRequest request = new UserApi.SetUserInfoRequest();
        request.nickname = nickname;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().put(url, headers, json, callback);
    }

    public void modifyUserInfo(final String userid, final String token, final UserApi.SetUserInfoRequest request, HttpCallback<UserApi.Response> callback) {
        String url = API_SERVER + String.format(MODIFY_USER_INFO, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().put(url, headers, json, callback);
    }

    public void modifyPassword(final String token, final String old_psw, final String new_psw, HttpCallback<UserApi.Response> callback) {
        String url = API_SERVER + MODIFY_PASSWORD;
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.ModifyPasswordRequest request = new UserApi.ModifyPasswordRequest();
        request.old_password = old_psw;
        request.new_password = new_psw;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().put(url, headers, json, callback);
    }

    public void resetPassword(final String email, final String verifycode, final String new_psw, HttpCallback<UserApi.Response> callback) {
        String url = API_SERVER + RESET_PASSWORD;
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .build();
        UserApi.ResetPasswordRequest request = new UserApi.ResetPasswordRequest();
        request.corpid = APPKEY;
        request.email = email;
        request.verifycode = verifycode;
        request.new_password = new_psw;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().put(url, headers, json, callback);
    }

    public void getSubscribeDevices(final String userid, final String token, HttpCallback<UserApi.UserSubscribedDevicesResponse> callback) {
        String url = API_SERVER + String.format(GET_SUB_DEVICES, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void subscribeDevice(final String userid, final String token, final String pkey, final String dname, final String mac,
                                HttpCallback<UserApi.SubscribeDeviceResponse> callback) {
        String url = API_SERVER + String.format(SUBSCRIBE_DEVICE, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.SubscribeDeviceRequest request = new UserApi.SubscribeDeviceRequest();
        request.product_key = pkey;
        request.device_name = dname;
        request.mac = mac;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public UserApi.SubscribeDeviceResponse subscribeDevice(final String userid, final String token, final String pkey, final String dname, final String mac) {
        String url = API_SERVER + String.format(SUBSCRIBE_DEVICE, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.SubscribeDeviceRequest request = new UserApi.SubscribeDeviceRequest();
        request.product_key = pkey;
        request.device_name = dname;
        request.mac = mac;
        String json = JSON.toJSONString(request);
        return OKHttpManager.getInstance().blockPost(url, headers, json, UserApi.SubscribeDeviceResponse.class);
    }

    public void unsubscribeDevice(final String userid, final String token, final String pkey, final String dname, final String mac) {
        String url = API_SERVER + String.format(UNSUBSCRIBE_DEVICE, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.UnsubscribeDeviceRequest request = new UserApi.UnsubscribeDeviceRequest();
        request.product_key = pkey;
        request.device_name = dname;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, null);
    }

    public void modifyDeviceName(final String token, final String pkey, final String dname, final String name) {
        String url = API_SERVER + String.format(MODIFY_DEVICE_INFO, pkey, dname);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.ModifyDeviceInfoRequest request = new UserApi.ModifyDeviceInfoRequest();
        request.name = name;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, null);
    }

    public void modifyDeviceInfo(final String token, final String pkey, final String dname, final UserApi.ModifyDeviceInfoRequest request) {
        String url = API_SERVER + String.format(MODIFY_DEVICE_INFO, pkey, dname);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().put(url, headers, json, null);
    }

    public void getDeviceInfo(final String token, final String pkey, final String dname, HttpCallback<UserApi.DeviceInfoResponse> callback) {
        String url = API_SERVER + String.format(GET_DEVICE_INFO, pkey, dname);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void createGroup(final String token, final String name, HttpCallback<UserApi.GroupResponse> callback) {
        String url = API_SERVER + CREATE_GROUP;
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.GroupRequest request = new UserApi.GroupRequest();
        request.name = name;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public void createGroup(final String token, final UserApi.GroupRequest request, HttpCallback<UserApi.GroupResponse> callback) {
        String url = API_SERVER + CREATE_GROUP;
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public void modifyGroupName(final String token, final String groupid, final String name, HttpCallback<UserApi.GroupResponse> callback) {
        String url = API_SERVER + String.format(MODIFY_GROUP_INFO, groupid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.GroupRequest request = new UserApi.GroupRequest();
        request.name = name;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().put(url, headers, json, callback);
    }

    public void modifyGroupInfo(final String token, final String groupid, final UserApi.GroupRequest request, HttpCallback<UserApi.GroupResponse> callback) {
        String url = API_SERVER + String.format(MODIFY_GROUP_INFO, groupid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().put(url, headers, json, callback);
    }

    public void getGroupInfo(final String token, final String groupid, HttpCallback<UserApi.GroupResponse> callback) {
        String url = API_SERVER + String.format(GET_GROUP_INFO, groupid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void deleteGroup(final String token, final String groupid) {
        String url = API_SERVER + String.format(DELETE_GROUP, groupid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().delete(url, headers, (String) null, null);
    }

    public void exitGroup(final String token, final String groupid, final String userid) {
        String url = API_SERVER + String.format(EXIT_GROUP, groupid, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().delete(url, headers, (String) null, null);
    }

    public void getGroups(final String userid, final String token, HttpCallback<UserApi.GroupsResponse> callback) {
        String url = API_SERVER + String.format(GET_USER_GROUPS, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void addDeviceToGroup(final String token, final String groupid, final String pkey, final String dname) {
        String url = API_SERVER + String.format(GROUP_ADD_DEVICE, groupid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.GroupAddDeviceRequest request = new UserApi.GroupAddDeviceRequest();
        request.product_key = pkey;
        request.device_name = dname;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, null);
    }

    public void removeDeviceFromGroup(final String token, final String groupid, final String pkey, final String dname) {
        String url = API_SERVER + String.format(GROUP_REMOVE_DEVICE, groupid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.GroupRemoveDeviceRequest request = new UserApi.GroupRemoveDeviceRequest();
        request.product_key = pkey;
        request.device_name = dname;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().delete(url, headers, json, null);
    }

    public void getGroupDevices(final String token, final String groupid, HttpCallback<UserApi.GroupDevicesResponse> callback) {
        String url = API_SERVER + String.format(GET_GROUP_DEVICES, groupid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void inviteUserToGroup(final String token, final String groupid, final String email, HttpCallback<UserApi.GroupInviteResponse> callback) {
        String url = API_SERVER + String.format(GROUP_INVITE_USER, groupid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.GroupInviteRequest request = new UserApi.GroupInviteRequest();
        request.email = email;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public void removeUserFromGroup(final String token, final String groupid, final String userid) {
        String url = API_SERVER + String.format(GROUP_REMOVE_USER, groupid, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().delete(url, headers, (String) null, null);
    }

    public void cancelInvite(final String token, final String groupid, final String invite_id) {
        String url = API_SERVER + String.format(GROUP_INVITE_CANCEL, groupid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.InviteActionRequest request = new UserApi.InviteActionRequest();
        request.invite_id = invite_id;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, null);
    }

    public void acceptInvite(final String token, final String groupid, final String invite_id) {
        String url = API_SERVER + String.format(GROUP_INVITE_ACCEPT, groupid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.InviteActionRequest request = new UserApi.InviteActionRequest();
        request.invite_id = invite_id;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, null);
    }

    public void denyInvite(final String token, final String groupid, final String invite_id) {
        String url = API_SERVER + String.format(GROUP_INVITE_DENY, groupid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.InviteActionRequest request = new UserApi.InviteActionRequest();
        request.invite_id = invite_id;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, null);
    }

    public void getInviterList(final String token, final String userid, HttpCallback<UserApi.InviteListResponse> callback) {
        String url = API_SERVER + String.format(INVITER_GET_LIST, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void getInviteeList(final String token, final String userid, HttpCallback<UserApi.InviteListResponse> callback) {
        String url = API_SERVER + String.format(INVITEE_GET_LIST, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void getFirmwareList(final String token, final String pkey, HttpCallback<UserApi.FirmwaresResponse> callback) {
        String url = API_SERVER + String.format(GET_FIRMWARE_LIST, pkey);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    private static class LazyHolder {
        private static final AliotServer INSTANCE = new AliotServer();
    }
}