package com.inledco.exoterra.main.me;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.base.SimpleAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class MsgFragment<T> extends BaseFragment {
    protected SwipeRefreshLayout msg_swipe_refresh;
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

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_msg;
    }

    @Override
    protected void initView(View view) {
        msg_swipe_refresh = view.findViewById(R.id.msg_swipe_refresh);
        msg_rv_show = view.findViewById(R.id.msg_rv_show);
        msg_rv_show.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void initEvent() {
        msg_swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMessages();
            }
        });
    }

    protected abstract void getMessages();
}
