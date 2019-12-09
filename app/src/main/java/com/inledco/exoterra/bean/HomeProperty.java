package com.inledco.exoterra.bean;

import android.support.annotation.NonNull;

public class HomeProperty {
    private int mZone;
    private int mSunrise;
    private int mSunset;

    public HomeProperty(int zone, int sunrise, int sunset) {
        mZone = zone;
        mSunrise = sunrise;
        mSunset = sunset;
    }

    public int getZone() {
        return mZone;
    }

    public void setZone(int zone) {
        mZone = zone;
    }

    public int getSunrise() {
        return mSunrise;
    }

    public void setSunrise(int sunrise) {
        mSunrise = sunrise;
    }

    public int getSunset() {
        return mSunset;
    }

    public void setSunset(int sunset) {
        mSunset = sunset;
    }

    @NonNull
    @Override
    public String toString() {
        return "zone: " + mZone + ", sunrise: " + mSunrise + ", sunset: " + mSunset;
    }
}
