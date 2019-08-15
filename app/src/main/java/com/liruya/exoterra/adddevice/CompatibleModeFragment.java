package com.liruya.exoterra.adddevice;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.liruya.base.BaseFragment;
import com.liruya.exoterra.R;
import com.liruya.exoterra.event.SubscribeChangedEvent;

import org.greenrobot.eventbus.EventBus;

public class CompatibleModeFragment extends BaseFragment {
    private DonutProgress apconfig_pb;

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
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_compatible_mode;
    }

    @Override
    protected void initView(View view) {
        apconfig_pb = view.findViewById(R.id.apconfig_pb);
    }

    @Override
    protected void initData() {
        mConnectNetViewModel = ViewModelProviders.of(getActivity()).get(ConnectNetViewModel.class);
        mConnectNetBean = mConnectNetViewModel.getData();
        if (mConnectNetBean == null) {
            return;
        }
        mConnectNetBean.setRunning(true);
        mConnectNetViewModel.postValue();
        mAPConfigLinker = new APConfigLinker(mConnectNetBean.getProductId(), mConnectNetBean.getSsid(), mConnectNetBean.getBssid(),
                                             mConnectNetBean.getPassword());
        mListener = new APConfigLinker.APConfigListener() {
            @Override
            public void onProgressUpdate(int progress) {
                apconfig_pb.setProgress(progress);
                apconfig_pb.setText("" + progress + " %");
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
            public void onAPConfigSuccess() {
                mConnectNetBean.setRunning(false);
                mConnectNetViewModel.postValue();
                showAPConfigSuccessDialog();
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
               .setCancelable(false)
               .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       getActivity().getSupportFragmentManager()
                                    .popBackStack();
                   }
               })
               .show();
    }
}
