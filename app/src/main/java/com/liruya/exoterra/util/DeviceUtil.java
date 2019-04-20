package com.liruya.exoterra.util;

import android.support.annotation.DrawableRes;
import android.text.TextUtils;

import com.liruya.exoterra.R;
import com.liruya.exoterra.xlink.XlinkConstants;

import java.util.ArrayList;
import java.util.List;

public class DeviceUtil {
    public static String getDefaultName(String pid) {
        if (TextUtils.isEmpty(pid)) {
            return "";
        }
        if (XlinkConstants.PRODUCT_ID_LEDSTRIP.equals(pid)) {
            return "Light Strip";
        }
        if (XlinkConstants.PRODUCT_ID_MONSOON.equals(pid)) {
            return "Monsoon";
        }
        if (XlinkConstants.PRODUCT_ID_SOCKET.equals(pid)) {
            return "Socket";
        }
        return "";
    }

    public static @DrawableRes int getProductIcon(String pid) {
        if (TextUtils.isEmpty(pid)) {
            return R.drawable.ic_device_default_white_64dp;
        }
        if (XlinkConstants.PRODUCT_ID_LEDSTRIP.equals(pid)) {
            return R.drawable.ic_light_white_64dp;
        }
        if (XlinkConstants.PRODUCT_ID_MONSOON.equals(pid)) {
            return R.mipmap.ic_monsoon_on_128;
        }
        if (XlinkConstants.PRODUCT_ID_SOCKET.equals(pid)) {
            return R.drawable.ic_socket_white_64dp;
        }
        return R.drawable.ic_device_default_white_64dp;
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
}
