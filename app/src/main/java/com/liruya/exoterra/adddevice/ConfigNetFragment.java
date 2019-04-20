package com.liruya.exoterra.adddevice;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.liruya.base.BaseFragment;
import com.liruya.exoterra.R;

public class ConfigNetFragment extends BaseFragment {
    private RadioGroup config_net_tab;
    private RadioButton config_net_smart;
    private RadioButton config_net_compatible;

    private ConnectNetViewModel mConnectNetViewModel;

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
        return R.layout.fragment_config_net;
    }

    @Override
    protected void initView(View view) {
        config_net_tab = view.findViewById(R.id.config_net_tab);
        config_net_smart = view.findViewById(R.id.config_net_smart);
        config_net_compatible = view.findViewById(R.id.config_net_compatible);
    }

    @Override
    protected void initData() {
        mConnectNetViewModel = ViewModelProviders.of(getActivity()).get(ConnectNetViewModel.class);
        mConnectNetViewModel.observe(this, new Observer<ConnectNetBean>() {
            @Override
            public void onChanged(@Nullable ConnectNetBean connectNetBean) {
                if (connectNetBean.isRunning()) {
                    config_net_smart.setEnabled(false);
                    config_net_compatible.setEnabled(false);
                } else {
                    config_net_smart.setEnabled(true);
                    config_net_compatible.setEnabled(true);
                }
            }
        });

        config_net_tab.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.config_net_smart:
                        getActivity().getSupportFragmentManager()
                                     .beginTransaction()
                                     .replace(R.id.config_net_fl, new SmartConfigFragment())
                                     .commit();
                        break;
                    case R.id.config_net_compatible:
                        getActivity().getSupportFragmentManager()
                                     .beginTransaction()
                                     .replace(R.id.config_net_fl, new CompatibleModeFragment())
                                     .commit();
                        break;
                }
            }
        });
        config_net_tab.check(R.id.config_net_smart);
    }

    @Override
    protected void initEvent() {

    }
}
