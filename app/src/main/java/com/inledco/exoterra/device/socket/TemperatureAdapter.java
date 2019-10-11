package com.inledco.exoterra.device.socket;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.inledco.exoterra.R;

import java.util.List;

public class TemperatureAdapter extends FragmentStatePagerAdapter {
    private Context mContext;
    private List<Fragment> mFragments;

    public TemperatureAdapter(FragmentManager fm, Context context, List<Fragment> fragments) {
        super(fm);
        mContext = context;
        mFragments = fragments;
    }

    @Override
    public Fragment getItem(int i) {
        if (i < 0 || i >= getCount()) {
            return null;
        }
        return mFragments.get(i);
    }

    @Override
    public int getCount() {
        return 12;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (position < 0 || position >= 12) {
            return null;
        }
        return mContext.getResources().getStringArray(R.array.months)[position];
    }
}
