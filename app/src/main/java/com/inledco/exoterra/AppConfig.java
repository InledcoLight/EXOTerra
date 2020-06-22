package com.inledco.exoterra;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    private static boolean loaded;

    private static final Properties properties = new Properties();

    public static void load(@NonNull Context context, @NonNull String path) {
        try {
            InputStream inStream = context.getAssets().open(path);
            properties.load(inStream);
            inStream.close();
            loaded = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean getBoolean(String key) {
        if (!loaded) {
            throw new RuntimeException("Please load AppConfig properties!");
        }
        if (properties.containsKey(key)) {
            String value = properties.getProperty(key);
            try {
                return Boolean.parseBoolean(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Integer getInteger(String key) {
        if (!loaded) {
            throw new RuntimeException("Please load AppConfig properties!");
        }
        if (properties.containsKey(key)) {
            String value = properties.getProperty(key);
            try {
                return Integer.parseInt(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getString(String key) {
        if (!loaded) {
            throw new RuntimeException("Please load AppConfig properties!");
        }
        if (properties.containsKey(key)) {
            String value = properties.getProperty(key);
            return value;
        }
        return null;
    }

    public static Byte getByte(String key) {
        if (!loaded) {
            throw new RuntimeException("Please load AppConfig properties!");
        }
        if (properties.containsKey(key)) {
            String value = properties.getProperty(key);
            try {
                return Byte.parseByte(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Short getShort(String key) {
        if (!loaded) {
            throw new RuntimeException("Please load AppConfig properties!");
        }
        if (properties.containsKey(key)) {
            String value = properties.getProperty(key);
            try {
                return Short.parseShort(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Float getFloat(String key) {
        if (!loaded) {
            throw new RuntimeException("Please load AppConfig properties!");
        }
        if (properties.containsKey(key)) {
            String value = properties.getProperty(key);
            try {
                return Float.parseFloat(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Double getDouble(String key) {
        if (!loaded) {
            throw new RuntimeException("Please load AppConfig properties!");
        }
        if (properties.containsKey(key)) {
            String value = properties.getProperty(key);
            try {
                return Double.parseDouble(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
