package com.inledco.exoterra.aliot;

import java.util.List;

public class LightViewModel extends DeviceViewModel<ExoLed> {
    public void setMode(int mode) {
        KeyValue attrMode = getData().setMode(mode);
        setProperty(attrMode);
    }

    public void setPower(boolean power) {
        KeyValue attrPower = getData().setPower(power);
        setProperty(attrPower);
    }

    public void setChannelBright(int idx, int bright) {
        KeyValue attrChnBright = getData().setChannelBright(idx, bright);
        setProperty(attrChnBright);
    }

    public void setChannelBrights(int[] brights) {
        List<KeyValue> attrsBrights = getData().setChannelBrights(brights);
        setProperty(attrsBrights);
    }

    public void setCustomBrights(int idx, int[] brights) {
        KeyValue attrCustomBrights = getData().setCustomBrights(idx, brights);
        setProperty(attrCustomBrights);
    }

    public void setSunriseRamp(int ramp) {
        KeyValue attrSunriseRamp = getData().setSunriseRamp(ramp);
        setProperty(attrSunriseRamp);
    }

    public void setSunriseAndRamp(int sunrise, int ramp) {
        KeyValue attrSunrise = getData().setSunrise(sunrise);
        KeyValue attrRamp = getData().setSunriseRamp(ramp);
        setProperty(attrSunrise, attrRamp);
    }

    public void setSunsetRamp(int ramp) {
        KeyValue attrSunsetRamp = getData().setSunsetRamp(ramp);
        setProperty(attrSunsetRamp);
    }

    public void setSunsetAndRamp(int sunset, int ramp) {
        KeyValue attrSunset = getData().setSunset(sunset);
        KeyValue attrRamp = getData().setSunsetRamp(ramp);
        setProperty(attrSunset, attrRamp);
    }

    public void setDayBrights(int[] brights) {
        KeyValue attrDayBrights = getData().setDayBrights(brights);
        setProperty(attrDayBrights);
    }

    public void setNightBrights(int[] brights) {
        KeyValue attrNightBrights = getData().setNightBrights(brights);
        setProperty(attrNightBrights);
    }

    public void setTurnoffEnable(boolean enable) {
        KeyValue attrTurnoffEnable = getData().setTurnoffEnable(enable);
        setProperty(attrTurnoffEnable);
    }

    public void setSTurnoffTime(int time) {
        KeyValue attrTurnoffTime = getData().setTurnoffTime(time);
        setProperty(attrTurnoffTime);
    }

    public void setTurnoff(boolean enable, int time) {
        KeyValue attrEnable = getData().setTurnoffEnable(enable);
        KeyValue attrTime = getData().setTurnoffTime(time);
        setProperty(attrEnable, attrTime);
    }
}
