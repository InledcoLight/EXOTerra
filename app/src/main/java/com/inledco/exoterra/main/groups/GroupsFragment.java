package com.inledco.exoterra.main.groups;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.Home;
import com.inledco.exoterra.common.OnItemClickListener;
import com.inledco.exoterra.event.DatapointChangedEvent;
import com.inledco.exoterra.event.HomeChangedEvent;
import com.inledco.exoterra.event.HomeDeviceChangedEvent;
import com.inledco.exoterra.event.HomePropertyChangedEvent;
import com.inledco.exoterra.group.GroupFragment;
import com.inledco.exoterra.manager.HomeManager;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkConstants;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import cn.xlink.restful.api.app.HomeApi;

public class GroupsFragment extends BaseFragment {
    private Toolbar groups_toolbar;
    private RecyclerView groups_rv;

    private final List<Home> mHomes = HomeManager.getInstance().getHomeList();
    private GroupsAdapter mAdapter;
    private final XlinkRequestCallback<List<Home>> mHomesResponseCallback = new XlinkRequestCallback<List<Home>>() {
        @Override
        public void onError(String error) {

        }

        @Override
        public void onSuccess(List<Home> homes) {
            mAdapter.refreshData();
        }
    };

    private final BroadcastReceiver mTimeChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_TIME_TICK:
                    mAdapter.updateTime();
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(mTimeChangeReceiver);
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHomeChangedEvent(HomeChangedEvent event) {
        HomeManager.getInstance().refreshHomeList(mHomesResponseCallback);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHomePropertyChangedEvent(HomePropertyChangedEvent event) {
        Log.e(TAG, "onHomePropertyChangedEvent: ");
        for (int i = 0; i < mHomes.size(); i++) {
            if (TextUtils.equals(event.getHomeid(), mHomes.get(i).getHome().id)) {
                mAdapter.updateData(i);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHomeDeviceChangedEvent(HomeDeviceChangedEvent event) {
        for (int i = 0; i < mHomes.size(); i++) {
            if (TextUtils.equals(event.getHomeid(), mHomes.get(i).getHome().id)) {
                mAdapter.updateData(i);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDatapointChangedEvent(DatapointChangedEvent event) {
        for (int i = 0; i < mHomes.size(); i++) {
            Home home = mHomes.get(i);
            for (int j = 0; j < home.getDevices().size(); j++) {
                HomeApi.HomeDevicesResponse.Device device = home.getDevices().get(j);
                String tag = device.productId + "_" + device.mac;
                if (TextUtils.equals(device.productId, XlinkConstants.PRODUCT_ID_SOCKET) && TextUtils.equals(tag, event.getDeviceTag())) {
                    mAdapter.updateData(i);
                    return;
                }
            }
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_groups;
    }

    @Override
    protected void initView(View view) {
        groups_toolbar = view.findViewById(R.id.groups_toolbar);
        groups_rv = view.findViewById(R.id.groups_rv);

        groups_toolbar.inflateMenu(R.menu.menu_groups);
        //        groups_rv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void initData() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        getActivity().registerReceiver(mTimeChangeReceiver, filter);

        mAdapter = new GroupsAdapter(getContext(), mHomes);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                HomeApi.HomesResponse.Home home = mHomes.get(position).getHome();
                String homeid = home.id;
                String homename = home.name;
                addFragmentToStack(R.id.main_fl, GroupFragment.newInstance(homeid, homename));
            }
        });
        groups_rv.setAdapter(mAdapter);
        HomeManager.getInstance().refreshHomeList(mHomesResponseCallback);
    }

    @Override
    protected void initEvent() {
        groups_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_groups_add:
//                        showCreateGroupDialog();
                        addFragmentToStack(R.id.main_fl, new AddHabitatFragment());
                        break;
                }
                return true;
            }
        });
    }

    private void showCreateGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add, null, false);
        final TextInputLayout til = view.findViewById(R.id.dialog_add_til);
        final TextInputEditText et_name = view.findViewById(R.id.dialog_add_name);
        builder.setTitle(R.string.create_habitat);
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
                    XlinkCloudManager.getInstance().createHome(name, new XlinkRequestCallback<HomeApi.HomeResponse>() {
                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                                 .show();
                        }

                        @Override
                        public void onSuccess(HomeApi.HomeResponse response) {
                            Toast.makeText(getContext(), "Create habitat success.", Toast.LENGTH_SHORT)
                                 .show();
//                            XlinkCloudManager.getInstance().createRoom(response.id, name, new XlinkRequestCallback<RoomApi.RoomResponse>() {
//                                @Override
//                                public void onError(String error) {
//                                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
//                                         .show();
//                                }
//
//                                @Override
//                                public void onSuccess(RoomApi.RoomResponse roomResponse) {
//                                    Log.e(TAG, "onSuccess: " + roomResponse.id + " " + roomResponse.name);
//                                }
//                            });
                            HomeManager.getInstance().refreshHomeList(mHomesResponseCallback);
                        }
                    });
                    dialog.dismiss();
                }
            }
        });
    }

