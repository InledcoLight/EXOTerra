package com.inledco.exoterra.aliot;

import android.support.annotation.NonNull;
import android.util.Base64;

import com.alibaba.fastjson.JSON;
import com.inledco.exoterra.AppConfig;
import com.inledco.exoterra.aliot.bean.InviteAction;
import com.inledco.exoterra.aliot.bean.InviteMessage;
import com.inledco.exoterra.manager.OKHttpManager;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Callback;
import okhttp3.Headers;

public class AliotServer {

    private final String TAG = "AliotServer";

//    private final String APP_KEY = "a3jdKlLMaEn";
    private final String APP_KEY;

//    private final String API_SERVER = "http://47.89.235.158:8086";
    private final String API_SERVER;

    private final String KEY_CONTENT_TYPE = "Content-Type";
    private final String CONTENT_TYPE_JSON = "application/json";
    private final String CONTENT_TYPE_FORM = "multipart/form-data";
    private final String KEY_AUTH = "Authorization";
    private final String AUTH_TYPE = "bearer ";

//    private final String SERVER_FILE_PATH = "http://47.89.235.158:8086/imgs/";
    private final String SERVER_FILE_PATH;

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
    private final String EXIT_GROUP = "/group/%1$s/user_exit/%2$s";

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
    private final String GROUP_REMOVE_USER = "/group/%1$s/remove_user/%2$s";

    //  {groupid}
    private final String GROUP_INVITE_CANCEL = "/group/%1$s/invite_cancel";

    //  {groupid}
    private final String GROUP_INVITE_ACCEPT = "/group/%1$s/invite_accept";

    //  {groupid}
    private final String GROUP_INVITE_DENY = "/group/%1$s/invite_deny";

    //  {userid}
    private final String INVITER_GET_LIST = "/groups/invite_list?inviter=%1$s";

    //  {userid}
    private final String INVITEE_GET_LIST = "/groups/invitee_list?invitee=%1$s";

    //  {productKey}
    private final String GET_FIRMWARE_LIST = "/product/%1$s/firmware_list";

    //  {productKey} {deviceName}
    private final String GET_DEVICE_PROPERTIES = "/product/%1$s/device/%2$s/get_properties";

    //  {productKey} {deviceName}
    private final String SET_DEVICE_PROPERTIES = "/product/%1$s/device/%2$s/set_properties";

    //  {productKey}
    private final String PUBLISH_TOPIC = "/product/%1$s/pub_topic";

    private final String COMMON_ALIYUNCS_API = "/aliyuncs/common";

    //  {productKey} {deviceName} {startTime} {endTime} {asc} {pageSize} {identifiers}
    private final String QUERY_DEVICE_HISTORY_PROPERTY = "/history/%1$s/%2$s?startTime=%3$d&endTime=%4$d&asc=%5$d&pageSize=%6$d&identifiers=%7$s";

    private final String UPLOAD_FILE = "/file/upload/file";

    private String mUserid;
    private String mToken;

