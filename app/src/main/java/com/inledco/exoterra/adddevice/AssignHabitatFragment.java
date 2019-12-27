package com.inledco.exoterra.adddevice;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.Home;
import com.inledco.exoterra.event.HomesRefreshedEvent;
import com.inledco.exoterra.main.groups.AddHabitatFragment;
import com.inledco.exoterra.manager.HomeManager;
import com.inledco.exoterra.util.DeviceUtil;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class AssignHabitatFragment extends BaseFragment {
    private ImageView assign_habitat_prdt;
    private ImageButton assign_habitat_add;
    private RecyclerView assign_habitat_rv;
    private Button assign_habitat_save;

    private ConnectNetViewModel mConnectNetViewModel;
    private ConnectNetBean mConnectNetBean;

    private List<Home> mHomes = HomeManager.getInstance().getHomeList();
    private AssignHabitatAdapter mAdapter;

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
        return R.layout.fragment_assign_habitat;
    }

    @Override
    protected void initView(View view) {
        assign_habitat_prdt = view.findViewById(R.id.assign_habitat_prdt);
        assign_habitat_add = view.findViewById(R.id.assign_habitat_add);
        assign_habitat_rv = view.findViewById(R.id.assign_habitat_rv);
        assign_habitat_save = view.findViewById(R.id.assign_habitat_save);
    }

    @Override
    protected void initData() {
        mConnectNetViewModel = ViewModelProviders.of(getActivity()).get(ConnectNetViewModel.class);
        mConnectNetBean = mConnectNetViewModel.getData();
        if (mConnectNetBean == null) {
            return;
        }

        final String pid = mConnectNetBean.getProductId();
        assign_habitat_prdt.setImageResource(DeviceUtil.getProductIcon(pid));
        mAdapter = new AssignHabitatAdapter(getContext(), mHomes);
        assign_habitat_rv.setAdapter(mAdapter);
    }

    @Override
    protected void initEvent() {
        assign_habitat_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragmentToStack(R.id.adddevice_fl, new AddHabitatFragment());
            }
        });

        assign_habitat_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String homeid = mAdapter.getSelectedHomeid();
                if (TextUtils.isEmpty(homeid)) {
                    return;
                }
                XlinkCloudManager.getInstance().addDeviceToHome(homeid, mConnectNetBean.getResultDevid(), new XlinkRequestCallback<String>() {
                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                             .show();
                    }

                    @Override
                    public void onSuccess(String s) {
                        getActivity().finish();
                    }
                });
            }
        });
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onHomesRefreshedEvent(HomesRefreshedEvent event) {
        mAdapter.notifyDataSetChanged();
    }
}
