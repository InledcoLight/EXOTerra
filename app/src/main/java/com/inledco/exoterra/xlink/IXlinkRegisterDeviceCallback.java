package com.inledco.exoterra.xlink;

import cn.xlink.sdk.v5.model.XDevice;

public interface IXlinkRegisterDeviceCallback extends IXlinkRequestCallback<XDevice> {
    void onDeviceAlreadyExists(XDevice xDevice);
}
