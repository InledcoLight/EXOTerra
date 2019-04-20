package com.liruya.exoterra.helper;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.lang.ref.WeakReference;

public class LocationHelper {
    private WeakReference<AppCompatActivity> mActivity;
    private WeakReference<Fragment> mFragment;

    public LocationHelper(AppCompatActivity activity) {
        mActivity = new WeakReference<>(activity);
    }

    public LocationHelper(Fragment fragment) {
        mFragment = new WeakReference<>(fragment);
    }

    private AppCompatActivity getActivity() {
        if (mActivity != null) {
            return mActivity.get();
        }
        if (mFragment != null && mFragment.get() != null) {
            return (AppCompatActivity) mFragment.get().getActivity();
        }
        return null;
    }

    public boolean isGpsEnabled() {
        if (getActivity() == null) {
            return false;
        }
        LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (manager != null) {
            return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        return false;
    }

    public boolean checkLocationPermisson() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi (Build.VERSION_CODES.M)
    public void requestLocationPermission(int requestCode) {
        if (mActivity != null) {
            mActivity.get().requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode);
//            ActivityCompat.requestPermissions(mActivity.get(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode);
        } else if (mFragment != null){
            mFragment.get().requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode);
        }
    }

    @TargetApi (Build.VERSION_CODES.M)
    public boolean shouldShowRequestPermissionRationale() {
        if (mActivity != null && mActivity.get() != null) {
            return mActivity.get().shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION);
//            return ActivityCompat.shouldShowRequestPermissionRationale(mActivity.get(), Manifest.permission.ACCESS_COARSE_LOCATION);
        } else if (mFragment != null && mFragment.get() != null){
            return mFragment.get().shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION);
        } else {
            return false;
        }
    }

    public void startAppDetailActivity() {
        if (getActivity() == null) {
            return;
        }
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                                getActivity().getPackageName(),
                                null);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startActivity(intent);
    }

    public void startLocationActivity() {
        if (getActivity() == null) {
            return;
        }
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startActivity(intent);
    }
}
