package com.liruya.exoterra.main.me;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.liruya.base.BaseFragment;
import com.liruya.exoterra.R;
import com.liruya.exoterra.splash.SplashActivity;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkRequestCallback;

import cn.xlink.restful.api.app.UserApi;
import cn.xlink.sdk.v5.manager.XLinkUserManager;
import cn.xlink.sdk.v5.module.main.XLinkSDK;

public class MeFragment extends BaseFragment {
    private Toolbar me_toolbar;
    private TextView me_tv_nickname;
    private TextView me_tv_email;
    private Button me_btn_logout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initData();
        initEvent();
        return view;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_me;
    }

    @Override
    protected void initView(View view) {
        me_toolbar = view.findViewById(R.id.me_toolbar);
        me_tv_nickname = view.findViewById(R.id.me_tv_nickname);
        me_tv_email = view.findViewById(R.id.me_tv_email);
        me_btn_logout = view.findViewById(R.id.me_btn_logout);

        me_toolbar.inflateMenu(R.menu.menu_me);
    }

    @Override
    protected void initData() {
        XlinkCloudManager.getInstance().getUserInfo(XLinkUserManager.getInstance()
                                                                    .getUid(), new XlinkRequestCallback<UserApi.UserInfoResponse>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "getUserInfoError: " + error );
            }

            @Override
            public void onSuccess(UserApi.UserInfoResponse response) {
                me_tv_nickname.setText(response.nickname);
                me_tv_email.setText(response.email);
            }
        });
    }

    @Override
    protected void initEvent() {
        me_btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XLinkSDK.logoutAndStop();
                relogin();
            }
        });
        me_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_me_msg:
                        getActivity().getSupportFragmentManager()
                                     .beginTransaction()
                                     .add(R.id.main_fl, new MessagesFragment())
                                     .addToBackStack("")
                                     .commit();
                        break;
                }
                return true;
            }
        });
    }

    private void relogin() {
        Intent intent = new Intent(getContext(), SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
