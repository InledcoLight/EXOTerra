package com.inledco.exoterra.main;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.AliotClient;
import com.inledco.exoterra.aliot.bean.InviteAction;
import com.inledco.exoterra.aliot.bean.InviteMessage;
import com.inledco.exoterra.base.BaseActivity;
import com.inledco.exoterra.event.DisconnectIotEvent;
import com.inledco.exoterra.main.devices.DevicesFragment;
import com.inledco.exoterra.main.devices.LocalDevicesFragment;
import com.inledco.exoterra.main.groups.DashboardFragment;
import com.inledco.exoterra.main.groups.GroupsFragment;
import com.inledco.exoterra.main.groups.GroupsLoginFragment;
import com.inledco.exoterra.main.me.PrefFragment;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.manager.GroupManager;
import com.inledco.exoterra.manager.UserManager;
import com.inledco.exoterra.manager.UserPref;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends BaseActivity {

    private BottomNavigationView main_bnv;

    private MainViewModel mMainViewModel;
    private AuthStatus mAuthStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
        initData();
        initEvent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        AliotClient.getInstance().deinit();
        DeviceManager.getInstance().clear();
        GroupManager.getInstance().clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult: " + requestCode + " " + resultCode);
        if (requestCode == 1 && resultCode == 1) {
            mAuthStatus.setAuthorized(UserManager.getInstance().isAuthorized());
            mMainViewModel.postValue();
            if (mAuthStatus.isAuthorized()) {
                GroupManager.getInstance().getAllGroups();
                DeviceManager.getInstance().getAllDevices();
                switch (main_bnv.getSelectedItemId()) {
                    case R.id.main_bnv_dashboard:
                        replaceFragment(R.id.main_fl_show, new DashboardFragment());
                        break;
                    case R.id.main_bnv_habitat:
                        replaceFragment(R.id.main_fl_show, new GroupsFragment());
                        break;
                    case R.id.main_bnv_devices:
                        replaceFragment(R.id.main_fl_show, new DevicesFragment());
                        break;
                    case R.id.main_bnv_pref:

                        break;
                }
            }
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        main_bnv = findViewById(R.id.main_bnv);
    }

    @Override
    protected void initData() {
        mAuthStatus = new AuthStatus();
        mAuthStatus.setAuthorized(UserManager.getInstance().isAuthorized());
        mAuthStatus.setIotInited(AliotClient.getInstance().isInited());
        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mMainViewModel.setData(mAuthStatus);

        if (mAuthStatus.isAuthorized()) {
            DeviceManager.getInstance().getSubscribedDevices();
            GroupManager.getInstance().getGroups();
        } else {
            UserPref.clearAuthorization(this);
            DeviceManager.getInstance().clear();
            GroupManager.getInstance().clear();
            UserManager.getInstance().deinit();
        }
    }

    @Override
    protected void initEvent() {
        main_bnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment = null;
                switch (menuItem.getItemId()) {
                    case R.id.main_bnv_home:
                        finish();
                        break;
                    case R.id.main_bnv_dashboard:
                        fragment = mAuthStatus.isAuthorized() ? new DashboardFragment() : new GroupsLoginFragment();
                        break;
                    case R.id.main_bnv_habitat:
                        fragment = mAuthStatus.isAuthorized() ? new GroupsFragment() : new GroupsLoginFragment();
                        break;
                    case R.id.main_bnv_devices:
                        fragment = mAuthStatus.isAuthorized() ? new DevicesFragment() : new LocalDevicesFragment();
                        break;
                    case R.id.main_bnv_pref:
                        fragment = new PrefFragment();
                        break;
                }
                if (fragment != null) {
                    replaceFragment(R.id.main_fl_show, fragment);
                }
                return true;
            }
        });
        main_bnv.setSelectedItemId(mAuthStatus.isAuthorized() ? R.id.main_bnv_dashboard : R.id.main_bnv_devices);
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onDiconnectIotEvent(DisconnectIotEvent event) {
        finish();
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onInviteEvent(InviteMessage message) {
        if (message == null || !TextUtils.equals(message.getAction(), InviteAction.ACCEPT.getAction())) {
            return;
        }
        GroupManager.getInstance().getGroups();
        DeviceManager.getInstance().getSubscribedDevices();
    }
}
