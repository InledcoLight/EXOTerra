package com.inledco.exoterra.aliot;

import com.inledco.exoterra.aliot.bean.XDevice;

import java.util.ArrayList;
import java.util.List;

public class ExoLed extends Device {
    private final String TAG = "ExoLed";

    private final int CHANNEL_COUNT_MAX         = 6;
    public static final int MODE_MANUAL         = 0;
    public static final int MODE_AUTO           = 1;
    public static final int MODE_PRO            = 2;
    private final int BRIGHT_MIN                = 0;
    private final int BRIGHT_MAX                = 1000;
    private final int RAMP_MIN                  = 0;
    private final int RAMP_MAX                  = 240;

    private final String KEY_CHANNEL_COUNT      = "ChannelCount";
    private final String KEY_CHN1_NAME          = "Chn1Name";
    private final String KEY_CHN2_NAME          = "Chn2Name";
    private final String KEY_CHN3_NAME          = "Chn3Name";
    private final String KEY_CHN4_NAME          = "Chn4Name";
    private final String KEY_CHN5_NAME          = "Chn5Name";
    private final String KEY_CHN6_NAME          = "Chn6Name";
    private final String[] KEY_CHN_NAMES        = new String[] {
        KEY_CHN1_NAME,
        KEY_CHN2_NAME,
        KEY_CHN3_NAME,
        KEY_CHN4_NAME,
        KEY_CHN5_NAME,
        KEY_CHN6_NAME
    };

    private final String KEY_MODE               = "Mode";

    private final String KEY_POWER              = "Power";
    private final String KEY_CHN1_BRIGHT        = "Chn1Bright";
    private final String KEY_CHN2_BRIGHT        = "Chn2Bright";
    private final String KEY_CHN3_BRIGHT        = "Chn3Bright";
    private final String KEY_CHN4_BRIGHT        = "Chn4Bright";
    private final String KEY_CHN5_BRIGHT        = "Chn5Bright";
    private final String KEY_CHN6_BRIGHT        = "Chn6Bright";
    private final String[] KEY_CHN_BRIGHTS      = new String[] {
        KEY_CHN1_BRIGHT,
        KEY_CHN2_BRIGHT,
        KEY_CHN3_BRIGHT,
        KEY_CHN4_BRIGHT,
        KEY_CHN5_BRIGHT,
        KEY_CHN6_BRIGHT
    };
    private final String KEY_CUSTOM1_BRIGHTS    = "Custom1Brights";
    private final String KEY_CUSTOM2_BRIGHTS    = "Custom2Brights";
    private final String KEY_CUSTOM3_BRIGHTS    = "Custom3Brights";
    private final String KEY_CUSTOM4_BRIGHTS    = "Custom4Brights";
    private final String[] KEY_CUSTOM_BRIGHTS     = new String[] {
        KEY_CUSTOM1_BRIGHTS,
        KEY_CUSTOM2_BRIGHTS,
        KEY_CUSTOM3_BRIGHTS,
        KEY_CUSTOM4_BRIGHTS
    };

    private final String KEY_SUNRISE_RAMP       = "SunriseRamp";
    private final String KEY_SUNSET_RAMP        = "SunsetRamp";
    private final String KEY_DAY_BRIGHTS        = "DayBrights";
    private final String KEY_NIGHT_BRIGHTS      = "NightBrights";
    private final String KEY_TURNOFF_ENABLE     = "TurnoffEnable";
    private final String KEY_TURNOFF_TIME       = "TurnoffTime";

    public ExoLed(XDevice xDevice) {
        super(xDevice);
    }

    public int getChannelCountMax() {
        return CHANNEL_COUNT_MAX;
    }

    public int getChannelCount() {
        return getPropertyInt(KEY_CHANNEL_COUNT);
    }

    public String getChn1Name() {
        return getPropertyString(KEY_CHN1_NAME);
    }

    public String getChn2Name() {
        return getPropertyString(KEY_CHN2_NAME);
    }

    public String getChn3Name() {
        return getPropertyString(KEY_CHN3_NAME);
    }

    public String getChn4Name() {
        return getPropertyString(KEY_CHN4_NAME);
    }

    public String getChn5Name() {
        return getPropertyString(KEY_CHN5_NAME);
    }

    public String getChn6Name() {
        return getPropertyString(KEY_CHN6_NAME);
    }

    public String getChannelName(int idx) {
        if (idx < 0 || idx >= KEY_CHN_NAMES.length) {
            return null;
        }
        return getPropertyString(KEY_CHN_NAMES[idx]);
    }

    public String[] getChannelNames() {
        int size = KEY_CHN_NAMES.length;
        String[] names = new String[size];
        for (int i = 0; i < size; i++) {
            names[i] = getChannelName(i);
        }
        return names;
    }

