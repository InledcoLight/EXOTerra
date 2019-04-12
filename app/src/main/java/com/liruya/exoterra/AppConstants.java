package com.liruya.exoterra;

public class AppConstants {
    public static final String  DEVICE_TAG              = "device_tag";
    public static final String  PRODUCT_ID              = "product_id";
    public static final String  TIMEZONE                = "timezone";
    public static final String  LONGITUDE               = "longitude";
    public static final String  LATITUDE                = "latitude";

    public static final byte    MONSOON_POWERON         = (byte) 0x80;
    public static final byte    MONSOON_POWEROFF        = 0x00;
    public static final int     MONSOON_TIMER_INVALID   = 0;
    public static final int     SOCKET_TIMER_INVALID    = 0xFFFFFFFF;

    public static final byte    SENSOR_TYPE_NONE                = 0x00;
    public static final byte    SENSOR_TYPE_REPTILE_TEMPERATURE = 0x01;
    public static final byte    SENSOR_TYPE_REPTILE_HUMIDITY    = 0x02;
}
