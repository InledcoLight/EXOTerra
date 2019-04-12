package com.liruya.exoterra.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.liruya.base.BaseActivity;
import com.liruya.exoterra.R;
import com.liruya.exoterra.main.devices.DevicesFragment;
import com.liruya.exoterra.scan.ScanActivity;
import com.liruya.exoterra.smartconfig.SmartconfigActivity;

public class MainActivity extends BaseActivity {

    private Toolbar main_toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.menu_main_smartconfig).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startSmartconfigActivity();
                return false;
            }
        });
        menu.findItem(R.id.menu_main_add).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startScanActivity();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        main_toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(main_toolbar);
    }

    @Override
    protected void initData() {
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.main_fl_show, new DevicesFragment())
                                   .commit();
    }

    @Override
    protected void initEvent() {

    }

    private void startSmartconfigActivity() {
        Intent intent = new Intent(MainActivity.this, SmartconfigActivity.class);
        startActivity(intent);
    }

    private void startScanActivity() {
        Intent intent = new Intent(MainActivity.this, ScanActivity.class);
        startActivity(intent);
    }
}
