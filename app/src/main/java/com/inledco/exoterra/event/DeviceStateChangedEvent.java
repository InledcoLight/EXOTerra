package com.inledco.exoterra.event;

import android.support.annotation.NonNull;

import cn.xlink.sdk.v5.model.XDevice;

public class DeviceStateChangedEvent {
    private String mDeviceTag;
    private XDevice.State mState;

    public DeviceStateChangedEvent(@NonNull String deviceTag, @NonNull XDevice.State state) {
        mDeviceTag = deviceTag;
        mState = state;
    }

    public String getDeviceTag() {
        return mDeviceTag;
    }

    public void setDeviceTag(String deviceTag) {
        mDeviceTag = deviceTag;
    }

    public XDevice.State getState() {
        return mState;
    }

    public void setState(XDevice.State state) {
        mState = state;
    }
}
