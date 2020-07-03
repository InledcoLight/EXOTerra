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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.AliotServer;
import com.inledco.exoterra.aliot.HttpCallback;
import com.inledco.exoterra.aliot.UserApi;
import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.ExoProduct;
import com.inledco.exoterra.event.GroupDeviceChangedEvent;
import com.inledco.exoterra.event.GroupsRefreshedEvent;
import com.inledco.exoterra.main.groups.AddHabitatFragment;
import com.inledco.exoterra.manager.GroupManager;
import com.inledco.exoterra.manager.UserManager;
import com.inledco.exoterra.view.GradientCornerButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class AssignHabitatFragment extends BaseFragment {
    private ImageView assign_habitat_prdt;
    private ImageButton assign_habitat_add;
    private RecyclerView assign_habitat_rv;
    private View assign_habitat_warning;
    private TextView warning_tv_msg;
    private GradientCornerButton assign_habitat_save;

    private ConnectNetViewModel mConnectNetViewModel;
    private ConnectNetBean mConnectNetBean;

    private List<Group> mGroups = new ArrayList<>();
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
        assign_habitat_warning = view.findViewById(R.id.assign_habitat_warning);
        warning_tv_msg = assign_habitat_warning.findViewById(R.id.warning_tv_msg);
        assign_habitat_save = view.findViewById(R.id.assign_habitat_save);

        warning_tv_msg.setText(R.string.no_habitat_warning);
    }

    @Override
    protected void initData() {
        mConnectNetViewModel = ViewModelProviders.of(getActivity()).get(ConnectNetViewModel.class);
        mConnectNetBean = mConnectNetViewModel.getData();
        if (mConnectNetBean == null) {
            return;
        }
        ExoProduct product = ExoProduct.getExoProduct(mConnectNetBean.getProductKey());
        if (product != null) {
            assign_habitat_prdt.setImageResource(product.getIcon());
        }
        for (Group group : GroupManager.getInstance().getAllGroups()) {
            if (TextUtils.equals(group.creator, UserManager.getInstance().getUserid())) {
                mGroups.add(group);
            }
        }
        mAdapter = new AssignHabitatAdapter(getContext(), mGroups);
        assign_habitat_rv.setAdapter(mAdapter);
    }

    @Override
    protected void initEvent() {
        assign_habitat_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragmentToStack(R.id.adddevice_root, new AddHabitatFragment());
            }
        });

        assign_habitat_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Group group = mAdapter.getSelectedGroup();
                if (group == null) {
                    return;
                }
                final String pkey = mConnectNetBean.getProductKey();
                final String dname = mConnectNetBean.getDeviceName();
                AliotServer.getInstance().addDeviceToGroup(group.groupid, pkey, dname, new HttpCallback<UserApi.Response>() {
                    @Override
                    public void onError(String error) {
                        dismissLoadDialog();
                        showToast(error);
                    }

                    @Override
                    public void onSuccess(UserApi.Response result) {
                        group.addDevice(mConnectNetBean.getProductKey(), mConnectNetBean.getDeviceName(), mConnectNetBean.getName());
                        EventBus.getDefault().post(new GroupDeviceChangedEvent(group.groupid));
                        dismissLoadDialog();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getActivity().finish();
                            }
                        });
                    }
                });
                showLoadDialog();
            }
        });
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onGroupsRefreshedEvent(GroupsRefreshedEvent event) {
        mGroups.clear();
        for (Group group : GroupManager.getInstance().getAllGroups()) {
            if (TextUtils.equals(group.creator, UserManager.getInstance().getUserid())) {
                mGroups.add(group);
            }
        }
        assign_habitat_warning.setVisibility(mGroups.size() == 0 ? View.VISIBLE : View.GONE);
        mAdapter.notifyDataSetChanged();
    }
}
