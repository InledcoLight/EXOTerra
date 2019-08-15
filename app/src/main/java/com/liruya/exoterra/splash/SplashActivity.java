package com.liruya.exoterra.splash;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.VideoView;

import com.liruya.base.BaseFullscreenActivity;
import com.liruya.exoterra.R;
import com.liruya.exoterra.login.LoginActivity;
import com.liruya.exoterra.main.MainActivity;
import com.liruya.exoterra.manager.UserManager;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkTaskHandler;

import cn.xlink.restful.api.app.UserApi;
import cn.xlink.sdk.v5.module.main.XLinkSDK;

public class SplashActivity extends BaseFullscreenActivity {

    private VideoView splash_vv;

    private boolean mPause;
    private int mProgress;
    private boolean mVideoCompleted;
    private final XlinkTaskHandler<UserApi.TokenRefreshResponse> mAuthinListener = new XlinkTaskHandler<>();
    private Runnable mRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mVideoCompleted && mPause) {
            Log.e(TAG, "onResume: " + mProgress);
            splash_vv.seekTo(mProgress);
            splash_vv.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!mVideoCompleted) {
            Log.e(TAG, "onPause: " + splash_vv.canPause() + "  " + splash_vv.getCurrentPosition() + "  " + splash_vv.getDuration());
            splash_vv.pause();
            mPause = true;
            mProgress = splash_vv.getCurrentPosition();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        splash_vv.stopPlayback();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initView() {
        splash_vv = findViewById(R.id.splash_vv);
    }

    @Override
    protected void initData() {
        Log.e(TAG, "initData: ");
        XLinkSDK.start();

        int userid = UserManager.getUserId(this);
        String authorize = UserManager.getAuthorize(this);
        final String refresh_token = UserManager.getRefreshToken(this);
        if (!UserManager.checkAuthorize(userid, authorize, refresh_token)) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    while (!mVideoCompleted);
                    gotoLoginActivity();
                }
            };
        } else {
            XlinkCloudManager.getInstance()
                             .refreshToken(userid, authorize, refresh_token, mAuthinListener);
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    while (!mVideoCompleted || !mAuthinListener.isOver());
                    if (mAuthinListener.isSuccess()) {
                        gotoMainActivity();
                    } else {
                        gotoLoginActivity();
                    }
                }
            };
        }

        splash_vv.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mVideoCompleted = true;
                mPause = false;
                return false;
            }
        });
        splash_vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.e(TAG, "onPrepared: " );
                mVideoCompleted = false;
                mPause = false;
                splash_vv.requestFocus();
                splash_vv.seekTo(0);
                splash_vv.start();
            }
        });
        splash_vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mVideoCompleted = true;
                mPause = false;
            }
        });
        splash_vv.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.splash);
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
