package com.inledco.exoterra.main.me;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.login.LoginActivity;
import com.inledco.exoterra.manager.UserManager;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import cn.xlink.restful.api.app.UserApi;
import cn.xlink.sdk.v5.manager.XLinkUserManager;
import cn.xlink.sdk.v5.module.main.XLinkSDK;

public class MeFragment extends BaseFragment {
    private Toolbar me_toolbar;
    private ImageView me_icon_usr;
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
        me_icon_usr = view.findViewById(R.id.me_icon_usr);
        me_tv_nickname = view.findViewById(R.id.me_tv_nickname);
        me_tv_email = view.findViewById(R.id.me_tv_email);
        me_btn_logout = view.findViewById(R.id.me_btn_logout);

        me_toolbar.inflateMenu(R.menu.menu_me);
        boolean login = XLinkUserManager.getInstance().isUserAuthorized();;
        me_toolbar.getMenu().findItem(R.id.menu_me_msg).setVisible(login);
        me_tv_nickname.setVisibility(login && !TextUtils.isEmpty(UserManager.getNickname()) ? View.VISIBLE : View.GONE);
        me_tv_nickname.setText(UserManager.getNickname());
        if (login) {
            me_tv_email.setText(UserManager.getEmail());
        }
        me_btn_logout.setVisibility(login ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void initData() {
        if (XLinkUserManager.getInstance().isUserAuthorized() && TextUtils.isEmpty(UserManager.getEmail())) {
            XlinkCloudManager.getInstance()
                             .getUserInfo(XLinkUserManager.getInstance()
                                                          .getUid(), new XlinkRequestCallback<UserApi.UserInfoResponse>() {
                                 @Override
                                 public void onStart() {

                                 }

                                 @Override
                                 public void onError(String error) {
                                     Log.e(TAG, "getUserInfoError: " + error);
                                 }

                                 @Override
                                 public void onSuccess(UserApi.UserInfoResponse response) {
                                     Log.e(TAG, "onSuccess: " + response.account);
                                     UserManager.setEmail(response.email);
                                     UserManager.setNickname(response.nickname);
                                     me_tv_nickname.setText(response.nickname);
                                     me_tv_email.setText(response.email);
                                     me_tv_nickname.setVisibility(TextUtils.isEmpty(response.nickname) ? View.GONE : View.VISIBLE);
                                 }
                             });
        }
    }

    @Override
    protected void initEvent() {
        me_icon_usr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (XLinkUserManager.getInstance().isUserAuthorized()) {

                } else {
                    login();
                    getActivity().finish();
                }
            }
        });
        me_btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int usrid = UserManager.getUserId(getContext());
                Log.e(TAG, "onUserLogout: " + usrid);
                XlinkCloudManager.getInstance().unregisterFCMMessageService(usrid, new XlinkRequestCallback<String>() {
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "onError: " + error);
                    }

                    @Override
                    public void onSuccess(String s) {
                        Log.e(TAG, "onSuccess: " + s);
                    }
                });
                XLinkSDK.logoutAndStop();
                logout();
            }
        });
        me_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_me_msg:
                        addFragmentToStack(R.id.main_fl, new MessagesFragment());
                        break;
                }
                return true;
            }
        });
    }

    private void logout() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void login() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
    }
}
