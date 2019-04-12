package com.liruya.exoterra.bean;

public class EXOMonsoonTimer {
    private int mTimer;
    private int mDuration;
    private boolean[] mWeeks;
    private boolean mEnable;

    public EXOMonsoonTimer() {
        mWeeks = new boolean[7];
    }

    public EXOMonsoonTimer(int timer) {
        mWeeks = new boolean[7];
        mTimer = timer&0xFFFF;
        mDuration = (timer&0x7F0000)>>16;
        for (int i = 0; i < 7; i++) {
            mWeeks[i] = ((timer & (1 << (24 + i))) == 0 ? false : true);
        }
        mEnable = (timer&0x80000000) == 0 ? false : true;
    }

    public boolean isValid() {
        if (mTimer < 0 || mTimer > 1439) {
            return false;
        }
        if (mDuration < 1 || mDuration > 127) {
            return false;
        }
        return true;
    }

    public int getValue() {
        int result = mTimer&0xFFFF;
        result |= (mDuration&0x7F)<<16;
        for (int i = 0; i < 7; i++) {
            if (mWeeks[i]) {
                result |= (1<<(24+i));
            }
        }
        if (mEnable) {
            result |= 0x80000000;
        }
        return result;
    }

    public int getTimer() {
        return mTimer;
    }

    public void setTimer(int timer) {
        if (timer >= 0 && timer <= 1439) {
            mTimer = timer;
        }
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int duration) {
        if (duration >= 0 && duration <= 127) {
            mDuration = duration;
        }
    }

    public boolean getWeek(int idx) {
        if (idx < 0 || idx > 6) {
            return false;
        }
        return mWeeks[idx];
    }

    public boolean[] getWeeks() {
        return mWeeks;
    }

    public void setWeek(int idx, boolean value) {
        if (idx >= 0 && idx < mWeeks.length) {
            mWeeks[idx] = value;
        }
    }

    public boolean isEnable() {
        return mEnable;
    }

    public void setEnable(boolean enable) {
        mEnable = enable;
    }
}
