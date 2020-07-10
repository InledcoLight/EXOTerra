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

import com.inledco.exoterra.R;
import com.inledco.exoterra.adddevice.AssignHabitatAdapter;
import com.inledco.exoterra.aliot.AliotServer;
import com.inledco.exoterra.aliot.HttpCallback;
import com.inledco.exoterra.aliot.UserApi;
import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.base.BaseFragment;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SetHabitatFragment extends BaseFragment {

    private RecyclerView set_habitat_rv;
    private ImageButton set_habitat_add;
    private View set_habitat_warning;
    private TextView warning_tv_msg;
    private GradientCornerButton set_habitat_back;
    private GradientCornerButton set_habitat_save;

    private final List<Group> mGroups = new ArrayList<>();
    private AssignHabitatAdapter mAdapter;

    private String mProductKey;
    private String mDeviceName;
    private String mName;

    public static SetHabitatFragment newInstance(final String pkey, final String dname, final String name) {
        Bundle args = new Bundle();
        args.putString("productKey", pkey);
        args.putString("deviceName", dname);
        args.putString("name", name);
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
            mProductKey = args.getString("productKey");
            mDeviceName = args.getString("deviceName");
            mName = args.getString("name");
        }

//        for (Group group : GroupManager.getInstance().getAllGroups()) {
//            if (TextUtils.equals(group.creator, UserManager.getInstance().getUserid())) {
//                mGroups.add(group);
//            }
//        }
        mGroups.addAll(GroupManager.getInstance().getAllGroups());
        Collections.sort(mGroups, new Comparator<Group>() {
            @Override
            public int compare(Group o1, Group o2) {
                String userid = UserManager.getInstance().getUserid();
                if (TextUtils.equals(o1.creator, o2.creator)) {
                    return 0;
                }
                if (TextUtils.equals(userid, o1.creator)) {
                    return -1;
                }
                if (TextUtils.equals(userid, o2.creator)) {
                    return 1;
                }
                return 0;
            }
        });
        mAdapter = new AssignHabitatAdapter(getContext(), mGroups);
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
                final Group group = mAdapter.getSelectedGroup();
                if (TextUtils.isEmpty(mProductKey) || TextUtils.isEmpty(mDeviceName) || group == null) {
                    return;
                }
                AliotServer.getInstance().addDeviceToGroup(group.groupid, mProductKey, mDeviceName, new HttpCallback<UserApi.Response>() {
                    @Override
                    public void onError(String error) {
                        dismissLoadDialog();
                        showToast(error);
                    }

                    @Override
                    public void onSuccess(UserApi.Response result) {
                        group.addDevice(mProductKey, mDeviceName, mName);
                        EventBus.getDefault().post(new GroupDeviceChangedEvent(group.groupid));
                        dismissLoadDialog();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getActivity().getSupportFragmentManager().popBackStack();
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
//        mGroups.clear();
//        for (Group group : GroupManager.getInstance().getAllGroups()) {
//            if (TextUtils.equals(group.creator, UserManager.getInstance().getUserid())) {
//                mGroups.add(group);
//            }
//        }
        mGroups.clear();
        mGroups.addAll(GroupManager.getInstance().getAllGroups());
        Collections.sort(mGroups, new Comparator<Group>() {
            @Override
            public int compare(Group o1, Group o2) {
                String userid = UserManager.getInstance().getUserid();
                if (TextUtils.equals(o1.creator, o2.creator)) {
                    return 0;
                }
                if (TextUtils.equals(userid, o1.creator)) {
                    return -1;
                }
                if (TextUtils.equals(userid, o2.creator)) {
                    return 1;
                }
                return 0;
            }
        });
        set_habitat_warning.setVisibility(mGroups.size() == 0 ? View.VISIBLE : View.GONE);
        mAdapter.notifyDataSetChanged();
    }
}
