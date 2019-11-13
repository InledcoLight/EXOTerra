package com.inledco.exoterra.bean;

import cn.xlink.restful.api.app.HomeApi;

public class RoomDevice {
    private String mRoomId;
    private int mDeviceId;
    private HomeApi.HomeDevicesResponse.Device mDevice;

    public RoomDevice(String roomId, HomeApi.HomeDevicesResponse.Device device) {
        mRoomId = roomId;
        mDeviceId = device.id;
        mDevice = device;
    }

    public String getRoomId() {
        return mRoomId;
    }


    public int getDeviceId() {
        return mDeviceId;
    }

    public HomeApi.HomeDevicesResponse.Device getDevice() {
        return mDevice;
    }
}
