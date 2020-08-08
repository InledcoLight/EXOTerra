package com.inledco.exoterra.uvbbuddy;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;

public class UvbResultFragment extends BaseFragment {

    private Button uvb_result_buy;
    private Button uvb_result_back;

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
        return R.layout.fragment_uvb_result;
    }

    @Override
    protected void initView(View view) {
        uvb_result_buy = view.findViewById(R.id.uvb_result_buy);
        uvb_result_back = view.findViewById(R.id.uvb_result_back);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initEvent() {
        uvb_result_buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        uvb_result_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }
}
