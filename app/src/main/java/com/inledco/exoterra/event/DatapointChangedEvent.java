package com.inledco.exoterra.event;

import android.support.annotation.NonNull;

public class DatapointChangedEvent {
    private String mDeviceTag;

    public DatapointChangedEvent(@NonNull String deviceTag) {
        mDeviceTag = deviceTag;
    }

    public String getDeviceTag() {
        return mDeviceTag;
    }
}
