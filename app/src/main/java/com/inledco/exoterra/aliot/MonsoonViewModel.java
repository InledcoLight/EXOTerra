package com.inledco.exoterra.aliot;

import java.util.List;

public class MonsoonViewModel extends DeviceViewModel<ExoMonsoon> {
    public void setKeyAction(int action) {
        KeyValue attrKeyAction = getData().setKeyAction(action);
        setProperty(attrKeyAction);
    }

    public void setPower(int power) {
        KeyValue attrPower = getData().setPower(power);
        setProperty(attrPower);
    }

    public void setCustomActions(List<Integer> actions) {
        KeyValue attrCustomActions = getData().setCustomActions(actions);
        setProperty(attrCustomActions);
    }

    public void setTimers(List<ExoMonsoon.Timer> timers) {
        KeyValue attrTimers = getData().setTimers(timers);
        setProperty(attrTimers);
    }
}
