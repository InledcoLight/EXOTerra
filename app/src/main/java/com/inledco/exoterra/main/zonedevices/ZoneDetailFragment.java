package com.inledco.exoterra.main.zonedevices;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.inledco.exoterra.AppConstants;
import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.Home;
import com.inledco.exoterra.bean.XHome;
import com.inledco.exoterra.main.HomeViewModel;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;
import com.inledco.exoterra.xlink.ZoneApi;

public class ZoneDetailFragment extends BaseFragment {

    private Toolbar zone_detail_toolbar;
    private LinearLayout zone_detail_ll_name;
    private TextView zone_detail_name;
    private TextView zone_detail_devices;
    private Button zone_detail_delete;

    private String mZoneid;
    private HomeViewModel mHomeViewModel;
    private XHome mXHome;

    public static ZoneDetailFragment newInstance(final String zoneid) {
        Bundle args = new Bundle();
        args.putString(AppConstants.ZONE_ID, zoneid);
        ZoneDetailFragment fragment = new ZoneDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

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
        return R.layout.fragment_zone_detail;
    }

    @Override
    protected void initView(View view) {
        zone_detail_toolbar = view.findViewById(R.id.zone_detail_toolbar);
        zone_detail_ll_name = view.findViewById(R.id.zone_detail_ll_name);
        zone_detail_name = view.findViewById(R.id.zone_detail_name);
        zone_detail_devices = view.findViewById(R.id.zone_detail_devices);
        zone_detail_delete = view.findViewById(R.id.zone_detail_delete);

        zone_detail_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right_white_32dp, 0);
        zone_detail_devices.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right_white_32dp, 0);
    }

    @Override
    protected void initData() {
        mHomeViewModel = ViewModelProviders.of(getActivity()).get(HomeViewModel.class);
        mXHome = mHomeViewModel.getData();
        mHomeViewModel.observe(this, new Observer<XHome>() {
            @Override
            public void onChanged(@Nullable XHome xHome) {
                mXHome = xHome;
                refreshData();
            }
        });
        Bundle args = getArguments();
        if (args != null) {
            mZoneid = args.getString(AppConstants.ZONE_ID);
        }

        refreshData();
    }

    @Override
    protected void initEvent() {
        zone_detail_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        zone_detail_ll_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRenameDialog();
            }
        });

        zone_detail_devices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragmentToStack(R.id.main_fl, AddZoneDeviceFragment.newInstance(mZoneid));
            }
        });

        zone_detail_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteZoneDialog();
            }
        });
    }

    private void refreshData() {
        if (mXHome != null && mXHome.getHome() != null) {
            for (Home.Zone zone : mXHome.getHome().zones) {
                if (TextUtils.equals(mZoneid, zone.id)) {
                    zone_detail_toolbar.setTitle(zone.name);
                    zone_detail_name.setText(zone.name);
                    return;
                }
            }
        }
    }

    private void showRenameDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_rename, null, false);
        final TextInputLayout til = view.findViewById(R.id.dialog_rename_til);
        final TextInputEditText et_name = view.findViewById(R.id.dialog_rename_et);
        til.setHint(getString(R.string.habitat_name));
        et_name.setText(zone_detail_name.getText());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final AlertDialog dialog = builder.setTitle(R.string.rename_habitat)
                                          .setNegativeButton(R.string.cancel, null)
                                          .setPositiveButton(R.string.ok, null)
                                          .setView(view)
                                          .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = et_name.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    et_name.setError(getString(R.string.input_empty));
                    return;
                }
                XlinkCloudManager.getInstance().renameZone(mXHome.getHome().id, mZoneid, name, new XlinkRequestCallback<ZoneApi.ZoneResponse>() {
                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                             .show();
                    }

                    @Override
                    public void onSuccess(ZoneApi.ZoneResponse response) {
                        mHomeViewModel.refreshHomeInfo();
                    }
                });
                dialog.dismiss();
            }
        });
    }

    private void showDeleteZoneDialog() {
        if (mXHome == null || mXHome.getHome() == null) {
            return;
        }
        final String homeid = mXHome.getHome().id;
        final CharSequence zonename = zone_detail_toolbar.getTitle();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.delete_device)
               .setMessage(getString(R.string.msg_delete, zonename))
               .setNegativeButton(R.string.cancel, null)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       XlinkCloudManager.getInstance().deleteZone(homeid, mZoneid, new XlinkRequestCallback<String>() {
                           @Override
                           public void onError(String error) {
                               Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                                    .show();
                           }

                           @Override
                           public void onSuccess(String s) {
                               mHomeViewModel.refreshHomeInfo();
                               getActivity().getSupportFragmentManager().popBackStack();
                           }
                       });
                   }
               })
               .setCancelable(false)
               .show();
    }
}
