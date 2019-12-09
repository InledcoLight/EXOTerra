package com.inledco.exoterra.bean;

import java.util.ArrayList;
import java.util.List;

import cn.xlink.restful.api.app.HomeApi;

public class Home {
    private HomeApi.HomesResponse.Home mHome;
    private HomeProperty mProperty;
    private final List<HomeApi.HomeDevicesResponse.Device> mDevices;

    public Home(HomeApi.HomesResponse.Home home) {
        this(home, null);
    }

    public Home(HomeApi.HomesResponse.Home home, HomeProperty property) {
        mHome = home;
        mProperty = property;
        mDevices = new ArrayList<>();
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
    }

    public List<HomeApi.HomeDevicesResponse.Device> getDevices() {
        return mDevices;
    }

    public void setDevices(List<HomeApi.HomeDevicesResponse.Device> devices) {
        mDevices.clear();
        if (devices != null && devices.size() > 0) {
            mDevices.addAll(devices);
        }
    }

    public int getDeviceCount() {
        return mDevices.size();
    }
}
