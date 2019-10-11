package com.inledco.exoterra.util;

import android.graphics.Color;
import android.util.Log;

import org.junit.Test;

import java.text.DecimalFormat;

public class SpectrumUtilTest {
    private final String TAG = "SpectrumUtilTest";

    @Test
    public void waveLengthToRGB() {
        DecimalFormat df = new DecimalFormat("##0");
        for (int i = 320; i <= 800; i++) {
            int rgb = SpectrumUtil.waveLengthToRGB(i);
            Log.e(TAG,
                  "" + i + " nm,"+ df.format(Color.red(rgb)) + "," + df.format(Color.green(rgb)) + "," + df.format(Color.blue(rgb)) + "," + rgb + ",0x" + Integer.toHexString(rgb));
        }
    }
}