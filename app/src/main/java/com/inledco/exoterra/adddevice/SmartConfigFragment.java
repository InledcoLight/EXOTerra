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

public class SmartConfigFragment extends BaseFragment {

    private ImageView netconfig_prdt;
    private TextView netconfig_title;
    private CircleSeekbar netconfig_csb;
    private TextView netconfig_progress;
    private Button netconfig_back;

    private boolean mSubscribe;
    private SmartConfigLinker mSmartConfigLinker;
    private SmartConfigListener mSmartConfigListener = new SmartConfigListener() {
        @Override
        public void onProgressUpdate(final int progress) {
            netconfig_csb.setProgress(progress);
            netconfig_progress.setText("" + progress + " %");
        }

        @Override
        public void onError(final String error) {
            mConnectNetViewModel.getData().setRunning(false);
            mConnectNetViewModel.postValue();
            showSmartConfigFailedDialog(error);
        }

        @Override
        public void onSuccess(String dname, String mac) {
            mConnectNetViewModel.getData().setRunning(false);
            mConnectNetViewModel.getData().setDeviceName(dname);
            mConnectNetViewModel.getData().setAddress(mac);
            mConnectNetViewModel.postValue();
            if (mSubscribe) {
                DeviceManager.getInstance().getSubscribedDevices();
                getActivity().getSupportFragmentManager().popBackStack(null, 1);
                replaceFragment(R.id.adddevice_fl, new ConfigDeviceFragment());
            } else {
                showSmartConfigSuccessDialog();
            }
        }

        @Override
        public void onEsptouchSuccess() {
            RouterUtil.putRouterPassword(getContext(), mConnectNetViewModel.getData().getSsid(), mConnectNetViewModel.getData().getPassword());
        }

        @Override
        public void onSubscribe(String pkey, String dname) {
            DeviceManager.getInstance().getSubscribedDevices();
        }
    };

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
    public void onDestroyView() {
        super.onDestroyView();
        if (mSmartConfigLinker != null) {
            mSmartConfigLinker.stopTask();
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
        ConnectNetBean bean = mConnectNetViewModel.getData();

        ExoProduct product = ExoProduct.getExoProduct(bean.getProductKey());
        if (product != null) {
            netconfig_prdt.setImageResource(product.getIcon());
        }
        netconfig_title.setText(R.string.smartconfig);

        mSubscribe = UserManager.getInstance().isAuthorized();
        String pid = bean.getProductKey();
        String ssid = bean.getSsid();
        String gateway = bean.getBssid();
        String psw = bean.getPassword();
        mSmartConfigLinker = new SmartConfigLinker((AppCompatActivity) getActivity(), mSubscribe, pid, ssid, gateway, psw);
        mSmartConfigLinker.setSmartConfigListener(mSmartConfigListener);

        mSmartConfigLinker.startTask();
        mConnectNetViewModel.getData().setRunning(true);
        mConnectNetViewModel.postValue();
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

    private void showSmartConfigFailedDialog(String error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("SmartConfig Failed")
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
                       mConnectNetViewModel.getData().setCompatibleMode(true);
                       mConnectNetViewModel.postValue();
                       getActivity().getSupportFragmentManager().popBackStack();
                   }
               })
               .setCancelable(false)
               .show();
    }

    private void showSmartConfigSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Config Success")
               .setMessage(mConnectNetViewModel.getData().getAddress())
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
