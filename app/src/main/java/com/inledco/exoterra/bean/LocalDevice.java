package com.inledco.exoterra.bean;

import android.support.annotation.NonNull;

public class LocalDevice extends LocalDevicePref {
//    private XDevice xDevice;
//
    public LocalDevice(@NonNull final LocalDevicePref localDevicePref) {
        super(localDevicePref.getPid(), localDevicePref.getMac(), localDevicePref.getName(), localDevicePref.getAddTime());
    }
//
//    public XDevice getxDevice() {
//        return xDevice;
//    }
//
//    public void setxDevice(XDevice xDevice) {
//        if (xDevice == null) {
//            return;
//        }
//        if (TextUtils.equals(xDevice.getProductId(), getPid())
//            && TextUtils.equals(xDevice.getMacAddress(), getMac())) {
//            this.xDevice = xDevice;
//        }
//    }
}
