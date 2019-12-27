package com.inledco.exoterra.bean;

import android.util.Log;

public class EXOSocketTimer {
    public static final byte ACTION_TURNOFF            = 0;
    public static final byte ACTION_TURNON             = 1;
    public static final byte ACTION_TURNON_PERIOD      = 2;

    private boolean mEnable;
    private byte    mAction;
    private byte    mRepeat;
    private byte    mHour;
    private byte    mMinute;
    private byte    mSecond;
    private byte    mEndHour;
    private byte    mEndMinute;
    private byte    mEndSecond;

    public boolean isValid() {
        if (mAction < ACTION_TURNOFF || mAction > ACTION_TURNON_PERIOD) {
            return false;
        }
        if (mHour < 0 || mMinute < 0 || mSecond < 0) {
            return false;
        }
        if (mHour > 23 || mMinute > 59 || mSecond > 59) {
            return false;
        }
        if (mEndHour < 0 || mEndMinute < 0 || mEndSecond < 0) {
            return false;
        }
        if (mEndHour > 23 || mEndMinute > 59 || mEndSecond > 59) {
            return false;
        }
        return true;
    }

    public byte getAction() {
        return mAction;
    }

    public void setAction(byte action) {
        mAction = action;
    }

    public byte getRepeat() {
        return mRepeat;
    }

    public void setRepeat(byte repeat) {
        mRepeat = repeat;
    }

    public byte getHour() {
        return mHour;
    }

    public void setHour(byte hour) {
        mHour = hour;
    }

    public byte getMinute() {
        return mMinute;
    }

    public void setMinute(byte minute) {
        mMinute = minute;
    }

    public byte getSecond() {
        return mSecond;
    }

    public void setSecond(byte second) {
        mSecond = second;
    }

    public byte getEndHour() {
        return mEndHour;
    }

    public void setEndHour(byte endHour) {
        mEndHour = endHour;
    }

    public byte getEndMinute() {
        return mEndMinute;
    }

    public void setEndMinute(byte endMinute) {
        mEndMinute = endMinute;
    }

    public byte getEndSecond() {
        return mEndSecond;
    }

    public void setEndSecond(byte endSecond) {
        mEndSecond = endSecond;
    }

    public boolean isEnable() {
        return mEnable;
    }

    public void setEnable(boolean enable) {
        mEnable = enable;
    }

    public byte[] toArray() {
        byte[] array = new byte[12];
        array[0] = (byte) (mEnable ? 0x01 : 0x00);
        array[1] = mAction;
        array[2] = mRepeat;
        array[3] = mHour;
        array[4] = mMinute;
        array[5] = mSecond;
        array[6] = mEndHour;
        array[7] = mEndMinute;
        array[8] = mEndSecond;
        array[9] = 0;
        array[10] = 0;
        array[11] = 0;
        return array;
    }

    public static class Builder {
        public static EXOSocketTimer createFromArray(final byte[] array) {
            if (array == null || array.length != 12) {
                return null;
            }
            if (array[0] != 0 && array[0] != 1) {
                return null;
            }
            if (array[1] < ACTION_TURNOFF || array[1] > ACTION_TURNON_PERIOD) {
                return null;
            }
            if (array[3] < 0 || array[4] < 0 || array[5] < 0) {
                return null;
            }
            if (array[3] > 23 || array[4] > 59 || array[5] > 59) {
                return null;
            }
            if (array[6] < 0 || array[7] < 0 || array[8] < 0) {
                return null;
            }
            if (array[6] > 23 || array[7] > 59 || array[8] > 59) {
                return null;
            }
            EXOSocketTimer timer = new EXOSocketTimer();
            timer.setEnable(array[0] == 1);
            timer.setAction(array[1]);
            timer.setRepeat(array[2]);
            timer.setHour(array[3]);
            timer.setMinute(array[4]);
            timer.setSecond(array[5]);
            timer.setEndHour(array[6]);
            timer.setEndMinute(array[7]);
            timer.setEndSecond(array[8]);
            return timer;
        }
    }
}
