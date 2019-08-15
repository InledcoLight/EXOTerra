package com.liruya.exoterra.adddevice;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CheckableImageButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.liruya.base.BaseFragment;
import com.liruya.exoterra.R;
import com.liruya.exoterra.event.SubscribeChangedEvent;

import org.greenrobot.eventbus.EventBus;

public class SmartConfigFragment extends BaseFragment {

    private DonutProgress smart_config_pb;
    private CheckableImageButton smart_config_cib_step1;
    private CheckableImageButton smart_config_cib_step2;
    private CheckableImageButton smart_config_cib_step3;
    private CheckableImageButton smart_config_cib_step4;
    private CheckBox smart_config_led;
    private ToggleButton smart_config_start;

    private SmartConfigLinker mSmartConfigLinker;
    private SmartConfigListener mSmartConfigListener = new SmartConfigListener() {
        @Override
        public void onProgressUpdate(final int progress) {
            smart_config_pb.setProgress(progress);
            smart_config_pb.setText("" + progress + " %");
        }

        @Override
        public void onError(final String error) {
            smart_config_start.setChecked(false);
            smart_config_start.setEnabled(true);
            showSmartConfigFailedDialog(error);
        }

        @SuppressLint ("RestrictedApi")
        @Override
        public void onSuccess() {
            smart_config_cib_step4.setChecked(true);
            smart_config_start.setChecked(false);
            smart_config_start.setEnabled(true);
            showSmartConfigSuccessDialog();
        }

        @SuppressLint ("RestrictedApi")
        @Override
        public void onEsptouchSuccess() {
            smart_config_cib_step1.setChecked(true);
            smart_config_start.setEnabled(false);
        }

        @SuppressLint ("RestrictedApi")
        @Override
        public void onDeviceScanned() {
            smart_config_cib_step2.setChecked(true);
        }

        @SuppressLint ("RestrictedApi")
        @Override
        public void onDeviceInitialized() {
            smart_config_cib_step3.setChecked(true);
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
    protected int getLayoutRes() {
        return R.layout.fragment_smart_config;
    }

    @Override
    protected void initView(View view) {
        smart_config_pb = view.findViewById(R.id.smart_config_pb);
        smart_config_cib_step1 = view.findViewById(R.id.smart_config_cib_step1);
        smart_config_cib_step2 = view.findViewById(R.id.smart_config_cib_step2);
        smart_config_cib_step3 = view.findViewById(R.id.smart_config_cib_step3);
        smart_config_cib_step4 = view.findViewById(R.id.smart_config_cib_step4);
        smart_config_led = view.findViewById(R.id.smart_config_led);
        smart_config_start = view.findViewById(R.id.smart_config_start);
    }

    @Override
    protected void initData() {
        mConnectNetViewModel = ViewModelProviders.of(getActivity()).get(ConnectNetViewModel.class);
        mConnectNetViewModel.observe(this, new Observer<ConnectNetBean>() {
            @SuppressLint ("RestrictedApi")
            @Override
            public void onChanged(@Nullable ConnectNetBean connectNetBean) {
                boolean isRunning = connectNetBean.isRunning();
                smart_config_led.setEnabled(!isRunning);
            }
        });

        ConnectNetBean bean = mConnectNetViewModel.getData();
        String pid = bean.getProductId();
        String ssid = bean.getSsid();
        String gateway = bean.getBssid();
        String psw = bean.getPassword();
        mSmartConfigLinker = new SmartConfigLinker((AppCompatActivity) getActivity(), pid, ssid, gateway, psw);
        mSmartConfigLinker.setSmartConfigListener(mSmartConfigListener);
    }

    @Override
    protected void initEvent() {
        smart_config_led.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                smart_config_start.setEnabled(isChecked);
            }
        });
        smart_config_start.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint ("RestrictedApi")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    smart_config_cib_step1.setChecked(false);
                    smart_config_cib_step2.setChecked(false);
                    smart_config_cib_step3.setChecked(false);
                    smart_config_cib_step4.setChecked(false);
                    mSmartConfigLinker.startTask();
                } else {
                    mSmartConfigLinker.stopTask();
                }
                mConnectNetViewModel.getData().setRunning(isChecked);
                mConnectNetViewModel.postValue();
            }
        });
    }

    private void runOnUiThread(Runnable runnable) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(runnable);
        }
    }

    private void showSmartConfigSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("SmartConfig Success")
               .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       EventBus.getDefault().post(new SubscribeChangedEvent());
                       getActivity().finish();
                   }
               })
               .show();
    }

    private void showSmartConfigFailedDialog(String error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("SmartConfig Failed")
               .setMessage(error)
               .show();
    }
}
