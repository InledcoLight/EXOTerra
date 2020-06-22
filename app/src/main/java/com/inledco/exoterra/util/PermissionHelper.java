package com.inledco.exoterra.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.lang.ref.WeakReference;

public class PermissionHelper {
    private WeakReference<AppCompatActivity> mActivity;

    public PermissionHelper(@NonNull AppCompatActivity activity) {
        mActivity = new WeakReference<>(activity);
    }

    public boolean checkPermisson(final String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        Activity activity = mActivity.get();
        if (activity == null) {
            return false;
        }
        boolean granted = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
        return granted;
    }

    @TargetApi (Build.VERSION_CODES.M)
    public void requestLocationPermission(int requestCode, String... permissions) {
        Activity activity = mActivity.get();
        if (activity == null) {
            return;
        }
        activity.requestPermissions(permissions, requestCode);
    }

    @TargetApi (Build.VERSION_CODES.M)
    public void requestLocationPermission(String... permissions) {
        requestLocationPermission(0, permissions);
    }

    @TargetApi (Build.VERSION_CODES.M)
    public boolean shouldShowRequestPermissionRationale(String permission) {
        Activity activity = mActivity.get();
        if (activity == null) {
            return false;
        }
        return activity.shouldShowRequestPermissionRationale(permission);
    }

    public void parsePermissionsResult(@NonNull String[] permissions, @NonNull int[] grantResults, PermissionListener listener) {
        if (permissions == null || grantResults == null || permissions.length != grantResults.length) {
            return;
        }
        int size = permissions.length;
        for (int i = 0; i < size; i++) {
            String permission = permissions[i];
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                if (listener != null) {
                    listener.onPermissionGranted(permission);
                }
            } else if (shouldShowRequestPermissionRationale(permission)) {
                if (listener != null) {
                    listener.onPermissionDenied(permission);
                }
            } else {
                if (listener != null) {
                    listener.onPermissionNotPrompt(permission);
                }
            }
        }
    }

    public void startAppDetailActivity() {
        Activity activity = mActivity.get();
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                                activity.getPackageName(),
                                null);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public void startLocationActivity() {
        Activity activity = mActivity.get();
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }
}
