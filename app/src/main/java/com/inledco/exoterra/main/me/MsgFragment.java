package com.inledco.exoterra.main.me;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.common.SimpleAdapter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class MsgFragment<T> extends BaseFragment {
    protected SmartRefreshLayout msg_swipe_refresh;
    protected RecyclerView msg_rv_show;

    protected Comparator<T> mComparator;

    protected final List<T> mMessages = new ArrayList<>();
    protected SimpleAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initData();
        initEvent();
        return view;
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
        return R.layout.fragment_msg;
    }

    @Override
    protected void initView(View view) {
        msg_swipe_refresh = view.findViewById(R.id.msg_swipe_refresh);
        msg_rv_show = view.findViewById(R.id.msg_rv_show);
        msg_rv_show.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        initHeader();
        ClassicsHeader header = new ClassicsHeader(getContext());
        msg_swipe_refresh.setRefreshHeader(header);
    }

    @Override
    protected void initEvent() {
        msg_swipe_refresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                getMessages();
            }
        });
    }

    protected abstract void getMessages();
}
