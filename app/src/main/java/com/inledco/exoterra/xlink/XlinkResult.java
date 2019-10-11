package com.inledco.exoterra.xlink;

public final class XlinkResult<T> {
    private boolean mSuccess;
    private String mError;
    private T mResult;

    public XlinkResult() {
        mSuccess = false;
        mError = null;
        mResult = null;
    }

    public XlinkResult(String error) {
        mSuccess = false;
        mError = error;
    }

    public XlinkResult(T result) {
        mSuccess = true;
        mResult = result;
    }

    public boolean isSuccess() {
        return mSuccess;
    }

    public String getError() {
        return mError;
    }

    public T getResult() {
        return mResult;
    }

    public void setSuccess(boolean success) {
        mSuccess = success;
    }

    public void setError(String error) {
        mError = error;
    }

    public void setResult(T result) {
        mResult = result;
    }
}
