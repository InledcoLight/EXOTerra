package com.liruya.exoterra.device.socket;

import com.liruya.exoterra.bean.EXOSocket;
import com.liruya.exoterra.bean.EXOSocketTimer;
import com.liruya.exoterra.device.DeviceViewModel;

import java.util.List;

public class SocketViewModel extends DeviceViewModel<EXOSocket> {

    public void setConnectDevice(String devname) {
        setDeviceDatapoint(getData().setConnectDevice(devname));
    }

    public void setPower(boolean power) {
        setDeviceDatapoint(getData().setPower(power));
    }

    public void removeTimer(int idx) {
        setDeviceDatapoints(getData().removeTimer(idx));
    }

    public void addTimer(EXOSocketTimer timer) {
        setDeviceDatapoint(getData().addTimer(timer));
    }

    public void setTimer(int idx, int timer) {
        setDeviceDatapoint(getData().setTimer(idx, timer));
    }

    public void setTimer(int idx, EXOSocketTimer timer) {
        setDeviceDatapoint(getData().setTimer(idx, timer));
    }

    public void setAllTimers(List<EXOSocketTimer> timers) {
        setDeviceDatapoints(getData().setAllTimers(timers));
    }

    public void setSensor1(byte type, boolean ntfy, boolean linkage, byte[] args) {
        setDeviceDatapoints(getData().setSensor1(type, ntfy, linkage, args));
    }

    public void setSensor2(byte type, boolean ntfy, byte[] args) {
        setDeviceDatapoints(getData().setSensor2(type, ntfy, args));
    }
}
