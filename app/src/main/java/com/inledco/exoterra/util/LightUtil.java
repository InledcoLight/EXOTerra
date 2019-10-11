package com.inledco.exoterra.util;

import android.content.Context;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;

import com.inledco.exoterra.R;

import java.util.HashMap;
import java.util.Map;

public class LightUtil {
    private static final String ICON_PREFIX = "icon_";
    private static final String VALUE_PREFIX = "value_";
    private static final Map<String, Integer> resMap;

    static {
        resMap = new HashMap<>();

        resMap.put(ICON_PREFIX + "red", R.drawable.ic_square_red);
        resMap.put(ICON_PREFIX + "green", R.drawable.ic_square_green);
        resMap.put(ICON_PREFIX + "blue", R.drawable.ic_square_blue);
        resMap.put(ICON_PREFIX + "pink", R.drawable.ic_square_pink);
        resMap.put(ICON_PREFIX + "purple", R.drawable.ic_square_purple);
        resMap.put(ICON_PREFIX + "cyan", R.drawable.ic_square_cyan);
        resMap.put(ICON_PREFIX + "teal", R.drawable.ic_square_teal);
        resMap.put(ICON_PREFIX + "white", R.drawable.ic_square_white);
        resMap.put(ICON_PREFIX + "warmwhite", R.drawable.ic_square_warmwhite);
        resMap.put(ICON_PREFIX + "coldwhite", R.drawable.ic_square_coldwhite);
        resMap.put(ICON_PREFIX + "purewhite", R.drawable.ic_square_purewhite);

        resMap.put(VALUE_PREFIX + "red", 0xFFD50000);
        resMap.put(VALUE_PREFIX + "green", 0xFF00C853);
        resMap.put(VALUE_PREFIX + "blue", 0xFF2962FF);
        resMap.put(VALUE_PREFIX + "pink", 0xFFC51162);
        resMap.put(VALUE_PREFIX + "purple", 0xFFAA00FF);
        resMap.put(VALUE_PREFIX + "cyan", 0xFF00B8D4);
        resMap.put(VALUE_PREFIX + "teal", 0xFF00BFA5);
        resMap.put(VALUE_PREFIX + "white", 0xFFFFFFFF);
        resMap.put(VALUE_PREFIX + "warmwhite", 0xFFFFB969);
        resMap.put(VALUE_PREFIX + "coldwhite", 0xFFC3C9FF);
        resMap.put(VALUE_PREFIX + "purewhite", 0xFFFFFFFF);
    }

    public static @DrawableRes int getIconRes(String color) {
        if (!TextUtils.isEmpty(color)) {
            if (color.endsWith("\0")) {
                color = color.substring(0, color.length() - 1);
            }
            color = color.toLowerCase();
            String key = ICON_PREFIX + color;
            if (resMap.containsKey(key)) {
                return resMap.get(key);
            }
        }
        return R.drawable.ic_square_white;
    }

    public static int getColorValue(String color) {
        if (!TextUtils.isEmpty(color)) {
            if (color.endsWith("\0")) {
                color = color.substring(0, color.length() - 1);
            }
            color = color.toLowerCase();
            String key = VALUE_PREFIX + color;
            if (resMap.containsKey(key)) {
                return resMap.get(key);
            }
        }
        return 0xFFFFFFFF;
    }

    public static Drawable getProgressDrawable(@NonNull Context context, String color) {
        int value = getColorValue(color);
        Drawable backgroud = context.getResources().getDrawable(R.drawable.custom_seekbar_background);
        GradientDrawable progressDraw = (GradientDrawable) context.getResources().getDrawable(R.drawable.custom_seekbar_progress);
        progressDraw.setColor(value);
        ClipDrawable clip = new ClipDrawable(progressDraw, Gravity.LEFT, ClipDrawable.HORIZONTAL);
        Drawable[] layers = new Drawable[] {backgroud, clip};
        LayerDrawable layer = new LayerDrawable(layers);
        layer.setId(0, android.R.id.background);
        layer.setId(1, android.R.id.progress);
        return layer;
    }

//    public static Drawable getIcon(@NonNull Context context, String color) {
//        GradientDrawable icon = (GradientDrawable) context.getResources().getDrawable(R.drawable.ic_square);
//        icon.setColor(getColorValue(color));
//        return icon;
//    }
}
