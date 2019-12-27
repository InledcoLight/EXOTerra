package com.inledco.exoterra.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import cn.xlink.restful.api.app.HomeApi;

public class Home {
    private final int defaultZone;
    private final int defaultSunrise = 360;
    private final int defaultSunset = 1080;

    private HomeApi.HomesResponse.Home mHome;
    private HomeProperty mProperty;
    private final List<HomeApi.HomeDevicesResponse.Device> mDevices;
    private boolean mPropertySynchronized;
    private boolean mDevicesSynchronized;

    public Home(HomeApi.HomesResponse.Home home) {
        this(home, null);
    }

    public Home(HomeApi.HomesResponse.Home home, HomeProperty property) {
        mHome = home;
        mProperty = property;
        mDevices = new ArrayList<>();
        defaultZone = TimeZone.getDefault().getRawOffset()/60000;
    }

    public HomeApi.HomesResponse.Home getHome() {
        return mHome;
    }

    public void setHome(HomeApi.HomesResponse.Home home) {
        mHome = home;
    }

    public HomeProperty getProperty() {
        return mProperty;
    }

    public void setProperty(HomeProperty property) {
        mProperty = property;
        mPropertySynchronized = true;
    }

    public List<HomeApi.HomeDevicesResponse.Device> getDevices() {
        return mDevices;
    }

    public void setDevices(List<HomeApi.HomeDevicesResponse.Device> devices) {
        mDevices.clear();
        if (devices != null && devices.size() > 0) {
            mDevices.addAll(devices);
        }
        mDevicesSynchronized = true;
    }

    public boolean isPropertySynchronized() {
        return mPropertySynchronized;
    }

    public boolean isDevicesSynchronized() {
        return mDevicesSynchronized;
    }

    public int getZone() {
        if (mProperty == null) {
            return defaultZone;
        }
        return mProperty.getZone();
    }

    public int getSunrise() {
        if (mProperty == null) {
            return defaultSunrise;
        }
        return mProperty.getSunrise();
    }

    public int getSunset() {
        if (mProperty == null) {
            return defaultSunset;
        }
        return mProperty.getSunset();
    }

    public int getDeviceCount() {
        return mDevices.size();
    }
}
