package com.inledco.exoterra.device.socket;

import com.inledco.exoterra.bean.EXOSocket;
import com.inledco.exoterra.bean.EXOSocketTimer;
import com.inledco.exoterra.device.DeviceViewModel;

import java.util.List;

public class SocketViewModel extends DeviceViewModel<EXOSocket> {

    public void setConnectDevice(String devname) {
        setApplicationSetDatapoint(getData().setConnectDevice(devname));
    }

    public void setMode(byte mode) {
        setDeviceDatapoint(getData().setMode(mode));
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

    public void setTimer(int idx, EXOSocketTimer timer) {
        setDeviceDatapoint(getData().setTimer(idx, timer));
    }

    public void setAllTimers(List<EXOSocketTimer> timers) {
        setDeviceDatapoints(getData().setAllTimers(timers));
    }

    public void setSensor1Notify(boolean enable, int lower, int upper) {
        setDeviceDatapoints(getData().setSV1Notify(enable, lower, upper));
    }

    public void setSensorLinkage(boolean linkage, byte[] args) {
//        setDeviceDatapoints(getData().setSV1LinkageArgs(linkage, args));
    }

    public void setSensor2Notify(boolean enable, int lower, int upper) {
        setDeviceDatapoints(getData().setSV2Notify(enable, lower, upper));
    }

    public void setSensor2Args(byte[] args) {
//        setDeviceDatapoints(getData().setSV2LinkageArgs(args));
    }

    public void setSensor1DayThreshold(int value) {
        setDeviceDatapoint(getData().setSV1DayThreshold(value));
    }

    public void setSensor1NightThreshold(int value) {
        setDeviceDatapoint(getData().setSV1NightThreshold(value));
    }

    public void setSensor2DayThreshold(int value) {
        setDeviceDatapoint(getData().setSV2DayThreshold(value));
    }

    public void setSensor2NightThreshold(int value) {
        setDeviceDatapoint(getData().setSV2NightThreshold(value));
    }
}
