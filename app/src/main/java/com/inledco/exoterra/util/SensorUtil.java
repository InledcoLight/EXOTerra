package com.inledco.exoterra.util;

import com.inledco.exoterra.GlobalSettings;
import com.inledco.exoterra.aliot.ExoSocket;

public class SensorUtil {
    public static String getSensorValueText(int value, int type) {
        String text = String.valueOf(value);
        switch (type) {
            case ExoSocket.SENSOR_TEMPERATURE:
                text = GlobalSettings.getTemperatureText(value);
                break;
            case ExoSocket.SENSOR_HUMIDITY:
                text = value/10 + "." + value%10;
                break;
        }
        return text;
    }

    public static String getSensorUnit(int type) {
        String text = "";
        switch (type) {
            case ExoSocket.SENSOR_TEMPERATURE:
                text = GlobalSettings.getTemperatureUnit();
                break;
            case ExoSocket.SENSOR_HUMIDITY:
                text = "%";
                break;
        }
        return text;
    }
}
