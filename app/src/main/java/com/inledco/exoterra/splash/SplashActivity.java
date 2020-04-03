package com.inledco.exoterra.splash;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseActivity;
import com.inledco.exoterra.login.LoginActivity;
import com.inledco.exoterra.main.MainActivity;
import com.inledco.exoterra.manager.UserManager;

public class SplashActivity extends BaseActivity {

    private final int DURATION = 1500;

    private Runnable mRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        final long time = System.currentTimeMillis();
        if (!UserManager.checkAuthorize(this)) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    while (System.currentTimeMillis() - time < DURATION);
                    gotoLoginActivity();
                }
            };
        } else {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    while (System.currentTimeMillis() - time < DURATION);
                }
            };
        }
        new Thread(mRunnable).start();
    }

    @Override
    protected void initEvent() {

    }

    private void gotoLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void  gotoMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
