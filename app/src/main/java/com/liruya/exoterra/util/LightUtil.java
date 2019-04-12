package com.liruya.exoterra.util;

import android.text.TextUtils;

import com.liruya.exoterra.R;

import java.util.HashMap;
import java.util.Map;

public class LightUtil {
    private static final String ICON_PRE = "icon_";
    private static final String PROGRESS_PRE = "progress_";
    private static final String THUMB_PRE = "thumb_";
    private static final String UP_PRE = "up_";
    private static final String DOWN_PRE = "down_";
    private static final String VALUE_PRE = "value_";
    private static Map<String, Integer> resMap;

    static {
        resMap = new HashMap<>();
        resMap.put(ICON_PRE + "red", R.drawable.ic_square_red);
        resMap.put(ICON_PRE + "green", R.drawable.ic_square_green);
        resMap.put(ICON_PRE + "blue", R.drawable.ic_square_blue);
        resMap.put(ICON_PRE + "pink", R.drawable.ic_square_pink);
        resMap.put(ICON_PRE + "purple", R.drawable.ic_square_purple);
        resMap.put(ICON_PRE + "cyan", R.drawable.ic_square_cyan);
        resMap.put(ICON_PRE + "teal", R.drawable.ic_square_teal);
        resMap.put(ICON_PRE + "white", R.drawable.ic_square_white);
        resMap.put(ICON_PRE + "warmwhite", R.drawable.ic_square_warmwhite);
        resMap.put(ICON_PRE + "coldwhite", R.drawable.ic_square_coldwhite);
        resMap.put(ICON_PRE + "purewhite", R.drawable.ic_square_purewhite);

        resMap.put(PROGRESS_PRE + "red", R.drawable.custom_seekbar_red);
        resMap.put(PROGRESS_PRE + "green", R.drawable.custom_seekbar_green);
        resMap.put(PROGRESS_PRE + "blue", R.drawable.custom_seekbar_blue);
        resMap.put(PROGRESS_PRE + "pink", R.drawable.custom_seekbar_pink);
        resMap.put(PROGRESS_PRE + "purple", R.drawable.custom_seekbar_purple);
        resMap.put(PROGRESS_PRE + "cyan", R.drawable.custom_seekbar_cyan);
        resMap.put(PROGRESS_PRE + "teal", R.drawable.custom_seekbar_teal);
        resMap.put(PROGRESS_PRE + "white", R.drawable.custom_seekbar_white);
        resMap.put(PROGRESS_PRE + "warmwhite", R.drawable.custom_seekbar_warmwhite);
        resMap.put(PROGRESS_PRE + "coldwhite", R.drawable.custom_seekbar_coldwhite);
        resMap.put(PROGRESS_PRE + "purewhite", R.drawable.custom_seekbar_purewhite);

        resMap.put(THUMB_PRE + "red", R.drawable.custom_thumb_red);
        resMap.put(THUMB_PRE + "green", R.drawable.custom_thumb_green);
        resMap.put(THUMB_PRE + "blue", R.drawable.custom_thumb_blue);
        resMap.put(THUMB_PRE + "pink", R.drawable.custom_thumb_pink);
        resMap.put(THUMB_PRE + "purple", R.drawable.custom_thumb_purple);
        resMap.put(THUMB_PRE + "cyan", R.drawable.custom_thumb_cyan);
        resMap.put(THUMB_PRE + "teal", R.drawable.custom_thumb_teal);
        resMap.put(THUMB_PRE + "white", R.drawable.custom_thumb_white);
        resMap.put(THUMB_PRE + "warmwhite", R.drawable.custom_thumb_warmwhite);
        resMap.put(THUMB_PRE + "coldwhite", R.drawable.custom_thumb_coldwhite);
        resMap.put(THUMB_PRE + "purewhite", R.drawable.custom_thumb_purewhite);

        resMap.put(VALUE_PRE + "red", 0xFFD50000);
        resMap.put(VALUE_PRE + "green", 0xFF00C853);
        resMap.put(VALUE_PRE + "blue", 0xFF2962FF);
        resMap.put(VALUE_PRE + "pink", 0xFFC51162);
        resMap.put(VALUE_PRE + "purple", 0xFFAA00FF);
        resMap.put(VALUE_PRE + "cyan", 0xFF00B8D4);
        resMap.put(VALUE_PRE + "teal", 0xFF00BFA5);
        resMap.put(VALUE_PRE + "white", 0xFFFFFFFF);
        resMap.put(VALUE_PRE + "warmwhite", 0xFFFFB969);
        resMap.put(VALUE_PRE + "coldwhite", 0xFFC3C9FF);
        resMap.put(VALUE_PRE + "purewhite", 0xFFFFFFFF);
    }

    public static int getIconRes(String color) {
        if (!TextUtils.isEmpty(color)) {
            if (color.endsWith("\0")) {
                color = color.substring(0, color.length() - 1);
            }
            color = color.toLowerCase();
            String key = ICON_PRE + color;
            if (resMap.containsKey(key)) {
                return resMap.get(key);
            }
        }
        return R.drawable.ic_square_white;
    }

    public static int getProgressRes(String color) {
        if (!TextUtils.isEmpty(color)) {
            if (color.endsWith("\0")) {
                color = color.substring(0, color.length() - 1);
            }
            color = color.toLowerCase();
            String key = PROGRESS_PRE + color;
            if (resMap.containsKey(key)) {
                return resMap.get(key);
            }
        }
        return R.drawable.custom_seekbar_white;
    }

    public static int getThumbRes(String color) {
        if (!TextUtils.isEmpty(color)) {
            if (color.endsWith("\0")) {
                color = color.substring(0, color.length() - 1);
            }
            color = color.toLowerCase();
            String key = THUMB_PRE + color;
            if (resMap.containsKey(key)) {
                return resMap.get(key);
            }
        }
        return R.drawable.custom_thumb_white;
    }

    public static int getColorValue(String color) {
        if (!TextUtils.isEmpty(color)) {
            if (color.endsWith("\0")) {
                color = color.substring(0, color.length() - 1);
            }
            color = color.toLowerCase();
            String key = VALUE_PRE + color;
            if (resMap.containsKey(key)) {
                return resMap.get(key);
            }
        }
        return 0xFFFFFFFF;
    }
}
