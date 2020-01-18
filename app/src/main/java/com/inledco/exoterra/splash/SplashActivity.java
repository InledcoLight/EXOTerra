package com.inledco.exoterra.splash;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseActivity;
import com.inledco.exoterra.login.LoginActivity;
import com.inledco.exoterra.main.MainActivity;
import com.inledco.exoterra.manager.UserManager;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkTaskHandler;

import cn.xlink.restful.api.app.UserApi;
import cn.xlink.sdk.v5.module.main.XLinkSDK;

public class SplashActivity extends BaseActivity {

    private final int DURATION = 1500;

    private final XlinkTaskHandler<UserApi.TokenRefreshResponse> mAuthinListener = new XlinkTaskHandler<>();
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
        XLinkSDK.start();

        int userid = UserManager.getUserId(this);
        String authorize = UserManager.getAuthorize(this);
        final String refresh_token = UserManager.getRefreshToken(this);
        final long time = System.currentTimeMillis();
        if (!UserManager.checkAuthorize(userid, authorize, refresh_token)) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    while (System.currentTimeMillis() - time < DURATION);
                    gotoLoginActivity();
                }
            };
        } else {
            XlinkCloudManager.getInstance()
                             .refreshToken(userid, authorize, refresh_token, mAuthinListener);
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    while (System.currentTimeMillis() - time < DURATION || !mAuthinListener.isOver());
                    if (mAuthinListener.isSuccess()) {
                        gotoMainActivity();
                    } else {
                        gotoLoginActivity();
                    }
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
