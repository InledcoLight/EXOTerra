package com.inledco.exoterra.util;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

public class FavouriteUtil {
    private static final String KEY_FAVOURITE = "favourite";

    private static Set<String> mFavourites;

    public static void addFavourite(@NonNull Context context, @NonNull final String homeid) {
        if (mFavourites == null) {
            getFavourites(context);
        }
        mFavourites.add(homeid);
        PrefUtil.put(context, KEY_FAVOURITE, mFavourites);
    }

    public static void removeFavourite(@NonNull Context context, @NonNull final String homeid) {
        if (mFavourites == null) {
            getFavourites(context);
        }
        mFavourites.remove(homeid);
        PrefUtil.put(context, KEY_FAVOURITE, mFavourites);
    }

    public static Set<String> getFavourites(@NonNull final Context context) {
        if (mFavourites == null) {
            mFavourites = new HashSet<>();
        }
        mFavourites.clear();
        Set<String> favourites = PrefUtil.getStringSet(context, KEY_FAVOURITE);
        if (favourites != null) {
            mFavourites.addAll(favourites);
        }
        return mFavourites;
    }
}
