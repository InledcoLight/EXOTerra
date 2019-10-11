package com.inledco.exoterra.bean;

public class TimeZone {
    private String mName;
    private int mOffset;

    public TimeZone(String name, int offset) {
        mName = name;
        mOffset = offset;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getOffset() {
        return mOffset;
    }

    public void setOffset(int offset) {
        mOffset = offset;
    }
}
