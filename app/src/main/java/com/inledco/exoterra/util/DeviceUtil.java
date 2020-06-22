package com.inledco.exoterra.util;

import android.support.annotation.DrawableRes;

import com.inledco.exoterra.R;

public class DeviceUtil {
    public static final int ESP8266_GATEWAY = 0x0104A8C0;       //192.168.4.1

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

//    public static boolean isEXODevice(@NonNull final ScanResult result) {
//        if (result.frequency < 2400 || result.frequency > 2500) {
//            return false;
//        }
//        String capabilities = result.capabilities;
//        if (capabilities.contains("WEP") || capabilities.contains("wep")
//            || capabilities.contains("WPA") || capabilities.contains("wpa")) {
//            return false;
//        }
//        String ssid = result.SSID;
//        if (ssid.startsWith("\"") && ssid.startsWith("\"")) {
//            ssid = ssid.substring(1, ssid.length()-1);
//        }
//        if (ssid.matches(EXO_STRIP_REGEX) == false &&
//            ssid.matches(EXO_SOCKET_REGEX) == false &&
//            ssid.matches(EXO_MONSOON_REGEX) == false) {
//            return false;
//        }
//        return true;
//    }

//    public static boolean isEXODevice(@NonNull final ScanResult result, @NonNull final String pkey) {
//        if (result.frequency < 2400 || result.frequency > 2500) {
//            return false;
//        }
//        String capabilities = result.capabilities;
//        if (capabilities.contains("WEP") || capabilities.contains("wep")
//            || capabilities.contains("WPA") || capabilities.contains("wpa")) {
//            return false;
//        }
//        String ssid = result.SSID;
//        if (ssid.startsWith("\"") && ssid.startsWith("\"")) {
//            ssid = ssid.substring(1, ssid.length()-1);
//        }
//        return false;
//    }

//    public static boolean isEXOStrip(@NonNull final ScanResult result) {
//        if (result.frequency < 2400 || result.frequency > 2500) {
//            return false;
//        }
//        String capabilities = result.capabilities;
//        if (capabilities.contains("WEP") || capabilities.contains("wep")
//            || capabilities.contains("WPA") || capabilities.contains("wpa")) {
//            return false;
//        }
//        String ssid = result.SSID;
//        if (ssid.startsWith("\"") && ssid.startsWith("\"")) {
//            ssid = ssid.substring(1, ssid.length()-1);
//        }
//        return ssid.matches(EXO_STRIP_REGEX);
//    }
//
//    public static boolean isEXOSocket(@NonNull final ScanResult result) {
//        if (result.frequency < 2400 || result.frequency > 2500) {
//            return false;
//        }
//        String capabilities = result.capabilities;
//        if (capabilities.contains("WEP") || capabilities.contains("wep")
//            || capabilities.contains("WPA") || capabilities.contains("wpa")) {
//            return false;
//        }
//        String ssid = result.SSID;
//        if (ssid.startsWith("\"") && ssid.startsWith("\"")) {
//            ssid = ssid.substring(1, ssid.length()-1);
//        }
//        return ssid.matches(EXO_SOCKET_REGEX);
//    }
//
//    public static boolean isEXOMonsoon(@NonNull final ScanResult result) {
//        if (result.frequency < 2400 || result.frequency > 2500) {
//            return false;
//        }
//        String capabilities = result.capabilities;
//        if (capabilities.contains("WEP") || capabilities.contains("wep")
//            || capabilities.contains("WPA") || capabilities.contains("wpa")) {
//            return false;
//        }
//        String ssid = result.SSID;
//        if (ssid.startsWith("\"") && ssid.startsWith("\"")) {
//            ssid = ssid.substring(1, ssid.length()-1);
//        }
//        return ssid.matches(EXO_MONSOON_REGEX);
//    }

//    public static class Product {
//        private final String mProductName;
//        private final String mDefaultName;
//        private final @DrawableRes int mIcon;
//
//        public Product(String productName, String defaultName, int icon) {
//            mProductName = productName;
//            mDefaultName = defaultName;
//            mIcon = icon;
//        }
//
//        public String getProductName() {
//            return mProductName;
//        }
//
//        public String getDefaultName() {
//            return mDefaultName;
//        }
//
//        public int getIcon() {
//            return mIcon;
//        }
//    }
}
