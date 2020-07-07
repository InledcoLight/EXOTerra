package com.inledco.exoterra.splash;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.VideoView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.AliotServer;
import com.inledco.exoterra.base.BaseActivity;
import com.inledco.exoterra.home.HomeActivity;
import com.inledco.exoterra.login.LoginActivity;
import com.inledco.exoterra.manager.UserManager;
import com.inledco.exoterra.manager.UserPref;

public class SplashActivity extends BaseActivity {

    private VideoView splash_video;
    private boolean skipped;

    private AsyncTask<String, Void, Boolean> mAuthTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
        initEvent();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initView() {
        splash_video = findViewById(R.id.splash_video);

        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.exo_terra_animation;
        splash_video.setVideoPath(videoPath);
        splash_video.requestFocus();
        splash_video.start();
    }

    @Override
    protected void initData() {
        final String mEmail = UserPref.readEmail(SplashActivity.this);
        final String mPassword = UserPref.readPassword(SplashActivity.this);
        final String mUserid = UserPref.readUserId(SplashActivity.this);
        final String mToken = UserPref.readAccessToken(SplashActivity.this);
        final String mSecret = UserPref.readSecret(SplashActivity.this);

        mAuthTask = new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {
                while (!splash_video.isPlaying());
                boolean result = false;
                if (params != null && params.length == 5) {
                    final String email = params[0];
                    final String password = params[1];
                    final String userid = params[2];
                    final String token = params[3];
                    final String secret = params[4];
                    if (!TextUtils.isEmpty(userid) && !TextUtils.isEmpty(token) && !TextUtils.isEmpty(secret)) {
                        result = getUserInfo(userid, token);
                    }
                    if (!result && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                        result = login(email, password);
                    }
                    skipped = false;
                }
                while (splash_video.isPlaying()) {
                    if (skipped) {
                        splash_video.pause();
                        break;
                    }
                }
                return result;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (result) {
                    String userid = UserManager.getInstance().getUserid();
                    String token = UserManager.getInstance().getToken();
                    AliotServer.getInstance().init(userid, token);
                }
                gotoHomeActivity();
            }
        };
        mAuthTask.execute(mEmail, mPassword, mUserid, mToken, mSecret);
    }

    @Override
    protected void initEvent() {
        splash_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipped = true;
            }
        });
    }

    private boolean login(String email, String password) {
        String error = UserManager.getInstance().login(SplashActivity.this, email, password);
        return (error == null);
    }

    private boolean getUserInfo(String userid, String token) {
        return UserManager.getInstance().getUserInfo(userid, token);
    }

    private void gotoLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void  gotoHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
