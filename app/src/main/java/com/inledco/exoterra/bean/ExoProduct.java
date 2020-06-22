package com.inledco.exoterra.bean;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.inledco.exoterra.R;

public enum ExoProduct {
    ExoLed("a3NZmGVkTVI", R.drawable.ic_strip, R.drawable.ic_strip_48dp),
    ExoSocket("a3pXBGXhUbn", R.drawable.ic_socket, R.drawable.ic_socket_48dp),
    ExoMonsoon("a3MsurD3c9T", R.drawable.ic_monsoon, R.drawable.ic_monsoon_48dp);

//    private final String PRODUCT_KEY_EXOLED         = "a3NZmGVkTVI";
//    private final String PRODUCT_KEY_EXOSOCKET      = "a3pXBGXhUbn";
//    private final String PRODUCT_KEY_EXOMONSOON     = "a3MsurD3c9T";

    private final String productKey;
    private final @DrawableRes int icon;
    private final @DrawableRes int iconSmall;

    ExoProduct(String productKey, int icon, int iconSmall) {
        this.productKey = productKey;
        this.icon = icon;
        this.iconSmall = iconSmall;
    }

    public String getProductKey() {
        return productKey;
    }

    public int getIcon() {
        return icon;
    }

    public int getIconSmall() {
        return iconSmall;
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
