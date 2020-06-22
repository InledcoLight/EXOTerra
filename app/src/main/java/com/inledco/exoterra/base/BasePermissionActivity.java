package com.inledco.exoterra.base;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.inledco.exoterra.R;

public abstract class BasePermissionActivity extends BaseActivity {
    @RequiresApi (api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions == null || grantResults == null || permissions.length != grantResults.length) {
            return;
        }
        int len = permissions.length;
        for (int i = 0; i < len; i++) {
            String permission = permissions[i];
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else if (shouldShowRequestPermissionRationale(permission)) {
                onPermissionDenied(permission);
            } else {
                onPermissionPermanentDenied(permission);
            }
        }
    }

    protected boolean checkPermission(@NonNull final String permssion) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return ContextCompat.checkSelfPermission(this, permssion) == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi (Build.VERSION_CODES.M)
    protected void requestPermission(int requestCode, @NonNull final String permission) {
        requestPermissions(new String[]{permission}, requestCode);
    }

    protected void showPermissionDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
               .setMessage(message)
               .setNegativeButton(R.string.cancel, null)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       startAppDetailActivity();
                   }
               })
               .setCancelable(false)
               .show();
    }

    protected void startAppDetailActivity() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                                getPackageName(),
                                null);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    protected abstract void onPermissionGranted(String permission);

    protected abstract void onPermissionDenied(String permission);

    protected abstract void onPermissionPermanentDenied(String permission);
}
