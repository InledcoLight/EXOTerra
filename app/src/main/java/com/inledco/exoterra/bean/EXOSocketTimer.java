package com.inledco.exoterra.bean;

public class EXOSocketTimer {
    private int mTimer;
    private boolean mAction;
    private boolean[] mWeeks;
    private boolean mEnable;

    public EXOSocketTimer() {
        mWeeks = new boolean[7];
    }

    public EXOSocketTimer(int timer) {
        mWeeks = new boolean[7];
        mTimer = timer&0xFFFF;
        mAction = (timer&0x010000) == 0 ? false : true;
        for (int i = 0; i < 7; i++) {
            mWeeks[i] = ((timer & (1 << (24 + i))) == 0 ? false : true);
        }
        mEnable = (timer&0x80000000) == 0 ? false : true;
    }

    public boolean isValid() {
        if (mTimer < 0 || mTimer > 1439) {
            return false;
        }
        return true;
    }

    public int getValue() {
        int result = mTimer&0xFFFF;
        if (mAction) {
            result |= 0x010000;
        }
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

    public boolean getAction() {
        return mAction;
    }

    public void setAction(boolean action) {
        mAction = action;
    }

    public boolean[] getWeeks() {
        return mWeeks;
    }

    public boolean getWeek(int idx) {
        if (idx >= 0 && idx < mWeeks.length) {
            return mWeeks[idx];
        }
        return false;
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
