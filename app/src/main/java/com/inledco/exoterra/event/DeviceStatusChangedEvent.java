package com.inledco.exoterra.event;

public class DeviceStatusChangedEvent {
    private String productKey;
    private String deviceName;

    public DeviceStatusChangedEvent(String productKey, String deviceName) {
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
