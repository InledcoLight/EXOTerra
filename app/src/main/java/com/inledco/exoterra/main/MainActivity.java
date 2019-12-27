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
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.inledco.exoterra.R;
import com.inledco.exoterra.adddevice.AddDeviceActivity;
import com.inledco.exoterra.base.BaseActivity;
import com.inledco.exoterra.main.devices.DevicesFragment;
import com.inledco.exoterra.main.devices.LocalDevicesFragment;
import com.inledco.exoterra.main.groups.GroupsFragment;
import com.inledco.exoterra.main.home.HomeFragment;
import com.inledco.exoterra.main.me.MeFragment;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.manager.HomeManager;
import com.inledco.exoterra.manager.UserManager;
import com.inledco.exoterra.push.AlipushService;
import com.inledco.exoterra.push.FCMMessageService;
import com.inledco.exoterra.push.FCMTokenListener;
import com.inledco.exoterra.scan.ScanActivity;
import com.inledco.exoterra.smartconfig.SmartconfigActivity;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import cn.xlink.restful.XLinkRestfulEnum;
import cn.xlink.sdk.v5.manager.XLinkUserManager;
import cn.xlink.sdk.v5.module.main.XLinkSDK;
import q.rorbin.badgeview.Badge;

public class MainActivity extends BaseActivity {

    private BottomNavigationView main_bnv;
    private BottomNavigationItemView main_me;
    private Badge badge;

    private HomeViewModel mHomeViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "onCreate: " + XLinkUserManager.getInstance().getUid() + "  " + XLinkUserManager.getInstance().getAccessToken());
//        initAlipush();
        initFCMService();

        initData();
        initEvent();
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

        main_bnv.getMenu()
                .findItem(R.id.main_bnv_habitat)
                .setVisible(XLinkUserManager.getInstance().isUserAuthorized());
    }

    @Override
    protected void initData() {
        XLinkSDK.start();
        DeviceManager.getInstance().syncSubcribeDevices(null);
        HomeManager.getInstance().refreshHomeList(null);
        replaceFragment(R.id.main_fl_show, new HomeFragment());
//        if (checkCameraPermissions() == false) {
//            requestCameraPermission();
//        } else {
//            scanQrCode();
//            test();
//        }

//        mHomeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
//        mHomeViewModel.setGetHomeInfoCallback(new XlinkRequestCallback<String>() {
//            @Override
//            public void onError(String error) {
//                Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT)
//                     .show();
//            }
//
//            @Override
//            public void onSuccess(String s) {
//
//            }
//        });
//
//        Home2Manager.getInstance().checkHome(new XlinkRequestCallback<String>() {
//            @Override
//            public void onError(String error) {
//                Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT)
//                     .show();
//                getMessageDialog().setTitle("Check home failed")
//                                  .setMessage(error)
//                                  .show();
//            }
//
//            @Override
//            public void onSuccess(final String s) {
//                mHomeViewModel.refreshHomeInfo();
//            }
//        });

//
//        test();
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
                        replaceFragment(R.id.main_fl_show, new HomeFragment());
                        break;
                    case R.id.main_bnv_devices:
                        if (XLinkUserManager.getInstance().isUserAuthorized()) {
                            replaceFragment(R.id.main_fl_show, new DevicesFragment());
                        } else {
                            replaceFragment(R.id.main_fl_show, new LocalDevicesFragment());
                        }
                        break;
                    case R.id.main_bnv_habitat:
                        replaceFragment(R.id.main_fl_show, new GroupsFragment());
                        break;
                    case R.id.main_bnv_me:
                        replaceFragment(R.id.main_fl_show, new MeFragment());
                        break;
                }
                return true;
            }
        });
    }

    private void initAlipush() {
        if (XLinkUserManager.getInstance().isUserAuthorized()) {
            CloudPushService pushService = PushServiceFactory.getCloudPushService();
            String deviceToken = pushService.getDeviceId();
            Log.e(TAG, "initAlipush: " + deviceToken);
            pushService.setPushIntentService(AlipushService.class);
            XlinkCloudManager.getInstance()
                             .registerAlipush(deviceToken, XLinkRestfulEnum.PushMessageOpenType.NONE, null, new XlinkRequestCallback<String>() {
                                 @Override
                                 public void onError(String error) {
                                     Log.e(TAG, "onError: registerAlipush - " + error);
                                 }

                                 @Override
                                 public void onSuccess(String s) {
                                     Log.e(TAG, "onSuccess: registerAlipush - " + s);
                                 }
                             });
        } else {
            int usrid = UserManager.getUserId(MainActivity.this);
            XlinkCloudManager.getInstance().unregisterAlipush(usrid, new XlinkRequestCallback<String>() {
                @Override
                public void onError(String error) {
                    Log.e(TAG, "onError: unregisterAlipush - " + error);
                }

                @Override
                public void onSuccess(String s) {
                    Log.e(TAG, "onSuccess: unregisterAlipush - " + s);
                }
            });
        }
    }

    private void initFCMService() {
        if (XLinkUserManager.getInstance().isUserAuthorized()) {
            FCMMessageService.syncToken(new FCMTokenListener() {
                @Override
                public void onTokenResult(final String token) {
                    if (!TextUtils.isEmpty(token)) {
                        Log.e(TAG, "onTokenResult: " + token);
                        XlinkCloudManager.getInstance()
                                         .registerFCMMessageService(token, new XlinkRequestCallback<String>() {
                                             @Override
                                             public void onError(String error) {
                                                 Log.e(TAG, "onError: registerFCM - " + error);
                                             }

                                             @Override
                                             public void onSuccess(String s) {
                                                 Log.e(TAG, "onSuccess: registerFCM - " + s);
                                             }
                                         });
                    }
                }
            });
        } else {
            int usrid = UserManager.getUserId(MainActivity.this);
            XlinkCloudManager.getInstance().unregisterFCMMessageService(usrid, new XlinkRequestCallback<String>() {
                @Override
                public void onError(String error) {
                    Log.e(TAG, "onError: unregisterAlipush - " + error);
                }

                @Override
                public void onSuccess(String s) {
                    Log.e(TAG, "onSuccess: unregisterAlipush - " + s);
                }
            });
        }
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

    private void test() {
    }
}
