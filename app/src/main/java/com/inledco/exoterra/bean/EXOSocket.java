package com.inledco.exoterra.bean;

import com.inledco.exoterra.AppConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.xlink.sdk.core.model.XLinkDataPoint;

public class EXOSocket extends Device{

    private final String TAG = "EXOSocket";

//    private final int INDEX_PROPERTY            = 0;
//    private final int INDEX_ZONE                = 1;
//    private final int INDEX_LONGITUDE           = 2;
//    private final int INDEX_LATITUDE            = 3;
//    private final int INDEX_DEVICE_DATETIME     = 4;
    private final int INDEX_SWITCH_COUNT_MAX    = 5;
    private final int INDEX_CONNECT_DEVICE      = 6;

    private final int INDEX_SWITCH_COUNT        = 10;
    private final int INDEX_POWER               = 11;
    private final int INDEX_TIMER1              = 12;

    private final int INDEX_S1_AVAILABLE        = 40;
    private final int INDEX_S1_TYPE             = 41;
    private final int INDEX_S1_VALUE            = 42;
    private final int INDEX_SV1_TYPE            = 43;
    private final int INDEX_SV1_NOTIFY_ENABLE   = 44;
    private final int INDEX_SV1_LINKAGE_ENABLE  = 45;
    private final int INDEX_SV1_ARGS1           = 46;
    private final int INDEX_SV1_ARGS2           = 47;
    private final int INDEX_SV1_ARGS3           = 48;
    private final int INDEX_SV1_ARGS4           = 49;
    private final int INDEX_S2_AVAILABLE        = 50;
    private final int INDEX_S2_TYPE             = 51;
    private final int INDEX_S2_VALUE            = 52;
    private final int INDEX_SV2_TYPE            = 53;
    private final int INDEX_SV2_NOTIFY_ENABLE   = 54;
    private final int INDEX_SV2_ARGS            = 55;
    private final int INDEX_SV1_THRD_LOWER      = 60;
    private final int INDEX_SV1_THRD_UPPER      = 61;
    private final int INDEX_S1_LOSS_FLAG        = 62;
    private final int INDEX_S1_OVER_FLAG        = 63;
    private final int INDEX_SV2_THRD_LOWER      = 64;
    private final int INDEX_SV2_THRD_UPPER      = 65;
    private final int INDEX_S2_LOSS_FLAG        = 66;
    private final int INDEX_S2_OVER_FLAG        = 67;

    public static final int TIMER_COUNT_MAX     = 24;

