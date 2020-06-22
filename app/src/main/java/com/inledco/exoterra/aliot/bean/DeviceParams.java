package com.inledco.exoterra.aliot.bean;

import java.util.HashMap;
import java.util.Map;

public class DeviceParams {
    private String id;
    private String productKey;
    private String deviceName;
    private final Map<String, Object> params = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductKey() {
        return productKey;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
