package com.inledco.exoterra.bean;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.inledco.exoterra.R;

public enum ExoProduct {
    ExoLed("a3NZmGVkTVI", R.drawable.devicon_strip, "Led Strip"),
    ExoSocket("a3pXBGXhUbn", R.drawable.devicon_socket, "Socket"),
    ExoMonsoon("a3MsurD3c9T", R.drawable.devicon_monsoon_multi, "Monsoon");

//    private final String PRODUCT_KEY_EXOLED         = "a3NZmGVkTVI";
//    private final String PRODUCT_KEY_EXOSOCKET      = "a3pXBGXhUbn";
//    private final String PRODUCT_KEY_EXOMONSOON     = "a3MsurD3c9T";

    private final String productKey;
    private final @DrawableRes int icon;
    private final String name;

    ExoProduct(String productKey, int icon, String name) {
        this.productKey = productKey;
        this.icon = icon;
        this.name = name;
    }

    public String getProductKey() {
        return productKey;
    }

    public int getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String getProductName() {
        return name();
    }

    public String getDefaultName() {
        return name();
    }

    public String getSsidRegex() {
        return String.format("%1$s_[0-9A-Fa-f]{6}$", name());
    }

    public static ExoProduct getExoProduct(@NonNull final String productKey) {
        for (ExoProduct product : ExoProduct.values()) {
            if (TextUtils.equals(productKey, product.getProductKey())) {
                return product;
            }
        }
        return null;
    }
}
