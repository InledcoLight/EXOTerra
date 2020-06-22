package com.inledco.exoterra.util;

public interface PermissionListener {
    void onPermissionGranted(String permission);

    void onPermissionDenied(String permission);

    void onPermissionNotPrompt(String permission);
}
