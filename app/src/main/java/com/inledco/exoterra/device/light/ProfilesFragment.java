package com.inledco.exoterra.device.light;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.LightViewModel;
import com.inledco.exoterra.base.BaseFragment;

import java.util.List;

public class ProfilesFragment extends BaseFragment {
    private Toolbar profiles_toolbar;
    private TabLayout profiles_tab;
    private ViewPager profiles_vp;

    private LightViewModel mLightViewModel;
    private List<Fragment> mFragments;
    private String[] mProfileNames;
    private ProfileAdapter mAdapter;

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
        return R.layout.fragment_profiles;
    }

    @Override
    protected void initView(View view) {
        profiles_toolbar = view.findViewById(R.id.profiles_toolbar);
        profiles_tab = view.findViewById(R.id.profiles_tab);
        profiles_vp = view.findViewById(R.id.profiles_vp);
    }

    @Override
    protected void initData() {
//        mFragments = new ArrayList<>();
//        mProfileNames = new String[13];
//        mLightViewModel = ViewModelProviders.of(getActivity()).get(LightViewModel.class);
//
//        for (int i = 0; i < 13; i++) {
//            mFragments.add(ProfileFragment.newInstance(i));
//            mProfileNames[i] = mLightViewModel.getData().getProfileName(i);
//        }
//        mAdapter = new ProfileAdapter(getActivity().getSupportFragmentManager(), mFragments, mProfileNames);
//        profiles_vp.setAdapter(mAdapter);
//        profiles_tab.setupWithViewPager(profiles_vp);
//        int select = mLightViewModel.getData().getSelectProfile();
//        if (select >= 0 && select <= 12) {
//            profiles_vp.setCurrentItem(select);
//        }
    }

    @Override
    protected void initEvent() {
        profiles_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    class ProfileAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> mFragments;
        private String[] mTitles;

        public ProfileAdapter(FragmentManager fm, List<Fragment> fragments, String[] titles) {
            super(fm);
            mFragments = fragments;
            mTitles = titles;
        }

        @Override
        public Fragment getItem(int i) {
            return mFragments.get(i);
        }

        @Override
        public int getCount() {
            return mFragments == null ? 0 : mFragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if (position < getCount() && mTitles != null && mTitles.length == getCount()) {
                return mTitles[position];
            }
            return "";
        }
    }
}
