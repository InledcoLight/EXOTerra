package com.liruya.exoterra.device.Monsoon;

import com.liruya.exoterra.bean.EXOMonsoon;
import com.liruya.exoterra.bean.EXOMonsoonTimer;
import com.liruya.exoterra.device.DeviceViewModel;

import java.util.List;

public class MonsoonViewModel extends DeviceViewModel<EXOMonsoon> {
//    public void setZone(short zone) {
//        setDeviceDatapoint(getData().setZone(zone));
//    }
//
//    public void setLongitude(float longitude) {
//        setDeviceDatapoint(getData().setLongitude(longitude));
//    }
//
//    public void setLatitude(float latitude) {
//        setDeviceDatapoint(getData().setLatitude(latitude));
//    }

    public void setKeyAction(byte action) {
        setDeviceDatapoint(getData().setKeyAction(action));
    }

    public void setPower(byte power) {
        setDeviceDatapoint(getData().setPower(power));
    }

    public void setCustomActions(List<Byte> actions) {
        setDeviceDatapoint(getData().setCustomActions(actions));
    }

    public void removeTimer(int idx) {
        setDeviceDatapoints(getData().removeTimer(idx));
    }

    public void addTimer(EXOMonsoonTimer timer) {
        setDeviceDatapoint(getData().addTimer(timer));
    }

    public void setTimer(int idx, int timer) {
        setDeviceDatapoint(getData().setTimer(idx, timer));
    }

    public void setTimer(int idx, EXOMonsoonTimer timer) {
        setDeviceDatapoint(getData().setTimer(idx, timer));
    }

    public void setAllTimers(List<EXOMonsoonTimer> timers) {
        setDeviceDatapoints(getData().setAllTimers(timers));
    }
}
