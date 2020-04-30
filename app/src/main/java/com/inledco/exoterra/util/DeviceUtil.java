package com.inledco.exoterra.util;

import android.net.wifi.ScanResult;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.AliotConsts;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DeviceUtil {
    public static final String EXO_STRIP_REGEX = "^ExoLed_[0-9A-Fa-f]{6}$";
    public static final String EXO_SOCKET_REGEX = "^ExoSocket_[0-9A-Fa-f]{6}$";
    public static final String EXO_MONSOON_REGEX = "^ExoMonsoon_[0-9A-Fa-f]{6}$";

    public static final String  EXO_STRIP_45CM = "Microtope Strip 45cm";
    public static final String  EXO_STRIP_60CM = "Microtope Strip 60cm";
    public static final String  EXO_STRIP_90CM = "Microtope Strip 90cm";
    public static final String  EXO_SOCKET_NA = "Microtope Socket NA";
    public static final String  EXO_SOCKET_EU = "Microtope Socket EU";
    public static final String  EXO_MONSOON_SOLO = "Microtope Monsoon Solo";
    public static final String  EXO_MONSOON_MULTI = "Microtope Monsoon Multi";

    public static final int ESP8266_GATEWAY = 0x0104A8C0;

    private static final Product mExoLed;
    private static final Product mExoSocket;
    private static final Product mExoMonsoon;

    private static final Map<String, Product> deviceMap;

    static {
        mExoLed = new Product(AliotConsts.PRODUCT_EXOLED, AliotConsts.PRODUCT_EXOLED, R.drawable.ic_strip, R.drawable.ic_strip_48dp);
        mExoSocket = new Product(AliotConsts.PRODUCT_EXOSOCKET, AliotConsts.PRODUCT_EXOSOCKET, R.drawable.ic_socket, R.drawable.ic_socket_48dp);
        mExoMonsoon = new Product(AliotConsts.PRODUCT_EXOMONSOON, AliotConsts.PRODUCT_EXOMONSOON, R.drawable.ic_monsoon, R.drawable.ic_monsoon_48dp);
        deviceMap = new LinkedHashMap<>();
        deviceMap.put(AliotConsts.PRODUCT_KEY_EXOLED, mExoLed);
        deviceMap.put(AliotConsts.PRODUCT_KEY_EXOSOCKET, mExoSocket);
        deviceMap.put(AliotConsts.PRODUCT_KEY_EXOMONSOON, mExoMonsoon);
    }

    public static String getProductName(String pkey) {
        if (deviceMap.containsKey(pkey)) {
            return deviceMap.get(pkey).getProductName();
        }
        return "";
    }

    public static String getDefaultName(String pkey) {
        if (deviceMap.containsKey(pkey)) {
            return deviceMap.get(pkey).getDefaultName();
        }
        return "";
    }

    public static @DrawableRes int getProductIcon(String pkey) {
        if (deviceMap.containsKey(pkey)) {
            return deviceMap.get(pkey).getIcon();
        }
        return R.drawable.ic_device_default_white_64dp;
    }

    public static @DrawableRes int getProductIconSmall(String pkey) {
        if (deviceMap.containsKey(pkey)) {
            return deviceMap.get(pkey).getIconSmall();
        }
        return R.drawable.ic_device_default_white_24dp;
    }

    public static @DrawableRes int getProductLedoffIcon(String pkey) {
//        if (XlinkConstants.PRODUCT_ID_LEDSTRIP.equals(pkey)) {
//            return R.mipmap.ic_strip_off_256;
//        }
//        if (XlinkConstants.PRODUCT_ID_MONSOON.equals(pkey)) {
//            return R.mipmap.ic_monsoon_off_256;
//        }
//        if (XlinkConstants.PRODUCT_ID_SOCKET.equals(pkey)) {
//            return R.mipmap.ic_socket_off_256;
//        }
//        return 0;
        return R.drawable.ic_power_gray;
    }

    public static @DrawableRes int getProductLedonIcon(String pkey) {
//        if (XlinkConstants.PRODUCT_ID_LEDSTRIP.equals(pkey)) {
//            return R.mipmap.ic_strip_on_256;
//        }
//        if (XlinkConstants.PRODUCT_ID_MONSOON.equals(pkey)) {
//            return R.mipmap.ic_monsoon_on_256;
//        }
//        if (XlinkConstants.PRODUCT_ID_SOCKET.equals(pkey)) {
//            return R.mipmap.ic_socket_on_256;
//        }
//        return 0;
        return R.drawable.ic_power_green;
    }

    public static List<String> getAllProducts() {
        return new ArrayList<>(deviceMap.keySet());
    }

    public static boolean containsProduct(String pkey) {
        return deviceMap.containsKey(pkey);
    }

    public static boolean isEXODevice(@NonNull final ScanResult result) {
        if (result.frequency < 2400 || result.frequency > 2500) {
            return false;
        }
        String capabilities = result.capabilities;
        if (capabilities.contains("WEP") || capabilities.contains("wep")
            || capabilities.contains("WPA") || capabilities.contains("wpa")) {
            return false;
        }
        String ssid = result.SSID;
        if (ssid.startsWith("\"") && ssid.startsWith("\"")) {
            ssid = ssid.substring(1, ssid.length()-1);
        }
        if (ssid.matches(EXO_STRIP_REGEX) == false &&
            ssid.matches(EXO_SOCKET_REGEX) == false &&
            ssid.matches(EXO_MONSOON_REGEX) == false) {
            return false;
        }
        return true;
    }

    public static boolean isEXODevice(@NonNull final ScanResult result, @NonNull final String pkey) {
        if (result.frequency < 2400 || result.frequency > 2500) {
            return false;
        }
        String capabilities = result.capabilities;
        if (capabilities.contains("WEP") || capabilities.contains("wep")
            || capabilities.contains("WPA") || capabilities.contains("wpa")) {
            return false;
        }
        String ssid = result.SSID;
        if (ssid.startsWith("\"") && ssid.startsWith("\"")) {
            ssid = ssid.substring(1, ssid.length()-1);
        }
//        if (ssid.matches(EXO_STRIP_REGEX) && XlinkConstants.PRODUCT_ID_LEDSTRIP.equals(pkey)) {
//            return true;
//        }
//        if (ssid.matches(EXO_SOCKET_REGEX) && XlinkConstants.PRODUCT_ID_SOCKET.equals(pkey)) {
//            return true;
//        }
//        if (ssid.matches(EXO_MONSOON_REGEX) && XlinkConstants.PRODUCT_ID_MONSOON.equals(pkey)) {
//            return true;
//        }
        return false;
    }

    public static boolean isEXOStrip(@NonNull final ScanResult result) {
        if (result.frequency < 2400 || result.frequency > 2500) {
            return false;
        }
        String capabilities = result.capabilities;
        if (capabilities.contains("WEP") || capabilities.contains("wep")
            || capabilities.contains("WPA") || capabilities.contains("wpa")) {
            return false;
        }
        String ssid = result.SSID;
        if (ssid.startsWith("\"") && ssid.startsWith("\"")) {
            ssid = ssid.substring(1, ssid.length()-1);
        }
        return ssid.matches(EXO_STRIP_REGEX);
    }

    public static boolean isEXOSocket(@NonNull final ScanResult result) {
        if (result.frequency < 2400 || result.frequency > 2500) {
            return false;
        }
        String capabilities = result.capabilities;
        if (capabilities.contains("WEP") || capabilities.contains("wep")
            || capabilities.contains("WPA") || capabilities.contains("wpa")) {
            return false;
        }
        String ssid = result.SSID;
        if (ssid.startsWith("\"") && ssid.startsWith("\"")) {
            ssid = ssid.substring(1, ssid.length()-1);
        }
        return ssid.matches(EXO_SOCKET_REGEX);
    }

    public static boolean isEXOMonsoon(@NonNull final ScanResult result) {
        if (result.frequency < 2400 || result.frequency > 2500) {
            return false;
        }
        String capabilities = result.capabilities;
        if (capabilities.contains("WEP") || capabilities.contains("wep")
            || capabilities.contains("WPA") || capabilities.contains("wpa")) {
            return false;
        }
        String ssid = result.SSID;
        if (ssid.startsWith("\"") && ssid.startsWith("\"")) {
            ssid = ssid.substring(1, ssid.length()-1);
        }
        return ssid.matches(EXO_MONSOON_REGEX);
    }
    public static class Product {
        private final String mProductName;
        private final String mDefaultName;
        private final @DrawableRes int mIcon;
        private final @DrawableRes int mIconSmall;

        public Product(String productName, String defaultName, int icon, int iconSmall) {
            mProductName = productName;
            mDefaultName = defaultName;
            mIcon = icon;
            mIconSmall = iconSmall;
        }

        public String getProductName() {
            return mProductName;
        }

        public String getDefaultName() {
            return mDefaultName;
        }

        public int getIcon() {
            return mIcon;
        }

        public int getIconSmall() {
            return mIconSmall;
        }
    }
}
