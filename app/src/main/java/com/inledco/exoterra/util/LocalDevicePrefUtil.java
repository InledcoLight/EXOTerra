package com.inledco.exoterra.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.inledco.exoterra.bean.LocalDevice;
import com.inledco.exoterra.bean.LocalDevicePref;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LocalDevicePrefUtil {
    private static final String filename = "local_devices";

    public static void putLocalDevice(@NonNull Context context, @NonNull String pid, @NonNull String mac, String name) {
        LocalDevicePref pref = new LocalDevicePref(pid, mac, name, System.currentTimeMillis());
        String json = new Gson().toJson(pref);
        PrefUtil.put(context, filename, pref.getTag(), json);
    }

    public static void deleteLocalDevice(@NonNull Context context, @NonNull String pid, @NonNull String mac) {
        PrefUtil.remove(context, filename, pid + "_" + mac);
    }

    public static List<LocalDevicePref> getLocalDevicePrefs(@NonNull Context context) {
        List<LocalDevicePref> prefs = new ArrayList<>();
        SharedPreferences sp = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        Map<String, ?> map = sp.getAll();
        Gson gson = new Gson();
        for (Object obj : map.values()) {
            if (obj instanceof String) {
                LocalDevicePref pref = gson.fromJson((String) obj, LocalDevicePref.class);
                if (pref != null) {
                    prefs.add(pref);
                }
            }
        }
        return prefs;
    }

    public static List<LocalDevice> getLocalDevices(@NonNull Context context) {
        List<LocalDevice> devices = new ArrayList<>();
        SharedPreferences sp = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        Map<String, ?> map = sp.getAll();
        Gson gson = new Gson();
        for (Object obj : map.values()) {
            if (obj instanceof String) {
                LocalDevicePref pref = gson.fromJson((String) obj, LocalDevicePref.class);
                if (pref != null) {
                    devices.add(new LocalDevice(pref));
                }
            }
        }
        return devices;
    }
}
