package com.inledco.exoterra.xlink;

import android.support.annotation.NonNull;

import cn.xlink.sdk.core.XLinkCoreException;
import cn.xlink.sdk.v5.listener.XLinkScanDeviceListener;
import cn.xlink.sdk.v5.model.XDevice;

public class XlinkScanDeviceCallback extends XLinkScanDeviceListener {
    private String mAddress;
    private boolean mOver;
    private boolean mSuccess;
    private String mError;

    public XlinkScanDeviceCallback(@NonNull String address) {
        mAddress = address;
    }

    @Override
    public void onScanResult(XDevice xDevice) {

    }

    @Override
    public void onError(XLinkCoreException e) {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onComplete(Void aVoid) {

    }
}
