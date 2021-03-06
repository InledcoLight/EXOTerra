package com.inledco.exoterra.main.me;

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
import com.inledco.exoterra.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class MessagesFragment extends BaseFragment {
    private Toolbar message_toolbar;
    private TabLayout message_tab;
    private ViewPager message_vp;
//    private TabLayout.OnTabSelectedListener mListener = new TabLayout.OnTabSelectedListener() {
//        @Override
//        public void onTabSelected(TabLayout.Tab tab) {
//            switch (tab.getPosition()) {
//                case 0:
//                    replaceFragment(R.id.messages_fl, InviteMsgFragment.newInstance(false));
//                    break;
//                case 1:
//                    replaceFragment(R.id.messages_fl, InviteMsgFragment.newInstance(true));
//                    break;
//            }
//        }
//
//        @Override
//        public void onTabUnselected(TabLayout.Tab tab) {
//
//        }
//
//        @Override
//        public void onTabReselected(TabLayout.Tab tab) {
//
//        }
//    };

    private String[] mTitles;
    private final List<Fragment> mFragments = new ArrayList<>();
    private MessagesAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initData();
        initEvent();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        message_tab.removeOnTabSelectedListener(mListener);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_messages;
    }

    @Override
    protected void initView(View view) {
        message_toolbar = view.findViewById(R.id.messages_toolbar);
        message_tab = view.findViewById(R.id.messages_tab);
        message_vp = view.findViewById(R.id.messages_vp);
    }

    @Override
    protected void initData() {
        InviteMsgFragment recvInviteFragment = InviteMsgFragment.newInstance(false);
        InviteMsgFragment sentInviteFragment = InviteMsgFragment.newInstance(true);
        mFragments.add(recvInviteFragment);
        mFragments.add(sentInviteFragment);
        mTitles = new String[] {getString(R.string.received), getString(R.string.sent)};
        mAdapter = new MessagesAdapter(getActivity().getSupportFragmentManager(), mFragments, mTitles);
        message_vp.setAdapter(mAdapter);
        message_tab.setupWithViewPager(message_vp);
    }

    @Override
    protected void initEvent() {
        message_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

//        message_tab.addOnTabSelectedListener(mListener);
    }

    private class MessagesAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> mFragments;
        private String[] mTitles;

        public MessagesAdapter(FragmentManager fm, List<Fragment> fragments, String[] titles) {
            super(fm);
            if (fragments == null || titles == null || fragments.size() != titles.length) {
                throw new RuntimeException("Invalid arguments");
            }
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
            return mTitles[position];
        }
    }
}
