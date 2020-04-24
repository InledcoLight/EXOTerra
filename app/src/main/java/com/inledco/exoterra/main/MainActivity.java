package com.inledco.exoterra.main;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.inledco.exoterra.GlobalSettings;
import com.inledco.exoterra.R;
import com.inledco.exoterra.adddevice.AddDeviceActivity;
import com.inledco.exoterra.aliot.AliotClient;
import com.inledco.exoterra.base.BaseActivity;
import com.inledco.exoterra.main.devices.DevicesFragment;
import com.inledco.exoterra.main.devices.LocalDevicesFragment;
import com.inledco.exoterra.main.groups.DashboardFragment;
import com.inledco.exoterra.main.groups.GroupsFragment;
import com.inledco.exoterra.main.me.MeFragment;
import com.inledco.exoterra.main.pref.PrefFragment;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.manager.GroupManager;
import com.inledco.exoterra.manager.UserManager;
import com.inledco.exoterra.scan.ScanActivity;
import com.inledco.exoterra.smartconfig.SmartconfigActivity;

import q.rorbin.badgeview.Badge;

public class MainActivity extends BaseActivity {

    private BottomNavigationView main_bnv;
    private BottomNavigationItemView main_me;
    private Badge badge;

    private boolean authorized = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
        initEvent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AliotClient.getInstance().deinit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            String code = result.getContents();
            String rawOcde = new String(Base64.decode(code.getBytes(), Base64.DEFAULT));
            Log.e(TAG, "onActivityResult: " + code + "\n" + rawOcde);
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        main_bnv = findViewById(R.id.main_bnv);
        main_me = main_bnv.findViewById(R.id.main_bnv_me);

        main_bnv.getMenu().findItem(R.id.main_bnv_home).setVisible(authorized);
        main_bnv.getMenu().findItem(R.id.main_bnv_habitat).setVisible(authorized);
    }

    @Override
    protected void initData() {
        GlobalSettings.init(this);
        authorized = UserManager.getInstance().isAuthorized();
        if (authorized) {
            main_bnv.setSelectedItemId(R.id.main_bnv_home);
            replaceFragment(R.id.main_fl_show, new DashboardFragment());
            String userid = UserManager.getInstance().getUserid();
            String secret = UserManager.getInstance().getSecret();
            AliotClient.getInstance().init(getApplicationContext(), userid, secret);
            DeviceManager.getInstance().getSubscribedDevices();
            GroupManager.getInstance().getGroups();
        } else {
            main_bnv.setSelectedItemId(R.id.main_bnv_devices);
            replaceFragment(R.id.main_fl_show, new LocalDevicesFragment());
        }
    }

    @Override
    protected void initEvent() {
        main_bnv.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem menuItem) {
//                if (menuItem.getItemId() == R.id.main_bnv_devices && DeviceManager.getInstance().getAllDevices().size() == 0) {
//                    replaceFragment(R.id.main_fl_show, new LocalDevicesFragment());
//                }
            }
        });
        main_bnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.main_bnv_home:
                        replaceFragment(R.id.main_fl_show, new DashboardFragment());
                        break;
                    case R.id.main_bnv_habitat:
                        replaceFragment(R.id.main_fl_show, new GroupsFragment());
                        break;
                    case R.id.main_bnv_devices:
                        if (authorized) {
                            replaceFragment(R.id.main_fl_show, new DevicesFragment());
                        } else {
                            replaceFragment(R.id.main_fl_show, new LocalDevicesFragment());
                        }
                        break;
                    case R.id.main_bnv_me:
                        replaceFragment(R.id.main_fl_show, new MeFragment());
                        break;
                    case R.id.main_bnv_pref:
                        replaceFragment(R.id.main_fl_show, new PrefFragment());
                        break;
                }
                return true;
            }
        });
    }

    private void startSmartconfigActivity() {
        Intent intent = new Intent(MainActivity.this, SmartconfigActivity.class);
        startActivity(intent);
    }

    private void startScanActivity() {
        Intent intent = new Intent(MainActivity.this, ScanActivity.class);
        startActivity(intent);
    }

    private void startAdddeviceActivity() {
        Intent intent = new Intent(MainActivity.this, AddDeviceActivity.class);
        startActivity(intent);
    }

    private boolean checkCameraPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        return ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi (Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
    }

    private void scanQrCode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setBeepEnabled(false).initiateScan();
    }
}
