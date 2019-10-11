package com.inledco.exoterra.event;

public class DatapointChangedEvent {
    private String mDeviceTag;

    public DatapointChangedEvent(String deviceTag) {
        mDeviceTag = deviceTag;
    }

    public String getDeviceTag() {
        return mDeviceTag;
    }

    public void setDeviceTag(String deviceTag) {
        mDeviceTag = deviceTag;
    }
}
