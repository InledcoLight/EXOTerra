package com.inledco.exoterra.bean;

import java.util.ArrayList;
import java.util.List;

import cn.xlink.restful.api.app.HomeApi;

public class XHome {
    private Home mHome;
    private List<HomeApi.HomeDevicesResponse.Device> mDevices;

    public XHome(Home home) {
        mHome = home;
        mDevices = new ArrayList<>();
    }

    public Home getHome() {
        return mHome;
    }

    public void setHome(Home home) {
        mHome = home;
    }

    public List<HomeApi.HomeDevicesResponse.Device> getDevices() {
        return mDevices;
    }

    public void setDevices(List<HomeApi.HomeDevicesResponse.Device> devices) {
        mDevices.clear();
        mDevices.addAll(devices);
    }
}
