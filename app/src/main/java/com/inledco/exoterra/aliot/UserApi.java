package com.inledco.exoterra.aliot;

import com.inledco.exoterra.aliot.bean.Triad;
import com.inledco.exoterra.aliot.bean.User;
import com.inledco.exoterra.aliot.bean.XDevice;
import com.inledco.exoterra.aliot.bean.XGroup;

import java.util.List;

public class UserApi {
//    public static class AResponse {
//        public int code;
//        public String msg;
//    }

//    public static class User {
//        public String userid;
//        public String email;
//        public String nickname;
//        public String create_date;
//        public String corpid;
//        public String avatar;
//        public String remark1;
//        public String remark2;
//        public String remark3;
//    }
//
//    public static class XDevice {
//        public String product_key;
//        public String device_name;
//        public String mac;
//        public String name;
//        public String remark1;
//        public String remark2;
//        public String remark3;
//        public int role;
//        public int firmware_version;
//        public boolean is_online;
//    }
//
//    public static class Triad {
//        public String product_key;
//        public String device_name;
//        public String device_secret;
//    }
//
//    public static class Group {
//        public String groupid;
//        public String name;
//        public String remark1;
//        public String remark2;
//        public String remark3;
//        public List<User> users;
//        public String creator;
//        public String create_time;
//        public String update_time;
//    }

    public static class Response extends ApiResponse<Object> {

    }

    public static class UserRegisterRequest {
        public String corpid;
        public String productKey;
        public String email;
        public String password;
        public String verifycode;
        public String nickname;
    }

    public static class UserLoginRequest {
        public String principal;
        public String credentials;
    }

    public static class UserLoginResponse extends ApiResponse {
        public String userId;
        public String access_token;
        public String token_type;
        public String refresh_token;
        public String secret;
        public int expires_in;
        public String nickeName;
        public String pic;
    }

    public static class GetUserInfoResponse extends ApiResponse<User> {

    }

    public static class SetUserInfoRequest {
        public String nickname;
        public String avatar;
        public String remark1;
        public String remark2;
        public String remark3;
    }

    public static class ModifyPasswordRequest {
        public String old_password;
        public String new_password;
    }

    public static class ResetPasswordRequest {
        public String corpid;
        public String email;
        public String verifycode;
        public String new_password;
    }

    public static class UserSubscribedDevicesResponse extends ApiResponse<List<XDevice>> {

    }

    public static class SubscribeDeviceRequest {
        public String product_key;
        public String device_name;
        public String mac;
    }

    public static class SubscribeDeviceResponse extends ApiResponse<Triad> {

    }

    public static class UnsubscribeDeviceRequest {
        public String product_key;
        public String device_name;
    }

    public static class ModifyDeviceInfoRequest {
        public String name;
        public String remark1;
        public String remark2;
        public String remark3;
    }

    public static class DeviceInfoResponse extends ApiResponse<XDevice> {

    }

    public static class GroupRequest {
        public String name;
        public String remark1;
        public String remark2;
        public String remark3;
    }

    public static class GroupResponse extends ApiResponse<XGroup> {

    }

    public static class GroupsResponse extends ApiResponse<List<XGroup>> {

    }

    public static class GroupAddDeviceRequest {
        public String product_key;
        public String device_name;
    }

    public static class GroupRemoveDeviceRequest {
        public String product_key;
        public String device_name;
    }

    public static class GroupDevicesResponse extends ApiResponse<List<XDevice>>{

    }

    public static class GroupInviteRequest {
        public String email;
        public long expire_time;
    }

    public static class GroupInviteResponse extends ApiResponse<GroupInviteResponse.InviteId> {

        public static class InviteId {
            public String invite_id;
        }
    }

    public static class InviteActionRequest {
        public String invite_id;
    }

    public static class InviteRecord {
        public String invite_id;
        public String groupid;
        public String group_name;
        public String inviter;
        public String invitee;
        public String create_time;
        public String end_time;
        public int status;
        public String invitee_email;
    }

    public static class InviteListResponse extends ApiResponse<List<InviteRecord>> {

    }

    public static class Firmware {
        public int version;
        public int size;
        public String url;
    }

    public static class FirmwareList {
        public String product_key;
        public List<Firmware> firmware_list;
    }

    public static class FirmwaresResponse extends ApiResponse<FirmwareList> {

    }
}
