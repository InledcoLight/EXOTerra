package com.inledco.exoterra.util;

import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.util.TimeZone;

public class TimeFormatUtil {

    public static String formatMinutesTime(@NonNull final DateFormat df, final int minutes) {
        int rawZone = TimeZone.getDefault().getRawOffset()/60000;
        long time = ((1440+minutes-rawZone)%1440)*60000;
        return df.format(time);
    }
}
