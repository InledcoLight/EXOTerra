package com.inledco.exoterra.aliot;

public class KeyValue {
    private final String attrKey;
    private Object attrValue;

    public KeyValue(String attrKey, Object attrValue) {
        this.attrKey = attrKey;
        this.attrValue = attrValue;
    }

    public String getAttrKey() {
        return attrKey;
    }

    public Object getAttrValue() {
        return attrValue;
    }
}