    public EXOSocket(Device device) {
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

    public int getSwitchCountMax() {
        return getUInt(INDEX_SWITCH_COUNT_MAX);
    }

    public String getConnectDevice() {
        return getString(INDEX_CONNECT_DEVICE);
    }

//    public DeviceApi.DeviceDataPointRequest.Command setConnectDevice(String devname) {
//        if (TextUtils.isEmpty(devname)) {
//            return null;
//        }
//        DeviceApi.DeviceDataPointRequest.Command cmd = new DeviceApi.DeviceDataPointRequest.Command<>();
//        cmd.index = INDEX_CONNECT_DEVICE;
//        cmd.value = devname;
//        return cmd;
//    }

    public XLinkDataPoint setConnectDevice(String devname) {
        return setString(INDEX_CONNECT_DEVICE, devname);
    }

    public boolean getS1Available() {
        return getBoolean(INDEX_S1_AVAILABLE);
    }

    public byte getS1Type() {
        return getByte(INDEX_S1_TYPE);
    }

    public int getS1Value() {
        return getInt(INDEX_S1_VALUE);
    }

    public int getSV1Type() {
        return getByte(INDEX_SV1_TYPE);
    }

    public XLinkDataPoint setSV1Type(byte type) {
        return setByte(INDEX_SV1_TYPE, type);
    }

    public boolean getSV1NotifyEnable() {
        return getBoolean(INDEX_SV1_NOTIFY_ENABLE);
    }

    public XLinkDataPoint setSV1NotifyEnable(boolean enable) {
        return setBoolean(INDEX_SV1_NOTIFY_ENABLE, enable);
    }

    public boolean getSV1LinkageEnable() {
        return getBoolean(INDEX_SV1_LINKAGE_ENABLE);
    }

    public XLinkDataPoint setSV1LinkageEnable(boolean enable) {
        return setBoolean(INDEX_SV1_LINKAGE_ENABLE, enable);
    }

    public byte[] getSV1LinkageArgs() {
        byte[] results = new byte[256];
        byte[] args1 = getByteArray(INDEX_SV1_ARGS1);
        byte[] args2 = getByteArray(INDEX_SV1_ARGS2);
        byte[] args3 = getByteArray(INDEX_SV1_ARGS3);
        byte[] args4 = getByteArray(INDEX_SV1_ARGS4);
        if (args1 == null || args1.length != 64) {
            return null;
        }
        System.arraycopy(args1, 0, results, 0, 64);
        if (args2 == null || args2.length != 64) {
            return null;
        }
        System.arraycopy(args2, 0, results, 64, 64);
        if (args3 == null || args3.length != 64) {
            return null;
        }
        System.arraycopy(args3, 0, results, 128, 64);
        if (args4 == null || args4.length != 64) {
            return null;
        }
        System.arraycopy(args4, 0, results, 192, 64);
        return results;
    }

    public List<XLinkDataPoint> setSV1LinkageArgs(byte[] args) {
        if (args == null || args.length != 256) {
//            throw new IllegalArgumentException("Invalid args");
            return null;
        }
        byte[] args1 = Arrays.copyOfRange(args, 0, 64);
        byte[] args2 = Arrays.copyOfRange(args, 64, 128);
        byte[] args3 = Arrays.copyOfRange(args, 128, 192);
        byte[] args4 = Arrays.copyOfRange(args, 192, 256);
        XLinkDataPoint dp1 = setByteArray(INDEX_SV1_ARGS1, args1);
        XLinkDataPoint dp2 = setByteArray(INDEX_SV1_ARGS2, args2);
        XLinkDataPoint dp3 = setByteArray(INDEX_SV1_ARGS3, args3);
        XLinkDataPoint dp4 = setByteArray(INDEX_SV1_ARGS4, args4);
        if (dp1 == null || dp2 == null || dp3 == null || dp4 == null) {
            return null;
        }
        final List<XLinkDataPoint> dps = new ArrayList<>();
        dps.add(dp1);
        dps.add(dp2);
        dps.add(dp3);
        dps.add(dp4);
        return dps;
    }

    public int getSV1ThrdLower() {
        return getInt(INDEX_SV1_THRD_LOWER);
    }

    public XLinkDataPoint setSV1ThrdLower(int thrd) {
        return setInt(INDEX_SV1_THRD_LOWER, thrd);
    }

    public int getSV1ThrdUpper() {
        return getInt(INDEX_SV1_THRD_UPPER);
    }

    public XLinkDataPoint setSV1ThrdUpper(int thrd) {
        return setInt(INDEX_SV1_THRD_UPPER, thrd);
    }

    public List<XLinkDataPoint> setSV1Notify(boolean enable, int thrd_lower, int thrd_upper) {
        final XLinkDataPoint dp0 = setSV1Type(getS1Type());
        final XLinkDataPoint dp1 = setSV1NotifyEnable(enable);
        final XLinkDataPoint dp2 = setSV1ThrdLower(thrd_lower);
        final XLinkDataPoint dp3 = setSV1ThrdUpper(thrd_upper);
        if (dp0 != null && dp1 != null && dp2 != null && dp3 != null) {
            List<XLinkDataPoint> dps = new ArrayList<>();
            dps.add(dp0);
            dps.add(dp1);
            dps.add(dp2);
            dps.add(dp3);
            return dps;
        }
        return null;
    }

    public List<XLinkDataPoint> setSV1LinkageArgs(boolean linkage, byte[] args) {
        XLinkDataPoint dp1 = setSV1Type(getS1Type());
        XLinkDataPoint dp2 = setSV1LinkageEnable(linkage);
        List<XLinkDataPoint> dps = setSV1LinkageArgs(args);
        if (dp1 == null || dp2 == null || dps == null) {
            return null;
        }
        dps.add(dp1);
        dps.add(dp2);
        return dps;
    }

    public byte getS1LossFlag() {
        return getByte(INDEX_S1_LOSS_FLAG);
    }

    public byte getS1OverFlag() {
        return getByte(INDEX_S1_OVER_FLAG);
    }

    public byte getS2LossFlag() {
        return getByte(INDEX_S2_LOSS_FLAG);
    }

    public byte getS2OverFlag() {
        return getByte(INDEX_S2_OVER_FLAG);
    }

    public boolean getS2Available() {
        return getBoolean(INDEX_S2_AVAILABLE);
    }

    public byte getS2Type() {
        return getByte(INDEX_S2_TYPE);
    }

    public int getS2Value() {
        return getInt(INDEX_S2_VALUE);
    }

    public int getSV2Type() {
        return getByte(INDEX_SV2_TYPE);
    }

    public XLinkDataPoint setSV2Type(byte type) {
        return setByte(INDEX_SV2_TYPE, type);
    }

    public boolean getSV2NotifyEnable() {
        return getBoolean(INDEX_SV2_NOTIFY_ENABLE);
    }

    public XLinkDataPoint setSV2NotifyEnable(boolean enable) {
        return setBoolean(INDEX_SV2_NOTIFY_ENABLE, enable);
    }

    public byte[] getSV2Args() {
        final byte[] args = getByteArray(INDEX_SV2_ARGS);
        if (args == null || args.length != 64) {
            return null;
        }
        return args;
    }

    public XLinkDataPoint setSV2Args(byte[] args) {
        if (args == null || args.length != 64) {
            return null;
        }
        return setByteArray(INDEX_SV2_ARGS, args);
    }

    public int getSV2ThrdLower() {
        return getInt(INDEX_SV2_THRD_LOWER);
    }

    public XLinkDataPoint setSV2ThrdLower(int thrd) {
        return setInt(INDEX_SV2_THRD_LOWER, thrd);
    }

    public int getSV2ThrdUpper() {
        return getInt(INDEX_SV2_THRD_UPPER);
    }

    public XLinkDataPoint setSV2ThrdUpper(int thrd) {
        return setInt(INDEX_SV2_THRD_UPPER, thrd);
    }

    public List<XLinkDataPoint> setSV2Notify(boolean enable, int thrd_lower, int thrd_upper) {
        final XLinkDataPoint dp0 = setSV2Type(getS2Type());
        final XLinkDataPoint dp1 = setSV2NotifyEnable(enable);
        final XLinkDataPoint dp2 = setSV2ThrdLower(thrd_lower);
        final XLinkDataPoint dp3 = setSV2ThrdUpper(thrd_upper);
        if (dp0 != null && dp1 != null && dp2 != null && dp3 != null) {
            List<XLinkDataPoint> dps = new ArrayList<>();
            dps.add(dp0);
            dps.add(dp1);
            dps.add(dp2);
            dps.add(dp3);
            return dps;
        }
        return null;
    }

    public List<XLinkDataPoint> setSV2LinkageArgs(byte[] args) {
        XLinkDataPoint dp1 = setSV2Type(getS2Type());
        XLinkDataPoint dp2 = setSV2Args(args);
        if (dp1 == null || dp2 == null) {
            return null;
        }
        List<XLinkDataPoint> dps = new ArrayList<>();
        dps.add(dp1);
        dps.add(dp2);
        return dps;
    }

    public boolean getPower() {
        return getBoolean(INDEX_POWER);
    }

    public XLinkDataPoint setPower(boolean power) {
        return setBoolean(INDEX_POWER, power);
    }

    public int getSwitchCount() {
        return getUInt(INDEX_SWITCH_COUNT);
    }

    public EXOSocketTimer getTimer(int idx) {
        if (idx < 0 || idx > TIMER_COUNT_MAX) {
            return null;
        }
        EXOSocketTimer tmr = new EXOSocketTimer(getUInt(INDEX_TIMER1+idx));
        if (tmr.isValid()) {
            return tmr;
        } else {
            return null;
        }
    }

    public List<EXOSocketTimer> getAllTimers() {
        List<EXOSocketTimer> timers = new ArrayList<>();
        for (int i = 0; i < TIMER_COUNT_MAX; i++) {
            int t = getUInt(INDEX_TIMER1+i);
            EXOSocketTimer tmr = new EXOSocketTimer(t);
            if (!tmr.isValid()) {
                break;
            }
            timers.add(tmr);
        }
        return timers;
    }

    public List<XLinkDataPoint> removeTimer(int idx) {
        List<EXOSocketTimer> timers = getAllTimers();
        if (idx >= 0 && idx < timers.size()) {
            timers.remove(idx);
            List<XLinkDataPoint> dps = new ArrayList<>();
            for (int i = idx; i < timers.size(); i++) {
                XLinkDataPoint dp = setTimer(i, timers.get(i));
                dps.add(dp);
            }
            for (int i = timers.size(); i < TIMER_COUNT_MAX; i++) {
                dps.add(setTimer(i, AppConstants.SOCKET_TIMER_INVALID));
            }
            return dps;
        }
        return null;
    }

    public XLinkDataPoint addTimer(EXOSocketTimer timer) {
        List<EXOSocketTimer> timers = getAllTimers();
        if (timers.size() < TIMER_COUNT_MAX) {
            return setTimer(timers.size(), timer);
        }
        return null;
    }

    public XLinkDataPoint setTimer(int idx, int timer) {
        if (idx < 0 || idx >= TIMER_COUNT_MAX) {
            return null;
        }
        return setUInt(INDEX_TIMER1 + idx, timer);
    }

    public XLinkDataPoint setTimer(int idx, EXOSocketTimer timer) {
        if (idx < 0 || idx >= TIMER_COUNT_MAX || timer == null || !timer.isValid()) {
            return null;
        }
        return setUInt(INDEX_TIMER1+idx, timer.getValue());
    }

    public List<XLinkDataPoint> setAllTimers(List<EXOSocketTimer> timers) {
        List<XLinkDataPoint> dps = new ArrayList<>();
        int[] tmrs = new int[TIMER_COUNT_MAX];
        for (int i = 0; i < TIMER_COUNT_MAX; i++) {
            tmrs[i] = AppConstants.SOCKET_TIMER_INVALID;
        }
        if (timers != null && timers.size() <= TIMER_COUNT_MAX) {
            for (int i = 0; i < timers.size(); i++) {
                tmrs[i] = timers.get(i).getValue();
            }
        }
        for (int i = 0; i < TIMER_COUNT_MAX; i++) {
            XLinkDataPoint dp = setUInt(INDEX_TIMER1 + i, tmrs[i]);
            dps.add(dp);
        }
        return dps;
    }
}
