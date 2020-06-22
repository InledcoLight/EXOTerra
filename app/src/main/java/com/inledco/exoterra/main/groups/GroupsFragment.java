package com.inledco.exoterra.main.groups;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.ADevice;
import com.inledco.exoterra.aliot.AliotConsts;
import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.common.OnItemClickListener;
import com.inledco.exoterra.event.GroupChangedEvent;
import com.inledco.exoterra.event.GroupDeviceChangedEvent;
import com.inledco.exoterra.event.GroupsRefreshedEvent;
import com.inledco.exoterra.group.GroupFragment;
import com.inledco.exoterra.manager.GroupManager;
import com.inledco.exoterra.manager.OnErrorCallback;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class GroupsFragment extends BaseFragment {
    private TextView groups_title;
    private SmartRefreshLayout groups_refresh;
    private View groups_warning;
    private TextView warning_tv_msg;
    private RecyclerView groups_rv;
    private ImageButton groups_ib_add;

    private final List<Group> mGroups = new ArrayList<>();
    private GroupsAdapter mAdapter;

    private final OnErrorCallback mCallback = new OnErrorCallback() {
        @Override
        public void onError(String error) {
            groups_refresh.finishRefresh(1000, false, false);
        }
    };

    private final BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_TIME_TICK:
                    if (mAdapter != null) {
                        mAdapter.updateTime();
                    }
                    break;
            }
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
        getActivity().unregisterReceiver(mTimeReceiver);
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGroupsRefreshedEvent(GroupsRefreshedEvent event) {
        mGroups.clear();
        mGroups.addAll(GroupManager.getInstance().getAllGroups());
        groups_warning.setVisibility(mGroups.size() == 0 ? View.VISIBLE : View.GONE);
        if (mAdapter != null) {
            mAdapter.refreshData();
        }
        groups_refresh.finishRefresh(500);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGroupChangedEvent(GroupChangedEvent event) {
        if (event == null || mAdapter == null) {
            return;
        }
        for (int i = 0; i < mGroups.size(); i++) {
            if (TextUtils.equals(event.getGroupid(), mGroups.get(i).groupid)) {
                mAdapter.notifyItemChanged(i);
                return;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGroupDeviceChangedEvent(GroupDeviceChangedEvent event) {
        if (event == null) {
            return;
        }
        for (int i = 0; i < mGroups.size(); i++) {
            if (TextUtils.equals(event.getGroupid(), mGroups.get(i).groupid)) {
                mAdapter.updateData(i);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDevicePropertyChangedEvent(ADevice adev) {
        if (adev == null) {
            return;
        }
        final String pkey = adev.getProductKey();
        if (TextUtils.equals(pkey, AliotConsts.PRODUCT_KEY_EXOSOCKET) == false) {
            return;
        }
        Group group = GroupManager.getInstance().getDeviceGroup(pkey, adev.getDeviceName());
        if (group == null) {
            return;
        }
        for (int i = 0; i < mGroups.size(); i++) {
            if (TextUtils.equals(group.groupid, mGroups.get(i).groupid)) {
                mAdapter.updateData(i);
            }
        }
    }

    private void initHeader() {
        ClassicsHeader.REFRESH_HEADER_LOADING = getString(R.string.loading);
        ClassicsHeader.REFRESH_HEADER_PULLING = getString(R.string.pulldown_to_refresh);
        ClassicsHeader.REFRESH_HEADER_RELEASE = getString(R.string.release_to_refresh);
        ClassicsHeader.REFRESH_HEADER_REFRESHING = getString(R.string.refreshing);
        ClassicsHeader.REFRESH_HEADER_FAILED = getString(R.string.refresh_failed);
        ClassicsHeader.REFRESH_HEADER_FINISH = getString(R.string.refresh_success);
        ClassicsHeader.REFRESH_HEADER_UPDATE = getString(R.string.last_update);
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
        initHeader();
        ClassicsHeader header = new ClassicsHeader(getContext());
        groups_refresh.setRefreshHeader(header);
    }

    @Override
    protected void initData() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        getActivity().registerReceiver(mTimeReceiver, filter);

        mGroups.addAll(GroupManager.getInstance().getAllGroups());
        mAdapter = new GroupsAdapter(getContext(), mGroups);

        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Group group = mGroups.get(position);
                String groupid = group.groupid;
                String name = group.name;
                addFragmentToStack(R.id.main_fl, GroupFragment.newInstance(groupid, name));
            }
        });
        groups_rv.setAdapter(mAdapter);

        if (GroupManager.getInstance().needSynchronize()) {
            GroupManager.getInstance().getGroups(mCallback);
        }
        if (GroupManager.getInstance().isSynchronizing() && !GroupManager.getInstance().isSynchronized()) {
            groups_refresh.autoRefresh();
        } else {
            groups_warning.setVisibility(mGroups.size() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void initEvent() {
        groups_refresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                GroupManager.getInstance().getGroups(mCallback);
            }
        });

        groups_ib_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragmentToStack(R.id.main_fl, AddHabitatFragment.newInstance(false));
            }
        });
    }
}
