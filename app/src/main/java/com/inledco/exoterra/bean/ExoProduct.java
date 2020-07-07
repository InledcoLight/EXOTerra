package com.inledco.exoterra.bean;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.inledco.exoterra.R;

public enum ExoProduct {
    ExoLed("a3NZmGVkTVI", R.drawable.devicon_strip),
    ExoSocket("a3pXBGXhUbn", R.drawable.devicon_socket),
    ExoMonsoon("a3MsurD3c9T", R.drawable.devicon_monsoon);

//    private final String PRODUCT_KEY_EXOLED         = "a3NZmGVkTVI";
//    private final String PRODUCT_KEY_EXOSOCKET      = "a3pXBGXhUbn";
//    private final String PRODUCT_KEY_EXOMONSOON     = "a3MsurD3c9T";

    private final String productKey;
    private final @DrawableRes int icon;

    ExoProduct(String productKey, int icon) {
        this.productKey = productKey;
        this.icon = icon;
    }

    public String getProductKey() {
        return productKey;
    }

    public int getIcon() {
        return icon;
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
