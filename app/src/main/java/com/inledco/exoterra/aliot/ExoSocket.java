package com.inledco.exoterra.aliot;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.annotation.JSONField;
import com.inledco.exoterra.aliot.bean.XDevice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExoSocket extends Device {
    private static final String TAG = "ExoSocket";

    public static final int TIMER_COUNT_MAX             = 24;
    public static final int MODE_TIMER                  = 0;
    public static final int MODE_SENSOR1                = 1;
    public static final int MODE_SENSOR2                = 2;
    public static final int SENSOR_COUNT_MAX            = 2;
    public static final int SENSOR_TEMPERATURE          = 1;
    public static final int SENSOR_HUMIDITY             = 2;

    private static final String KEY_SWITCH_MAX          = "SwitchMax";
    private static final String KEY_SWITCH_COUNT        = "SwitchCount";
    private static final String KEY_MODE                = "Mode";
    private static final String KEY_POWER               = "Power";
    private static final String KEY_TIMER1              = "T1";
    private static final String KEY_TIMER2              = "T2";
    private static final String KEY_TIMER3              = "T3";
    private static final String KEY_TIMER4              = "T4";
    private static final String KEY_TIMER5              = "T5";
    private static final String KEY_TIMER6              = "T6";
    private static final String KEY_TIMER7              = "T7";
    private static final String KEY_TIMER8              = "T8";
    private static final String KEY_TIMER9              = "T9";
    private static final String KEY_TIMER10             = "T10";
    private static final String KEY_TIMER11             = "T11";
    private static final String KEY_TIMER12             = "T12";
    private static final String KEY_TIMER13             = "T13";
    private static final String KEY_TIMER14             = "T14";
    private static final String KEY_TIMER15             = "T15";
    private static final String KEY_TIMER16             = "T16";
    private static final String KEY_TIMER17             = "T17";
    private static final String KEY_TIMER18             = "T18";
    private static final String KEY_TIMER19             = "T19";
    private static final String KEY_TIMER20             = "T20";
    private static final String KEY_TIMER21             = "T21";
    private static final String KEY_TIMER22             = "T22";
    private static final String KEY_TIMER23             = "T23";
    private static final String KEY_TIMER24             = "T24";
    private static final String[] KEY_TIMERS            = new String[] {
        KEY_TIMER1,
        KEY_TIMER2,
        KEY_TIMER3,
        KEY_TIMER4,
        KEY_TIMER5,
        KEY_TIMER6,
        KEY_TIMER7,
        KEY_TIMER8,
        KEY_TIMER9,
        KEY_TIMER10,
        KEY_TIMER11,
        KEY_TIMER12,
        KEY_TIMER13,
        KEY_TIMER14,
        KEY_TIMER15,
        KEY_TIMER16,
        KEY_TIMER17,
        KEY_TIMER18,
        KEY_TIMER19,
        KEY_TIMER20,
        KEY_TIMER21,
        KEY_TIMER22,
        KEY_TIMER23,
        KEY_TIMER24
    };
    private static final String KEY_SENSOR_AVAILABLE    = "SensorAvailable";
    private static final String KEY_SENSOR              = "Sensor";
    private static final String KEY_SENSOR_CONFIG       = "SensorConfig";

    public ExoSocket() {
    }

    public ExoSocket(String productKey, String deviceName) {
        super(productKey, deviceName);
    }

    public ExoSocket(XDevice xDevice) {
        super(xDevice);
    }

    public int getSwitchCountMax() {
        return getPropertyInt(KEY_SWITCH_MAX);
    }

    public int getSwitchCount() {
        return getPropertyInt(KEY_SWITCH_COUNT);
    }

    public int getMode() {
        return getPropertyInt(KEY_MODE);
    }

    public boolean getPower() {
        return getPropertyBool(KEY_POWER);
    }

    public Timer getTimer(int idx) {
        if (idx < 0 || idx >= KEY_TIMERS.length) {
            return null;
        }
        int[] obj = getPropertyIntArray(KEY_TIMERS[idx]);
        return parseArrayToTimer(obj);
    }

    public List<Timer> getTimers() {
        List<Timer> timers = new ArrayList<>();
        for (int i = 0; i < KEY_TIMERS.length; i++) {
            Timer tmr = getTimer(i);
            if (tmr != null) {
                timers.add(tmr);
            } else {
                break;
            }
        }
        return timers;
    }

    public boolean getSensorAvailable() {
        return getPropertyBool(KEY_SENSOR_AVAILABLE);
    }

    public Sensor[] getSensor() {
        Object obj = getPropertyValue(KEY_SENSOR);
        if (obj != null && obj instanceof JSONArray) {
            JSONArray ja = (JSONArray) obj;
            Sensor[] result = new Sensor[ja.size()];
            for (int i = 0; i < ja.size(); i++) {
                try {
                    result[i] = ja.getObject(i, Sensor.class);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return result;
        }
        return null;
    }

    public SensorConfig[] getSensorConfig() {
        Object obj = getPropertyValue(KEY_SENSOR_CONFIG);
        if (obj != null && obj instanceof JSONArray) {
            JSONArray ja = (JSONArray) obj;
            SensorConfig[] result = new SensorConfig[ja.size()];
            for (int i = 0; i < ja.size(); i++) {
                try {
                    result[i] = ja.getObject(i, SensorConfig.class);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return result;
        }
        return null;
    }

    public KeyValue setMode(int mode) {
        if (mode < MODE_TIMER || mode > MODE_SENSOR2) {
            return null;
        }
        return new KeyValue(KEY_MODE, mode);
    }

    public KeyValue setPower(boolean power) {
        return new KeyValue(KEY_POWER, power ? 1 : 0);
    }

    public KeyValue setTimer(int idx, Timer timer) {
        if (idx < 0 || idx >= KEY_TIMERS.length
            || timer == null || timer.isValid() == false) {
            return null;
        }
        return new KeyValue(KEY_TIMERS[idx], timer.toArray());
    }

    public List<KeyValue> setTimers(List<Timer> timers) {
        if (timers == null || timers.size() > KEY_TIMERS.length) {
            return null;
        }
        List<KeyValue> result = new ArrayList<>();
        int[] dummyArray = new int[0];
        int cnt = 0;
        for (int i = 0; i < timers.size(); i++) {
            Timer tmr = timers.get(i);
            if (tmr == null || tmr.isValid() == false) {
                break;
            }
            result.add(new KeyValue(KEY_TIMERS[i], tmr.toArray()));
            cnt++;
        }
        for (int i = cnt; i < KEY_TIMERS.length; i++) {
            result.add(new KeyValue(KEY_TIMERS[i], Arrays.copyOf(dummyArray, dummyArray.length)));
        }
        return result;
    }

    public KeyValue setSensorConfig(SensorConfig[] configs) {
        if (configs == null || configs.length > SENSOR_COUNT_MAX) {
            return null;
        }
        return new KeyValue(KEY_SENSOR_CONFIG, configs);
    }

    private Timer parseArrayToTimer(int[] array) {
        if (array == null || array.length != 9) {
            return null;
        }
        //  enable
        if (array[0] < 0 || array[0] > 1) {
            return null;
        }
        //  action
        if (array[1] < 0 || array[1] > 2) {
            return null;
        }
        //  repeat
        if (array[2] < 0 || array[2] > 0xFF) {
            return null;
        }
        //  hour
        if (array[3] < 0 || array[3] > 23) {
            return null;
        }
        //  minute
        if (array[4] < 0 || array[4] > 59) {
            return null;
        }
        //  second
        if (array[5] < 0 || array[5] > 59) {
            return null;
        }
        //  end_hour
        if (array[6] < 0 || array[6] > 23) {
            return null;
        }
        //  end_minute
        if (array[7] < 0 || array[7] > 59) {
            return null;
        }
        //  end_second
        if (array[8] < 0 || array[8] > 59) {
            return null;
        }
        boolean enable = (array[0] == 1 ? true : false);
        Timer timer = new Timer();
        timer.setEnable(enable);
        timer.setAction(array[1]);
        timer.setRepeat(array[2]&0x7F);
        timer.setHour(array[3]);
        timer.setMinute(array[4]);
        timer.setSecond(array[5]);
        timer.setEndHour(array[6]);
        timer.setEndMinute(array[7]);
        timer.setEndSecond(array[8]);
        return timer;
    }

    @Override
    protected String getProductName() {
        return "exosocket";
    }

    public static class Timer {
        public static final int ACTION_TURNOFF  = 0;
        public static final int ACTION_TURNON   = 1;
        public static final int ACTION_PERIOD   = 2;

        private boolean enable;
        private int action;
        private int repeat;
        private int hour;
        private int minute;
        private int second;
        private int end_hour;
        private int end_minute;
        private int end_second;

        public boolean isValid() {
            if (action < ACTION_TURNOFF || action > ACTION_PERIOD) {
                return false;
            }
            if (repeat < 0 || repeat > 0x7F) {
                return false;
            }
            if (hour < 0 || hour > 23) {
                return false;
            }
            if (minute < 0 || minute > 59) {
                return false;
            }
            if (second < 0 || second > 59) {
                return false;
            }
            if (end_hour < 0 || end_hour > 23) {
                return false;
            }
            if (end_minute < 0 || end_minute > 59) {
                return false;
            }
            if (end_second < 0 || end_second > 59) {
                return false;
            }
            return true;
        }

        public int[] toArray() {
            if (isValid()) {
                int[] array = new int[9];
                array[0] = (enable ? 1: 0);
                array[1] = action;
                array[2] = repeat;
                array[3] = hour;
                array[4] = minute;
                array[5] = second;
                array[6] = end_hour;
                array[7] = end_minute;
                array[8] = end_second;
                return array;
            }
            return new int[0];
        }

        public boolean isEnable() {
            return enable;
        }

        public int getAction() {
            return action;
        }

        public int getRepeat() {
            return repeat;
        }

        public int getHour() {
            return hour;
        }

        public int getMinute() {
            return minute;
        }

        public int getSecond() {
            return second;
        }

        public int getEndHour() {
            return end_hour;
        }

        public int getEndMinute() {
            return end_minute;
        }

        public int getEndSecond() {
            return end_second;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public void setAction(int action) {
            if (action >= 0 && action <= 2) {
                this.action = action;
            }
        }

        public void setRepeat(int repeat) {
            if (repeat >= 0 && repeat <= 0x7F) {
                this.repeat = repeat;
            }
        }

        public void setHour(int hour) {
            if (hour >= 0 && hour <= 23) {
                this.hour = hour;
            }
        }

        public void setMinute(int minute) {
            if (minute >= 0 && minute <= 59) {
                this.minute= minute;
            }
        }

        public void setSecond(int second) {
            if (second >= 0 && second <= 59) {
                this.second = second;
            }
        }

        public void setEndHour(int end_hour) {
            if (end_hour >= 0 && end_hour <= 23) {
                this.end_hour = end_hour;
            }
        }

        public void setEndMinute(int end_minute) {
            if (end_minute >= 0 && end_minute <= 59) {
                this.end_minute = end_minute;
            }
        }

        public void setEndSecond(int end_second) {
            if (end_second >= 0 && end_second <= 59) {
                this.end_second = end_second;
            }
        }

        public void setTime(int hour, int minute, int second) {
            setHour(hour);
            setMinute(minute);
            setSecond(second);
        }

        public void setEndTime(int end_hour, int end_minute, int end_second) {
            setEndHour(end_hour);
            setEndMinute(end_minute);
            setEndSecond(end_second);
        }
    }

    public static class Sensor {
        private int Type;
        private int Value;
        private int Min;
        private int Max;

        public int getType() {
            return Type;
        }

        public int getValue() {
            return Value;
        }

        public int getMin() {
            return Min;
        }

        public int getMax() {
            return Max;
        }

        public void setType(int type) {
            Type = type;
        }

        public void setValue(int value) {
            Value = value;
        }

        public void setMin(int min) {
            Min = min;
        }

        public void setMax(int max) {
            Max = max;
        }
    }

    public static class SensorConfig {
        @JSONField(name = "Type")
        private int type;
        @JSONField(name = "Ntfy")
        private int ntfy;
        @JSONField(name = "Lower")
        private int lower;
        @JSONField(name = "Upper")
        private int upper;
        @JSONField(name = "Day")
        private int day;
        @JSONField(name = "Night")
        private int night;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getNtfy() {
            return ntfy;
        }

        public void setNtfy(int ntfy) {
            this.ntfy = ntfy;
        }

        public int getLower() {
            return lower;
        }

        public void setLower(int lower) {
            this.lower = lower;
        }

        public int getUpper() {
            return upper;
        }

        public void setUpper(int upper) {
            this.upper = upper;
        }

        public int getDay() {
            return day;
        }

        public void setDay(int day) {
            this.day = day;
        }

        public int getNight() {
            return night;
        }

        public void setNight(int night) {
            this.night = night;
        }
    }
}
