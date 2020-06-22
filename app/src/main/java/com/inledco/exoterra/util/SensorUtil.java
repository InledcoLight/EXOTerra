package com.inledco.exoterra.util;

import com.inledco.exoterra.GlobalSettings;
import com.inledco.exoterra.aliot.ExoSocket;
import com.inledco.exoterra.bean.ExoSensor;

public class SensorUtil {
    public static int getSensorColor(int type) {
        int color = 0xFFFFFFFF;
        switch (type) {
            case ExoSocket.SENSOR_TEMPERATURE:
                color = 0xFFF7931D;
                break;
            case ExoSocket.SENSOR_HUMIDITY:
                color = 0xFF00AEEF;
                break;
        }
        return color;
    }

    public static String getSensorName(int type) {
        String name = "Sensor";
        switch (type) {
            case ExoSocket.SENSOR_TEMPERATURE:
                name = "Temperature";
                break;
            case ExoSocket.SENSOR_HUMIDITY:
                name = "Humidity";
                break;
        }
        return name;
    }

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

    public static String getSensorUnit(String name) {
        ExoSensor sensor = ExoSensor.valueOf(name);
        if (sensor == null) {
            return "";
        }
        int type = sensor.getType();
        return getSensorUnit(type);
    }
}