//    private void showRenameGroupDialog(@NonNull final String homeid) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add, null, false);
//        final TextInputLayout til = view.findViewById(R.id.dialog_add_til);
//        final TextInputEditText et_name = view.findViewById(R.id.dialog_add_name);
//        builder.setTitle(R.string.rename_group);
//        builder.setView(view);
//        builder.setNegativeButton(R.string.cancel, null);
//        builder.setPositiveButton(R.string.ok, null);
//        final AlertDialog dialog = builder.show();
//        Button btn_ok = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
//        btn_ok.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String name = et_name.getText().toString();
//                if (TextUtils.isEmpty(name)) {
//                    til.setError(getString(R.string.input_empty));
//                } else {
//                    XlinkCloudManager.getInstance().renameHome(homeid, name, new XlinkRequestCallback<String>() {
//                        @Override
//                        public void onError(String error) {
//                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
//                                 .show();
//                        }
//
//                        @Override
//                        public void onSuccess(String response) {
//                            Toast.makeText(getContext(), "Rename habitat success.", Toast.LENGTH_SHORT)
//                                 .show();
//                            HomeManager.getInstance().refreshHomeList(mHomesResponseCallback);
//                        }
//                    });
//                    dialog.dismiss();
//                }
//            }
//        });
//    }

//    private void showItemActionDialog(@NonNull final Home2 home2) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.BottomDialogTheme);
//        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_action, null, false);
//        Button btn_rename = view.findViewById(R.id.dialog_action_act1);
//        Button btn_delete = view.findViewById(R.id.dialog_action_act2);
//        Button btn_cancel = view.findViewById(R.id.dialog_action_cancel);
//        btn_rename.setText(R.string.rename);
//        btn_rename.setVisibility(View.VISIBLE);
//        final AlertDialog dialog = builder.setView(view)
//                                          .setCancelable(false)
//                                          .show();
//        Window window = dialog.getWindow();
//        window.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL);
//        WindowManager.LayoutParams lp = window.getAttributes();
//        lp.width = Resources.getSystem().getDisplayMetrics().widthPixels;
//        window.setAttributes(lp);
//        btn_cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//        btn_rename.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showRenameGroupDialog(home2.id);
//                dialog.dismiss();
//            }
//        });
//        btn_delete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                XlinkCloudManager.getInstance().deleteHome(home2, new XlinkRequestCallback<String>() {
////                    @Override
////                    public void onError(String error) {
////                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
////                             .show();
////                    }
////
////                    @Override
////                    public void onSuccess(String s) {
////                        mHome2s.remove(home2);
////                        mAdapter.notifyDataSetChanged();
////                    }
////                });
//                dialog.dismiss();
//            }
//        });
//    }
}
