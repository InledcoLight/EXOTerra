package com.inledco.exoterra.helper;

import android.content.Context;
import android.support.annotation.NonNull;

public class VerifycodeHelper {
    private final int VERIFYCODE_PERIOD = 60000;
    private long mLastSentTime;

    private VerifycodeHelper() {

    }

    public static VerifycodeHelper getInstance() {
        return LazyHolder.INSTANCE;
    }

    public boolean canSend() {
        return System.currentTimeMillis() - mLastSentTime > VERIFYCODE_PERIOD;
    }

    public void recordSentTime(@NonNull Context context) {

    }

    private static class LazyHolder {
        private static final VerifycodeHelper INSTANCE = new VerifycodeHelper();
    }
}
