package com.inledco.exoterra.util;

import android.support.annotation.IntDef;
import android.util.Log;

public class LogUtil {
    public static final int LOG_DISABLED = 0;
    public static final int LOG_LEVEL_V = 1;
    public static final int LOG_LEVEL_D = 2;
    public static final int LOG_LEVEL_I = 3;
    public static final int LOG_LEVEL_W = 4;
    public static final int LOG_LEVEL_E = 5;

    @IntDef ({LOG_DISABLED, LOG_LEVEL_V, LOG_LEVEL_D, LOG_LEVEL_I, LOG_LEVEL_W, LOG_LEVEL_E})
    public @interface LOG_LEVEL {}

    private static @LOG_LEVEL int mLogLevel = LOG_LEVEL_V;

    public static void setLogLevel(@LOG_LEVEL int level) {
        mLogLevel = level;
    }

    public static @LOG_LEVEL int getLogLevel() {
        return mLogLevel;
    }

    public static void v(String tag, String msg) {
        if (mLogLevel > LOG_DISABLED && mLogLevel <= LOG_LEVEL_V) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (mLogLevel > LOG_DISABLED && mLogLevel <= LOG_LEVEL_D) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (mLogLevel > LOG_DISABLED && mLogLevel <= LOG_LEVEL_I) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (mLogLevel > LOG_DISABLED && mLogLevel <= LOG_LEVEL_W) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (mLogLevel > LOG_DISABLED && mLogLevel <= LOG_LEVEL_E) {
            Log.e(tag, msg);
        }
    }
}
