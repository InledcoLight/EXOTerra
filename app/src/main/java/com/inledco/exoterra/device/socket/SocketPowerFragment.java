package com.inledco.exoterra.device.socket;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.EXOSocket;

public class SocketPowerFragment extends BaseFragment {

    private CheckedTextView power_ctv;

    private SocketViewModel mSocketViewModel;
    private EXOSocket mSocket;

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
        return R.layout.fragment_power;
    }

    @Override
    protected void initView(View view) {
        power_ctv = view.findViewById(R.id.power_ctv);
    }

    @Override
    protected void initData() {
        mSocketViewModel = ViewModelProviders.of(getActivity()).get(SocketViewModel.class);
        mSocket = mSocketViewModel.getData();
        mSocketViewModel.observe(this, new Observer<EXOSocket>() {
            @Override
            public void onChanged(@Nullable EXOSocket exoSocket) {
                refreshData();
            }
        });
        if (mSocket == null) {
            return;
        }

        refreshData();
    }

    @Override
    protected void initEvent() {
        power_ctv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSocketViewModel.setPower(!power_ctv.isChecked());
            }
        });
    }

    private void refreshData() {
        power_ctv.setChecked(mSocket.getPower());
    }
}
