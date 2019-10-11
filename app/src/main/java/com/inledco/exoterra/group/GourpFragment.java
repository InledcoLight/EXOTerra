package com.inledco.exoterra.group;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.device.DeviceActivity;
import com.inledco.exoterra.event.HomeDeviceChangedEvent;
import com.inledco.exoterra.util.RegexUtil;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import cn.xlink.restful.api.app.HomeApi;

public class GourpFragment extends BaseFragment {
    private Toolbar group_toolbar;
    private RecyclerView group_rv;

    private String mHomeId;
    private final List<HomeApi.HomeDevicesResponse.Device> mDevices = new ArrayList<>();
    private GroupDevicesAdapter mAdapter;

    private final XlinkRequestCallback<HomeApi.HomeDevicesResponse> mHomeDevicesCallback = new XlinkRequestCallback<HomeApi.HomeDevicesResponse>() {
        @Override
        public void onStart() {

        }

        @Override
        public void onError(final String error) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                         .show();
                }
            });
        }

        @Override
        public void onSuccess(HomeApi.HomeDevicesResponse response) {
            mDevices.clear();
            mDevices.addAll(response.list);
            mAdapter.notifyDataSetChanged();
        }
    };

    public static GourpFragment newInstance(@NonNull final String homeid, @NonNull final String homename) {
        Bundle args = new Bundle();
        args.putString("homeid", homeid);
        args.putString("homename", homename);
        GourpFragment frag = new GourpFragment();
        frag.setArguments(args);
        return frag;
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
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHomeDeviceChangedEvent(HomeDeviceChangedEvent event) {
        XlinkCloudManager.getInstance().getHomeDeviceList(mHomeId, mHomeDevicesCallback);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_group;
    }

    @Override
    protected void initView(View view) {
        group_toolbar = view.findViewById(R.id.group_toolbar);
        group_rv = view.findViewById(R.id.group_rv);

        group_toolbar.inflateMenu(R.menu.menu_group);
        group_rv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void initData() {
        mAdapter = new GroupDevicesAdapter(getContext(), mDevices) {
            @Override
            protected void onItemClick(int position) {
                HomeApi.HomeDevicesResponse.Device device = mDevices.get(position);
                String deviceTag = device.productId + "_" + device.mac;
                gotoDeviceActivity(deviceTag);
            }

            @Override
            protected boolean onItemLongClick(int position) {
                showItemActionDialog(mDevices.get(position));
                return true;
            }
        };
        group_rv.setAdapter(mAdapter);
        Bundle args = getArguments();
        if (args != null) {
            mHomeId = args.getString("homeid");
            String homename = args.getString("homename");
            if (TextUtils.isEmpty(mHomeId) == false) {
                group_toolbar.setTitle(homename);
                XlinkCloudManager.getInstance().getHomeDeviceList(mHomeId, mHomeDevicesCallback);
            }
        }
    }

    @Override
    protected void initEvent() {
        group_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        group_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_group_add:
                        addFragmentToStack(R.id.main_fl, AddGroupDeviceFragment.newInstance(mHomeId));
                        break;
                    case R.id.menu_group_share:
                        showShareHomeDialog();
                        break;
                }
                return true;
            }
        });
    }

    private void showShareHomeDialog() {
        if (TextUtils.isEmpty(mHomeId)) {
            return;
        }
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_share, null, false);
        final TextInputLayout til = view.findViewById(R.id.dialog_share_til);
        final TextInputEditText et_email = view.findViewById(R.id.dialog_share_email);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final AlertDialog dialog = builder.setTitle(R.string.invite_join_home)
                                          .setView(view)
                                          .setNegativeButton(R.string.cancel, null)
                                          .setPositiveButton(R.string.share, null)
                                          .show();
        Button btn_share = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = et_email.getText().toString();
                if (RegexUtil.isEmail(email)) {
                    shareHome(email);
                    dialog.dismiss();
                } else {
                    til.setError(getString(R.string.error_email));
                }
            }
        });
    }

    private void shareHome(@NonNull final String email) {
        XlinkCloudManager.getInstance().shareHome(mHomeId, email, new XlinkRequestCallback<HomeApi.UserInviteResponse>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                     .show();
            }

            @Override
            public void onSuccess(HomeApi.UserInviteResponse response) {
                Toast.makeText(getContext(), "Share Home Success.", Toast.LENGTH_SHORT)
                     .show();
            }
        });
    }

    private void gotoDeviceActivity(String deviceTag) {
        Intent intent = new Intent(getContext(), DeviceActivity.class);
        intent.putExtra("device_tag", deviceTag);
        startActivity(intent);
    }

    private void showItemActionDialog(@NonNull final HomeApi.HomeDevicesResponse.Device device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.BottomDialogTheme);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_action, null, false);
        Button btn_delete = view.findViewById(R.id.dialog_action_act2);
        Button btn_cancel = view.findViewById(R.id.dialog_action_cancel);
        btn_delete.setText(R.string.delete);
        final AlertDialog dialog = builder.setView(view)
                                          .setCancelable(false)
                                          .show();
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = Resources.getSystem().getDisplayMetrics().widthPixels;
        window.setAttributes(lp);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XlinkCloudManager.getInstance().removeDeviceFromHome(mHomeId, device.id, new XlinkRequestCallback<String>() {
                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                             .show();
                    }

                    @Override
                    public void onSuccess(String s) {
                        mDevices.remove(device);
                        mAdapter.notifyDataSetChanged();
                    }
                });
                dialog.dismiss();
            }
        });
    }
}
