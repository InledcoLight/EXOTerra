package com.inledco.exoterra.bean;

import java.util.ArrayList;
import java.util.List;

import cn.xlink.restful.api.app.HomeApi;

public class XHome {
    private Home2 mHome2;
    private List<HomeApi.HomeDevicesResponse.Device> mDevices;

    public XHome(Home2 home2) {
        mHome2 = home2;
        mDevices = new ArrayList<>();
    }

    public Home2 getHome2() {
        return mHome2;
    }

    public void setHome2(Home2 home2) {
        mHome2 = home2;
    }

    public List<HomeApi.HomeDevicesResponse.Device> getDevices() {
        return mDevices;
    }

    public void setDevices(List<HomeApi.HomeDevicesResponse.Device> devices) {
        mDevices.clear();
        mDevices.addAll(devices);
    }
}
