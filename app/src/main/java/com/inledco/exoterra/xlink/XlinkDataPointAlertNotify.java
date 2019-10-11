package com.inledco.exoterra.xlink;

import cn.xlink.sdk.common.JsonBuilder;

public class XlinkDataPointAlertNotify {
    private int index;
    private Object value;
    private String msg;

    public XlinkDataPointAlertNotify() {
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String toString() {
        return (new JsonBuilder()).put("index", this.index).put("value", this.value).put("msg", this.msg).toString();
    }
}
