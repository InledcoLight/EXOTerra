package com.inledco.exoterra.util;

import android.content.Context;
import android.support.annotation.NonNull;

import com.inledco.exoterra.manager.UserManager;

import java.util.HashSet;
import java.util.Set;

public class FavouriteUtil {
    private static final String KEY_FAVOURITE = "favourite_";

    private static Set<String> mFavourites;

    public static void addFavourite(@NonNull Context context, @NonNull final String homeid) {
        if (mFavourites == null) {
            getFavourites(context);
        }
        mFavourites.add(homeid);
        PrefUtil.put(context, KEY_FAVOURITE + UserManager.getInstance().getUserid(), mFavourites);
    }

    public static void removeFavourite(@NonNull Context context, @NonNull final String homeid) {
        if (mFavourites == null) {
            getFavourites(context);
        }
        mFavourites.remove(homeid);
        PrefUtil.put(context, KEY_FAVOURITE + UserManager.getInstance().getUserid(), mFavourites);
    }

    public static Set<String> getFavourites(@NonNull final Context context) {
        if (mFavourites == null) {
            mFavourites = new HashSet<>();
        }
        mFavourites.clear();
        Set<String> favourites = PrefUtil.getStringSet(context, KEY_FAVOURITE + UserManager.getInstance().getUserid());
        if (favourites != null) {
            mFavourites.addAll(favourites);
        }
        return mFavourites;
    }
}
