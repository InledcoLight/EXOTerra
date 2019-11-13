package com.inledco.exoterra.device;

import com.inledco.exoterra.base.BaseViewModel;
import com.inledco.exoterra.bean.Device;

public class DeviceBaseViewModel extends BaseViewModel<Device> {
    private String mRoomId;

    public String getRoomId() {
        return mRoomId;
    }

    public void setRoomId(String roomId) {
        mRoomId = roomId;
    }
}
