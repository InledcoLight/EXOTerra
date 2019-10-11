package com.inledco.exoterra.bean;

public class TimePoint {
    private final int CHANNEL_COUNT_MIN = 1;
    private final int CHANNEL_COUNT_MAX = 6;
    private int mTimer;
    private byte[] mBrights;

    public TimePoint(int timer, byte[] brights) {
        if (timer >= 0 && timer <= 1439 && brights != null &&
            brights.length >= CHANNEL_COUNT_MIN && brights.length <= CHANNEL_COUNT_MAX) {
            mTimer = timer;
            mBrights = brights;
        }
    }

    public boolean isValid() {
        if (mTimer < 0 || mTimer > 1439) {
            return false;
        }
        if (mBrights == null || mBrights.length < CHANNEL_COUNT_MIN || mBrights.length > CHANNEL_COUNT_MAX) {
            return false;
        }
        return true;
    }

    public int getTimer() {
        return mTimer;
    }

    public void setTimer(int timer) {
        if (timer < 0 || timer > 1439) {
            return;
        }
        mTimer = timer;
    }

    public byte[] getBrights() {
        return mBrights;
    }

    public void setBright(int chn, int bright) {
        if (mBrights != null && chn >= 0 && chn < mBrights.length) {
            if (bright >= 0 && bright <= 100) {
                mBrights[chn] = (byte) bright;
            }
        }
    }
}