    private AliotServer() {
        APP_KEY = AppConfig.getString("appKey");
        API_SERVER = AppConfig.getString("apiServer");
        SERVER_FILE_PATH = AppConfig.getString("fileUrl");
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
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void register(final String email, final String password, final String verifycode, final String nickname, HttpCallback<UserApi.Response> callback) {
        String url = API_SERVER + USER_REGISTER;
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .build();
        UserApi.UserRegisterRequest request = new UserApi.UserRegisterRequest();
        request.corpid = APP_KEY;
        request.productKey = APP_KEY;
        request.email = email;
        request.password = password;
        request.verifycode = verifycode;
        request.nickname = nickname;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public UserApi.UserLoginResponse login(final String email, final String password) {
        String url = API_SERVER + USER_LOGIN;
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
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
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
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

    public void logout(HttpCallback<UserApi.Response> callback) {
        logout(mToken, callback);
    }

    public UserApi.GetUserInfoResponse getUserInfo(final String userid, final String token) {
        String url = API_SERVER + String.format(GET_USER_INFO, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.GetUserInfoResponse response = OKHttpManager.getInstance().blockGet(url, headers, UserApi.GetUserInfoResponse.class);
        return response;
    }

    public UserApi.GetUserInfoResponse getUserInfo() {
        return getUserInfo(mUserid, mToken);
    }

    public void getUserInfo(final String userid, final String token, HttpCallback<UserApi.GetUserInfoResponse> callback) {
        String url = API_SERVER + String.format(GET_USER_INFO, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void getUserInfo(HttpCallback<UserApi.GetUserInfoResponse> callback) {
        getUserInfo(mUserid, mToken, callback);
    }


    public void modifyUserNickname(final String userid, final String token, final String nickname, HttpCallback<UserApi.Response> callback) {
        String url = API_SERVER + String.format(MODIFY_USER_INFO, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.SetUserInfoRequest request = new UserApi.SetUserInfoRequest();
        request.nickname = nickname;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().put(url, headers, json, callback);
    }

    public void modifyUserNickname(final String nickname, HttpCallback<UserApi.Response> callback) {
        modifyUserNickname(mUserid, mToken, nickname, callback);
    }

    public void modifyUserInfo(final String userid, final String token, final UserApi.SetUserInfoRequest request, HttpCallback<UserApi.Response> callback) {
        String url = API_SERVER + String.format(MODIFY_USER_INFO, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().put(url, headers, json, callback);
    }

    public void modifyUserInfo(final UserApi.SetUserInfoRequest request, HttpCallback<UserApi.Response> callback) {
        modifyUserInfo(mUserid, mToken, request, callback);
    }

    public void modifyPassword(final String token, final String old_psw, final String new_psw, HttpCallback<UserApi.Response> callback) {
        String url = API_SERVER + MODIFY_PASSWORD;
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.ModifyPasswordRequest request = new UserApi.ModifyPasswordRequest();
        request.old_password = old_psw;
        request.new_password = new_psw;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().put(url, headers, json, callback);
    }

    public void modifyPassword(final String old_psw, final String new_psw, HttpCallback<UserApi.Response> callback) {
        modifyPassword(mToken, old_psw, new_psw, callback);
    }

    public void resetPassword(final String email, final String verifycode, final String new_psw, HttpCallback<UserApi.Response> callback) {
        String url = API_SERVER + RESET_PASSWORD;
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .build();
        UserApi.ResetPasswordRequest request = new UserApi.ResetPasswordRequest();
        request.corpid = APP_KEY;
        request.email = email;
        request.verifycode = verifycode;
        request.new_password = new_psw;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().put(url, headers, json, callback);
    }

    public void getSubscribeDevices(final String userid, final String token, HttpCallback<UserApi.UserSubscribedDevicesResponse> callback) {
        String url = API_SERVER + String.format(GET_SUB_DEVICES, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void getSubscribeDevices(HttpCallback<UserApi.UserSubscribedDevicesResponse> callback) {
        getSubscribeDevices(mUserid, mToken, callback);
    }

    public void subscribeDevice(final String userid, final String token, final String pkey, final String dname, final String mac,
                                HttpCallback<UserApi.SubscribeDeviceResponse> callback) {
        String url = API_SERVER + String.format(SUBSCRIBE_DEVICE, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.SubscribeDeviceRequest request = new UserApi.SubscribeDeviceRequest();
        request.product_key = pkey;
        request.device_name = dname;
        request.mac = mac;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public void subscribeDevice(final String pkey, final String dname, final String mac, HttpCallback<UserApi.SubscribeDeviceResponse> callback) {
        subscribeDevice(mUserid, mToken, pkey, dname, mac, callback);
    }

    public UserApi.SubscribeDeviceResponse subscribeDevice(final String userid, final String token, final String pkey, final String dname, final String mac) {
        String url = API_SERVER + String.format(SUBSCRIBE_DEVICE, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.SubscribeDeviceRequest request = new UserApi.SubscribeDeviceRequest();
        request.product_key = pkey;
        request.device_name = dname;
        request.mac = mac;
        String json = JSON.toJSONString(request);
        return OKHttpManager.getInstance().blockPost(url, headers, json, UserApi.SubscribeDeviceResponse.class);
    }

    public UserApi.SubscribeDeviceResponse subscribeDevice(final String pkey, final String dname, final String mac) {
        return subscribeDevice(mUserid, mToken, pkey, dname, mac);
    }

    public void unsubscribeDevice(final String userid, final String token, final String pkey, final String dname, final HttpCallback<UserApi.Response> callback) {
        String url = API_SERVER + String.format(UNSUBSCRIBE_DEVICE, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.UnsubscribeDeviceRequest request = new UserApi.UnsubscribeDeviceRequest();
        request.product_key = pkey;
        request.device_name = dname;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public void unsubscribeDevice(final String pkey, final String dname, final HttpCallback<UserApi.Response> callback) {
        unsubscribeDevice(mUserid, mToken, pkey, dname, callback);
    }

    public void modifyDeviceName(final String token, final String pkey, final String dname, final String name, HttpCallback<UserApi.Response> callback) {
        String url = API_SERVER + String.format(MODIFY_DEVICE_INFO, pkey, dname);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.ModifyDeviceInfoRequest request = new UserApi.ModifyDeviceInfoRequest();
        request.name = name;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().put(url, headers, json, callback);
    }

    public void modifyDeviceName(final String pkey, final String dname, final String name, HttpCallback<UserApi.Response> callback) {
        modifyDeviceName(mToken, pkey, dname, name, callback);
    }

    public void modifyDeviceInfo(final String token, final String pkey, final String dname, final UserApi.ModifyDeviceInfoRequest request,
                                 final HttpCallback<UserApi.Response> callback) {
        String url = API_SERVER + String.format(MODIFY_DEVICE_INFO, pkey, dname);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().put(url, headers, json, callback);
    }

    public void modifyDeviceInfo(final String pkey, final String dname, final UserApi.ModifyDeviceInfoRequest request, final HttpCallback<UserApi.Response> callback) {
        modifyDeviceInfo(mToken, pkey, dname, request, callback);
    }

    public void getDeviceInfo(final String token, final String pkey, final String dname, HttpCallback<UserApi.DeviceInfoResponse> callback) {
        String url = API_SERVER + String.format(GET_DEVICE_INFO, pkey, dname);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void getDeviceInfo(final String pkey, final String dname, HttpCallback<UserApi.DeviceInfoResponse> callback) {
        getDeviceInfo(mToken, pkey, dname, callback);
    }

    public void createGroup(final String token, final String name, HttpCallback<UserApi.GroupResponse> callback) {
        String url = API_SERVER + CREATE_GROUP;
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.GroupRequest request = new UserApi.GroupRequest();
        request.name = name;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public void createGroup(final String name, HttpCallback<UserApi.GroupResponse> callback) {
        createGroup(mToken, name, callback);
    }

    public void createGroup(final String token, final UserApi.GroupRequest request, HttpCallback<UserApi.GroupResponse> callback) {
        String url = API_SERVER + CREATE_GROUP;
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public void createGroup(final UserApi.GroupRequest request, HttpCallback<UserApi.GroupResponse> callback) {
        createGroup(mToken, request, callback);
    }

    public void modifyGroupName(final String token, final String groupid, final String name, HttpCallback<UserApi.GroupResponse> callback) {
        String url = API_SERVER + String.format(MODIFY_GROUP_INFO, groupid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.GroupRequest request = new UserApi.GroupRequest();
        request.name = name;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().put(url, headers, json, callback);
    }

    public void modifyGroupName(final String groupid, final String name, HttpCallback<UserApi.GroupResponse> callback) {
        modifyGroupName(mToken, groupid, name, callback);
    }

    public void modifyGroupInfo(final String token, final String groupid, final UserApi.GroupRequest request, HttpCallback<UserApi.GroupResponse> callback) {
        String url = API_SERVER + String.format(MODIFY_GROUP_INFO, groupid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().put(url, headers, json, callback);
    }

    public void modifyGroupInfo(final String groupid, final UserApi.GroupRequest request, HttpCallback<UserApi.GroupResponse> callback) {
        modifyGroupInfo(mToken, groupid, request, callback);
    }

    public void getGroupInfo(final String token, final String groupid, HttpCallback<UserApi.GroupResponse> callback) {
        String url = API_SERVER + String.format(GET_GROUP_INFO, groupid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void getGroupInfo(final String groupid, HttpCallback<UserApi.GroupResponse> callback) {
        getGroupInfo(mToken, groupid, callback);
    }

    public void deleteGroup(final String token, final String groupid, HttpCallback<UserApi.Response> callback) {
        String url = API_SERVER + String.format(DELETE_GROUP, groupid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().delete(url, headers, (String) null, callback);
    }

    public void deleteGroup(final String groupid, HttpCallback<UserApi.Response> callback) {
        deleteGroup(mToken, groupid, callback);
    }

    public void exitGroup(final String userid, final String token, final String groupid, final HttpCallback<UserApi.Response> callback) {
        String url = API_SERVER + String.format(EXIT_GROUP, groupid, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().delete(url, headers, (String) null, callback);
    }

    public void exitGroup(final String groupid, final HttpCallback<UserApi.Response> callback) {
        exitGroup(mUserid, mToken, groupid, callback);
    }

    public void getGroups(final String userid, final String token, HttpCallback<UserApi.GroupsResponse> callback) {
        String url = API_SERVER + String.format(GET_USER_GROUPS, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void getGroups(HttpCallback<UserApi.GroupsResponse> callback) {
        getGroups(mUserid, mToken, callback);
    }

    public void addDeviceToGroup(final String token, final String groupid, final String pkey, final String dname, final HttpCallback<UserApi.Response> callback) {
        String url = API_SERVER + String.format(GROUP_ADD_DEVICE, groupid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.GroupAddDeviceRequest request = new UserApi.GroupAddDeviceRequest();
        request.product_key = pkey;
        request.device_name = dname;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public void addDeviceToGroup(final String groupid, final String pkey, final String dname, final HttpCallback<UserApi.Response> callback) {
        addDeviceToGroup(mToken, groupid, pkey, dname, callback);
    }

    public void removeDeviceFromGroup(final String token, final String groupid, final String pkey, final String dname, final HttpCallback<UserApi.Response> callback) {
        String url = API_SERVER + String.format(GROUP_REMOVE_DEVICE, groupid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.GroupRemoveDeviceRequest request = new UserApi.GroupRemoveDeviceRequest();
        request.product_key = pkey;
        request.device_name = dname;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().delete(url, headers, json, callback);
    }

    public void removeDeviceFromGroup(final String groupid, final String pkey, final String dname, final HttpCallback<UserApi.Response> callback) {
        removeDeviceFromGroup(mToken, groupid, pkey, dname, callback);
    }

    public void getGroupDevices(final String token, final String groupid, HttpCallback<UserApi.GroupDevicesResponse> callback) {
        String url = API_SERVER + String.format(GET_GROUP_DEVICES, groupid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void getGroupDevices(final String groupid, HttpCallback<UserApi.GroupDevicesResponse> callback) {
        getGroupDevices(mToken, groupid, callback);
    }

    public void inviteUserToGroup(final String token, final String groupid, final String email, HttpCallback<UserApi.GroupInviteResponse> callback) {
        String url = API_SERVER + String.format(GROUP_INVITE_USER, groupid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.GroupInviteRequest request = new UserApi.GroupInviteRequest();
        request.email = email;
        request.expire_time = 48;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public void inviteUserToGroup(final String groupid, final String email, HttpCallback<UserApi.GroupInviteResponse> callback) {
        inviteUserToGroup(mToken, groupid, email, callback);
    }

    public void removeUserFromGroup(final String token, final String groupid, final String userid, final HttpCallback<UserApi.Response> callback) {
        String url = API_SERVER + String.format(GROUP_REMOVE_USER, groupid, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().delete(url, headers, (String) null, callback);
    }

    public void removeUserFromGroup(final String groupid, final String userid, final HttpCallback<UserApi.Response> callback) {
        removeUserFromGroup(mToken, groupid, userid, callback);
    }

    public void cancelInvite(final String token, final String groupid, final String invite_id, final HttpCallback<UserApi.Response> callback) {
        String url = API_SERVER + String.format(GROUP_INVITE_CANCEL, groupid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.InviteActionRequest request = new UserApi.InviteActionRequest();
        request.invite_id = invite_id;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public void cancelInvite(final String groupid, final String invite_id, final HttpCallback<UserApi.Response> callback) {
        cancelInvite(mToken, groupid, invite_id, callback);
    }

    public void acceptInvite(final String token, final String groupid, final String invite_id, final HttpCallback<UserApi.Response> callback) {
        String url = API_SERVER + String.format(GROUP_INVITE_ACCEPT, groupid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.InviteActionRequest request = new UserApi.InviteActionRequest();
        request.invite_id = invite_id;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public void acceptInvite(final String groupid, final String invite_id, final HttpCallback<UserApi.Response> callback) {
        acceptInvite(mToken, groupid, invite_id, callback);
    }

    public void denyInvite(final String token, final String groupid, final String invite_id, final HttpCallback<UserApi.Response> callback) {
        String url = API_SERVER + String.format(GROUP_INVITE_DENY, groupid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.InviteActionRequest request = new UserApi.InviteActionRequest();
        request.invite_id = invite_id;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public void denyInvite(final String groupid, final String invite_id, final HttpCallback<UserApi.Response> callback) {
        denyInvite(mToken, groupid, invite_id, callback);
    }

    public void getInviterList(final String userid, final String token, HttpCallback<UserApi.InviteListResponse> callback) {
        String url = API_SERVER + String.format(INVITER_GET_LIST, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void getInviterList(HttpCallback<UserApi.InviteListResponse> callback) {
        getInviterList(mUserid, mToken, callback);
    }

    public void getInviteeList(final String userid, final String token, HttpCallback<UserApi.InviteListResponse> callback) {
        String url = API_SERVER + String.format(INVITEE_GET_LIST, userid);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void getInviteeList(HttpCallback<UserApi.InviteListResponse> callback) {
        getInviteeList(mUserid, mToken, callback);
    }

    public void getFirmwareList(final String token, final String pkey, HttpCallback<UserApi.FirmwaresResponse> callback) {
        String url = API_SERVER + String.format(GET_FIRMWARE_LIST, pkey);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void getFirmwareList(final String pkey, HttpCallback<UserApi.FirmwaresResponse> callback) {
        getFirmwareList(mToken, pkey, callback);
    }

    public void queryDeviceHistoryProperties(final String token, final String pkey, final String dname, final UserApi.DeviceHistoryPropertiesRequest request,
                                             HttpCallback<UserApi.DeviceHistoryPropertiesResponse> callback) {
        final StringBuilder sb = new StringBuilder();
        if (request != null && request.identifiers != null) {
            for (int i = 0; i < request.identifiers.length; i++) {
                sb.append(request.identifiers[i]);
                if (i < request.identifiers.length - 1) {
                    sb.append(",");
                }
            }
        }
        final String identifiers = new String(sb);
        String url = API_SERVER + String.format(QUERY_DEVICE_HISTORY_PROPERTY, pkey, dname,
                                                request.startTime, request.endTime, request.asc, request.pageSize, identifiers);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void queryDeviceHistoryProperties(final String pkey, final String dname, final UserApi.DeviceHistoryPropertiesRequest request,
                                             HttpCallback<UserApi.DeviceHistoryPropertiesResponse> callback) {
        queryDeviceHistoryProperties(mToken, pkey, dname, request, callback);
    }

    public UserApi.GetDevicePropertiesResponse getDeviceProperties(final String token, final String pkey, final String dname) {
        String url = API_SERVER + String.format(GET_DEVICE_PROPERTIES, pkey, dname);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        return OKHttpManager.getInstance().blockGet(url, headers, UserApi.GetDevicePropertiesResponse.class);
    }

    public UserApi.GetDevicePropertiesResponse getDeviceProperties(final String pkey, final String dname) {
        String url = API_SERVER + String.format(GET_DEVICE_PROPERTIES, pkey, dname);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + mToken)
                                               .build();
        return OKHttpManager.getInstance().blockGet(url, headers, UserApi.GetDevicePropertiesResponse.class);
    }

    public void getDeviceProperties(final String token, final String pkey, final String dname, final HttpCallback<UserApi.GetDevicePropertiesResponse> callback) {
        String url = API_SERVER + String.format(GET_DEVICE_PROPERTIES, pkey, dname);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().get(url, headers, callback);
    }

    public void getDeviceProperties(final String pkey, final String dname, final HttpCallback<UserApi.GetDevicePropertiesResponse> callback) {
        getDeviceProperties(mToken, pkey, dname, callback);
    }

    public void setDeviceProperties(final String token, final String pkey, final String dname, final String items, final HttpCallback<UserApi.SetDevicePropertiesResponse> callback) {
        String url = API_SERVER + String.format(SET_DEVICE_PROPERTIES, pkey, dname);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.SetDevicePropertiesRequest request = new UserApi.SetDevicePropertiesRequest();
        request.items = items;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public void setDeviceProperties(final String pkey, final String dname, final String items, final HttpCallback<UserApi.SetDevicePropertiesResponse> callback) {
        setDeviceProperties(mToken, pkey, dname, items, callback);
    }

    public void publishTopic(String token, String pkey, String topic, String message, int qos, HttpCallback<UserApi.PublishTopicResponse> callback) {
        String url = API_SERVER + String.format(PUBLISH_TOPIC, pkey);
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.PublishTopicRequest request = new UserApi.PublishTopicRequest();
        request.topic = topic;
        request.message = Base64.encodeToString(message.getBytes(), Base64.DEFAULT);
        if (qos == 1) {
            request.qos = 1;
        }
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public void publishTopic(String pkey, String topic, String message, int qos, HttpCallback<UserApi.PublishTopicResponse> callback) {
        publishTopic(mToken, pkey, topic, message, qos, callback);
    }

    public void getDeviceProperties(String token, String pkey, String dname, HttpCallback<UserApi.PublishTopicResponse> callback, String... keys) {
        String topicFmt = "/%1$s/%2$s/user/get";
        String topic = String.format(topicFmt, pkey, dname);
        Map<String, Object> payload = new HashMap<>();
        payload.put("params", keys);
        String message = JSON.toJSONString(payload);
        publishTopic(token, pkey, topic, message, 1, callback);
    }

    public void getDeviceProperties(String pkey, String dname, HttpCallback<UserApi.PublishTopicResponse> callback, String... keys) {
        getDeviceProperties(mToken, pkey, dname, callback, keys);
    }

    public void upgradeFirmware(String token, String pkey, String dname, int version, String url, HttpCallback<UserApi.PublishTopicResponse> callback) {
        String topicFmt = "/%1$s/%2$s/user/fota/upgrade";
        String topic = String.format(topicFmt, pkey, dname);
        UserApi.FirmwareInfo info = new UserApi.FirmwareInfo();
        info.version = String.valueOf(version);
        info.url = SERVER_FILE_PATH + url;
        Map<String, Object> payload = new HashMap<>();
        payload.put("message", "success");
        payload.put("data", info);
        String message = JSON.toJSONString(payload);
        publishTopic(token, pkey, topic, message, 1, callback);
    }

    public void upgradeFirmware(String pkey, String dname, int version, String url, HttpCallback<UserApi.PublishTopicResponse> callback) {
        upgradeFirmware(mToken, pkey, dname, version, url, callback);
    }

    private void sendGroupMessage(String token, InviteAction action, String inviter, String invitee, String invite_id, String groupid, String groupname) {
        if (action == null) {
            return;
        }
        final String topicFmt = "/%1$s/%2$s/user/group/listen";
        String topic;
        switch (action) {
            case INVITE:
            case CANCEL:
            case REMOVE:
            case DELETE:
                topic = String.format(topicFmt, APP_KEY, invitee);
                break;
            case ACCEPT:
            case DENY:
            case EXIT:
                topic = String.format(topicFmt, APP_KEY, inviter);
                break;
            default:
                return;
        }
        InviteMessage message = new InviteMessage();
        message.setAction(action.getAction());
        message.setInviter(inviter);
        message.setInvitee(invitee);
        message.setInvite_id(invite_id);
        message.setGroupid(groupid);
        message.setGroupname(groupname);
        String payload = JSON.toJSONString(message);
        publishTopic(token, APP_KEY, topic, payload,0, new HttpCallback<UserApi.PublishTopicResponse>() {
            @Override
            public void onError(String error) {

            }

            @Override
            public void onSuccess(UserApi.PublishTopicResponse result) {

            }
        });
    }

    public void invite(final String invitee, final String invite_id, final String groupid, final String groupname) {
        sendGroupMessage(InviteAction.INVITE, mUserid, invitee, invite_id, groupid, groupname);
    }

    public void inviteCancel(final String invitee, final String invite_id, final String groupid, final String groupname) {
        sendGroupMessage(InviteAction.CANCEL, mUserid, invitee, invite_id, groupid, groupname);
    }

    public void inviteAccept(final String inviter, final String invite_id, final String groupid, final String groupname) {
        sendGroupMessage(InviteAction.ACCEPT, inviter, mUserid, invite_id, groupid, groupname);
    }

    public void inviteDeny(final String inviter, final String invite_id, final String groupid, final String groupname) {
        sendGroupMessage(InviteAction.INVITE, inviter, mUserid, invite_id, groupid, groupname);
    }

    public void removeUser(final String invitee, final String groupid, final String groupname) {
        sendGroupMessage(InviteAction.REMOVE, mUserid, invitee, null, groupid, groupname);
    }

    public void exitGroup(final String inviter, final String groupid, final String groupname) {
        sendGroupMessage(InviteAction.EXIT, inviter, mUserid, null, groupid, groupname);
    }

    public void deleteGroup(final String invitee, final String groupid, final String groupname) {
        sendGroupMessage(InviteAction.DELETE, mUserid, invitee, null, groupid, groupname);
    }

    private void sendGroupMessage(InviteAction action, String inviter, String invitee, String invite_id, String groupid, String groupname) {
        sendGroupMessage(mToken, action, inviter, invitee, invite_id, groupid, groupname);
    }

    public void commonAliyuncsApi(final String token, final String action, final Map<String, String> params, final Callback callback) {
        String url = API_SERVER + COMMON_ALIYUNCS_API;
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_JSON)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        UserApi.CommonAliyuncsRequest request = new UserApi.CommonAliyuncsRequest();
        request.action = action;
        request.params = params;
        String json = JSON.toJSONString(request);
        OKHttpManager.getInstance().post(url, headers, json, callback);
    }

    public void commonAliyuncsApi(final String action, final Map<String, String> params, final Callback callback) {
        commonAliyuncsApi(mToken, action, params, callback);
    }

    public void uploadFile(final String token, final String name, final String path, final HttpCallback<UserApi.FileUploadResponse> callback) {
        String url = API_SERVER + UPLOAD_FILE;
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_FORM)
                                               .add(KEY_AUTH, AUTH_TYPE + token)
                                               .build();
        OKHttpManager.getInstance().upload(url, headers, name, path, callback);
    }

    public void uploadFile(final String name, final String path, final HttpCallback<UserApi.FileUploadResponse> callback) {
        String url = API_SERVER + UPLOAD_FILE;
        Headers headers = new Headers.Builder().add(KEY_CONTENT_TYPE, CONTENT_TYPE_FORM)
                                               .add(KEY_AUTH, AUTH_TYPE + mToken)
                                               .build();
        OKHttpManager.getInstance().upload(url, headers, name, path, callback);
    }

    private static class LazyHolder {
        private static final AliotServer INSTANCE = new AliotServer();
    }
}