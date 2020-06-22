package com.inledco.exoterra.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliyun.alink.linkkit.api.ILinkKitConnectListener;
import com.aliyun.alink.linksdk.tools.AError;
import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.AliotClient;
import com.inledco.exoterra.base.BaseActivity;
import com.inledco.exoterra.main.MainActivity;
import com.inledco.exoterra.manager.UserManager;
import com.inledco.exoterra.push.FCMMessageService;
import com.inledco.exoterra.push.FCMTokenListener;

public class HomeActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout home_ll_microtope;
    private LinearLayout home_ll_uvb;
    private LinearLayout home_ll_restore;
    private TextView home_tv_newprdt;
    private TextView home_tv_events;
    private TextView home_tv_sheets;
    private TextView home_tv_exotv;
    private ImageButton home_ib_facebook;
    private ImageButton home_ib_twitter;
    private ImageButton home_ib_instagram;
    private ImageButton home_ib_youtube;
    private ImageButton home_ib_email;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
        initEvent();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dismissLoadDialog();
        AliotClient.getInstance().deinit();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_home;
    }

    @Override
    protected void initView() {
        home_ll_microtope = findViewById(R.id.home_ll_microtope);
        home_ll_uvb = findViewById(R.id.home_ll_uvb);
        home_ll_restore = findViewById(R.id.home_ll_restore);
        home_tv_newprdt = findViewById(R.id.home_tv_newprdt);
        home_tv_events = findViewById(R.id.home_tv_events);
        home_tv_sheets = findViewById(R.id.home_tv_sheets);
        home_tv_exotv = findViewById(R.id.home_tv_exotv);
        home_ib_facebook = findViewById(R.id.home_ib_facebook);
        home_ib_twitter = findViewById(R.id.home_ib_twitter);
        home_ib_instagram = findViewById(R.id.home_ib_instagram);
        home_ib_youtube = findViewById(R.id.home_ib_youtube);
        home_ib_email = findViewById(R.id.home_ib_email);

        home_tv_newprdt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add_white_24dp, 0);
        home_tv_events.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add_white_24dp, 0);
        home_tv_sheets.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add_white_24dp, 0);
        home_tv_exotv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add_white_24dp, 0);
    }

    @Override
    protected void initData() {
        FCMMessageService.syncToken(new FCMTokenListener() {
            @Override
            public void onTokenResult(String token) {
                Log.e(TAG, "onTokenResult: " + token);
            }
        });
    }

    @Override
    protected void initEvent() {
        home_ll_microtope.setOnClickListener(this);
        home_ll_uvb.setOnClickListener(this);
        home_ll_restore.setOnClickListener(this);
        home_tv_newprdt.setOnClickListener(this);
        home_tv_events.setOnClickListener(this);
        home_tv_sheets.setOnClickListener(this);
        home_tv_exotv.setOnClickListener(this);
        home_ib_facebook.setOnClickListener(this);
        home_ib_twitter.setOnClickListener(this);
        home_ib_instagram.setOnClickListener(this);
        home_ib_youtube.setOnClickListener(this);
        home_ib_email.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        boolean authorized = UserManager.getInstance().isAuthorized();
        switch (v.getId()) {
            case R.id.home_ll_microtope:
                if (authorized) {
                    String userid = UserManager.getInstance().getUserid();
                    String secret = UserManager.getInstance().getSecret();
                    boolean result = AliotClient.getInstance().init(getApplicationContext(), userid, secret, new ILinkKitConnectListener() {
                        @Override
                        public void onError(AError aError) {
                            dismissLoadDialog();
                            showToast(aError.getMsg());
                        }

                        @Override
                        public void onInitDone(Object o) {
                            dismissLoadDialog();
                            startMainActivity();
                        }
                    });
                    if (result) {
                        showLoadDialog();
                    }
                } else {
                    startMainActivity();
                }
                break;
            case R.id.home_ll_uvb:

                break;
            case R.id.home_ll_restore:

                break;
            case R.id.home_tv_newprdt:
                addFragmentToStack(R.id.home_root, new NewProductsFragment());
                break;
            case R.id.home_tv_events:

                break;
            case R.id.home_tv_sheets:

                break;
            case R.id.home_tv_exotv:

                break;
            case R.id.home_ib_facebook:

                break;
            case R.id.home_ib_twitter:

                break;
            case R.id.home_ib_instagram:

                break;
            case R.id.home_ib_youtube:

                break;
            case R.id.home_ib_email:
//                OKHttpManager.getInstance().get(CloudApi.test(), null, new Callback() {
//                    @Override
//                    public void onFailure(Call call, IOException e) {
//                        Log.e(TAG, "onFailure: " + e.getMessage());
//                    }
//
//                    @Override
//                    public void onResponse(Call call, Response response) throws IOException {
//                        Log.e(TAG, "onResponse: " + response.body().string());
//                    }
//                });
                break;
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
