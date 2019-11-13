package com.inledco.exoterra.util;

import android.net.wifi.ScanResult;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.inledco.exoterra.R;
import com.inledco.exoterra.xlink.XlinkConstants;

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

    public static String getDefaultName(String pid) {
        if (TextUtils.isEmpty(pid)) {
            return "";
        }
        if (XlinkConstants.PRODUCT_ID_LEDSTRIP.equals(pid)) {
            return "EXOTerraStrip";
        }
        if (XlinkConstants.PRODUCT_ID_MONSOON.equals(pid)) {
            return "EXOTerraMonsoon";
        }
        if (XlinkConstants.PRODUCT_ID_SOCKET.equals(pid)) {
            return "EXOTerraSocket";
        }
        return "";
    }

    public static @DrawableRes int getProductIcon(String pid) {
        if (XlinkConstants.PRODUCT_ID_LEDSTRIP.equals(pid)) {
            return R.drawable.ic_strip;
        }
        if (XlinkConstants.PRODUCT_ID_MONSOON.equals(pid)) {
            return R.drawable.ic_monsoon;
        }
        if (XlinkConstants.PRODUCT_ID_SOCKET.equals(pid)) {
            return R.drawable.ic_socket;
        }
        return R.drawable.ic_device_default_white_64dp;
    }

    public static @DrawableRes int getProductIconSmall(String pid) {
        if (XlinkConstants.PRODUCT_ID_LEDSTRIP.equals(pid)) {
            return R.drawable.ic_strip_24dp;
        }
        if (XlinkConstants.PRODUCT_ID_MONSOON.equals(pid)) {
            return R.drawable.ic_monsoon_24dp;
        }
        if (XlinkConstants.PRODUCT_ID_SOCKET.equals(pid)) {
            return R.drawable.ic_socket_24dp;
        }
        return R.drawable.ic_device_default_white_24dp;
    }

    public static @DrawableRes int getProductLedoffIcon(String pid) {
        if (XlinkConstants.PRODUCT_ID_LEDSTRIP.equals(pid)) {
            return R.mipmap.ic_strip_off_256;
        }
        if (XlinkConstants.PRODUCT_ID_MONSOON.equals(pid)) {
            return R.mipmap.ic_monsoon_off_256;
        }
        if (XlinkConstants.PRODUCT_ID_SOCKET.equals(pid)) {
            return R.mipmap.ic_socket_off_256;
        }
        return 0;
    }

    public static @DrawableRes int getProductLedonIcon(String pid) {
        if (XlinkConstants.PRODUCT_ID_LEDSTRIP.equals(pid)) {
            return R.mipmap.ic_strip_on_256;
        }
        if (XlinkConstants.PRODUCT_ID_MONSOON.equals(pid)) {
            return R.mipmap.ic_monsoon_on_256;
        }
        if (XlinkConstants.PRODUCT_ID_SOCKET.equals(pid)) {
            return R.mipmap.ic_socket_on_256;
        }
        return 0;
    }

    public static String getProductType(String pid) {
        if (TextUtils.isEmpty(pid)) {
            return "";
        }
        if (XlinkConstants.PRODUCT_ID_LEDSTRIP.equals(pid)) {
            return "EXO Led Strip";
        }
        if (XlinkConstants.PRODUCT_ID_MONSOON.equals(pid)) {
            return "EXO Monsoon";
        }
        if (XlinkConstants.PRODUCT_ID_SOCKET.equals(pid)) {
            return "EXO Socket";
        }
        return pid;
    }

    public static List<String> getAllProducts() {
        final List<String> products = new ArrayList<>();
        products.add(XlinkConstants.PRODUCT_ID_LEDSTRIP);
        products.add(XlinkConstants.PRODUCT_ID_SOCKET);
        products.add(XlinkConstants.PRODUCT_ID_MONSOON);
        return products;
    }

    public static boolean containsProduct(String prdt) {
        if (TextUtils.isEmpty(prdt)) {
            return false;
        }
        if (TextUtils.equals(XlinkConstants.PRODUCT_ID_LEDSTRIP, prdt)) {
            return true;
        }
        if (TextUtils.equals(XlinkConstants.PRODUCT_ID_SOCKET, prdt)) {
            return true;
        }
        if (TextUtils.equals(XlinkConstants.PRODUCT_ID_MONSOON, prdt)) {
            return true;
        }
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

    public static boolean isEXODevice(@NonNull final ScanResult result, @NonNull final String pid) {
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
        if (ssid.matches(EXO_STRIP_REGEX) && XlinkConstants.PRODUCT_ID_LEDSTRIP.equals(pid)) {
            return true;
        }
        if (ssid.matches(EXO_SOCKET_REGEX) && XlinkConstants.PRODUCT_ID_SOCKET.equals(pid)) {
            return true;
        }
        if (ssid.matches(EXO_MONSOON_REGEX) && XlinkConstants.PRODUCT_ID_MONSOON.equals(pid)) {
            return true;
        }
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
