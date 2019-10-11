package com.inledco.exoterra.bean;

import android.support.annotation.IntDef;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.xlink.sdk.core.model.XLinkDataPoint;

public class EXOLedstrip extends Device {
    private final String TAG = "EXOLedstrip";

//    private final int INDEX_PROPERTY            = 0;
//    private final int INDEX_ZONE                = 1;
//    private final int INDEX_LONGITUDE           = 2;
//    private final int INDEX_LATITUDE            = 3;
//    private final int INDEX_DEVICE_DATETIME     = 4;
    private final int INDEX_CHANNEL_COUNT       = 10;
    private final int INDEX_CHN1_NAME           = 11;
    private final int INDEX_CHN2_NAME           = 12;
    private final int INDEX_CHN3_NAME           = 13;
    private final int INDEX_CHN4_NAME           = 14;
    private final int INDEX_CHN5_NAME           = 15;
    private final int INDEX_CHN6_NAME           = 16;
    private final int INDEX_MODE                = 17;
    private final int INDEX_POWER               = 18;
    private final int INDEX_CHN1_BRIGHT         = 19;
    private final int INDEX_CHN2_BRIGHT         = 20;
    private final int INDEX_CHN3_BRIGHT         = 21;
    private final int INDEX_CHN4_BRIGHT         = 22;
    private final int INDEX_CHN5_BRIGHT         = 23;
    private final int INDEX_CHN6_BRIGHT         = 24;
    private final int INDEX_CUSTOM1_BRIGHTS     = 25;
    private final int INDEX_CUSTOM2_BRIGHTS     = 26;
    private final int INDEX_CUSTOM3_BRIGHTS     = 27;
    private final int INDEX_CUSTOM4_BRIGHTS     = 28;
    private final int INDEX_GIS_ENABLE          = 29;
    private final int INDEX_GIS_SUNRISE         = 30;
    private final int INDEX_GIS_SUNSET          = 31;
    private final int INDEX_GIS_VALID           = 32;
    private final int INDEX_SUNRISE             = 33;
    private final int INDEX_SUNRISE_RAMP        = 34;
    private final int INDEX_DAY_BRIGHTS         = 35;
    private final int INDEX_SUNSET              = 36;
    private final int INDEX_SUNSET_RAMP         = 37;
    private final int INDEX_NIGHT_BRIGHTS       = 38;
    private final int INDEX_TURNOFF_ENABLE      = 39;
    private final int INDEX_TURNOFF_TIME        = 40;
    private final int INDEX_PROFILE0_COUNT      = 41;
    private final int INDEX_PROFILE0_TIMERS     = 42;
    private final int INDEX_PROFILE0_BRIGHTS    = 43;
    private final int INDEX_PROFILE1_NAME       = 44;
    private final int INDEX_PROFILE1_COUNT      = 45;
    private final int INDEX_PROFILE1_TIMERS     = 46;
    private final int INDEX_PROFILE1_BRIGHTS    = 47;
    private final int INDEX_SELECT_PROFILE      = 92;

    private final int CHANNEL_COUNT_MAX         = 6;
    private final int CUSTOM_COUNT_MAX          = 4;
    private final int PROFILE_COUNT_MAX         = 12;
    private final int BRIGHT_MAX                = 1000;
    private final int POINTS_COUNT_MIN          = 4;
    private final int POINTS_COUNT_MAX          = 10;

    public static final int MODE_MANUAL         = 0;
    public static final int MODE_AUTO           = 1;
    public static final int MODE_PRO            = 2;

    @IntDef( {MODE_MANUAL, MODE_AUTO, MODE_PRO} )
    public @interface Mode {}

    public EXOLedstrip(Device device) {
        super(device.getXDevice());
    }

//    public String getProperty() {
//        return getString(INDEX_PROPERTY);
//    }
//
//    public short getZone() {
//        return getShort(INDEX_ZONE);
//    }
//
//    public XLinkDataPoint setZone(short zone) {
//        if (zone < -1200 || zone > 1200 || zone%100<= -60 || zone%100 >= 60) {
//            return null;
//        }
//        return setShort(INDEX_ZONE, zone);
//    }
//
//    public float getLongitude() {
//        return getFloat(INDEX_LONGITUDE);
//    }
//
//    public XLinkDataPoint setLongitude(float longitude) {
//        if (longitude < -180 || longitude > 180) {
//            return null;
//        }
//        return setFloat(INDEX_LONGITUDE, longitude);
//    }
//
//    public float getLatitude() {
//        return getFloat(INDEX_LATITUDE);
//    }
//
//    public XLinkDataPoint setLatitude(float latitude) {
//        if (latitude < -60 || latitude > 60) {
//            return null;
//        }
//        return setFloat(INDEX_LATITUDE, latitude);
//    }
//
//    public String getDeviceDatetime() {
//        return getString(INDEX_DEVICE_DATETIME);
//    }

