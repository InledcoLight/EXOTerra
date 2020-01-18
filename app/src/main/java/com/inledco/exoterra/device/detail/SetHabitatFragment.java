package com.inledco.exoterra.device.detail;

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
import android.widget.Toast;

import com.inledco.exoterra.AppConstants;
import com.inledco.exoterra.R;
import com.inledco.exoterra.adddevice.AssignHabitatAdapter;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.Home;
import com.inledco.exoterra.event.HomesRefreshedEvent;
import com.inledco.exoterra.main.groups.AddHabitatFragment;
import com.inledco.exoterra.manager.HomeManager;
import com.inledco.exoterra.view.GradientCornerButton;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class SetHabitatFragment extends BaseFragment {

    private RecyclerView set_habitat_rv;
    private ImageButton set_habitat_add;
    private View set_habitat_warning;
    private TextView warning_tv_msg;
    private GradientCornerButton set_habitat_back;
    private GradientCornerButton set_habitat_save;

    private List<Home> mHomes = HomeManager.getInstance().getHomeList();
    private AssignHabitatAdapter mAdapter;

    private int mDeviceId;

    public static SetHabitatFragment newInstance(final int devid) {
        Bundle args = new Bundle();
        args.putInt(AppConstants.DEVICE_ID, devid);
        SetHabitatFragment fragment = new SetHabitatFragment();
        fragment.setArguments(args);
        return fragment;
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

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_set_habitat;
    }

    @Override
    protected void initView(View view) {
        set_habitat_add = view.findViewById(R.id.set_habitat_add);
        set_habitat_rv = view.findViewById(R.id.set_habitat_rv);
        set_habitat_warning = view.findViewById(R.id.set_habitat_warning);
        warning_tv_msg = set_habitat_warning.findViewById(R.id.warning_tv_msg);
        set_habitat_back = view.findViewById(R.id.set_habitat_back);
        set_habitat_save = view.findViewById(R.id.set_habitat_save);

        warning_tv_msg.setText(R.string.no_habitat_warning);
    }

    @Override
    protected void initData() {
        Bundle args = getArguments();
        if (args != null) {
            mDeviceId = args.getInt(AppConstants.DEVICE_ID);
        }

        mAdapter = new AssignHabitatAdapter(getContext(), mHomes);
        set_habitat_rv.setAdapter(mAdapter);
    }

    @Override
    protected void initEvent() {
        set_habitat_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragmentToStack(R.id.device_root, new AddHabitatFragment());
            }
        });

        set_habitat_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        set_habitat_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String homeid = mAdapter.getSelectedHomeid();
                if (mDeviceId == 0 || TextUtils.isEmpty(homeid)) {
                    return;
                }
                XlinkCloudManager.getInstance().addDeviceToHome(homeid, mDeviceId, new XlinkRequestCallback<String>() {
                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                             .show();
                    }

                    @Override
                    public void onSuccess(String s) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                });
            }
        });
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onHomesRefreshedEvent(HomesRefreshedEvent event) {
        set_habitat_warning.setVisibility(mHomes.size() == 0 ? View.VISIBLE : View.GONE);
        mAdapter.notifyDataSetChanged();
    }
}
