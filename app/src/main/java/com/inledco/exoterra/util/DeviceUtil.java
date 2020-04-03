package com.inledco.exoterra.util;

import android.net.wifi.ScanResult;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.AliotConsts;

import java.util.ArrayList;
import java.util.List;

public class DeviceUtil {
    public static final String EXO_STRIP_REGEX = "^EXOTerraStrip_[0-9A-Fa-f]{6}$";
    public static final String EXO_SOCKET_REGEX = "^EXOTerraSocket_[0-9A-Fa-f]{6}$";
    public static final String EXO_MONSOON_REGEX = "^EXOTerraMonsoon_[0-9A-Fa-f]{6}$";

    public static final String  EXO_STRIP_45CM = "Microtope Strip 45cm";
    public static final String  EXO_STRIP_60CM = "Microtope Strip 60cm";
    public static final String  EXO_STRIP_90CM = "Microtope Strip 90cm";
    public static final String  EXO_SOCKET_NA = "Microtope Socket NA";
    public static final String  EXO_SOCKET_EU = "Microtope Socket EU";
    public static final String  EXO_MONSOON_SOLO = "Microtope Monsoon Solo";
    public static final String  EXO_MONSOON_MULTI = "Microtope Monsoon Multi";

    public static final int ESP8266_GATEWAY = 0x0104A8C0;

    public static String getProductName(String pkey) {
        if (TextUtils.equals(pkey, AliotConsts.PRODUCT_KEY_EXOLED)) {
            return AliotConsts.PRODUCT_EXOLED;
        }
        if (TextUtils.equals(pkey, AliotConsts.PRODUCT_KEY_EXOSOCKET)) {
            return AliotConsts.PRODUCT_EXOSOCKET;
        }
        if (TextUtils.equals(pkey, AliotConsts.PRODUCT_KEY_EXOMONSOON)) {
            return AliotConsts.PRODUCT_EXOMONSOON;
        }
        return "";
    }

    public static String getDefaultName(String pkey) {
//        if (TextUtils.isEmpty(pkey)) {
//            return "";
//        }
//        if (XlinkConstants.PRODUCT_ID_LEDSTRIP.equals(pkey)) {
//            return "EXOTerraStrip";
//        }
//        if (XlinkConstants.PRODUCT_ID_MONSOON.equals(pkey)) {
//            return "EXOTerraMonsoon";
//        }
//        if (XlinkConstants.PRODUCT_ID_SOCKET.equals(pkey)) {
//            return "EXOTerraSocket";
//        }
        return "";
    }

    public static @DrawableRes int getProductIcon(String pkey) {
//        if (XlinkConstants.PRODUCT_ID_LEDSTRIP.equals(pkey)) {
//            return R.drawable.ic_strip;
//        }
//        if (XlinkConstants.PRODUCT_ID_MONSOON.equals(pkey)) {
//            return R.drawable.ic_monsoon;
//        }
//        if (XlinkConstants.PRODUCT_ID_SOCKET.equals(pkey)) {
//            return R.drawable.ic_socket;
//        }
        return R.drawable.ic_device_default_white_64dp;
    }

    public static @DrawableRes int getProductIconSmall(String pkey) {
//        if (XlinkConstants.PRODUCT_ID_LEDSTRIP.equals(pkey)) {
//            return R.drawable.ic_strip_48dp;
//        }
//        if (XlinkConstants.PRODUCT_ID_MONSOON.equals(pkey)) {
//            return R.drawable.ic_monsoon_48dp;
//        }
//        if (XlinkConstants.PRODUCT_ID_SOCKET.equals(pkey)) {
//            return R.drawable.ic_socket_48dp;
//        }
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

    public static String getProductType(String pkey) {
//        if (TextUtils.isEmpty(pkey)) {
//            return "";
//        }
//        if (XlinkConstants.PRODUCT_ID_LEDSTRIP.equals(pkey)) {
//            return "Microtope Led Strip";
//        }
//        if (XlinkConstants.PRODUCT_ID_MONSOON.equals(pkey)) {
//            return "Microtope Monsoon";
//        }
//        if (XlinkConstants.PRODUCT_ID_SOCKET.equals(pkey)) {
//            return "Microtope Socket";
//        }
        return pkey;
    }

    public static List<String> getAllProducts() {
        final List<String> products = new ArrayList<>();
//        products.add(XlinkConstants.PRODUCT_ID_LEDSTRIP);
//        products.add(XlinkConstants.PRODUCT_ID_SOCKET);
//        products.add(XlinkConstants.PRODUCT_ID_MONSOON);
        return products;
    }

    public static boolean containsProduct(String prdt) {
//        if (TextUtils.isEmpty(prdt)) {
//            return false;
//        }
//        if (TextUtils.equals(XlinkConstants.PRODUCT_ID_LEDSTRIP, prdt)) {
//            return true;
//        }
//        if (TextUtils.equals(XlinkConstants.PRODUCT_ID_SOCKET, prdt)) {
//            return true;
//        }
//        if (TextUtils.equals(XlinkConstants.PRODUCT_ID_MONSOON, prdt)) {
//            return true;
//        }
        return false;
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
}
