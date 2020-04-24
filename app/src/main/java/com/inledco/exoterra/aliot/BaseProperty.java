package com.inledco.exoterra.aliot;

public class BaseProperty {
    protected long time;
    protected Object value;
    protected boolean updated;

    public BaseProperty() {
    }

    public BaseProperty(long time, Object value) {
        this.time = time;
        this.value = value;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }
}
