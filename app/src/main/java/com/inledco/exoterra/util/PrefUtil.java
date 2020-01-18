package com.inledco.exoterra.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

public class PrefUtil {
    public synchronized static void put(@NonNull Context context, @NonNull String key, boolean value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit().putBoolean(key, value);
        editor.apply();
    }

    public synchronized static void put(@NonNull Context context, @NonNull String key, float value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit().putFloat(key, value);
        editor.apply();
    }

    public synchronized static void put(@NonNull Context context, @NonNull String key, int value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit().putInt(key, value);
        editor.apply();
    }

    public synchronized static void put(@NonNull Context context, @NonNull String key, long value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit().putLong(key, value);
        editor.apply();
    }

    public synchronized static void put(@NonNull Context context, @NonNull String key, String value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit().putString(key, value);
        editor.apply();
    }

    public synchronized static void put(@NonNull Context context, @NonNull String key, Set<String> value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit().putStringSet(key, value);
        editor.apply();
    }

    public synchronized static void put(@NonNull Context context, @NonNull String key, @NonNull Object object) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            String objectStr = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
            baos.close();
            oos.close();
            SharedPreferences.Editor editor = sp.edit().putString(key, objectStr);
            editor.apply();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void put(@NonNull Context context, @NonNull String file, @NonNull String key, boolean value) {
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit().putBoolean(key, value);
        editor.apply();
    }

    public synchronized static void put(@NonNull Context context, @NonNull String file, @NonNull String key, float value) {
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit().putFloat(key, value);
        editor.apply();
    }

    public synchronized static void put(@NonNull Context context, @NonNull String file, @NonNull String key, int value) {
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit().putInt(key, value);
        editor.apply();
    }

    public synchronized static void put(@NonNull Context context, @NonNull String file, @NonNull String key, long value) {
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit().putLong(key, value);
        editor.apply();
    }

    public synchronized static void put(@NonNull Context context, @NonNull String file, @NonNull String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit().putString(key, value);
        editor.apply();
    }

    public synchronized static void put(@NonNull Context context, @NonNull String file, @NonNull String key, Set<String> value) {
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit().putStringSet(key, value);
        editor.apply();
    }

    public synchronized static void put(@NonNull Context context, @NonNull String file, @NonNull String key, @NonNull Object object) {
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            String objectStr = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
            baos.close();
            oos.close();
            SharedPreferences.Editor editor = sp.edit().putString(key, objectStr);
            editor.apply();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public synchronized static void putBoolean(@NonNull Context context, @NonNull String file, @NonNull String key, boolean value) {
//        if (TextUtils.isEmpty(file) || TextUtils.isEmpty(key)) {
//            return;
//        }
//        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sp.edit()
//                                            .putBoolean(key, value);
//        editor.apply();
//    }
//
//    public synchronized static void putInt(@NonNull Context context, @NonNull String file, @NonNull String key, int value) {
//        if (TextUtils.isEmpty(file) || TextUtils.isEmpty(key)) {
//            return;
//        }
//        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sp.edit()
//                                            .putInt(key, value);
//        editor.apply();
//    }
//
//    public synchronized static void putFloat(@NonNull Context context, @NonNull String file, @NonNull String key, float value) {
//        if (TextUtils.isEmpty(file) || TextUtils.isEmpty(key)) {
//            return;
//        }
//        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sp.edit()
//                                            .putFloat(key, value);
//        editor.apply();
//    }
//
//    public synchronized static void putLong(@NonNull Context context, @NonNull String file, @NonNull String key, long value) {
//        if (TextUtils.isEmpty(file) || TextUtils.isEmpty(key)) {
//            return;
//        }
//        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sp.edit()
//                                            .putLong(key, value);
//        editor.apply();
//    }
//
//    public synchronized static void putString(@NonNull Context context, @NonNull String file, @NonNull String key, String value) {
//        if (TextUtils.isEmpty(file) || TextUtils.isEmpty(key)) {
//            return;
//        }
//        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sp.edit()
//                                            .putString(key, value);
//        editor.apply();
//    }

    public synchronized static boolean getBoolean(@NonNull Context context, @NonNull String key, boolean def) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(key, def);
    }

    public synchronized static int getInt(@NonNull Context context,  @NonNull String key, int def) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(key, def);
    }

    public synchronized static float getFloat(@NonNull Context context,  @NonNull String key, float def) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getFloat(key, def);
    }

    public synchronized static long getLong(@NonNull Context context, @NonNull String key, long def) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(key, def);
    }

    public synchronized static String getString(@NonNull Context context, @NonNull String key, String def) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(key, def);
    }

    public synchronized static Set<String> getStringSet(@NonNull Context context, @NonNull String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getStringSet(key, null);
    }

    public synchronized static Object getObject(@NonNull Context context, @NonNull String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String objectStr = sp.getString(key, "");
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

    public synchronized static boolean getBoolean(@NonNull Context context, @NonNull String file, @NonNull String key, boolean def) {
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        return sp.getBoolean(key, def);
    }

    public synchronized static int getInt(@NonNull Context context, @NonNull String file, @NonNull String key, int def) {
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        return sp.getInt(key, def);
    }

    public synchronized static float getFloat(@NonNull Context context, @NonNull String file, @NonNull String key, float def) {
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        return sp.getFloat(key, def);
    }

    public synchronized static long getLong(@NonNull Context context, @NonNull String file, @NonNull String key, long def) {
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        return sp.getLong(key, def);
    }

    public synchronized static String getString(@NonNull Context context, @NonNull String file, @NonNull String key, String def) {
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        return sp.getString(key, def);
    }

    public synchronized static Set<String> getStringSet(@NonNull Context context, @NonNull String file, @NonNull String key) {
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        return sp.getStringSet(key, null);
    }

    public synchronized static Object getObject(@NonNull Context context, @NonNull String file, @NonNull String key) {
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        String objectStr = sp.getString(key, "");
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

    public synchronized static void remove(@NonNull Context context, @NonNull String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (sp.contains(key)) {
            SharedPreferences.Editor editor = sp.edit().remove(key);
            editor.apply();
        }
    }

    public synchronized static void remove(@NonNull Context context, @NonNull String file, @NonNull String key) {
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        if (sp.contains(key)) {
            SharedPreferences.Editor editor = sp.edit().remove(key);
            editor.apply();
        }
    }

    public synchronized static void clear(@NonNull Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit().clear();
        editor.apply();
    }

    public synchronized static void clear(@NonNull Context context, @NonNull String file) {
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit().clear();
        editor.apply();
    }

    public synchronized static Set<String> getAllKeys(@NonNull Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getAll().keySet();
    }

    public synchronized static Set<String> getAllKeys(@NonNull Context context, @NonNull String file) {
        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        return sp.getAll().keySet();
    }
}
