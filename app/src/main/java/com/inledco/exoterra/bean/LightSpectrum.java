package com.inledco.exoterra.bean;

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

public class LightSpectrum {
    private boolean mValid;
    private int mStart;
    private int mEnd;
    private int mChannelCount;
    private ChannelSpectrum[] mChannelSpectrums;

    public LightSpectrum(int start, int end, @NonNull String[] names) {
        if (start < 0 || end < 0 || start >= end || names.length <= 0) {
            mValid = false;
        }
        else {
            mValid = true;
            mStart = start;
            mEnd = end;
            mChannelCount = names.length;
            int size = end - start + 1;
            mChannelSpectrums = new ChannelSpectrum[mChannelCount];
            for (int i = 0; i < mChannelCount; i++) {
                mChannelSpectrums[i] = new ChannelSpectrum(names[i], size);
            }
        }
    }

    public boolean isValid() {
        return mValid;
    }

    public int getStart() {
        return mStart;
    }

    public int getEnd() {
        return mEnd;
    }

    public int getChannelCount() {
        return mChannelCount;
    }

    public boolean put(int idx, float[] values) {
        if (mValid && idx >= 0 && idx < mEnd - mStart + 1 && values != null && values.length == mChannelCount) {
            for (int i = 0; i < mChannelCount; i++) {
                mChannelSpectrums[i].setValue(idx, values[i]);
            }
            return true;
        }
        return false;
    }

    /**
     * 获取单个通道的光强度
     * @param chn
     * @param idx
     * @return
     */
    public float get(int chn, int idx) {
        if (mValid && chn >= 0 || chn < mChannelCount) {
            return mChannelSpectrums[chn].getGainValue(idx);
        }
        return 0;
    }

    /**
     * 获取合并后的光强度
     * @param idx
     * @return
     */
    public float get(int idx) {
        float value = 0;
        if (mValid) {
            for (int i = 0; i < mChannelSpectrums.length; i++) {
                value += mChannelSpectrums[i].getGainValue(idx);
            }
        }
        return value;
    }

    public float getMax() {
        float totalMax = 0;
        if (mValid) {
            for (int i = 0; i < mEnd - mStart + 1; i++) {
                float totalValue = 0;
                for (int j = 0; j < mChannelSpectrums.length; j++) {
                    totalValue += mChannelSpectrums[j].getGainValue(i);
                }
                if (totalMax < totalValue) {
                    totalMax = totalValue;
                }
            }
        }
        if (totalMax <= 0) {
            return 1;
        }
        return totalMax;
    }

    public void setGain(int chn, @FloatRange (from = 0.0f, to = 1.0f) float gain) {
        if (mValid && chn >= 0 && chn < mChannelSpectrums.length) {
            mChannelSpectrums[chn].setGain(gain);
        }
    }

    @NonNull
    @Override
    public String toString() {
        if (mValid) {
            String str = "start: " + mStart + " end: " + mEnd + "\n";
            for (int i = 0; i < mChannelSpectrums.length; i++) {
                str = str + mChannelSpectrums[i].toString() + "\n";
            }
            return str;
        } else {
            return "Invalid.";
        }
    }
}
