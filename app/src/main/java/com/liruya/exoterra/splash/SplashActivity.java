package com.liruya.exoterra.splash;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.liruya.base.BaseActivity;
import com.liruya.exoterra.R;
import com.liruya.exoterra.login.LoginActivity;
import com.liruya.exoterra.main.MainActivity;
import com.liruya.exoterra.manager.UserManager;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkTaskCallback;

import cn.xlink.restful.api.app.UserApi;
import cn.xlink.sdk.v5.module.main.XLinkSDK;

public class SplashActivity extends BaseActivity {

    private boolean[] mResult = new boolean[]{false, false};
    private long mStartTime;

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
        final String refresh_token = UserManager.getRefreshToken(this);
        if (userid == 0 || TextUtils.isEmpty(authorize) || TextUtils.isEmpty(refresh_token)) {
            mResult[0] = true;
            mResult[1] = false;
        } else {
            XlinkCloudManager.getInstance()
                             .refreshToken(userid, authorize, refresh_token, new XlinkTaskCallback<UserApi.TokenRefreshResponse>() {
                                 @Override
                                 public void onError(String error) {
                                                    mResult[0] = true;
                                     mResult[1] = false;
                                     Toast.makeText(SplashActivity.this, error, Toast.LENGTH_SHORT)
                                          .show();
                                 }

                                 @Override
                                 public void onStart() {

                                 }

                                 @Override
                                 public void onComplete(UserApi.TokenRefreshResponse response) {
                                     mResult[0] = true;
                                     mResult[1] = true;
                                 }
                             });
        }
        mStartTime = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mResult[0] == false || System.currentTimeMillis() - mStartTime < 1500);
                if (mResult[1]) {
                    gotoMainActivity();
                } else {
                    gotoLoginActivity();
                }
            }
        }).start();
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
