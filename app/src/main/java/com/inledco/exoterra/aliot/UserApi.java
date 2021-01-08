package com.inledco.exoterra.aliot;

import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.aliot.bean.Triad;
import com.inledco.exoterra.aliot.bean.User;
import com.inledco.exoterra.aliot.bean.XDevice;
import com.inledco.exoterra.aliot.bean.XGroup;

import java.util.List;
import java.util.Map;

public class UserApi {

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

    public static class GroupsResponse extends ApiResponse<List<Group>> {

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

    public static class GroupInviteResponse extends ApiResponse<GroupInviteResponse.InviteResult> {

        public static class InviteResult {
            public String invite_id;
            public String invitee;
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
        public long create_time;
        public long end_time;
        public int status;
        public String invitee_email;
    }

    public static class InviteListResponse extends ApiResponse<List<InviteRecord>> {

    }

//    public static class ThingProperty {
//        public String identifier;
//        public String value;
//        public String time;
//        public String dateType;
//    }

    public static class GetDevicePropertiesResponse extends ApiResponse<List<ThingProperty>> {

    }

    public static class SetDevicePropertiesRequest {
        public String items;
    }

    public static class SetDevicePropertiesResponse extends ApiResponse<String> {

    }

    public static class InvokeDeviceServiceRequest {
        public String params;
    }

    public static class ServiceResult {
        public String id;
        public Object result;
    }

    public static class InvokeDeviceServiceResponse extends ApiResponse<ServiceResult> {

    }

    public static class GetDeviceTimeResult {
        public String device_datetime;
    }

    public static class GetDeviceTimeResponse extends ApiResponse<GetDeviceTimeResult> {

    }

    public static class PublishTopicRequest {
        public String topic;
        public String message;
        public int qos;
    }

    public static class PublishTopicResponse extends ApiResponse<String> {

    }

    public static class CommonAliyuncsRequest {
        public String action;
        public Map<String, String> params;
    }

    public static class CommonAliyuncsResponse extends ApiResponse<String> {

    }

    public static class Firmware {
        public int version;
        public float firmSize;
        public String url;
    }

    public static class FirmwareList {
        public String product_key;
        public List<Firmware> firmware_list;
    }

    public static class FirmwaresResponse extends ApiResponse<FirmwareList> {

    }

    public static class FirmwareInfo {
        public String version;
        public String url;
    }

    public static class UpgradeProgress {
        public int step;
        public String desc;
    }

    public static class FotaProgress {
        public String productKey;
        public String deviceName;
        public UpgradeProgress params;
    }

    public static class DeviceHistoryPropertiesRequest {
        public long startTime;
        public long endTime;
        public int asc;                     // 0倒序 1正序
        public int pageSize;                // 最大100
        public String[] identifiers;         // 属性标识
    }

    public static class KeyValue {
        public String Time;
        public String Value;
    }

    public static class PropertyDataInfo {
        public String identifier;
        public List<KeyValue> list;
    }

    public static class DeviceHistoryPropertiesResponse extends ApiResponse<List<PropertyDataInfo>> {

    }

    public static class SntpRequet {
        public String deviceSendTime;
    }

    public static class SntpResponse {
        public String deviceSendTime;
        public String serverSendTime;
        public String serverRecvTime;
    }

    public static class FileUploadResponse extends ApiResponse<String> {

    }
}
