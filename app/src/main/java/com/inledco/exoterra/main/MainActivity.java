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

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.iot.model.v20180120.SetDevicePropertyRequest;
import com.aliyuncs.iot.model.v20180120.SetDevicePropertyResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.inledco.exoterra.GlobalSettings;
import com.inledco.exoterra.R;
import com.inledco.exoterra.adddevice.AddDeviceActivity;
import com.inledco.exoterra.base.BaseActivity;
import com.inledco.exoterra.main.devices.DevicesFragment;
import com.inledco.exoterra.main.devices.LocalDevicesFragment;
import com.inledco.exoterra.main.groups.GroupsFragment;
import com.inledco.exoterra.main.me.MeFragment;
import com.inledco.exoterra.main.pref.PrefFragment;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.manager.HomeManager;
import com.inledco.exoterra.manager.UserManager;
import com.inledco.exoterra.push.FCMMessageService;
import com.inledco.exoterra.push.FCMTokenListener;
import com.inledco.exoterra.scan.ScanActivity;
import com.inledco.exoterra.smartconfig.SmartconfigActivity;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import cn.xlink.sdk.v5.manager.XLinkUserManager;
import cn.xlink.sdk.v5.module.main.XLinkSDK;
import q.rorbin.badgeview.Badge;

public class MainActivity extends BaseActivity {

    private BottomNavigationView main_bnv;
    private BottomNavigationItemView main_me;
    private Badge badge;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "onCreate: " + XLinkUserManager.getInstance().getUid() + "  " + XLinkUserManager.getInstance().getAccessToken());
//        initAlipush();
        initFCMService();

        initData();
        initEvent();
//        initAliot();
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

        boolean login = XLinkUserManager.getInstance().isUserAuthorized();
        main_bnv.getMenu().findItem(R.id.main_bnv_home).setVisible(login);
        main_bnv.getMenu().findItem(R.id.main_bnv_habitat).setVisible(login);
    }

    @Override
    protected void initData() {
        GlobalSettings.init(this);
        XLinkSDK.start();
        DeviceManager.getInstance().syncSubcribeDevices(null);
        HomeManager.getInstance().refreshHomeList(null);
        if (XLinkUserManager.getInstance().isUserAuthorized()) {
            main_bnv.setSelectedItemId(R.id.main_bnv_home);
            replaceFragment(R.id.main_fl_show, GroupsFragment.newInstance(true));
        } else {
            main_bnv.setSelectedItemId(R.id.main_bnv_devices);
            replaceFragment(R.id.main_fl_show, new LocalDevicesFragment());
        }

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
                        replaceFragment(R.id.main_fl_show, GroupsFragment.newInstance(true));
                        break;
                    case R.id.main_bnv_habitat:
                        replaceFragment(R.id.main_fl_show, GroupsFragment.newInstance(false));
                        break;
                    case R.id.main_bnv_devices:
                        if (XLinkUserManager.getInstance().isUserAuthorized()) {
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

    private void initAliot() {
//        String accessKey = "";
//        String accessSecret = "";
//        String accessKey = "LTAI4FwGrXTiyWhizBnf6NJq";
//        String accessSecret = "2chF61ExE3HvQ3Si3PhalcGibvjPha";//
        String accessKey = "LTAI4Fe6PdzpbzqhNV9y8bv7";
        String accessSecret = "ggNfjFlBO2KIzvo1BOnymFVEvprcFM";
        try {
            DefaultProfile.addEndpoint("cn-shanghai", "cn-shanghai", "Iot", "iot.cn-shanghai.aliyuncs.com");
            IClientProfile profile = DefaultProfile.getProfile("cn-shanghai", accessKey, accessSecret);
            final DefaultAcsClient client = new DefaultAcsClient(profile);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    SetDevicePropertyResponse response = null;
                    try {
                        response = client.getAcsResponse(setLedPower(null, "a1layga4ANI", "2CF432121FC9", false));
                    } catch (ClientException e) {
                        e.printStackTrace();
                    }
                    Log.e(TAG, "initAliot: " + response.getSuccess());
                }
            }).start();

//            final RegisterDeviceRequest request = new RegisterDeviceRequest();
//            request.setRegionId("cn-shanghai");
//            request.setDeviceName("18FE34D4178D");
//            request.setProductKey("a1layga4ANI");
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    RegisterDeviceResponse response = null;
//                    try {
//                        response = client.getAcsResponse(request);
//                    } catch (ClientException e) {
//                        e.printStackTrace();
//                    }
//                    Log.e(TAG, "initAliot: " + new Gson().toJson(response));
//                }
//            }).start();
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    private SetDevicePropertyRequest setLedPower(String iotid, String pkey, String dname, boolean power) {
        SetDevicePropertyRequest request = new SetDevicePropertyRequest();
        request.setProductKey(pkey);
        request.setDeviceName(dname);
        request.setIotId(iotid);
        request.setItems("{\"Power\":" + (power ? 1 : 0) + "}");
        return request;
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
