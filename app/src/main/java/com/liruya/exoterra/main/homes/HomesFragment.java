package com.liruya.exoterra.main.homes;

import android.content.DialogInterface;
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

import com.liruya.base.BaseFragment;
import com.liruya.exoterra.R;
import com.liruya.exoterra.event.HomeChangedEvent;
import com.liruya.exoterra.home.HomeFragment;
import com.liruya.exoterra.manager.HomeManager;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkRequestCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import cn.xlink.restful.api.app.HomeApi;

public class HomesFragment extends BaseFragment {
    private Toolbar home_toolbar;
    private RecyclerView home_rv;

    private final List<HomeApi.HomesResponse.Home> mHomes = HomeManager.getInstance().getHomeList();
    private HomesAdapter mAdapter;
    private final XlinkRequestCallback<List<HomeApi.HomesResponse.Home>> mHomesResponseCallback = new XlinkRequestCallback<List<HomeApi.HomesResponse.Home>>() {
        @Override
        public void onStart() {

        }

        @Override
        public void onError(String error) {

        }
        @Override
        public void onSuccess(List<HomeApi.HomesResponse.Home> homes) {
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
        return R.layout.fragment_homes;
    }

    @Override
    protected void initView(View view) {
        home_toolbar = view.findViewById(R.id.homes_toolbar);
        home_rv = view.findViewById(R.id.homes_rv);

        home_toolbar.inflateMenu(R.menu.menu_homes);
        home_rv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void initData() {
        mAdapter = new HomesAdapter(getContext(), mHomes) {
            @Override
            protected void onItemClick(int position) {
                String homeid = mHomes.get(position).id;
                String homename = mHomes.get(position).name;
                getActivity().getSupportFragmentManager()
                             .beginTransaction()
                             .add(R.id.main_fl, HomeFragment.newInstance(homeid, homename))
                             .addToBackStack("")
                             .commit();
            }

            @Override
            protected boolean onItemLongClick(int position) {
                showItemActionDialog(mHomes.get(position));
                return true;
            }
        };
        home_rv.setAdapter(mAdapter);
        HomeManager.getInstance().syncHomeList(mHomesResponseCallback);
    }

    @Override
    protected void initEvent() {
        home_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                showCreateHomeDialog();
                return true;
            }
        });
    }

    private void showCreateHomeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_home, null, false);
        final TextInputLayout til = view.findViewById(R.id.dialog_create_home_til);
        final TextInputEditText et_name = view.findViewById(R.id.dialog_create_home_name);
        builder.setTitle(R.string.create_home);
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
                    XlinkCloudManager.getInstance().createHome(name, new XlinkRequestCallback<HomeApi.HomeResponse>() {
                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                                 .show();
                        }

                        @Override
                        public void onSuccess(HomeApi.HomeResponse response) {
                            Toast.makeText(getContext(), "Create home success.", Toast.LENGTH_SHORT)
                                 .show();
                            HomeManager.getInstance().syncHomeList(mHomesResponseCallback);
                        }
                    });
                    dialog.dismiss();
                }
            }
        });
    }

    private void showItemActionDialog(@NonNull final HomeApi.HomesResponse.Home home) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.BottomDialogTheme);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_action, null, false);
        Button btn_action = view.findViewById(R.id.dialog_action_act1);
        Button btn_cancel = view.findViewById(R.id.dialog_action_cancel);
        btn_action.setText(R.string.delete);
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
        btn_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XlinkCloudManager.getInstance().deleteHome(home, new XlinkRequestCallback<String>() {
                    @Override
                    public void onStart() {

                    }

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
