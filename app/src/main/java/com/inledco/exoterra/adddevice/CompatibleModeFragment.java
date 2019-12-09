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
import android.widget.ImageView;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.event.SubscribeChangedEvent;
import com.inledco.exoterra.util.DeviceUtil;
import com.inledco.exoterra.util.RouterUtil;

import org.greenrobot.eventbus.EventBus;

import cn.xlink.sdk.v5.manager.XLinkUserManager;

public class CompatibleModeFragment extends BaseFragment {
    private ImageView netconfig_prdt;
    private TextView netconfig_title;
    private DonutProgress netconfig_pb;

    private ConnectNetViewModel mConnectNetViewModel;
    private ConnectNetBean mConnectNetBean;

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
        netconfig_pb = view.findViewById(R.id.netconfig_pb);
    }

    @Override
    protected void initData() {
        mConnectNetViewModel = ViewModelProviders.of(getActivity()).get(ConnectNetViewModel.class);
        mConnectNetBean = mConnectNetViewModel.getData();
        if (mConnectNetBean == null) {
            return;
        }
        netconfig_prdt.setImageResource(DeviceUtil.getProductIcon(mConnectNetBean.getProductId()));
        netconfig_title.setText(R.string.compatible_mode);

        mConnectNetBean.setRunning(true);
        mConnectNetViewModel.postValue();
        boolean subscribe = XLinkUserManager.getInstance().isUserAuthorized();
        mAPConfigLinker = new APConfigLinker(subscribe, mConnectNetBean.getProductId(), mConnectNetBean.getSsid(), mConnectNetBean.getBssid(),
                                             mConnectNetBean.getPassword(), (AppCompatActivity) getActivity());
        mListener = new APConfigLinker.APConfigListener() {
            @Override
            public void onProgressUpdate(int progress) {
                netconfig_pb.setProgress(progress);
                netconfig_pb.setText("" + progress + " %");
            }

//            @Override
//            public void onSubscribeFailed(String error) {
//                mConnectNetBean.setRunning(false);
//                mConnectNetViewModel.postValue();
//                showAPConfigFailedDialog(error);
//            }

            @Override
            public void onTimeout() {
                mConnectNetBean.setRunning(false);
                mConnectNetViewModel.postValue();
                showAPConfigFailedDialog(getString(R.string.timeout));
            }

            @Override
            public void onAPConfigSuccess(int devid, String mac) {
                mConnectNetBean.setRunning(false);
                mConnectNetBean.setResultDevid(devid);
                mConnectNetBean.setResultAddress(mac);
                mConnectNetViewModel.postValue();
//                showAPConfigSuccessDialog();
                RouterUtil.putRouterPassword(getContext(), mConnectNetViewModel.getData().getSsid(), mConnectNetViewModel.getData().getPassword());
                addFragmentToStack(R.id.adddevice_fl, new ConfigDeviceFragment());
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

    }

    private void showAPConfigSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Config Success")
               .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       EventBus.getDefault().post(new SubscribeChangedEvent());
                       getActivity().finish();
                   }
               })
               .show();
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
}
