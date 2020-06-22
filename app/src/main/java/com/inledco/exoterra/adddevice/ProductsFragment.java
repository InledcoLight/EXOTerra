package com.inledco.exoterra.adddevice;

import android.Manifest;
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

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BasePermissionFragment;
import com.inledco.exoterra.bean.ExoProduct;
import com.inledco.exoterra.common.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class ProductsFragment extends BasePermissionFragment {

    private RecyclerView products_rv;
    private Button products_exit;
    private final List<ExoProduct> mProducts = new ArrayList<>();
    private ProductAdapter mAdapter;

    private String mProductKey;
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
    protected void onPermissionGranted(String permission) {
        if (TextUtils.equals(Manifest.permission.ACCESS_COARSE_LOCATION, permission)) {
            mConnectNetViewModel.getData().setProductKey(mProductKey);
            addFragmentToStack(R.id.adddevice_fl, new ConnectNetFragment());
        }
    }

    @Override
    protected void onPermissionDenied(String permission) {
        if (TextUtils.equals(Manifest.permission.ACCESS_COARSE_LOCATION, permission)) {
            showToast(R.string.msg_location_permission);
        }
    }

    @Override
    protected void onPermissionPermanentDenied(String permission) {
        if (TextUtils.equals(Manifest.permission.ACCESS_COARSE_LOCATION, permission)) {
            showPermissionDialog(getString(R.string.title_location_permission),
                                 getString(R.string.msg_location_permission));
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_products;
    }

    @Override
    protected void initView(View view) {
        products_rv = view.findViewById(R.id.products_rv);
        products_exit = view.findViewById(R.id.products_exit);
    }

    @Override
    protected void initData() {
        mConnectNetViewModel = ViewModelProviders.of(getActivity()).get(ConnectNetViewModel.class);
        for (ExoProduct product : ExoProduct.values()) {
            mProducts.add(product);
        }
        mAdapter = new ProductAdapter(getContext(), mProducts);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                mProductKey = mProducts.get(position).getProductKey();
                if (checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    mConnectNetViewModel.getData().setProductKey(mProductKey);
                    addFragmentToStack(R.id.adddevice_fl, new ConnectNetFragment());
                } else {
                    requestPermission(0, Manifest.permission.ACCESS_COARSE_LOCATION);
                }
            }
        });
        products_rv.setAdapter(mAdapter);
    }

    @Override
    protected void initEvent() {
        products_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }
}