    public byte getChannelCount() {
        return getByte(INDEX_CHANNEL_COUNT);
    }

    public String getChannelName(int chn) {
        if (chn < 0 || chn >= CHANNEL_COUNT_MAX) {
            return "";
        }
        return getString(INDEX_CHN1_NAME + chn);
    }

    public String[] getChannelNames() {
        int count = getChannelCount();
        String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            names[i] = getChannelName(i);
        }
        return names;
    }

    public @Mode int getMode() {
        return getByte(INDEX_MODE);
    }

    public XLinkDataPoint setMode(@Mode int mode) {
        return setByte(INDEX_MODE, (byte) mode);
    }

    public boolean getPower() {
        return getBoolean(INDEX_POWER);
    }

    public XLinkDataPoint setPower(boolean power) {
        return setBoolean(INDEX_POWER, power);
    }

    public int getBright(int chn) {
        if (chn < 0 || chn >= CHANNEL_COUNT_MAX) {
            return 0;
        }
        return getUShort(INDEX_CHN1_BRIGHT + chn);
    }

    public int[] getBrights() {
        int count = getChannelCount();
        int[] brights = new int[count];
        for (int i = 0; i < count; i++) {
            brights[i] = getBright(i);
        }
        return brights;
    }

    public XLinkDataPoint setBright(int chn, int bright) {
        if (chn < 0 || chn > CHANNEL_COUNT_MAX || bright < 0 || bright > BRIGHT_MAX) {
            return null;
        }
        return setUShort(INDEX_CHN1_BRIGHT+chn, (short) bright);
    }

    public List<XLinkDataPoint> setAllBrights(int[] brights) {
        if (brights == null || brights.length != getChannelCount()) {
            return null;
        }
        List<XLinkDataPoint> dps = new ArrayList<>();
        for (int i = 0; i < brights.length; i++) {
            int value = brights[i];
            if (value < 0 || value > BRIGHT_MAX) {
                value = BRIGHT_MAX;
            }
            XLinkDataPoint dp = setUShort(INDEX_CHN1_BRIGHT+i, (short) value);
            dps.add(dp);
        }
        return dps;
    }

    public byte[] getCustomBrights(int idx) {
        if (idx < 0 || idx >= CUSTOM_COUNT_MAX) {
            return null;
        }
        return getByteArray(INDEX_CUSTOM1_BRIGHTS + idx);
    }

    public XLinkDataPoint setCustomBrights(int idx, byte[] brights) {
        if (idx < 0 || idx >= CUSTOM_COUNT_MAX || brights == null || brights.length != getChannelCount()){
            return null;
        }
        for (int i = 0; i < brights.length; i++) {
            if (brights[i] < 0 || brights[i] > 100) {
                brights[i] = 100;
            }
        }
        return setByteArray(INDEX_CUSTOM1_BRIGHTS + idx, brights);
    }

