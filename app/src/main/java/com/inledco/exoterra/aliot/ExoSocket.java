package com.inledco.exoterra.aliot;

import com.alibaba.fastjson.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExoSocket extends Device {
    private final String TAG = "ExoSocket";

    public static final int TIMER_COUNT_MAX           = 24;
    public static final int MODE_TIMER          = 0;
    public static final int MODE_SENSOR1        = 1;
    public static final int MODE_SENSOR2        = 2;
    public static final int SENSOR_COUNT_MAX    = 2;
    public static final int SENSOR_TEMPERATURE  = 1;
    public static final int SENSOR_HUMIDITY     = 2;

    private final String KEY_SWITCH_COUNT_MAX   = "SwitchCountMax";
    private final String KEY_SWITCH_COUNT       = "SwitchCount";

    private final String KEY_MODE               = "Mode";

    private final String KEY_POWER              = "Power";

    private final String KEY_TIMER1             = "Timer1";
    private final String KEY_TIMER2             = "Timer2";
    private final String KEY_TIMER3             = "Timer3";
    private final String KEY_TIMER4             = "Timer4";
    private final String KEY_TIMER5             = "Timer5";
    private final String KEY_TIMER6             = "Timer6";
    private final String KEY_TIMER7             = "Timer7";
    private final String KEY_TIMER8             = "Timer8";
    private final String KEY_TIMER9             = "Timer9";
    private final String KEY_TIMER10            = "Timer10";
    private final String KEY_TIMER11            = "Timer11";
    private final String KEY_TIMER12            = "Timer12";
    private final String KEY_TIMER13            = "Timer13";
    private final String KEY_TIMER14            = "Timer14";
    private final String KEY_TIMER15            = "Timer15";
    private final String KEY_TIMER16            = "Timer16";
    private final String KEY_TIMER17            = "Timer17";
    private final String KEY_TIMER18            = "Timer18";
    private final String KEY_TIMER19            = "Timer19";
    private final String KEY_TIMER20            = "Timer20";
    private final String KEY_TIMER21            = "Timer21";
    private final String KEY_TIMER22            = "Timer22";
    private final String KEY_TIMER23            = "Timer23";
    private final String KEY_TIMER24            = "Timer24";
    private final String[] KEY_TIMERS           = new String[] {
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

    private final String KEY_SENSOR_AVAILABLE   = "SensorAvailable";
    private final String KEY_SENSOR             = "Sensor";
    private final String KEY_SENSOR_CONFIG      = "SensorConfig";

    public int getSwitchCountMax() {
        return getPropertyInt(KEY_SWITCH_COUNT_MAX);
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
                if (ja.get(i) != null) {
                    result[i] = ja.getObject(i, Sensor.class);
                } else {
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
                if (ja.get(i) != null) {
                    result[i] = ja.getObject(i, SensorConfig.class);
                } else {
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
        int[] dummyArray = new int[] {0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF};
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
        if (array[2] < 0 || array[2] > 0x7F) {
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
        timer.setRepeat(array[2]);
        timer.setHour(array[3]);
        timer.setMinute(array[4]);
        timer.setSecond(array[5]);
        timer.setEndHour(array[6]);
        timer.setEndMinute(array[7]);
        timer.setEndSecond(array[8]);
        return timer;
    }

    public static class Timer {
        public static final int ACTION_TURNOFF          = 0;
        public static final int ACTION_TURNON           = 1;
        public static final int ACTION_TURNON_PERIOD    = 2;

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
            if (action < 0 || action > 2) {
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
    }

    public static class Sensor {
        private int Type;
        private int Value;
        private int ThrdLower;
        private int ThrdUpper;

        public int getType() {
            return Type;
        }

        public int getValue() {
            return Value;
        }

        public int getThrdLower() {
            return ThrdLower;
        }

        public int getThrdUpper() {
            return ThrdUpper;
        }

        public void setType(int type) {
            Type = type;
        }

        public void setValue(int value) {
            Value = value;
        }

        public void setThrdLower(int thrdLower) {
            ThrdLower = thrdLower;
        }

        public void setThrdUpper(int thrdUpper) {
            ThrdUpper = thrdUpper;
        }
    }

    public static class SensorConfig {
        private int Type;
        private int NtfyEnable;
        private int NtfyLower;
        private int NtfyUpper;
        private int Day;
        private int Night;

        public int getType() {
            return Type;
        }

        public void setType(int type) {
            Type = type;
        }

        public int getNtfyEnable() {
            return NtfyEnable;
        }

        public void setNtfyEnable(int ntfyEnable) {
            NtfyEnable = ntfyEnable;
        }

        public int getNtfyLower() {
            return NtfyLower;
        }

        public void setNtfyLower(int ntfyLower) {
            NtfyLower = ntfyLower;
        }

        public int getNtfyUpper() {
            return NtfyUpper;
        }

        public void setNtfyUpper(int ntfyUpper) {
            NtfyUpper = ntfyUpper;
        }

        public int getDay() {
            return Day;
        }

        public void setDay(int day) {
            Day = day;
        }

        public int getNight() {
            return Night;
        }

        public void setNight(int night) {
            Night = night;
        }
    }
}
