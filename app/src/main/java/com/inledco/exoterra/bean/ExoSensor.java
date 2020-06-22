package com.inledco.exoterra.bean;

public enum ExoSensor {
    UnkownSensor(0),
    Temperature(1),
    Humidity(2);

    private final int type;

    ExoSensor(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
