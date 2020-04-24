package com.inledco.exoterra.main.groups;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.common.OnItemClickListener;
import com.inledco.exoterra.event.GroupChangedEvent;
import com.inledco.exoterra.event.GroupsRefreshedEvent;
import com.inledco.exoterra.group.GroupFragment;
import com.inledco.exoterra.manager.GroupManager;
import com.inledco.exoterra.manager.OnErrorCallback;
import com.inledco.exoterra.util.FavouriteUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DashboardFragment extends BaseFragment {
    private TextView groups_title;
    private SwipeRefreshLayout groups_refresh;
    private View groups_warning;
    private TextView warning_tv_msg;
    private RecyclerView groups_rv;
    private ImageButton groups_ib_add;

    private Set<String> mFavourites;
    private final List<Group> mFavouriteGroups = new ArrayList<>();

    private final List<Group> mGroups = new ArrayList<>();
    private DashboardAdapter mAdapter;

    private final OnErrorCallback mCallback = new OnErrorCallback() {
        @Override
        public void onError(String error) {
            stopRefresh();
        }
    };

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
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGroupsRefreshedEvent(GroupsRefreshedEvent event) {
        mGroups.clear();
        mGroups.addAll(GroupManager.getInstance().getAllGroups());
        mFavouriteGroups.clear();
        mFavourites = FavouriteUtil.getFavourites(getContext());
        for (int i = 0; i < mGroups.size(); i++) {
            Group group = mGroups.get(i);
            if (mFavourites.contains(group.groupid)) {
                mFavouriteGroups.add(group);
            }
        }
        groups_warning.setVisibility(mFavouriteGroups.size() == 0 ? View.VISIBLE : View.GONE);
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        groups_refresh.setRefreshing(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGroupChangedEvent(GroupChangedEvent event) {
        if (event == null || mAdapter == null) {
            return;
        }
        for (int i = 0; i < mFavouriteGroups.size(); i++) {
            if (TextUtils.equals(event.getGroupid(), mFavouriteGroups.get(i).groupid)) {
                mAdapter.notifyItemChanged(i);
                return;
            }
        }
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
        groups_rv.setLayoutManager(new GridLayoutManager(getContext(), 4));
    }

    @Override
    protected void initData() {
        groups_title.setVisibility(View.VISIBLE);
        mGroups.addAll(GroupManager.getInstance().getAllGroups());
        mFavourites = FavouriteUtil.getFavourites(getContext());
        for (int i = 0; i < mGroups.size(); i++) {
            Group group = mGroups.get(i);
            if (mFavourites.contains(group.groupid)) {
                mFavouriteGroups.add(group);
            }
        }
        mAdapter = new DashboardAdapter(getContext(), mFavouriteGroups);

        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Group group = mFavouriteGroups.get(position);
                String groupid = group.groupid;
                String name = group.name;
                addFragmentToStack(R.id.main_fl, GroupFragment.newInstance(groupid, name));
            }
        });
        groups_rv.setAdapter(mAdapter);

        if (GroupManager.getInstance().needSynchronize()) {
            groups_refresh.setRefreshing(true);
            GroupManager.getInstance().getGroups(mCallback);
        }
    }

    @Override
    protected void initEvent() {
        groups_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GroupManager.getInstance().getGroups(mCallback);
            }
        });

        groups_ib_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragmentToStack(R.id.main_fl, AddHabitatFragment.newInstance(true));
            }
        });
    }

    private void stopRefresh() {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groups_refresh.setRefreshing(false);
            }
        });
    }
}
