package com.liruya.exoterra.bean;

import com.liruya.exoterra.AppConstants;

import java.util.ArrayList;
import java.util.List;

import cn.xlink.restful.api.app.DeviceApi;
import cn.xlink.sdk.common.ByteUtil;
import cn.xlink.sdk.core.model.XLinkDataPoint;

public class EXOMonsoon extends Device {
    private final String TAG = "EXOMonsoon";

//    private final int INDEX_PROPERTY            = 0;
//    private final int INDEX_ZONE                = 1;
//    private final int INDEX_LONGITUDE           = 2;
//    private final int INDEX_LATITUDE            = 3;
//    private final int INDEX_DEVICE_DATETIME     = 4;
    private final int INDEX_STATUS              = 10;
    private final int INDEX_KEY_ACTION          = 11;
    private final int INDEX_POWER               = 12;
    private final int INDEX_POWERON_TMR         = 13;
    private final int INDEX_CUSTOM_ACTIONS      = 14;
    private final int INDEX_TIMER1              = 15;

    public static final int TIMER_COUNT_MAX     = 24;
    public static final int CUSTOM_ACTIONS_MAX  = 8;

    public EXOMonsoon(Device device) {
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

    public byte getStatus() {
        return getByte(INDEX_STATUS);
    }

    public byte getKeyAction() {
        return getByte(INDEX_KEY_ACTION);
    }

    public XLinkDataPoint setKeyAction(byte action) {
        if ((action&0x80)==0x80) {
            return null;
        }
        return setByte(INDEX_KEY_ACTION, action);
    }

    public byte getPower() {
        return getByte(INDEX_POWER);
    }

    public XLinkDataPoint setPower(byte power) {
        return setByte(INDEX_POWER, power);
    }

    public short getPoweronTmr() {
        return getShort(INDEX_POWERON_TMR);
    }

    public int getCustomActionsIndex() {
        return INDEX_CUSTOM_ACTIONS;
    }

    public List<Byte> getCustomActions() {
        List<Byte> results = new ArrayList<>();
        byte[] actions = getByteArray(INDEX_CUSTOM_ACTIONS);
        if (actions == null) {
            return results;
        }
        int len = 0;
        for (byte a : actions) {
            if (a >= 1 && a <= 127) {
                len++;
                if (len >= CUSTOM_ACTIONS_MAX) {
                    break;
                }
            } else {
                break;
            }
        }
        for (int i = 0; i < len; i++) {
            results.add(actions[i]);
        }
        return results;
    }

//    public XLinkDataPoint setCustomActions1(List<Byte> actions) {
//        if (actions == null || actions.size() > CUSTOM_ACTIONS_MAX) {
//            return null;
//        }
//        byte[] values = new byte[CUSTOM_ACTIONS_MAX];
//        for (int i = 0; i < actions.size(); i++) {
//            values[i] = actions.get(i);
//        }
//        return setByteArray(INDEX_CUSTOM_ACTIONS, values);
//    }

    public DeviceApi.DeviceDataPointRequest.Command setCustomActions(List<Byte> actions) {
        if (actions != null && actions.size() <= CUSTOM_ACTIONS_MAX) {
            byte[] values = new byte[actions.size()];
            for (int i = 0; i < actions.size(); i++) {
                values[i] = actions.get(i);
            }
            DeviceApi.DeviceDataPointRequest.Command cmd = new DeviceApi.DeviceDataPointRequest.Command<>();
            cmd.index = INDEX_CUSTOM_ACTIONS;
            cmd.value = ByteUtil.bytesToHex(values);            //字节数组需要转换为字符串
            return cmd;
        }
        return null;
    }

    public EXOMonsoonTimer getTimer(int idx) {
        if (idx < 0 || idx >= TIMER_COUNT_MAX) {
            return null;
        }
        int timer = getUInt(INDEX_TIMER1 + idx);
        return new EXOMonsoonTimer(timer);
    }

    public List<EXOMonsoonTimer> getAllTimers() {
        List<EXOMonsoonTimer> timers = new ArrayList<>();
        for (int i = 0; i < TIMER_COUNT_MAX; i++) {
            int t = getUInt(INDEX_TIMER1+i);
            EXOMonsoonTimer tmr = new EXOMonsoonTimer(t);
            if (!tmr.isValid()) {
                break;
            }
            timers.add(tmr);
        }
        return timers;
    }

    public List<XLinkDataPoint> removeTimer(int idx) {
        List<EXOMonsoonTimer> timers = getAllTimers();
        if (idx >= 0 && idx < timers.size()) {
            timers.remove(idx);
            List<XLinkDataPoint> dps = new ArrayList<>();
            for (int i = idx; i < timers.size(); i++) {
                XLinkDataPoint dp = setTimer(i, timers.get(i));
                dps.add(dp);
            }
            for (int i = timers.size(); i < TIMER_COUNT_MAX; i++) {
                dps.add(setTimer(timers.size(), AppConstants.MONSOON_TIMER_INVALID));
            }
            return dps;
        }
        return null;
    }

    public XLinkDataPoint addTimer(EXOMonsoonTimer timer) {
        List<EXOMonsoonTimer> timers = getAllTimers();
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

    public XLinkDataPoint setTimer(int idx, EXOMonsoonTimer timer) {
        if (idx < 0 || idx >= TIMER_COUNT_MAX || timer == null) {
            return null;
        }
        return setUInt(INDEX_TIMER1 + idx, timer.getValue());
    }

    public List<XLinkDataPoint> setAllTimers(List<EXOMonsoonTimer> timers) {
        List<XLinkDataPoint> dps = new ArrayList<>();
        int[] tmrs = new int[TIMER_COUNT_MAX];
        for (int i = 0; i < TIMER_COUNT_MAX; i++) {
            tmrs[i] = AppConstants.MONSOON_TIMER_INVALID;
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

//    public class Timer {
//        private int mTimer;
//        private int mDuration;
//        private boolean[] mWeeks;
//        private boolean mEnable;
//
//        public Timer() {
//            mWeeks = new boolean[7];
//        }
//
//        public Timer(int timer) {
//            mWeeks = new boolean[7];
//            mTimer = timer&0xFFFF;
//            mDuration = (timer&0x7F0000)>>16;
//            for (int i = 0; i < 7; i++) {
//                mWeeks[i] = ((timer & (1 << (24 + i))) == 0 ? false : true);
//            }
//            mEnable = (timer&0x80000000) == 0 ? false : true;
//        }
//
//        public boolean isValid() {
//            if (mTimer < 0 || mTimer > 1439) {
//                return false;
//            }
//            if (mDuration < 0 || mDuration > 127) {
//                return false;
//            }
//            return true;
//        }
//
//        public int getValue() {
//            int result = mTimer&0xFFFF;
//            result |= (mDuration&0x7F)<<16;
//            for (int i = 0; i < 7; i++) {
//                if (mWeeks[i]) {
//                    result |= (1<<(24+i));
//                }
//            }
//            if (mEnable) {
//                result |= 0x80000000;
//            }
//            return result;
//        }
//
//        public int getTimer() {
//            return mTimer;
//        }
//
//        public void setTimer(int timer) {
//            if (timer >= 0 && timer <= 1439) {
//                mTimer = timer;
//            }
//        }
//
//        public int getDuration() {
//            return mDuration;
//        }
//
//        public void setDuration(int duration) {
//            if (duration >= 0 && duration <= 127) {
//                mDuration = duration;
//            }
//        }
//
//        public boolean getWeek(int idx) {
//            if (idx < 0 || idx > 6) {
//                return false;
//            }
//            return mWeeks[idx];
//        }
//
//        public boolean[] getWeeks() {
//            return mWeeks;
//        }
//
//        public void setWeek(int idx, boolean value) {
//            if (idx >= 0 && idx < mWeeks.length) {
//                mWeeks[idx] = value;
//            }
//        }
//
//        public boolean isEnable() {
//            return mEnable;
//        }
//
//        public void setEnable(boolean enable) {
//            mEnable = enable;
//        }
//    }
}
