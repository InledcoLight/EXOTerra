package com.inledco.exoterra.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.inledco.exoterra.AppConstants;

import java.lang.ref.WeakReference;

public class VerifycodeManager {
    private WeakReference<Context> mWeakContext;

    private final int VERIFYCODE_PERIOD = 30000;

    public VerifycodeManager(@NonNull Context context) {
        mWeakContext = new WeakReference<>(context);
        init();
    }

    public void init() {
        SharedPreferences sp1 = mWeakContext.get().getSharedPreferences(AppConstants.FILE_VERIFYCODE_REGISTER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp1.edit();
        for (String email : sp1.getAll().keySet()) {
            long time = sp1.getLong(email, 0);
            if (System.currentTimeMillis() - time > VERIFYCODE_PERIOD) {
                editor.remove(email);
            }
        }
        editor.apply();
    }

    public void addRegisterVerifycode(@NonNull final String email) {
        long time = System.currentTimeMillis();
        SharedPreferences sp = mWeakContext.get().getSharedPreferences(AppConstants.FILE_VERIFYCODE_REGISTER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(email, time);
        editor.apply();
    }

    public void addResetVerifycode(@NonNull final String email) {
        long time = System.currentTimeMillis();
        SharedPreferences sp = mWeakContext.get().getSharedPreferences(AppConstants.FILE_VERIFYCODE_RESET, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(email, time);
        editor.apply();
    }

    public long getRegisterExpired(@NonNull final String email) {
        SharedPreferences sp = mWeakContext.get().getSharedPreferences(AppConstants.FILE_VERIFYCODE_REGISTER, Context.MODE_PRIVATE);
        if (sp.contains(email)) {
            long time = sp.getLong(email, 0);
            long result = time + VERIFYCODE_PERIOD - System.currentTimeMillis();
            if (result > 0) {
                return result/1000;
            }
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(email);
            editor.apply();
        }
        return 0;
    }

    public long getResetExpired(@NonNull final String email) {
        SharedPreferences sp = mWeakContext.get().getSharedPreferences(AppConstants.FILE_VERIFYCODE_RESET, Context.MODE_PRIVATE);
        if (sp.contains(email)) {
            long time = sp.getLong(email, 0);
            long result = time + VERIFYCODE_PERIOD - System.currentTimeMillis();
            if (result > 0) {
                return result/1000;
            }
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(email);
            editor.apply();
        }
        return 0;
    }
}
