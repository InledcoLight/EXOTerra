package com.inledco.exoterra.aliot;

import com.inledco.exoterra.aliot.bean.XDevice;

import java.util.ArrayList;
import java.util.List;

public class ExoMonsoon extends Device {
    private static final String TAG = "ExoMonsoon";

    public static final int TIMER_COUNT_MAX         = 24;
    public static final int SPRAY_OFF               = 0;
    public static final int SPRAY_DEFAULT           = 5;
    public static final int SPRAY_MIN               = 1;
    public static final int SPRAY_MAX               = 120;
    public static final int CUSTOM_ACTIONS_MAX      = 8;

    private static final String KEY_BUTTON_ACTION   = "KeyAction";
    private static final String KEY_POWER           = "Power";
    private static final String KEY_CUSTOM_ACTIONS  = "CustomActions";
    private static final String KEY_TIMERS          = "Timers";

    public ExoMonsoon() {
    }

    public ExoMonsoon(String productKey, String deviceName) {
        super(productKey, deviceName);
    }

    public ExoMonsoon(XDevice xDevice) {
        super(xDevice);
    }

    public int getKeyAction() {
        return getPropertyInt(KEY_BUTTON_ACTION);
    }

    public int getPower() {
        return getPropertyInt(KEY_POWER);
    }

    public long getPowerTime() {
        return getPropertyTime(KEY_POWER);
    }

    public List<Integer> getCustomActions() {
        List<Integer> result = new ArrayList<>();
        int[] array = getPropertyIntArray(KEY_CUSTOM_ACTIONS);
        if (array != null && array.length <= CUSTOM_ACTIONS_MAX) {
            for (int a : array) {
                if (a > 0 && a <= 120) {
                    result.add(a);
                } else {
                    break;
                }
            }
        }
        return result;
    }

    public List<Timer> getTimers() {
        List<Timer> result = new ArrayList<>();
        int[] array = getPropertyIntArray(KEY_TIMERS);
        if (array != null && array.length <= TIMER_COUNT_MAX) {
            for (int a : array) {
                Timer tmr = parseIntToTimer(a);
                if (tmr == null || !tmr.isValid()) {
                    break;
                }
                result.add(tmr);
            }
        }
        return result;
    }

    public KeyValue setKeyAction(int action) {
        if (action < SPRAY_MIN || action > SPRAY_MAX) {
            return null;
        }
        return new KeyValue(KEY_BUTTON_ACTION, action);
    }

    public KeyValue setPower(int power) {
        if (power < SPRAY_OFF || power > SPRAY_MAX) {
            return null;
        }
        return new KeyValue(KEY_POWER, power);
    }

    public KeyValue setCustomActions(List<Integer> actions) {
        if (actions == null || actions.size() > CUSTOM_ACTIONS_MAX) {
            return null;
        }
        int[] array = new int[CUSTOM_ACTIONS_MAX];
        for (int i = 0; i < array.length; i++) {
            array[i] = 0xFF;
        }
        for (int i = 0; i < actions.size(); i++) {
            int act = actions.get(i);
            if (act < SPRAY_MIN || act > SPRAY_MAX) {
                return null;
            }
            array[i] = act;
        }
        return new KeyValue(KEY_CUSTOM_ACTIONS, array);
    }

    public KeyValue setTimers(List<Timer> timers) {
        if (timers == null || timers.size() > TIMER_COUNT_MAX) {
            return null;
        }
        int[] array = new int[TIMER_COUNT_MAX];
        for (int i = 0; i < array.length; i++) {
            array[i] = 0xFFFFFFFF;
        }
        for (int i = 0; i < timers.size(); i++) {
            Timer tmr = timers.get(i);
            if (tmr == null || tmr.isValid() == false) {
                break;
            }
            array[i] = tmr.toInteger();
        }
        return new KeyValue(KEY_TIMERS, array);
    }

    private Timer parseIntToTimer(int value) {
        int tmr = value&0x1FFFF;
        int period = (value>>17)&0x7F;
        if (tmr > 1439 || period > 120 || period == 0) {
            return null;
        }
        int repeat = (value>>24)&0x7F;
        boolean enable = (value < 0);
        Timer timer = new Timer();
        timer.setEnable(enable);
        timer.setPeriod(period);
        timer.setRepeat(repeat);
        timer.setTimer(tmr);
        return timer;
    }

    @Override
    protected String getProductName() {
        return "exomonsoon";
    }

    public static class Timer {
        private boolean enable;
        private int     repeat;
        private int     period;
        private int     timer;

        public boolean isValid() {
            if (timer < 0 || timer > 1439) {
                return false;
            }
            if (period < 1 || period > 120) {
                return false;
            }
            if (repeat < 0 || repeat > 0x7F) {
                return false;
            }
            return true;
        }

        public int toInteger() {
            if (isValid()) {
                int result = timer;
                result |= (period<<17);
                result |= (repeat<<24);
                if (enable) {
                    result |= 0x80000000;
                }
                return result;
            }
            return 0xFFFFFFFF;
        }

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public int getRepeat() {
            return repeat;
        }

        public void setRepeat(int repeat) {
            if (repeat >= 0 && repeat <= 0x7F) {
                this.repeat = repeat;
            }
        }

        public int getPeriod() {
            return period;
        }

        public void setPeriod(int period) {
            if (period >= 1 && period <= 120) {
                this.period = period;
            }
        }

        public int getTimer() {
            return timer;
        }

        public void setTimer(int timer) {
            if (timer >= 0 && timer <= 1439) {
                this.timer = timer;
            }
        }
    }
}
