package com.liruya.exoterra.event;

public class DeviceStateChangedEvent {
    private String mDeviceTag;

    public DeviceStateChangedEvent(String deviceTag) {
        mDeviceTag = deviceTag;
    }

    public String getDeviceTag() {
        return mDeviceTag;
    }

    public void setDeviceTag(String deviceTag) {
        mDeviceTag = deviceTag;
    }
}
