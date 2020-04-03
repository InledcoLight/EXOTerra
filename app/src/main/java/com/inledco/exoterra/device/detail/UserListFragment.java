package com.inledco.exoterra.device.detail;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;

public class UserListFragment extends BaseFragment {
    private Toolbar frag_toolbar;
    private SwipeRefreshLayout frag_swipe;
    private RecyclerView frag_rv;

    private int mDeviceId;

//    private final List<DeviceApi.DeviceSubscribeUsersResponse.UserBean> mUsers = new ArrayList<>();
    private DeviceUserAdapter mAdapter;

    public static UserListFragment newInstance(final int devid) {
        Bundle args = new Bundle();
        args.putInt("deviceid", devid);
        UserListFragment fragment = new UserListFragment();
        fragment.setArguments(args);
        return fragment;
    }

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
        return R.layout.fragment_toolbar_recyclerview;
    }

    @Override
    protected void initView(View view) {
        frag_toolbar = view.findViewById(R.id.frag_toolbar);
        frag_swipe = view.findViewById(R.id.frag_swipe);
        frag_rv = view.findViewById(R.id.frag_rv);

        frag_toolbar.setTitle(R.string.device_users);
        frag_rv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void initData() {
//        mAdapter = new DeviceUserAdapter(getContext(), mUsers);
//        frag_rv.setAdapter(mAdapter);
//
//        Bundle args = getArguments();
//        if (args != null && args.containsKey("deviceid")) {
//            mDeviceId = args.getInt("deviceid");
//            frag_swipe.setRefreshing(true);
//            getUserList();
//            frag_swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//                @Override
//                public void onRefresh() {
//                    getUserList();
//                }
//            });
//        }
    }

    @Override
    protected void initEvent() {
        frag_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private void getUserList() {
//        XlinkCloudManager.getInstance()
//                         .getDeviceUserList(mDeviceId, new XlinkRequestCallback<DeviceApi.DeviceSubscribeUsersResponse>() {
//                             @Override
//                             public void onError(final String error) {
//                                 getActivity().runOnUiThread(new Runnable() {
//                                     @Override
//                                     public void run() {
//                                         Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
//                                              .show();
//                                         frag_swipe.setRefreshing(false);
//                                     }
//                                 });
//                             }
//
//                             @Override
//                             public void onSuccess(DeviceApi.DeviceSubscribeUsersResponse response) {
//                                 frag_swipe.setRefreshing(false);
//                                 mUsers.clear();
//                                 mUsers.addAll(response.list);
//                                 mAdapter.notifyDataSetChanged();
//                             }
//                         });
    }
}
