package com.inledco.exoterra.main.homes;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.inledco.exoterra.AppConstants;
import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import cn.xlink.restful.XLinkRestfulEnum;

public class HomeMemberFragment extends BaseFragment {
    private Toolbar home_member_toolbar;
    private ImageView home_member_icon;
    private TextView home_member_name;
    private TextView home_member_usrid;
    private TextView home_member_email;
    private TextView home_member_role;
    private Button home_member_delete;

    private boolean mIsHomeAdmin;
    private String mHomeId;
    private int mUserId;
    private int mRole;
    private String mNickname;
    private String mEmail;

    public static HomeMemberFragment newInstance(final boolean isHomeAdmin, final String homeid, final int userid, final int role, final String nickname, final String email) {
        Bundle args = new Bundle();
        args.putBoolean(AppConstants.IS_HOME_ADMIN, isHomeAdmin);
        args.putString(AppConstants.HOME_ID, homeid);
        args.putInt(AppConstants.USER_ID, userid);
        args.putString(AppConstants.EMAIL, email);
        args.putInt(AppConstants.ROLE, role);
        args.putString(AppConstants.NICKNAME, nickname);
        HomeMemberFragment fragment = new HomeMemberFragment();
        fragment.setArguments(args);
        return fragment;
    }

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
        return R.layout.fragment_home_member;
    }

    @Override
    protected void initView(View view) {
        home_member_toolbar = view.findViewById(R.id.home_member_toolbar);
        home_member_icon = view.findViewById(R.id.home_member_icon);
        home_member_name = view.findViewById(R.id.home_member_name);
        home_member_usrid = view.findViewById(R.id.home_member_usrid);
        home_member_email = view.findViewById(R.id.home_member_email);
        home_member_role = view.findViewById(R.id.home_member_role);
        home_member_delete = view.findViewById(R.id.home_member_delete);
    }

    @Override
    protected void initData() {
        Bundle args = getArguments();
        if (args != null) {
            mIsHomeAdmin = args.getBoolean(AppConstants.IS_HOME_ADMIN);
            mHomeId = args.getString(AppConstants.HOME_ID);
            mUserId = args.getInt(AppConstants.USER_ID);
            mRole = args.getInt(AppConstants.ROLE);
            mNickname = args.getString(AppConstants.NICKNAME);
            mEmail = args.getString(AppConstants.EMAIL);
            if (TextUtils.isEmpty(mNickname)) {
                mNickname = "" + mUserId;
            }

            home_member_toolbar.setTitle(mNickname);
            int suAdmin = XLinkRestfulEnum.HomeUserType.SUPER_ADMIN.getValue();
            int admin = XLinkRestfulEnum.HomeUserType.ADMIN.getValue();
            if (mRole == suAdmin || mRole == admin) {
                home_member_icon.setImageResource(R.drawable.ic_admin_white_64dp);
                home_member_role.setText(R.string.admin);
            } else if (mIsHomeAdmin) {
                home_member_delete.setVisibility(View.VISIBLE);
            }
            home_member_name.setText(mNickname);
            home_member_usrid.setText("" + mUserId);
            home_member_email.setText(mEmail);
        }
    }

    @Override
    protected void initEvent() {
        home_member_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        home_member_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XlinkCloudManager.getInstance().deleteHomeUser(mHomeId, mUserId, new XlinkRequestCallback<String>() {
                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                             .show();
                    }

                    @Override
                    public void onSuccess(String s) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                });
            }
        });
    }
}
