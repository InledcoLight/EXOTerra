package com.liruya.exoterra.xlink;

import cn.xlink.restful.XLinkCallback;
import cn.xlink.restful.XLinkRestfulError;
import cn.xlink.sdk.core.error.XLinkErrorCodeHelper;

public abstract class XlinkRequestCallback<T> extends XLinkCallback<T> {

    @Override
    public void onHttpError(Throwable throwable) {
        onError(throwable.getMessage());
    }

    @Override
    public void onApiError(XLinkRestfulError.ErrorWrapper.Error error) {
        onError(XLinkErrorCodeHelper.getErrorCodeName(error.code));
    }

    public void onStart() {

    }

    public abstract void onError(String error);
}
