package com.inledco.exoterra.adddevice;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.inledco.exoterra.AppConstants;
import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseActivity;

public class AddDeviceActivity extends BaseActivity {

    private Toolbar adddevice_toolbar;

    private ConnectNetBean mConnectNetBean;
    private ConnectNetViewModel mConnectNetViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
        initEvent();
    }

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
        String homeid = null;
        Intent intent = getIntent();
        if (intent != null) {
            homeid = intent.getStringExtra(AppConstants.HOME_ID);
        }
        mConnectNetBean = new ConnectNetBean(homeid);
        mConnectNetViewModel = ViewModelProviders.of(this).get(ConnectNetViewModel.class);
        mConnectNetViewModel.setData(mConnectNetBean);
        replaceFragment(R.id.adddevice_fl, new ProductsFragment());
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
            AlertDialog.Builder builder = new AlertDialog.Builder(AddDeviceActivity.this);
            builder.setTitle("Stop Add Device")
                   .setMessage("This will stop add device, confirm to exit?")
                   .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           AddDeviceActivity.super.onBackPressed();
                       }
                   })
                   .setNegativeButton(R.string.cancel, null)
                   .setCancelable(false)
                   .show();
        } else {
            super.onBackPressed();
        }
    }
}
