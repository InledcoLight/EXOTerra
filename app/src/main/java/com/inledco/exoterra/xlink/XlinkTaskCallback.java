package com.inledco.exoterra.xlink;

import cn.xlink.sdk.core.XLinkCoreException;
import cn.xlink.sdk.v5.listener.XLinkTaskListener;

public abstract class XlinkTaskCallback<T> extends XLinkTaskListener<T> {

    @Override
    public void onError(XLinkCoreException e) {
        onError("error code: " + e.getErrorCode() + "\n" + e.getErrorName());
    }

    @Override
    public void onStart() {

    }

    public abstract void onError(String error);
}