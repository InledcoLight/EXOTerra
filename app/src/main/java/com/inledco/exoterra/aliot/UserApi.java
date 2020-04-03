package com.inledco.exoterra.aliot;

import java.util.List;

public class UserApi {
    public static class TestResponse {
        public int user_id;
        public String access_token;
        public String refresh_token;
        public int expire_in;
        public String authorize;
    }

    public static class UserRegisterRequest {
        public String corpid;
        public String email;
        public String password;
        public String verifycode;
        public String nickname;
    }

    public static class UserRegisterResponse {

    }

    public static class UserLoginRequest {
        public String corpid;
        public String email;
        public String password;
    }

    public static class UserLoginResponse {
        public String userid;
        public String token;
    }

    public static class UserInfoResponse {
        public String userid;
        public String email;
        public String nickname;
        public String create_date;
        public String corpid;
        public String avatar;
        public String remark1;
        public String remark2;
        public String remark3;
    }

    public static class UserInfoRequest {
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

    public static class UserSubscribedDevicesResponse {
        public List<Device> devices;

        public static class Device {
            public String product_key;
            public String device_name;
            public String mac;
            public String name;
            public String remark1;
            public String remark2;
            public String remark3;
            public int role;
            public int firmware_version;
            public boolean is_online;
        }
    }

    public static class SubscribeDeviceRequest {
        public String product_key;
        public String device_name;
        public String mac;
    }

    public static class SubscribeDeviceResponse {
        public String product_key;
        public String device_name;
        public String device_secret;
    }

    public static class UnsubscribeDeviceRequest {
        public String product_key;
        public String device_name;
    }

    public static class DeviceInfoRequest {
        public String name;
        public String remark1;
        public String remark2;
        public String remark3;
    }

    public static class DeviceInfoResponse {
        public String product_key;
        public String device_name;
        public String mac;
        public String name;
        public String remark1;
        public String remark2;
        public String remark3;
        public int role;
        public int firmware_version;
        public boolean is_online;
    }

    public static class GroupRequest {
        public String name;
        public String remark1;
        public String remark2;
        public String remark3;
    }

    public static class GroupResponse {
        public String groupid;
        public String name;
        public String remark1;
        public String remark2;
        public String remark3;
        public List<User> users;
        public String creator;
        public String create_time;
        public String update_time;

        public static class User {
            public String userid;
            public String email;
            public String nickname;
            public String avatar;
            public String remark1;
            public String remark2;
            public String remark3;
            public int role;
        }
    }

    public static class GroupsResponse {

        public List<Group> groups;

        public static class Group {
            public String groupid;
            public String name;
            public String remark1;
            public String remark2;
            public String remark3;
            public String creator;
            public String create_time;
            public String update_time;
        }
    }

    public static class GroupAddDeviceRequest {
        public String product_key;
        public String device_name;
    }

    public static class GroupRemoveDeviceRequest {
        public String product_key;
        public String device_name;
    }

    public static class GroupDevicesResponse {

        public List<Device> devices;

        public static class Device {
            public String product_key;
            public String device_name;
            public String mac;
            public String name;
            public String remark1;
            public String remark2;
            public String remark3;
            public int role;
            public int firmware_version;
            public boolean is_online;
        }
    }

    public static class GroupInviteRequest {
        public String email;
        public long expire_time;
    }

    public static class GroupInviteResponse {
        public String invite_id;
    }

    public static class InviteActionRequest {
        public String invite_id;
    }

    public static class InviteListResponse {
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

    public static class FirmwaresResponse {
        public String product_key;
        public List<Firmware> firmware_list;

        public static class Firmware {
            public int version;
            public int size;
            public String url;
        }
    }
}
