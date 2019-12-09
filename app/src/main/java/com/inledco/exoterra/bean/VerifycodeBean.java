package com.inledco.exoterra.bean;

public class VerifycodeBean {
    private final String mEmail;
    private final int mType;
    private final long mSendTime;

    public VerifycodeBean(String email, int type) {
        mEmail = email;
        mType = type;
        mSendTime = System.currentTimeMillis();
    }

    public String getEmail() {
        return mEmail;
    }

    public int getType() {
        return mType;
    }

    public long getSendTime() {
        return mSendTime;
    }
}
