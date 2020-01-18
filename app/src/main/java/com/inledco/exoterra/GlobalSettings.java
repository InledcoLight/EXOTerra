package com.inledco.exoterra;

import android.content.Context;
import android.support.annotation.NonNull;

import com.inledco.exoterra.util.PrefUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class GlobalSettings {

    private static boolean mIs24HourFormat;
    private static boolean mIsCelsius;

    public static void init(@NonNull final Context context) {
        mIs24HourFormat = PrefUtil.getBoolean(context, AppConstants.KEY_TIMEFORMAT, true);
        mIsCelsius = PrefUtil.getBoolean(context, AppConstants.KEY_TEMPUNIT, true);
    }

    public static boolean is24HourFormat() {
        return mIs24HourFormat;
    }

    public static boolean isCelsius() {
        return mIsCelsius;
    }

    public static void setIs24HourFormat(@NonNull final Context context, boolean is24Hour) {
        PrefUtil.put(context, AppConstants.KEY_TIMEFORMAT, is24Hour);
        mIs24HourFormat = is24Hour;
    }

    public static void setIsCelsius(@NonNull final Context context, boolean isCelsius) {
        PrefUtil.put(context, AppConstants.KEY_TEMPUNIT, isCelsius);
        mIsCelsius = isCelsius;
    }

    public static DateFormat getDateTimeFormat() {
        String format = is24HourFormat() ? AppConstants.DATETIME_FORMAT_24HOUR : AppConstants.DATETIME_FORMAT_12HOUR;
        return new SimpleDateFormat(format);
    }

    public static DateFormat getTimeFormat() {
        String format = is24HourFormat() ? AppConstants.TIME_FORMAT_24HOUR : AppConstants.TIME_FORMAT_12HOUR;
        return new SimpleDateFormat(format);
    }

    public static String getTemperatureText(int value) {
        if (isCelsius()) {
            return (value/10) + "." + (value%10) + "\n℃";
        } else {
            value = 9 * value / 5 + 320;
            return (value/10) + "." + (value%10) + "\n℉";
        }
    }

    public static String getHumidityText(int value) {
        return (value/10) + "." + (value%10) + "\n%";
    }

    public static String getTemperatureUnit() {
//        return "℉";
        return "℃";
    }
}
