package com.liruya.exoterra.adddevice;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.liruya.base.BaseImmersiveActivity;
import com.liruya.exoterra.R;

public class AddDeviceActivity extends BaseImmersiveActivity {

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
        mConnectNetBean = new ConnectNetBean();
        mConnectNetViewModel = ViewModelProviders.of(this).get(ConnectNetViewModel.class);
        mConnectNetViewModel.setData(mConnectNetBean);
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.adddevice_fl, new ProductsFragment())
                                   .commit();
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
}
