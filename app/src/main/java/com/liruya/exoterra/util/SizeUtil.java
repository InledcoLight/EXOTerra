package com.liruya.exoterra.util;

import android.content.res.Resources;

public class SizeUtil {
    public static int dp2px(float dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density + 0.5f);
    }

    public static int px2dp(float px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density + 0.5f);
    }

    public static int sp2px(float sp) {
        return (int) (sp * Resources.getSystem().getDisplayMetrics().scaledDensity + 0.5f);
    }

    public static int px2sp(float px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().scaledDensity + 0.5f);
    }
}
