package com.inledco.exoterra.bean;

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

import java.util.Arrays;

public final class ChannelSpectrum {
    private final String mName;
    private final float[] mValues;

    @FloatRange (from = 0.0f, to = 1.0f)
    private float mGain;

    public ChannelSpectrum(@NonNull String name, int size) {
        mName = name;
        mValues = new float[size];
        mGain = 1.0f;
    }

    public final int getSize() {
        return mValues.length;
    }

    public String getName() {
        return mName;
    }

    @FloatRange(from = 0.0f, to = 1.0f)
    public float getGain() {
        return mGain;
    }

    public void setGain(@FloatRange(from = 0.0f, to = 1.0f) float gain) {
        mGain = gain;
    }

    public float getValue(int idx) {
        if (idx >= 0 && idx < mValues.length) {
            return mValues[idx];
        }
        return 0;
    }

    public boolean setValue(int idx, float value) {
        if (idx >= 0 && idx < mValues.length) {
            mValues[idx] = value;
            return true;
        }
        return false;
    }

    public float getGainValue(int idx) {
        return getValue(idx) * mGain;
    }

    @NonNull
    @Override
    public String toString() {
        return mName + ": " + Arrays.toString(mValues);
    }
}
