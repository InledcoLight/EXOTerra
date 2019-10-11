package com.inledco.exoterra.device.socket;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.device.Monsoon.DeviceViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class SocketFragment extends BaseFragment {
    private TabLayout socket_tabs;
    private ViewPager socket_vp;

    private String[] mTitles;
    private List<Fragment> mFragments;
    private DeviceViewPagerAdapter mAdapter;

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
        return R.layout.fragment_socket;
    }

    @Override
    protected void initView(View view) {
        socket_tabs = view.findViewById(R.id.socket_tabs);
        socket_vp = view.findViewById(R.id.socket_vp);
    }

    @Override
    protected void initData() {
        mTitles = new String[2];
        mTitles[0] = "Control";
        mTitles[1] = "Timers";
        mFragments = new ArrayList<>();
        SocketControlFragment controlFragment = new SocketControlFragment();
        SocketTimersFragment timersFragment = new SocketTimersFragment();
        mFragments.add(controlFragment);
        mFragments.add(timersFragment);
        mAdapter = new DeviceViewPagerAdapter(getActivity().getSupportFragmentManager(), mTitles, mFragments);
        socket_vp.setAdapter(mAdapter);
        socket_tabs.setupWithViewPager(socket_vp);
    }

    @Override
    protected void initEvent() {

    }
}
