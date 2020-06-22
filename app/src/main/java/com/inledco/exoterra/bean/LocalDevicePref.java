package com.inledco.exoterra.bean;

public class LocalDevicePref {
    private final String productKey;
    private final String deviceName;
    private final String mac;
    private final long addTime;

    public LocalDevicePref(String productKey, String deviceName, String mac, long addTime) {
        this.productKey = productKey;
        this.deviceName = deviceName;
        this.mac = mac;
        this.addTime = addTime;
    }

    public String getProductKey() {
        return productKey;
    }

    public String getMac() {
        return mac;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public long getAddTime() {
        return addTime;
    }

    public String getTag() {
        return productKey + "_" + mac;
    }
}
