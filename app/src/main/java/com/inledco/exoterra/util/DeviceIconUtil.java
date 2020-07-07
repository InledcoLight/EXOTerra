package com.inledco.exoterra.util;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import com.inledco.exoterra.R;

import java.util.ArrayList;
import java.util.List;

public class DeviceIconUtil {
    private final static List<Integer> mDeviceIcons;

    static {
        mDeviceIcons = new ArrayList<>();
        mDeviceIcons.add(R.drawable.devicon_socket);
        mDeviceIcons.add(R.drawable.devicon_socket_eu);
        mDeviceIcons.add(R.drawable.devicon_socket_sensor);
        mDeviceIcons.add(R.drawable.devicon_socket_double);
        mDeviceIcons.add(R.drawable.devicon_socket_double_sensor);
        mDeviceIcons.add(R.drawable.devicon_socket_double_sensor_double);
        mDeviceIcons.add(R.drawable.devicon_strip);
        mDeviceIcons.add(R.drawable.devicon_strip_45cm);
        mDeviceIcons.add(R.drawable.devicon_strip_60cm);
        mDeviceIcons.add(R.drawable.devicon_strip_90cm);
        mDeviceIcons.add(R.drawable.devicon_linear);
        mDeviceIcons.add(R.drawable.devicon_linear_uvb);
        mDeviceIcons.add(R.drawable.devicon_halogen_spot);
        mDeviceIcons.add(R.drawable.devicon_cfl_bulb);
        mDeviceIcons.add(R.drawable.devicon_reptile_uvb);
        mDeviceIcons.add(R.drawable.devicon_nano_basking);
        mDeviceIcons.add(R.drawable.devicon_turtle_uvb);
        mDeviceIcons.add(R.drawable.devicon_incandecent);
        mDeviceIcons.add(R.drawable.devicon_incandecent_ir);
        mDeviceIcons.add(R.drawable.devicon_heat_lamp_sun);
        mDeviceIcons.add(R.drawable.devicon_heat_lamp_moon);
        mDeviceIcons.add(R.drawable.devicon_basking_spot_sun);
        mDeviceIcons.add(R.drawable.devicon_basking_spot_moon);
        mDeviceIcons.add(R.drawable.devicon_basking_spot_ir);
        mDeviceIcons.add(R.drawable.devicon_basking_spot_sunadd);
        mDeviceIcons.add(R.drawable.devicon_heat_spot_sun);
        mDeviceIcons.add(R.drawable.devicon_heat_spot_moon);
        mDeviceIcons.add(R.drawable.devicon_heat_lamp_sun2);
        mDeviceIcons.add(R.drawable.devicon_heat_lamp_moon2);
        mDeviceIcons.add(R.drawable.devicon_nano_led);
        mDeviceIcons.add(R.drawable.devicon_nano_led_sun);
        mDeviceIcons.add(R.drawable.devicon_ceramic_heater);
        mDeviceIcons.add(R.drawable.devicon_solarglo);
        mDeviceIcons.add(R.drawable.devicon_sunray);
        mDeviceIcons.add(R.drawable.devicon_moonlight);
        mDeviceIcons.add(R.drawable.devicon_reptile_dome);
        mDeviceIcons.add(R.drawable.devicon_light_dome);
        mDeviceIcons.add(R.drawable.devicon_compact_top);
        mDeviceIcons.add(R.drawable.devicon_linear_top);
        mDeviceIcons.add(R.drawable.devicon_solarray);
        mDeviceIcons.add(R.drawable.devicon_turtle_uvb2);
        mDeviceIcons.add(R.drawable.devicon_light_unit);
        mDeviceIcons.add(R.drawable.devicon_heat_mat);
        mDeviceIcons.add(R.drawable.devicon_heat_cable);
        mDeviceIcons.add(R.drawable.devicon_heat_rock);
        mDeviceIcons.add(R.drawable.devicon_monsoon_solo);
        mDeviceIcons.add(R.drawable.devicon_monsoon_multi);
        mDeviceIcons.add(R.drawable.devicon_monsoon);
        mDeviceIcons.add(R.drawable.devicon_humidifier);
        mDeviceIcons.add(R.drawable.devicon_humidifier2);
        mDeviceIcons.add(R.drawable.devicon_dripper_plant);
        mDeviceIcons.add(R.drawable.devicon_water_fall);
        mDeviceIcons.add(R.drawable.devicon_leak_sensor);
        mDeviceIcons.add(R.drawable.devicon_pump);
        mDeviceIcons.add(R.drawable.devicon_fan);
        mDeviceIcons.add(R.drawable.devicon_cooling_fan);
        mDeviceIcons.add(R.drawable.devicon_camera1);
        mDeviceIcons.add(R.drawable.devicon_camera2);
    }

    public static List<Integer> getDeviceIcons() {
        return mDeviceIcons;
    }

    public static int getDeviceIconRes(Context context, String name, int defRes) {
        Resources resources = context.getResources();
        for (Integer res : mDeviceIcons) {
            try {
                String resName = resources.getResourceEntryName(res);
                if (TextUtils.equals(name, resName)) {
                    return res;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return defRes;
    }
}
