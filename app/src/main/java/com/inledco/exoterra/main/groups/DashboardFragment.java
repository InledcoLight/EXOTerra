package com.inledco.exoterra.main.groups;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.inledco.exoterra.util.SizeUtil;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DashboardFragment extends BaseFragment {
    private TextView groups_title;
    private SmartRefreshLayout groups_refresh;
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
            groups_refresh.finishRefresh(1000, false, false);
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
        groups_refresh.finishRefresh(500);
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
        groups_rv.setLayoutManager(new GridLayoutManager(getContext(), 3));
        groups_rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int spanCount = 3;
                int space = SizeUtil.dp2px(8);
                int position = parent.getChildAdapterPosition(view);
                int col = position%spanCount;

                outRect.left = space*col/spanCount;
                outRect.right = space*(spanCount-col-1)/spanCount;
                if (position >= spanCount) {
                    outRect.top = space;
                }
            }
        });
        initHeader();
        ClassicsHeader header = new ClassicsHeader(getContext());
        groups_refresh.setRefreshHeader(header);
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
            GroupManager.getInstance().getGroups(mCallback);
        }
        if (GroupManager.getInstance().isSynchronizing() && !GroupManager.getInstance().isSynchronized()) {
            groups_refresh.autoRefresh();
        } else {
            groups_warning.setVisibility(mFavouriteGroups.size() == 0 ? View.VISIBLE : View.GONE);
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
                addFragmentToStack(R.id.main_fl, AddHabitatFragment.newInstance(true));
            }
        });
    }
}
