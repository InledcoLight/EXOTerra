package com.inledco.exoterra.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.common.SimpleAdapter;
import com.inledco.exoterra.main.MainActivity;
import com.inledco.exoterra.uvbbuddy.UvbMainActivity;
import com.inledco.exoterra.view.HorizontalMatrixImageView;

import java.util.ArrayList;
import java.util.List;

public class NewProductsFragment extends BaseFragment implements View.OnClickListener {
    private RecyclerView new_products_rv;
    private Button new_products_home;
    private Button new_products_microtope;
    private Button new_products_uvb;
    private Button new_products_restore;
    private Button new_products_pref;

    private final List<Integer> mIcons = new ArrayList<>();
    private HorizontalIconAdapter mAdapter;

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
        return R.layout.fragment_new_products;
    }

    @Override
    protected void initView(View view) {
//        new_products_icon1 = view.findViewById(R.id.new_products_icon1);
//        new_products_icon2 = view.findViewById(R.id.new_products_icon2);
//        new_products_icon3 = view.findViewById(R.id.new_products_icon3);
        new_products_rv = view.findViewById(R.id.new_products_rv);
        new_products_home = view.findViewById(R.id.new_products_home);
        new_products_microtope = view.findViewById(R.id.new_products_microtope);
        new_products_uvb = view.findViewById(R.id.new_products_uvb);
        new_products_restore = view.findViewById(R.id.new_products_restore);
        new_products_pref = view.findViewById(R.id.new_products_pref);

        new_products_home.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_reptile, 0, 0);
        new_products_microtope.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_microtope, 0, 0);
        new_products_uvb.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_uvb, 0, 0);
        new_products_restore.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_resore_40dp, 0, 0);
        new_products_pref.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_prefs_white, 0, 0);

//        new_products_icon1.setImageResource(R.drawable.ic_product_1);
//        new_products_icon2.setImageResource(R.drawable.ic_product_2);
//        new_products_icon3.setImageResource(R.drawable.ic_product_3);
    }

    @Override
    protected void initData() {
        mIcons.add(R.drawable.ic_product_1);
        mIcons.add(R.drawable.ic_product_2);
        mIcons.add(R.drawable.ic_product_3);

        mAdapter = new HorizontalIconAdapter(getContext(), mIcons);
        new_products_rv.setAdapter(mAdapter);
    }

    @Override
    protected void initEvent() {
        new_products_home.setOnClickListener(this);
        new_products_microtope.setOnClickListener(this);
        new_products_uvb.setOnClickListener(this);
        new_products_restore.setOnClickListener(this);
        new_products_pref.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.new_products_home:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.new_products_microtope:
                startMainActivity();
                break;
            case R.id.new_products_uvb:
                startUvbMainActivity();
                break;
            case R.id.new_products_restore:

                break;
            case R.id.new_products_pref:

                break;
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);
    }

    private void startUvbMainActivity() {
        Intent intent = new Intent(getContext(), UvbMainActivity.class);
        startActivity(intent);
    }

    private class HorizontalIconAdapter extends SimpleAdapter<Integer, HorizontalIconViewHolder> {
        public HorizontalIconAdapter(@NonNull Context context, List<Integer> data) {
            super(context, data);
        }

        @Override
        protected int getItemLayoutResId() {
            return R.layout.item_horizon_image;
        }

        @NonNull
        @Override
        public HorizontalIconViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new HorizontalIconViewHolder(createView(viewGroup));
        }

        @Override
        public void onBindViewHolder(@NonNull HorizontalIconViewHolder holder, int i) {
            final int postion = holder.getAdapterPosition();
            Integer res = mData.get(postion);
            holder.icon.setImageResource(res);
        }
    }

    private class HorizontalIconViewHolder extends RecyclerView.ViewHolder {

        private HorizontalMatrixImageView icon;
        public HorizontalIconViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.item_horizontal_icon);
        }
    }
}