    public int getMode() {
        return getPropertyInt(KEY_MODE);
    }

    public boolean getPower() {
        return getPropertyBool(KEY_POWER);
    }

    public int getChn1Bright() {
        return getPropertyInt(KEY_CHN1_BRIGHT);
    }

    public int getChn2Bright() {
        return getPropertyInt(KEY_CHN2_BRIGHT);
    }

    public int getChn3Bright() {
        return getPropertyInt(KEY_CHN3_BRIGHT);
    }

    public int getChn4Bright() {
        return getPropertyInt(KEY_CHN4_BRIGHT);
    }

    public int getChn5Bright() {
        return getPropertyInt(KEY_CHN5_BRIGHT);
    }

    public int getChn6Bright() {
        return getPropertyInt(KEY_CHN6_BRIGHT);
    }

    public int getChannelBright(int idx) {
        if (idx < 0 || idx >= KEY_CHN_BRIGHTS.length) {
            return 0;
        }
        return getPropertyInt(KEY_CHN_BRIGHTS[idx]);
    }

    public int[] getChannelBrights() {
        int size = getChannelCount();
        int[] brights = new int[size];
        for (int i = 0; i < size; i++) {
            brights[i] = getChannelBright(i);
        }
        return brights;
    }

    public int[] getCustom1Brights() {
        return getPropertyIntArray(KEY_CUSTOM1_BRIGHTS);
    }

    public int[] getCustom2Brights() {
        return getPropertyIntArray(KEY_CUSTOM2_BRIGHTS);
    }

    public int[] getCustom3Brights() {
        return getPropertyIntArray(KEY_CUSTOM3_BRIGHTS);
    }

    public int[] getCustom4Brights() {
        return getPropertyIntArray(KEY_CUSTOM4_BRIGHTS);
    }

    public int[] getCustomBrights(int idx) {
        if (idx < 0 || idx >= KEY_CUSTOM_BRIGHTS.length) {
            return null;
        }
        return getPropertyIntArray(KEY_CUSTOM_BRIGHTS[idx]);
    }

    public int getSunriseRamp() {
        return getPropertyInt(KEY_SUNRISE_RAMP);
    }

    public int getSunsetRamp() {
        return getPropertyInt(KEY_SUNSET_RAMP);
    }

    public int[] getDayBrights() {
        return getPropertyIntArray(KEY_DAY_BRIGHTS);
    }

    public int[] getNightBrights() {
        return getPropertyIntArray(KEY_NIGHT_BRIGHTS);
    }

    public boolean getTurnoffEnable() {
        return getPropertyBool(KEY_TURNOFF_ENABLE);
    }

    public int getTurnoffTime() {
        return getPropertyInt(KEY_TURNOFF_TIME);
    }

    public KeyValue setMode(int mode) {
        if (mode < MODE_MANUAL || mode > MODE_PRO) {
            return null;
        }
        return new KeyValue(KEY_MODE, mode);
    }

    public KeyValue setPower(boolean power) {
        return new KeyValue(KEY_POWER, power ? 1 : 0);
    }

    public KeyValue setChn1Bright(int bright) {
        if (bright < BRIGHT_MIN || bright > BRIGHT_MAX) {
            return null;
        }
        return new KeyValue(KEY_CHN1_BRIGHT, bright);
    }

    public KeyValue setChn2Bright(int bright) {
        if (bright < BRIGHT_MIN || bright > BRIGHT_MAX) {
            return null;
        }
        return new KeyValue(KEY_CHN2_BRIGHT, bright);
    }

    public KeyValue setChn3Bright(int bright) {
        if (bright < BRIGHT_MIN || bright > BRIGHT_MAX) {
            return null;
        }
        return new KeyValue(KEY_CHN3_BRIGHT, bright);
    }

    public KeyValue setChn4Bright(int bright) {
        if (bright < BRIGHT_MIN || bright > BRIGHT_MAX) {
            return null;
        }
        return new KeyValue(KEY_CHN4_BRIGHT, bright);
    }

    public KeyValue setChn5Bright(int bright) {
        if (bright < BRIGHT_MIN || bright > BRIGHT_MAX) {
            return null;
        }
        return new KeyValue(KEY_CHN5_BRIGHT, bright);
    }

    public KeyValue setChn6Bright(int bright) {
        if (bright < BRIGHT_MIN || bright > BRIGHT_MAX) {
            return null;
        }
        return new KeyValue(KEY_CHN6_BRIGHT, bright);
    }

