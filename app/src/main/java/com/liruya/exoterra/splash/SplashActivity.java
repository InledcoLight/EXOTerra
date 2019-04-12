package com.liruya.exoterra.splash;

import android.content.Intent;
import android.os.CountDownTimer;
import android.text.TextUtils;

import com.liruya.base.BaseActivity;
import com.liruya.exoterra.R;
import com.liruya.exoterra.login.LoginActivity;
import com.liruya.exoterra.main.MainActivity;
import com.liruya.exoterra.manager.UserManager;
import com.liruya.exoterra.xlink.XlinkCloudManager;

import cn.xlink.restful.api.app.UserApi;
import cn.xlink.sdk.core.XLinkCoreException;
import cn.xlink.sdk.v5.listener.XLinkTaskListener;
import cn.xlink.sdk.v5.module.main.XLinkSDK;

public class SplashActivity extends BaseActivity {

    private CountDownTimer mTimer;
    private boolean[] mResult = new boolean[]{false, false};

    @Override
    protected void onStart() {
        super.onStart();
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
        String refresh_token = UserManager.getRefreshToken(this);
        if (userid == 0 || TextUtils.isEmpty(authorize) || TextUtils.isEmpty(refresh_token)) {
            mResult[0] = true;
            mResult[1] = false;
        } else {
            XlinkCloudManager.getInstance()
                             .refreshToken(userid, authorize, refresh_token, new XLinkTaskListener<UserApi.TokenRefreshResponse>() {
                                 @Override
                                 public void onError(XLinkCoreException e) {
                                    mResult[0] = true;
                                    mResult[1] = false;
                                 }

                                 @Override
                                 public void onStart() {

                                 }

                                 @Override
                                 public void onComplete(UserApi.TokenRefreshResponse tokenRefreshResponse) {
                                    mResult[0] = true;
                                    mResult[1] = true;
                                 }
                             });
        }
        mTimer = new CountDownTimer(1500, 500) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if (mResult[0]) {
                    if (mResult[1]) {
                        gotoMainActivity();
                    } else {
                        gotoLoginActivity();
                    }
                } else {
                    gotoMainActivity();
                }
            }
        };
        mTimer.start();
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
