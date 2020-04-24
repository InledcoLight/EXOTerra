package com.inledco.exoterra.event;

public class DeviceChangedEvent {
    private final String productKey;
    private final String deviceName;

    public DeviceChangedEvent(String productKey, String deviceName) {
        this.productKey = productKey;
        this.deviceName = deviceName;
    }

    public String getTag() {
        return productKey + "_" + deviceName;
    }

    public String getProductKey() {
        return productKey;
    }

    public String getDeviceName() {
        return deviceName;
    }
}
