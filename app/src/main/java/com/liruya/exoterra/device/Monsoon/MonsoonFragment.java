package com.liruya.exoterra.device.Monsoon;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.liruya.base.BaseFragment;
import com.liruya.exoterra.R;

import java.util.ArrayList;
import java.util.List;

public class MonsoonFragment extends BaseFragment {

    private TabLayout monsoon_tabs;
    private ViewPager monsoon_vp;

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
        return R.layout.fragment_monsoon;
    }

    @Override
    protected void initView(View view) {
        monsoon_tabs = view.findViewById(R.id.monsoon_tabs);
        monsoon_vp = view.findViewById(R.id.monsoon_vp);
    }

    @Override
    protected void initData() {
        mTitles = new String[2];
        mTitles[0] = "Control";
        mTitles[1] = "Timers";
        mFragments = new ArrayList<>();
        MonsoonControlFragment controlFragment = new MonsoonControlFragment();
        MonsoonTimersFragment timersFragment = new MonsoonTimersFragment();
        mFragments.add(controlFragment);
        mFragments.add(timersFragment);
        mAdapter = new DeviceViewPagerAdapter(getActivity().getSupportFragmentManager(), mTitles, mFragments);
        monsoon_vp.setAdapter(mAdapter);
        monsoon_tabs.setupWithViewPager(monsoon_vp);
    }

    @Override
    protected void initEvent() {

    }
}
