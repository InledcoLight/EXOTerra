package com.inledco.exoterra.main.groups;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.Home;
import com.inledco.exoterra.event.HomeChangedEvent;
import com.inledco.exoterra.event.HomeDeviceChangedEvent;
import com.inledco.exoterra.event.HomePropertyChangedEvent;
import com.inledco.exoterra.event.HomesRefreshedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GroupsFragment extends BaseFragment {
    private TextView groups_title;
    private SwipeRefreshLayout groups_refresh;
    private View groups_warning;
    private TextView warning_tv_msg;
    private RecyclerView groups_rv;
    private ImageButton groups_ib_add;

    private boolean showOnlyFavourite;
    private Set<String> mFavourites;
    private final List<Home> mFavouriteHomes = new ArrayList<>();

    private List<Home> mHomes;
    private GroupsAdapter mAdapter;

    private static final String KEY_ONLY_FAVOURITE = "only_favourite";

    private final BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_TIME_TICK:
                    mAdapter.updateTime();
                    break;
            }
        }
    };

    public static GroupsFragment newInstance(final boolean onlyFavourite) {
        Bundle args = new Bundle();
        args.putBoolean(KEY_ONLY_FAVOURITE, onlyFavourite);
        GroupsFragment fragment = new GroupsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        EventBus.getDefault().register(this);
        initData();
        initEvent();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(mTimeReceiver);
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHomesRefreshedEvent(HomesRefreshedEvent event) {
//        groups_refresh.setRefreshing(false);
//        if (showOnlyFavourite) {
//            mFavouriteHomes.clear();
//            for (int i = 0; i < mHomes.size(); i++) {
//                Home home = mHomes.get(i);
//                if (mFavourites.contains(home.getHome().id)) {
//                    mFavouriteHomes.add(home);
//                }
//            }
//            groups_warning.setVisibility(mFavouriteHomes.size() == 0 ? View.VISIBLE : View.GONE);
//        } else {
//            groups_warning.setVisibility(mHomes.size() == 0 ? View.VISIBLE : View.GONE);
//        }
//        mAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHomeChangedEvent(HomeChangedEvent event) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHomePropertyChangedEvent(HomePropertyChangedEvent event) {
//        for (int i = 0; i < mHomes.size(); i++) {
//            if (TextUtils.equals(event.getHomeid(), mHomes.get(i).getHome().id)) {
//                mAdapter.updateData(i);
//            }
//        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHomeDeviceChangedEvent(HomeDeviceChangedEvent event) {
//        for (int i = 0; i < mHomes.size(); i++) {
//            if (TextUtils.equals(event.getHomeid(), mHomes.get(i).getHome().id)) {
//                mAdapter.updateData(i);
//            }
//        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_groups;
    }

    @Override
    protected void initView(View view) {
        groups_title = view.findViewById(R.id.groups_title);
        groups_refresh = view.findViewById(R.id.groups_refresh);
        groups_warning = view.findViewById(R.id.groups_warning);
        warning_tv_msg = groups_warning.findViewById(R.id.warning_tv_msg);
        groups_rv = view.findViewById(R.id.groups_rv);
        groups_ib_add = view.findViewById(R.id.groups_ib_add);

        warning_tv_msg.setText(R.string.no_habitat_warning);
    }

    @Override
    protected void initData() {
        Bundle args = getArguments();
        if (args != null) {
            showOnlyFavourite = args.getBoolean(KEY_ONLY_FAVOURITE);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        getActivity().registerReceiver(mTimeReceiver, filter);

//        mHomes = HomeManager.getInstance().getHomeList();
//        if (!showOnlyFavourite) {
//            mAdapter = new GroupsAdapter(getContext(), mHomes);
//        } else {
//            groups_title.setVisibility(View.VISIBLE);
//            mFavourites = FavouriteUtil.getFavourites(getContext());
//            for (int i = 0; i < mHomes.size(); i++) {
//                Home home = mHomes.get(i);
//                if (mFavourites.contains(home.getHome().id)) {
//                    mFavouriteHomes.add(home);
//                }
//            }
//            mAdapter = new GroupsAdapter(getContext(), mFavouriteHomes);
//        }
//
//        mAdapter.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(int position) {
//                HomeApi.HomesResponse.Home home;
//                if (showOnlyFavourite) {
//                    home = mFavouriteHomes.get(position).getHome();
//                } else {
//                    home = mHomes.get(position).getHome();
//                }
//                String homeid = home.id;
//                String homename = home.name;
//                addFragmentToStack(R.id.main_fl, GroupFragment.newInstance(homeid, homename));
//            }
//        });
//        groups_rv.setAdapter(mAdapter);
//        groups_refresh.setRefreshing(true);
//        HomeManager.getInstance().refreshHomeList(null);
    }

    @Override
    protected void initEvent() {
//        groups_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                HomeManager.getInstance().refreshHomeList(null);
//            }
//        });
//
//        groups_ib_add.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                addFragmentToStack(R.id.main_fl, AddHabitatFragment.newInstance(showOnlyFavourite));
//            }
//        });
    }
}