    public KeyValue setChannelBright(int idx, int bright) {
        if (idx < 0 || idx >= KEY_CHN_BRIGHTS.length || bright < BRIGHT_MIN || bright > BRIGHT_MAX) {
            return null;
        }
        return new KeyValue(KEY_CHN_BRIGHTS[idx], bright);
    }

    public List<KeyValue> setChannelBrights(int[] brights) {
        if (brights == null || brights.length != getChannelCount()) {
            return null;
        }
        List<KeyValue> attrs = new ArrayList<>();
        for (int i = 0; i < brights.length; i++) {
            int brt = brights[i];
            if (brt < BRIGHT_MIN || brt > BRIGHT_MAX) {
                return null;
            }
            attrs.add(new KeyValue(KEY_CHN_BRIGHTS[i], brt));
        }
        return attrs;
    }

    public KeyValue setCustom1Brights(int[] brights) {
        if (brights == null || brights.length != getChannelCount()) {
            return null;
        }
        for (int i = 0; i < brights.length; i++) {
            int brt = brights[i];
            if (brt < BRIGHT_MIN || brt > BRIGHT_MAX) {
                return null;
            }
        }
        return new KeyValue(KEY_CUSTOM1_BRIGHTS, brights);
    }

    public KeyValue setCustom2Brights(int[] brights) {
        if (brights == null || brights.length != getChannelCount()) {
            return null;
        }
        for (int i = 0; i < brights.length; i++) {
            int brt = brights[i];
            if (brt < BRIGHT_MIN || brt > BRIGHT_MAX) {
                return null;
            }
        }
        return new KeyValue(KEY_CUSTOM2_BRIGHTS, brights);
    }

    public KeyValue setCustom3Brights(int[] brights) {
        if (brights == null || brights.length != getChannelCount()) {
            return null;
        }
        for (int i = 0; i < brights.length; i++) {
            int brt = brights[i];
            if (brt < BRIGHT_MIN || brt > BRIGHT_MAX) {
                return null;
            }
        }
        return new KeyValue(KEY_CUSTOM3_BRIGHTS, brights);
    }

    public KeyValue setCustom4Brights(int[] brights) {
        if (brights == null || brights.length != getChannelCount()) {
            return null;
        }
        for (int i = 0; i < brights.length; i++) {
            int brt = brights[i];
            if (brt < BRIGHT_MIN || brt > BRIGHT_MAX) {
                return null;
            }
        }
        return new KeyValue(KEY_CUSTOM4_BRIGHTS, brights);
    }

    public KeyValue setCustomBrights(int idx, int[] brights) {
        if (idx < 0 || idx >= KEY_CUSTOM_BRIGHTS.length || brights == null || brights.length != getChannelCount()) {
            return null;
        }
        for (int i = 0; i < brights.length; i++) {
            int brt = brights[i];
            if (brt < BRIGHT_MIN || brt > BRIGHT_MAX) {
                return null;
            }
        }
        return new KeyValue(KEY_CUSTOM_BRIGHTS[idx], brights);
    }

    public KeyValue setSunriseRamp(int ramp) {
        if (ramp < RAMP_MIN || ramp > RAMP_MAX) {
            return null;
        }
        return new KeyValue(KEY_SUNRISE_RAMP, ramp);
    }

    public KeyValue setSunsetRamp(int ramp) {
        if (ramp < RAMP_MIN || ramp > RAMP_MAX) {
            return null;
        }
        return new KeyValue(KEY_SUNSET_RAMP, ramp);
    }

    public KeyValue setDayBrights(int[] brights) {
        if (brights == null || brights.length != getChannelCount()) {
            return null;
        }
        for (int i = 0; i < brights.length; i++) {
            int brt = brights[i];
            if (brt < BRIGHT_MIN || brt > BRIGHT_MAX) {
                return null;
            }
        }
        return new KeyValue(KEY_DAY_BRIGHTS, brights);
    }

    public KeyValue setNightBrights(int[] brights) {
        if (brights == null || brights.length != getChannelCount()) {
            return null;
        }
        for (int i = 0; i < brights.length; i++) {
            int brt = brights[i];
            if (brt < BRIGHT_MIN || brt > BRIGHT_MAX) {
                return null;
            }
        }
        return new KeyValue(KEY_NIGHT_BRIGHTS, brights);
    }

    public KeyValue setTurnoffEnable(boolean enable) {
        return new KeyValue(KEY_TURNOFF_ENABLE, enable ? 1 : 0);
    }

    public KeyValue setTurnoffTime(int time) {
        if (time < 0 || time > 1439) {
            return null;
        }
        return new KeyValue(KEY_TURNOFF_TIME, time);
    }

    @Override
    protected String getProductName() {
        return "exoled";
    }
}
