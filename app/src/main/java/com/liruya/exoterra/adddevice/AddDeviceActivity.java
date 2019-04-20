package com.liruya.exoterra.adddevice;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.liruya.base.BaseActivity;
import com.liruya.exoterra.R;

public class AddDeviceActivity extends BaseActivity {

    private final int REQUEST_LOCATION_CODE = 1;

    private Toolbar adddevice_toolbar;

    private ConnectNetBean mConnectNetBean;
    private ConnectNetViewModel mConnectNetViewModel;

//    private LocationHelper mLocationHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
        initEvent();
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_LOCATION_CODE) {
//            if (permissions == null || permissions.length != 1 || grantResults == null || grantResults.length != 1) {
//                return;
//            }
//            if (Manifest.permission.ACCESS_COARSE_LOCATION.equals(permissions[0])) {
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                } else if (mLocationHelper.shouldShowRequestPermissionRationale()) {
//                    Toast.makeText(AddDeviceActivity.this, R.string.msg_location_permission, Toast.LENGTH_LONG)
//                         .show();
//                } else {
//                    showLocationPermissionDialog();
//                }
//            }
//        }
//    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_adddevice;
    }

    @Override
    protected void initView() {
        adddevice_toolbar = findViewById(R.id.adddevice_toolbar);

        setSupportActionBar(adddevice_toolbar);
    }

    @Override
    protected void initData() {
        mConnectNetBean = new ConnectNetBean();
        mConnectNetViewModel = ViewModelProviders.of(this).get(ConnectNetViewModel.class);
        mConnectNetViewModel.setData(mConnectNetBean);
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.adddevice_fl, new ProductsFragment())
                                   .commit();

//        mLocationHelper = new LocationHelper(AddDeviceActivity.this);
//        if (mLocationHelper.checkLocationPermisson() == false) {
//            mLocationHelper.requestLocationPermission(REQUEST_LOCATION_CODE);
//        }
    }

    @Override
    protected void initEvent() {
        adddevice_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mConnectNetViewModel.getData().isRunning()) {

        } else {
            super.onBackPressed();
        }
    }

//    private void showLocationPermissionDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(AddDeviceActivity.this);
//        builder.setTitle(R.string.title_location_permission)
//               .setMessage(R.string.msg_location_permission)
//               .setNegativeButton(R.string.cancel, null)
//               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                   @Override
//                   public void onClick(DialogInterface dialog, int which) {
//                       mLocationHelper.startAppDetailActivity();
//                   }
//               })
//               .setCancelable(false)
//               .show();
//    }
}
