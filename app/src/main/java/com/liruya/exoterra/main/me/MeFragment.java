package com.liruya.exoterra.main.me;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.liruya.base.BaseFragment;
import com.liruya.exoterra.R;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkRequestCallback;

import cn.xlink.restful.api.app.UserApi;
import cn.xlink.sdk.v5.manager.XLinkUserManager;

public class MeFragment extends BaseFragment {

    private TextView me_tv_nickname;
    private TextView me_tv_email;

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
        me_tv_nickname = view.findViewById(R.id.me_tv_nickname);
        me_tv_email = view.findViewById(R.id.me_tv_email);
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

    }
}
