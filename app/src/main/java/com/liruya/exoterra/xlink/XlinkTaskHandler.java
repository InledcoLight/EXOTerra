package com.liruya.exoterra.xlink;

public class XlinkTaskHandler<T> extends XlinkTaskCallback<T> {
    private boolean mOver;
    private boolean mSuccess;
    private String mError;
    private T mResult;

    public boolean isOver() {
        return mOver;
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

    @Override
    public void onStart() {
        mOver = false;
        mSuccess = false;
        mError = null;
        mResult = null;
    }

    @Override
    public void onComplete(T t) {
        mResult = t;
        mSuccess = true;
        mOver = true;
    }

    @Override
    public void onError(String error) {
        mError = error;
        mSuccess = false;
        mOver = true;
    }
}
