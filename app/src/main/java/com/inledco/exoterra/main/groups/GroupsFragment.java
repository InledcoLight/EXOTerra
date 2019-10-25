package com.inledco.exoterra.main.groups;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
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
import com.inledco.exoterra.event.HomeChangedEvent;
import com.inledco.exoterra.group.GroupFragment;
import com.inledco.exoterra.manager.HomeManager;
import com.inledco.exoterra.xlink.HomeExtendApi;
import com.inledco.exoterra.xlink.RoomApi;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import cn.xlink.restful.api.app.HomeApi;

public class GroupsFragment extends BaseFragment {
    private Toolbar groups_toolbar;
    private RecyclerView groups_rv;

    private final List<HomeExtendApi.HomesResponse.Home> mHomes = HomeManager.getInstance().getHomeList();
    private GroupsAdapter mAdapter;
    private final XlinkRequestCallback<List<HomeExtendApi.HomesResponse.Home>> mHomesResponseCallback = new XlinkRequestCallback<List<HomeExtendApi.HomesResponse.Home>>() {
        @Override
        public void onError(String error) {

        }

        @Override
        public void onSuccess(List<HomeExtendApi.HomesResponse.Home> homes) {
            mAdapter.notifyDataSetChanged();
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
        GridLayoutManager layoutManager = (GridLayoutManager) groups_rv.getLayoutManager();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHomeChangedEvent(HomeChangedEvent event) {
        HomeManager.getInstance().syncHomeList(mHomesResponseCallback);
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
        mAdapter = new GroupsAdapter(getContext(), mHomes) {
            @Override
            protected void onItemClick(int position) {
                String homeid = mHomes.get(position).id;
                String homename = mHomes.get(position).name;
                addFragmentToStack(R.id.main_fl, GroupFragment.newInstance(homeid, homename));
            }

            @Override
            protected boolean onItemLongClick(int position) {
                showItemActionDialog(mHomes.get(position));
                return true;
            }
        };
        groups_rv.setAdapter(mAdapter);
        HomeManager.getInstance().syncHomeList(mHomesResponseCallback);
    }

    @Override
    protected void initEvent() {
        groups_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                showCreateGroupDialog();
                return true;
            }
        });
    }

    private void showCreateGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_group, null, false);
        final TextInputLayout til = view.findViewById(R.id.dialog_create_group_til);
        final TextInputEditText et_name = view.findViewById(R.id.dialog_create_group_name);
        builder.setTitle(R.string.create_group);
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
                        public void onError(String error) {
                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                                 .show();
                        }

                        @Override
                        public void onSuccess(HomeApi.HomeResponse response) {
                            Toast.makeText(getContext(), "Create home success.", Toast.LENGTH_SHORT)
                                 .show();
                            XlinkCloudManager.getInstance().createRoom(response.id, name, new XlinkRequestCallback<RoomApi.RoomResponse>() {
                                @Override
                                public void onError(String error) {
                                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                                         .show();
                                }

                                @Override
                                public void onSuccess(RoomApi.RoomResponse roomResponse) {
                                    Log.e(TAG, "onSuccess: " + roomResponse.id + " " + roomResponse.name);
                                }
                            });
                            HomeManager.getInstance().syncHomeList(mHomesResponseCallback);
                        }
                    });
                    dialog.dismiss();
                }
            }
        });
    }

    private void showRenameGroupDialog(@NonNull final String homeid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_group, null, false);
        final TextInputLayout til = view.findViewById(R.id.dialog_create_group_til);
        final TextInputEditText et_name = view.findViewById(R.id.dialog_create_group_name);
        builder.setTitle(R.string.rename_group);
        builder.setView(view);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, null);
        final AlertDialog dialog = builder.show();
        Button btn_ok = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = et_name.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    til.setError(getString(R.string.input_empty));
                } else {
                    XlinkCloudManager.getInstance().renameHome(homeid, name, new XlinkRequestCallback<String>() {
                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                                 .show();
                        }

                        @Override
                        public void onSuccess(String response) {
                            Toast.makeText(getContext(), "Rename home success.", Toast.LENGTH_SHORT)
                                 .show();
                            HomeManager.getInstance().syncHomeList(mHomesResponseCallback);
                        }
                    });
                    dialog.dismiss();
                }
            }
        });
    }

    private void showItemActionDialog(@NonNull final HomeExtendApi.HomesResponse.Home home) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.BottomDialogTheme);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_action, null, false);
        Button btn_rename = view.findViewById(R.id.dialog_action_act1);
        Button btn_delete = view.findViewById(R.id.dialog_action_act2);
        Button btn_cancel = view.findViewById(R.id.dialog_action_cancel);
        btn_rename.setText(R.string.rename);
        btn_rename.setVisibility(View.VISIBLE);
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
        btn_rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRenameGroupDialog(home.id);
                dialog.dismiss();
            }
        });
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XlinkCloudManager.getInstance().deleteHome(home, new XlinkRequestCallback<String>() {
                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                             .show();
                    }

                    @Override
                    public void onSuccess(String s) {
                        mHomes.remove(home);
                        mAdapter.notifyDataSetChanged();
                    }
                });
                dialog.dismiss();
            }
        });
    }
}
