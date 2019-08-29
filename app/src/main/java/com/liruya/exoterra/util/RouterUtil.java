package com.liruya.exoterra.util;

import android.content.Context;
import android.support.annotation.NonNull;

public class RouterUtil {
    private static final String KEY_ROUTER_PREFIX = "router_ssid_";

    public static void putRouterPassword(@NonNull Context context, @NonNull final String ssid, final String password) {
        PrefUtil.put(context, KEY_ROUTER_PREFIX + ssid, password);
    }

    public static String getRouterPassword(@NonNull Context context, @NonNull final String ssid) {
        return PrefUtil.getString(context, KEY_ROUTER_PREFIX + ssid, "");
    }
}
