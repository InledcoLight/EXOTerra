package com.inledco.exoterra.adddevice;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.helper.LocationHelper;
import com.inledco.exoterra.util.DeviceUtil;

import java.util.List;

public class ProductsFragment extends BaseFragment {

    private final int REQUEST_LOCATION_CODE = 1;

    private RecyclerView products_rv;
    private final List<String> mProducts = DeviceUtil.getAllProducts();
    private ProductAdapter mAdapter;

    private String mProductId;
    private ConnectNetViewModel mConnectNetViewModel;

    private LocationHelper mLocationHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initData();
        initEvent();
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_CODE) {
            if (permissions == null || permissions.length != 1 || grantResults == null || grantResults.length != 1) {
                return;
            }
            if (Manifest.permission.ACCESS_COARSE_LOCATION.equals(permissions[0])) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mConnectNetViewModel.getData()
                                        .setProductId(mProductId);
                    addFragmentToStack(R.id.adddevice_fl, new ConnectNetFragment());
                } else if (mLocationHelper.shouldShowRequestPermissionRationale()) {
                    Toast.makeText(getContext(), R.string.msg_location_permission, Toast.LENGTH_LONG)
                         .show();
                } else {
                    showLocationPermissionDialog();
                }
            }
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_products;
    }

    @Override
    protected void initView(View view) {
        products_rv = view.findViewById(R.id.products_rv);
    }

    @Override
    protected void initData() {
        mConnectNetViewModel = ViewModelProviders.of(getActivity()).get(ConnectNetViewModel.class);
        mAdapter = new ProductAdapter(getContext(), mProducts) {
            @Override
            protected void onClickItem(String prdt) {
                mProductId = prdt;
                if (mLocationHelper == null) {
                    mLocationHelper = new LocationHelper(ProductsFragment.this);
                }
                if (mLocationHelper.checkLocationPermisson() == false) {
                    mLocationHelper.requestLocationPermission(REQUEST_LOCATION_CODE);
                } else {
                    mConnectNetViewModel.getData()
                                        .setProductId(mProductId);
                    addFragmentToStack(R.id.adddevice_fl, new ConnectNetFragment());
                }
            }
        };

        products_rv.setAdapter(mAdapter);
    }

    @Override
    protected void initEvent() {

    }

    private void showLocationPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.title_location_permission)
               .setMessage(R.string.msg_location_permission)
               .setNegativeButton(R.string.cancel, null)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       mLocationHelper.startAppDetailActivity();
                   }
               })
               .setCancelable(false)
               .show();
    }
}
