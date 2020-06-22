package com.inledco.exoterra.aliot.bean;

public class DeviceLabel {
    private String attrKey;
    private Object attrValue;

    public DeviceLabel(String key, Object value) {
        attrKey = key;
        attrValue = value;
    }

    public String getAttrKey() {
        return attrKey;
    }

    public Object getAttrValue() {
        return attrValue;
    }
}
