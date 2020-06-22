package com.inledco.exoterra.main.groups;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.login.LoginActivity;

public class GroupsLoginFragment extends BaseFragment {

    private Button groups_login_btn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initData();
        initEvent();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult: " + requestCode + " " + resultCode);
        if (requestCode == 1) {

        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_groups_login;
    }

    @Override
    protected void initView(View view) {
        groups_login_btn = view.findViewById(R.id.groups_login_btn);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initEvent() {
        groups_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoginActivity();
            }
        });
    }

    private void startLoginActivity() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        getActivity().startActivityForResult(intent, 1);
    }
}