//    public DeviceApi.DeviceDataPointRequest.Command setCustomBrights(int idx, byte[] brights) {
//        if (idx < 0 || idx >= CUSTOM_COUNT_MAX || brights == null || brights.length != getChannelCount()){
//            return null;
//        }
//        for (int i = 0; i < brights.length; i++) {
//            if (brights[i] < 0 || brights[i] > 100) {
//                brights[i] = 100;
//            }
//        }
//        DeviceApi.DeviceDataPointRequest.Command cmd = new DeviceApi.DeviceDataPointRequest.Command<>();
//        cmd.index = INDEX_CUSTOM1_BRIGHTS+idx;
//        cmd.value = ByteUtil.bytesToHex(brights);            //字节数组需要转换为字符串
//        return cmd;
//    }

    public boolean getGisEnable() {
        return getBoolean(INDEX_GIS_ENABLE);
    }

    public XLinkDataPoint setGisEnable(boolean enable) {
        return setBoolean(INDEX_GIS_ENABLE, enable);
    }

    public int getGisSunrise() {
        return getUShort(INDEX_GIS_SUNRISE);
    }

    public int getGisSunset() {
        return getUShort(INDEX_GIS_SUNSET);
    }

    public boolean getGisValid() {
        return getBoolean(INDEX_GIS_VALID);
    }

    public int getSunrise() {
        return getUShort(INDEX_SUNRISE);
    }

    public XLinkDataPoint setSunrise(int sunrise) {
        if (sunrise < 0 || sunrise > 1439) {
            return null;
        }
        return setUShort(INDEX_SUNRISE, (short) sunrise);
    }

    public int getSunriseRamp() {
        return getByte(INDEX_SUNRISE_RAMP)&0xFF;
    }

    public XLinkDataPoint setSunriseRamp(int ramp) {
        if (ramp < 0 || ramp > 240) {
            return null;
        }
        return setByte(INDEX_SUNRISE_RAMP, (byte) ramp);
    }

    public byte[] getDayBrights() {
        return getByteArray(INDEX_DAY_BRIGHTS);
    }

    public XLinkDataPoint setDayBrights(byte[] brights) {
        if (brights == null || brights.length != getChannelCount()) {
            return null;
        }
        for (int i = 0; i < brights.length; i++) {
            if (brights[i] < 0 || brights[i] > 100) {
                brights[i] = 100;
            }
        }
        return setByteArray(INDEX_DAY_BRIGHTS, brights);
    }

    public int getSunset() {
        return getUShort(INDEX_SUNSET);
    }

    public XLinkDataPoint setSunset(int sunset) {
        if (sunset < 0 || sunset > 1439) {
            return null;
        }
        return setUShort(INDEX_SUNSET, (short) sunset);
    }

    public int getSunsetRamp() {
        return getByte(INDEX_SUNSET_RAMP)&0xFF;
    }

    public XLinkDataPoint setSunsetRamp(int ramp) {
        if (ramp < 0 || ramp > 240) {
            return null;
        }
        return setByte(INDEX_SUNSET_RAMP, (byte) ramp);
    }

    public byte[] getNightBrights() {
        return getByteArray(INDEX_NIGHT_BRIGHTS);
    }

    public XLinkDataPoint setNightBrights(byte[] brights) {
        if (brights == null || brights.length != getChannelCount()) {
            return null;
        }
        for (int i = 0; i < brights.length; i++) {
            if (brights[i] < 0 || brights[i] > 100) {
                brights[i] = 100;
            }
        }
        return setByteArray(INDEX_NIGHT_BRIGHTS, brights);
    }

    public boolean getTurnoffEnable() {
        return getBoolean(INDEX_TURNOFF_ENABLE);
    }

    public XLinkDataPoint setTurnoffEnable(boolean enable) {
        return setBoolean(INDEX_TURNOFF_ENABLE, enable);
    }

    public int getTurnoffTime() {
        return getUShort(INDEX_TURNOFF_TIME);
    }

    public XLinkDataPoint setTurnoffTime(int time) {
        if (time < 0 || time > 1439) {
            return null;
        }
        return setUShort(INDEX_TURNOFF_TIME, (short) time);
    }

    public Profile getProfile(int idx) {
        if (idx < 0 || idx > PROFILE_COUNT_MAX) {
            return null;
        }
        int chn_count = getChannelCount();
        int point_count = getByte(INDEX_PROFILE0_COUNT+4*idx);
        byte[] timers = getByteArray(INDEX_PROFILE0_TIMERS+4*idx);
        byte[] brights = getByteArray(INDEX_PROFILE0_BRIGHTS+4*idx);
        return new Profile.Builder().create(chn_count, point_count, timers, brights);
    }

    public List<XLinkDataPoint> setProfile(int idx, Profile profile) {
        if (idx < 0 || idx > PROFILE_COUNT_MAX || profile == null || !profile.isValid()) {
            return null;
        }
        XLinkDataPoint dp1 = setByte(INDEX_PROFILE0_COUNT+4*idx, profile.getPointCount());
        XLinkDataPoint dp2 = setByteArray(INDEX_PROFILE0_TIMERS+4*idx, profile.getTimesArray());
        XLinkDataPoint dp3 = setByteArray(INDEX_PROFILE0_BRIGHTS+4*idx, profile.getBrightsArray());
        List<XLinkDataPoint> dps = new ArrayList<>();
        dps.add(dp1);
        dps.add(dp2);
        dps.add(dp3);
        return dps;
    }

    public String getProfileName(int idx) {
        if (idx < 0 || idx > PROFILE_COUNT_MAX) {
            return "";
        }
        if (idx == 0) {
            return "Default";
        }
        String name = getString(INDEX_PROFILE1_NAME+4*idx-4);
        if (TextUtils.isEmpty(name)) {
            name = "Profile " + idx;
        }
        return name;
    }

    public XLinkDataPoint setProfileName(int idx, String name) {
        if (idx <= 0 || idx > PROFILE_COUNT_MAX || TextUtils.isEmpty(name)) {
            return null;
        }
        return setString(INDEX_PROFILE1_NAME+4*idx-4, name);
    }

    public int getSelectProfile() {
        int select = getByte(INDEX_SELECT_PROFILE);
        if (select < 0 || select > 13) {
            select = 0;
        }
        return select;
    }

    public XLinkDataPoint setSelectProfile(int select) {
        return setByte(INDEX_SELECT_PROFILE, (byte) select);
    }
}
