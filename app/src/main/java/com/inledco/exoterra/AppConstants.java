package com.inledco.exoterra;
public class AppConstants {

    //云平台对应包名生成的appid
    public static final String  APPID                   = "2e0042bafe5fc600";

    public static final String  FILE_VERIFYCODE_REGISTER    = "verifycode_register";
    public static final String  FILE_VERIFYCODE_RESET       = "verifycode_reset";

    public static final String  IS_HOME_ADMIN           = "is_home_admin";
    public static final String  EMAIL                   = "email";
    public static final String  USER_ID                 = "user_id";
    public static final String  ROLE                    = "role";
    public static final String  NICKNAME                = "nickname";
    public static final String  HOME_ID                 = "home_id";
    public static final String  DEVICE_ID               = "device_id";
    public static final String  DEVICE_TAG              = "device_tag";
    public static final String  PRODUCT_ID              = "product_id";
    public static final String  TIMEZONE                = "timezone";
    public static final String  LONGITUDE               = "longitude";
    public static final String  LATITUDE                = "latitude";

    public static final String  SPECIFICATION           = "spec";

    public static final byte    MONSOON_POWERON         = (byte) 0xF8;
    public static final byte    MONSOON_POWEROFF        = 0x00;
    public static final int     MONSOON_TIMER_INVALID   = 0;
    public static final int     SOCKET_TIMER_INVALID    = 0xFFFFFFFF;

    public static final byte    SENSOR_TYPE_NONE                = 0x00;
    public static final byte    SENSOR_TYPE_REPTILE_TEMPERATURE = 0x01;
    public static final byte    SENSOR_TYPE_REPTILE_HUMIDITY    = 0x02;


    public static final String  DATETIME_FORMAT_24HOUR  = "yyyy-MM-dd HH:mm";
    public static final String  DATETIME_FORMAT_12HOUR  = "yyyy-MM-dd hh:mm a";
    public static final String  TIME_FORMAT_24HOUR      = "HH:mm";
    public static final String  TIME_FORMAT_12HOUR      = "hh:mm a";

    //通知
    public static final String  NOTIFY_CHANNEL_SHARE            = "share";
    public static final String  NOTIFY_CHANNEL_SHARE_RECEIPT    = "share_receipt";
    public static final String  NOTIFY_CHANNEL_INVITE           = "invite";
    public static final String  NOTIFY_CHANNEL_HOME_MEMBER_CHANGED    = "home_member_changed";
    public static final String  NOTIFY_CHANNEL_DELETE_HOME      = "delete_home";
    public static final String  NOTIFY_CHANNEL_ALARM            = "alarm";

    //云平台访问access_token (访问平台接口通过access_key_secret生成, 有效期十年)
    public static final String  XCP_ACCESS_TOKEN_ADMIN          = "QUM3NUNGRTcxMEM1NzY5MzQ0MDBENTY1OTcxQUZGRDExQkUyMEJGN0ZDMUE5RTlFNjJCRkZFMjE4MzIxODRBRA==";

    //云平台查询设备列表接口
    public static final String  XCP_QUERY_DEVICE_URL            = "https://api2.xlink.cn/v2/product/%1$s/devices";
    public static final String  XCP_IMPORT_DEVICE_URL            = "https://api2.xlink.cn/v2/product/%1$s/device_import_batch_2";

    public static final String  KEY_TIMEFORMAT = "timeformat";
    public static final String  KEY_TEMPUNIT = "tempunit";

    public static final String GROUP_INVITE = "home_invite";
    public static final String  NOTIFICATION_ID = "notification_id";
    public static final String  INVITE_ID = "invite_id";
    public static final String  DENY = "deny";
    public static final String  ACCEPT = "accept";
    public static final String  ACTION = "action";

    public static final String  JOIN_HOME = "join_home";
    public static final String  LEAVE_HOME = "leave_home";
    public static final String  DELETE_HOME = "delete_home";

    public static final String  DEVICE_ONLINE_STATE_ALARM = "device_status";
    public static final String  DATAPOINT_ALARM = "datapoint";
}
