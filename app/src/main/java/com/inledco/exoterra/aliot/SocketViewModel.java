package com.inledco.exoterra.aliot;

import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.util.List;

public class SocketViewModel extends DeviceViewModel<ExoSocket> {
    public void setMode(int mode) {
        KeyValue attrMode = getData().setMode(mode);
        setProperty(attrMode);
    }

    public void setPower(boolean power) {
        KeyValue attrPower = getData().setPower(power);
        setProperty(attrPower);
    }

    public void setTimer(int idx, ExoSocket.Timer timer) {
        KeyValue attrTimer = getData().setTimer(idx, timer);
        setProperty(attrTimer);
    }

    public void setTimers(List<ExoSocket.Timer> timers) {
        List<KeyValue> attrTimers = getData().setTimers(timers);
        setProperty(attrTimers);
    }

    public void setSensorConfig(ExoSocket.SensorConfig[] configs) {
        KeyValue attrSensorConfig = getData().setSensorConfig(configs);
        Log.e(TAG, "setSensorConfig: " + JSON.toJSONString(configs));
        Log.e(TAG, "setSensorConfig: " + JSON.toJSONString(attrSensorConfig));
        setProperty(attrSensorConfig);
    }
}
