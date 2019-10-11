package com.inledco.exoterra.bean;

public class Result {
    private boolean mSuccess;
    private String mError;

    public Result() {
        mSuccess = false;
    }

    public Result(boolean success, String error) {
        mSuccess = success;
        mError = error;
    }

    public boolean isSuccess() {
        return mSuccess;
    }

    public void setSuccess(boolean success) {
        mSuccess = success;
    }

    public String getError() {
        return mError;
    }

    public void setError(String error) {
        mError = error;
    }
}
