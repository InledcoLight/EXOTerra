package com.inledco.exoterra.aliot;

public class DeviceParam {
    private String region;
    private String productKey;
    private String productSecret;
    private String deviceName;
    private String deviceSecret;
    private String mac;
    private int zone;
    private long time;

    public DeviceParam() {
    }

    public DeviceParam(String region, String productKey, String productSecret, String deviceName, String deviceSecret, int zone, long time) {
        this.region = region;
        this.productKey = productKey;
        this.productSecret = productSecret;
        this.deviceName = deviceName;
        this.deviceSecret = deviceSecret;
        this.zone = zone;
        this.time = time;
    }

    public DeviceParam(String region, String productKey, String productSecret, String deviceSecret, int zone, long time) {
        this.region = region;
        this.productKey = productKey;
        this.productSecret = productSecret;
        this.deviceSecret = deviceSecret;
        this.zone = zone;
        this.time = time;
    }

    public DeviceParam(String deviceSecret, int zone, long time) {
        this.deviceSecret = deviceSecret;
        this.zone = zone;
        this.time = time;
    }

    public DeviceParam(int zone, long time) {
        this.zone = zone;
        this.time = time;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getProductKey() {
        return productKey;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }

    public String getProductSecret() {
        return productSecret;
    }

    public void setProductSecret(String productSecret) {
        this.productSecret = productSecret;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceSecret() {
        return deviceSecret;
    }

    public void setDeviceSecret(String deviceSecret) {
        this.deviceSecret = deviceSecret;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getZone() {
        return zone;
    }

    public void setZone(int zone) {
        this.zone = zone;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
