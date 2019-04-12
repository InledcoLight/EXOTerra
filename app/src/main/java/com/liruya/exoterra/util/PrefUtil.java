package com.liruya.exoterra.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

public class PrefUtil {
    public synchronized static void putBoolean(Context context, String file, String key, boolean value) {
        if (context == null || TextUtils.isEmpty(file) || TextUtils.isEmpty(key)) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit()
                                            .putBoolean(key, value);
        editor.apply();
    }

    public synchronized static void putInt(Context context, String file, String key, int value) {
        if (context == null || TextUtils.isEmpty(file) || TextUtils.isEmpty(key)) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit()
                                            .putInt(key, value);
        editor.apply();
    }

    public synchronized static void putFloat(Context context, String file, String key, float value) {
        if (context == null || TextUtils.isEmpty(file) || TextUtils.isEmpty(key)) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit()
                                            .putFloat(key, value);
        editor.apply();
    }

    public synchronized static void putLong(Context context, String file, String key, long value) {
        if (context == null || TextUtils.isEmpty(file) || TextUtils.isEmpty(key)) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit()
                                            .putLong(key, value);
        editor.apply();
    }

    public synchronized static void putString(Context context, String file, String key, String value) {
        if (context == null || TextUtils.isEmpty(file) || TextUtils.isEmpty(key)) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit()
                                            .putString(key, value);
        editor.apply();
    }

    public synchronized static void putObject(Context context, String file, String key, Object object) {
        if (context == null || TextUtils.isEmpty(file) || TextUtils.isEmpty(key)) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        if (object == null) {
            SharedPreferences.Editor editor = sp.edit()
                                                .remove(key);
            editor.apply();
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        String objectStr = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
        try {
            baos.close();
            oos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        SharedPreferences.Editor editor = sp.edit()
                                            .putString(key, objectStr);
        editor.apply();
    }

    public synchronized static boolean getBoolean(Context context, String file, String key, boolean def) {
        if (context == null || TextUtils.isEmpty(file) || TextUtils.isEmpty(key)) {
            return def;
        }
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        return sp.getBoolean(key, def);
    }

    public synchronized static int getInt(Context context, String file, String key, int def) {
        if (context == null || TextUtils.isEmpty(file) || TextUtils.isEmpty(key)) {
            return def;
        }
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        return sp.getInt(key, def);
    }

    public synchronized static float getFloat(Context context, String file, String key, float def) {
        if (context == null || TextUtils.isEmpty(file) || TextUtils.isEmpty(key)) {
            return def;
        }
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        return sp.getFloat(key, def);
    }

    public synchronized static long getLong(Context context, String file, String key, long def) {
        if (context == null || TextUtils.isEmpty(file) || TextUtils.isEmpty(key)) {
            return def;
        }
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        return sp.getLong(key, def);
    }

    public synchronized static String getString(Context context, String file, String key, String def) {
        if (context == null || TextUtils.isEmpty(file) || TextUtils.isEmpty(key)) {
            return null;
        }
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        return sp.getString(key, def);
    }

    public synchronized static Object getObject(Context context, String file, String key) {
        if (context == null || TextUtils.isEmpty(file) || TextUtils.isEmpty(key)) {
            return null;
        }
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        String objectStr = sp.getString(key, "");
        if (objectStr == null || objectStr.equals("")) {
            return null;
        }
        byte[] objBytes = Base64.decode(objectStr.getBytes(), Base64.DEFAULT);
        ByteArrayInputStream bais = new ByteArrayInputStream(objBytes);
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object object = ois.readObject();
            bais.close();
            ois.close();
            return object;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized static void remove(Context context, String file, String key) {
        if (context == null || TextUtils.isEmpty(file) || TextUtils.isEmpty(key)) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        if (sp.contains(key)) {
            SharedPreferences.Editor editor = sp.edit()
                                                .remove(key);
            editor.apply();
        }
    }

    public synchronized static void clear(Context context, String file) {
        if (context == null || TextUtils.isEmpty(file)) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit()
                                            .clear();
        editor.apply();
    }

    public synchronized static Set<String> getAllKeys(Context context, String file) {
        if (context == null || TextUtils.isEmpty(file)) {
            return null;
        }
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        return sp.getAll()
                 .keySet();
    }
}
