package com.inledco.exoterra.adddevice;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.ExoProduct;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.manager.UserManager;
import com.inledco.exoterra.util.RouterUtil;
import com.inledco.exoterra.view.CircleSeekbar;


public class CompatibleModeFragment extends BaseFragment {
    private ImageView netconfig_prdt;
    private TextView netconfig_title;
    private CircleSeekbar netconfig_csb;
    private TextView netconfig_progress;
    private Button netconfig_back;

    private ConnectNetViewModel mConnectNetViewModel;
    private ConnectNetBean mConnectNetBean;

    private boolean mSubscribe;
    private APConfigLinker mAPConfigLinker;
    private APConfigLinker.APConfigListener mListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initData();
        initEvent();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mAPConfigLinker != null) {
            mAPConfigLinker.stopTask();
            mConnectNetViewModel.getData().setRunning(false);
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_netconfig;
    }

    @Override
    protected void initView(View view) {
        netconfig_prdt = view.findViewById(R.id.netconfig_prdt);
        netconfig_title = view.findViewById(R.id.netconfig_title);
        netconfig_csb = view.findViewById(R.id.netconfig_csb);
        netconfig_progress = view.findViewById(R.id.netconfig_progress);
        netconfig_back = view.findViewById(R.id.netconfig_back);
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
            netconfig_prdt.setImageResource(product.getIcon());
        }
        netconfig_title.setText(R.string.compatible_mode_msg);

        mConnectNetBean.setRunning(true);
        mConnectNetViewModel.postValue();
        mSubscribe = UserManager.getInstance().isAuthorized();
        mAPConfigLinker = new APConfigLinker(mSubscribe,
                                             mConnectNetBean.getProductKey(),
                                             mConnectNetBean.getSsid(),
                                             mConnectNetBean.getBssid(),
                                             mConnectNetBean.getPassword(),
                                             mConnectNetBean.getNetworkId(),
                                             (AppCompatActivity) getActivity());
        mListener = new APConfigLinker.APConfigListener() {
            @Override
            public void onProgressUpdate(int progress) {
                netconfig_csb.setProgress(progress);
                netconfig_progress.setText("" + progress + " %");
            }

            @Override
            public void onTimeout() {
                mConnectNetBean.setRunning(false);
                mConnectNetViewModel.postValue();
                showAPConfigFailedDialog(getString(R.string.timeout));
            }

            @Override
            public void onAPConfigSuccess(String dname, String mac) {
                mConnectNetBean.setRunning(false);
                mConnectNetBean.setDeviceName(dname);
                mConnectNetBean.setAddress(mac);
                mConnectNetViewModel.postValue();
                RouterUtil.putRouterPassword(getContext(), mConnectNetViewModel.getData().getSsid(), mConnectNetViewModel.getData().getPassword());
                if (mSubscribe) {
                    DeviceManager.getInstance().getSubscribedDevices();
                    getActivity().getSupportFragmentManager().popBackStack(null, 1);
                    addFragmentToStack(R.id.adddevice_fl, new ConfigDeviceFragment());
                } else {
                    showAPConfigSuccessDialog();
                }
            }

            @Override
            public void onAPConfigFailed(String error) {
                mConnectNetBean.setRunning(false);
                mConnectNetViewModel.postValue();
                showAPConfigFailedDialog(error);
            }
        };
        mAPConfigLinker.setListener(mListener);
        mAPConfigLinker.startTask();
    }

    @Override
    protected void initEvent() {
        netconfig_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private void showAPConfigFailedDialog(String error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Config Failed")
               .setMessage(error)
               .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       getActivity().getSupportFragmentManager().popBackStack();
                   }
               })
               .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       getActivity().finish();
                   }
               })
               .setNeutralButton("Change config mode", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       mConnectNetViewModel.getData().setCompatibleMode(false);
                       mConnectNetViewModel.postValue();
                       getActivity().getSupportFragmentManager().popBackStack();
                   }
               })
               .setCancelable(false)
               .show();
    }

    private void showAPConfigSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Config Success")
               .setMessage(mConnectNetBean.getAddress())
               .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       getActivity().finish();
                   }
               })
               .setCancelable(false)
               .show();
    }
}
