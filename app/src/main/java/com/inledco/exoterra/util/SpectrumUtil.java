package com.inledco.exoterra.util;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.annotation.NonNull;

import com.inledco.exoterra.bean.LightSpectrum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SpectrumUtil {
    private static final String TAG = "SpectrumUtil";

    static private double Gamma = 0.80;
    static private double IntensityMax = 255;

    /** Taken from Earl F. Glynn's web page:
     * <a href="http://www.efg2.com/Lab/ScienceAndEngineering/Spectra.htm">Spectra Lab Report</a>
     * */
    public static int waveLengthToRGB(double wavelength){
        double factor;
        double red, green, blue;

        if((wavelength >= 380) && (wavelength<440)){
            red = -(wavelength - 440) / (440 - 380);
            green = 0.0;
            blue = 1.0;
        }else if((wavelength >= 440) && (wavelength<490)){
            red = 0.0;
            green = (wavelength - 440) / (490 - 440);
            blue = 1.0;
        }else if((wavelength >= 490) && (wavelength<510)){
            red = 0.0;
            green = 1.0;
            blue = -(wavelength - 510) / (510 - 490);
        }else if((wavelength >= 510) && (wavelength<580)){
            red = (wavelength - 510) / (580 - 510);
            green = 1.0;
            blue = 0.0;
        }else if((wavelength >= 580) && (wavelength<645)){
            red = 1.0;
            green = -(wavelength - 645) / (645 - 580);
            blue = 0.0;
        }else if((wavelength >= 645) && (wavelength<781)){
            red = 1.0;
            green = 0.0;
            blue = 0.0;
        }else{
            red = 0.0;
            green = 0.0;
            blue = 0.0;
        }

        // Let the intensity fall off near the vision limits

        if((wavelength >= 380) && (wavelength<420)){
            factor = 0.3 + 0.7*(wavelength - 380) / (420 - 380);
        }else if((wavelength >= 420) && (wavelength<701)){
            factor = 1.0;
        }else if((wavelength >= 701) && (wavelength<781)){
            factor = 0.3 + 0.7*(780 - wavelength) / (780 - 700);
        }else{
            factor = 0.0;
        }

        // Don't want 0^x = 1 for x <> 0
        int r = red == 0.0 ? 0 : (int) Math.round(IntensityMax * Math.pow(red * factor, Gamma));
        int g = green == 0.0 ? 0 : (int) Math.round(IntensityMax * Math.pow(green * factor, Gamma));
        int b = blue == 0.0 ? 0 : (int) Math.round(IntensityMax * Math.pow(blue * factor, Gamma));

        return Color.rgb(r, g, b);
    }

    public static LightSpectrum loadDataFromAssets(@NonNull final AssetManager manager, @NonNull final String path) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(manager.open(path), "UTF-8"));
            int start = 0;
            int end = 0;
            String[] names = null;
            int idx;
            String line = reader.readLine();
            if (line != null && line.startsWith("start:")) {
                start = Integer.parseInt(line.substring("start:".length()));
            } else {
                return null;
            }
            line = reader.readLine();
            if (line != null && line.startsWith("end:")) {
                end = Integer.parseInt(line.substring("end:".length()));
            } else {
                return null;
            }
            line = reader.readLine();
            if (line != null && line.startsWith("wavelength#")) {
                names = line.substring("wavelength#".length()).split(",");
                if (names == null && names.length <= 0) {
                    return null;
                }
            } else {
                return null;
            }

            LightSpectrum lightSpectrum = new LightSpectrum(start, end, names);
            if (!lightSpectrum.isValid()) {
                return null;
            }
            int linenum = 0;
            while ((line = reader.readLine()) != null) {
                idx = line.indexOf("#");
                if (idx >= 0) {
                    idx++;
                }
                String[] split = line.substring(idx).split(",");
                if (split == null || split.length != names.length) {
                    return null;
                }
                float[] values = new float[split.length];
                for (int i = 0; i < values.length; i++) {
                    values[i] = Float.parseFloat(split[i]);
                }
                lightSpectrum.put(linenum, values);
                linenum++;
            }
            return lightSpectrum;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
