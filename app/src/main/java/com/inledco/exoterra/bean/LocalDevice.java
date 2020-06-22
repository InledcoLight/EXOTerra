package com.inledco.exoterra.bean;

import android.support.annotation.NonNull;

public class LocalDevice extends LocalDevicePref {
    private String ssid;
    private String localIp;
    private String localPort;

    public LocalDevice(@NonNull final LocalDevicePref localDevicePref) {
        super(localDevicePref.getProductKey(), localDevicePref.getDeviceName(), localDevicePref.getMac(), localDevicePref.getAddTime());
    }
}
