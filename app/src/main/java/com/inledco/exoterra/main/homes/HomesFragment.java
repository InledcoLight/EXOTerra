package com.inledco.exoterra.main.homes;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.Home2;
import com.inledco.exoterra.common.OnItemClickListener;
import com.inledco.exoterra.event.HomeChangedEvent;
import com.inledco.exoterra.manager.Home2Manager;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import cn.xlink.restful.api.app.HomeApi;

public class HomesFragment extends BaseFragment {

    private Toolbar homes_toolbar;
//    private SwipeRefreshLayout homes_swipe;
    private RecyclerView homes_rv;
    private FloatingActionButton homes_fab_add;

    private final List<Home2> mHome2s = new ArrayList<>();
    private HomesAdapter mAdapter;

//    private final XlinkRequestCallback<HomeApi.HomesResponse> mGetHomesCallback = new XlinkRequestCallback<HomeApi.HomesResponse>() {
//        @Override
//        public void onError(String error) {
//            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
//                 .show();
//            homes_swipe.setRefreshing(false);
//        }
//
//        @Override
//        public void onSuccess(HomeApi.HomesResponse response) {
//            mHome2s.clear();
//            mHome2s.addAll(response.list);
//            mAdapter.notifyDataSetChanged();
//            homes_swipe.setRefreshing(false);
//        }
//    };

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

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_homes;
    }

    @Override
    protected void initView(View view) {
        homes_toolbar = view.findViewById(R.id.homes_toolbar);
//        homes_swipe = view.findViewById(R.id.homes_swipe);
        homes_rv = view.findViewById(R.id.homes_rv);
        homes_fab_add = view.findViewById(R.id.homes_fab_add);

        homes_rv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void initData() {
        mHome2s.addAll(Home2Manager.getInstance().getHome2List());
        mAdapter = new HomesAdapter(getContext(),
                                    mHome2s, Home2Manager.getInstance().getCurrentHomeId());
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String homeid = mHome2s.get(position).id;
                addFragmentToStack(R.id.main_fl, HomeDetailFragment.newInstance(homeid));
            }
        });
        homes_rv.setAdapter(mAdapter);

//        homes_swipe.setRefreshing(true);
//        XlinkCloudManager.getInstance().getHome2List(mGetHomesCallback);
    }

    @Override
    protected void initEvent() {
        homes_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

//        homes_swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                XlinkCloudManager.getInstance().getHome2List(mGetHomesCallback);
//            }
//        });

        homes_fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddHomeDialog();
            }
        });
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onHomeChangedEvent(HomeChangedEvent event) {
//        XlinkCloudManager.getInstance().getHome2List(mGetHomesCallback);
        mHome2s.clear();
        mHome2s.addAll(Home2Manager.getInstance().getHome2List());
        mAdapter.notifyDataSetChanged();
    }

    private void showAddHomeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add, null, false);
        final TextInputLayout til = view.findViewById(R.id.dialog_add_til);
        final TextInputEditText et_name = view.findViewById(R.id.dialog_add_name);
        til.setHint(getString(R.string.home_name));
        builder.setTitle(R.string.add_home);
        builder.setView(view);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, null);
        final AlertDialog dialog = builder.show();
        Button btn_ok = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = et_name.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    til.setError(getString(R.string.input_empty));
                } else {
                    XlinkCloudManager.getInstance().addHome(name, new XlinkRequestCallback<HomeApi.HomeResponse>() {
                        @Override
                        public void onSuccess(HomeApi.HomeResponse response) {
                            Home2Manager.getInstance().refreshHomeList();
//                            XlinkCloudManager.getInstance().getHome2List(mGetHomesCallback);
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                                 .show();
                        }
                    });
                    dialog.dismiss();
                }
            }
        });
    }
}
