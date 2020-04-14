package com.inledco.exoterra.bean;

public class Result {
    private boolean mSuccess;
    private String mMessage;

    public Result() {
        mSuccess = false;
    }

    public Result(boolean success, String message) {
        mSuccess = success;
        mMessage = message;
    }

    public boolean isSuccess() {
        return mSuccess;
    }

    public void setSuccess(boolean success) {
        mSuccess = success;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }
}
