package com.inledco.exoterra.event;

public class DevicePropertyChangedEvent {
    private int mDeviceId;
    private String mDeviceName;

    public DevicePropertyChangedEvent(int devid, String devname) {
        mDeviceId = devid;
        mDeviceName = devname;
    }

    public int getDeviceId() {
        return mDeviceId;
    }

    public String getDeviceName() {
        return mDeviceName;
    }
}
