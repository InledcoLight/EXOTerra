package com.liruya.exoterra.adddevice;

import android.net.wifi.WifiInfo;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.liruya.esptouch.EsptouchLinker;
import com.liruya.esptouch.EsptouchResult;
import com.liruya.esptouch.IEsptouchLinkListener;
import com.liruya.exoterra.xlink.IXlinkRegisterDeviceCallback;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkRequestCallback;
import com.liruya.exoterra.xlink.XlinkTaskCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import cn.xlink.restful.api.app.DeviceApi;
import cn.xlink.sdk.core.XLinkCoreException;
import cn.xlink.sdk.core.model.DataPointValueType;
import cn.xlink.sdk.core.model.XLinkDataPoint;
import cn.xlink.sdk.v5.listener.XLinkScanDeviceListener;
import cn.xlink.sdk.v5.model.XDevice;

public class SmartConfigLinker {
    private final String TAG = "SmartConfigLinker";

    private final int SCAN_DEVICE_TIMEOUT       = 15000;
    private final int SCAN_RETRY_INTERVAL       = 1000;
    private final int SUBSCRIBE_DEVICE_TIMEOUT  = 35000;
    private final int INITIAL_DATA_TIMEOUT      = 10000;

    private final int INDEX_ZONE                = 1;
    private final int INDEX_LONGITUDE           = 2;
    private final int INDEX_LATITUDE            = 3;

    private final int PROGRESS_ESPTOUCH         = 48;
    private final int PROGRESS_SCAN             = 60;
    private final int PROGRESS_REGISTER         = 64;
    private final int PROGRESS_SUBSCRIBE        = 92;
    private final int PROGRESS_INITIALIZE       = 100;

    /**
     * 配网60s  扫描15s  注册5s  订阅35s  初始化10s
     * 48%  60%  64%  92%  100%
     */

    private int mProgress;
    private final CountDownTimer mTimer;

    private String mProductId;
    private String mAddress;
    private String mSsid;
    private String mGateway;
    private String mPassword;
    private final EsptouchLinker mEsptouchLinker;
    private final IEsptouchLinkListener mLinkListener;

    private boolean mScanned;
    private final XLinkScanDeviceListener mScanListener;
    private final IXlinkRegisterDeviceCallback mRegisterLisener;
    private final XlinkTaskCallback<XDevice> mSubscribeListener;
    private final XlinkTaskCallback<XDevice> mDataListener;

    private final Handler mHandler;
    private SmartConfigListener mListener;

    public SmartConfigLinker(AppCompatActivity activity, String pid, String ssid, String gateway, String psw) {
        mProductId = pid;
        mSsid = ssid;
        mGateway = gateway;
        mPassword = psw;
        mHandler = new Handler();
        mEsptouchLinker = new EsptouchLinker(activity);
        mTimer = new CountDownTimer(125000, 1250) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (mProgress < 99) {
                    mProgress++;
                    if (mListener != null) {
                        mListener.onProgressUpdate(mProgress);
                    }
                }
            }

            @Override
            public void onFinish() {

            }
        };
        mLinkListener = new IEsptouchLinkListener() {
            @Override
            public void onWifiChanged(WifiInfo wifiInfo) {

            }

            @Override
            public void onLocationChanged() {

            }

            @Override
            public void onStart() {
                mTimer.start();
            }

            @Override
            public void onFailed() {
                mTimer.cancel();
                if (mListener != null) {
                    mListener.onEsptouchFailed();
                }
            }

            @Override
            public void onLinked(EsptouchResult result) {
                Log.e(TAG, "onLinked: " + result.getAddress() + " " + result.getInetAddress().getHostAddress() );
            }

            @Override
            public void onCompleted(List<EsptouchResult> results) {
                if (results == null || results.size() == 0) {
                    mTimer.cancel();
                    if (mListener != null) {
                        mListener.onEsptouchFailed();
                    }
                } else {
                    mProgress = PROGRESS_ESPTOUCH;
                    mAddress = results.get(0).getAddress();
                    scan();
                    if (mListener != null) {
                        mListener.onProgressUpdate(mProgress);
                        mListener.onEsptouchSuccess();
                    }
                }
            }
        };
        mScanListener = new XLinkScanDeviceListener() {
            @Override
            public void onScanResult(XDevice xDevice) {
                if (xDevice != null && TextUtils.equals(mAddress.toUpperCase(), xDevice.getMacAddress().toUpperCase())) {
                    mScanned = true;
                    mProgress = PROGRESS_SCAN;
                    register(xDevice);
                    if (mListener != null) {
                        mListener.onProgressUpdate(mProgress);
                        mListener.onScanSuccess();
                    }
                }
            }

            @Override
            public void onError(XLinkCoreException e) {
                if (mScanned == false) {
                    mTimer.cancel();
                    if (mListener != null) {
                        mListener.onScanError(e.getErrorName());
                    }
                }
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(Void aVoid) {

            }
        };
        mRegisterLisener = new IXlinkRegisterDeviceCallback() {
            @Override
            public void onDeviceAlreadyExists(XDevice xDevice) {
                mProgress = PROGRESS_REGISTER;
                subscribe(xDevice);
                if (mListener != null) {
                    mListener.onProgressUpdate(mProgress);
                    mListener.onRegisterSuccess();
                }
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onError(String error) {
                mTimer.cancel();
                if (mListener != null) {
                    mListener.onRegisterError(error);
                }
            }

            @Override
            public void onSuccess(XDevice xDevice) {
                mProgress = PROGRESS_REGISTER;
                initDevice(xDevice, SUBSCRIBE_DEVICE_TIMEOUT);
                if (mListener != null) {
                    mListener.onProgressUpdate(mProgress);
                    mListener.onRegisterSuccess();
                    mListener.onSubscribeSuccess();
                }
            }
        };
        mSubscribeListener = new XlinkTaskCallback<XDevice>() {
            @Override
            public void onError(String error) {
                mTimer.cancel();
                if (mListener != null) {
                    mListener.onSubscribeError(error);
                }
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(XDevice xDevice) {
                Log.e(TAG, "onComplete: " + xDevice.toJson() );
                mProgress = PROGRESS_SUBSCRIBE;
                initDevice(xDevice);
                if (mListener != null) {
                    mListener.onProgressUpdate(mProgress);
                    mListener.onSubscribeSuccess();
                }
            }
        };
        mDataListener = new XlinkTaskCallback<XDevice>() {
            @Override
            public void onError(String error) {
                mTimer.cancel();
                if (mListener != null) {
                    mListener.onInitDeviceError(error);
                }
            }

            @Override
            public void onStart() {
            }

            @Override
            public void onComplete(XDevice xDevice) {
                mTimer.cancel();
                mProgress = PROGRESS_INITIALIZE;
                if (mListener != null) {
                    mListener.onProgressUpdate(mProgress);
                    mListener.onInitDeviceSuccess();
                }
            }
        };
        mEsptouchLinker.setEsptouchLinkListener(mLinkListener);
    }

    public void start() {
        mScanned = false;
        mProgress = 0;
        if (mListener != null) {
            mListener.onProgressUpdate(mProgress);
        }
        mEsptouchLinker.start(mSsid, mGateway, mPassword, 1, true);
    }

    public void stop() {
        mTimer.cancel();
        mEsptouchLinker.stop();
    }

    private void scan() {
        XlinkCloudManager.getInstance()
                         .scanDevice(mProductId, SCAN_DEVICE_TIMEOUT, SCAN_RETRY_INTERVAL, mScanListener);
    }

    private void register(@NonNull XDevice xDevice) {
        XlinkCloudManager.getInstance().registerDevice(xDevice, mRegisterLisener);
    }

    private void subscribe(@NonNull final XDevice xDevice) {
        XlinkCloudManager.getInstance().subscribeDevice(xDevice, null, SUBSCRIBE_DEVICE_TIMEOUT, mSubscribeListener);
//        XlinkCloudManager.getInstance().subscribeDeviceBySn(mProductId, mAddress.toLowerCase(), new XlinkRequestCallback<DeviceApi.SnSubscribeResponse>() {
//            @Override
//            public void onStart() {
//                mSubscribeListener.onStart();
//            }
//
//            @Override
//            public void onError(String error) {
//                mSubscribeListener.onError(error);
//            }
//
//            @Override
//            public void onSuccess(DeviceApi.SnSubscribeResponse response) {
//                xDevice.setDeviceId(response.id);
//                mSubscribeListener.onComplete(xDevice);
//            }
//        });
    }

    private void initDevice(@NonNull final XDevice xDevice) {
        final int rawZone = TimeZone.getDefault().getRawOffset()/60000;
        final int zone = (rawZone/60)*100 + (rawZone%60);
        final List<XLinkDataPoint> dps = new ArrayList<>();
        final XLinkDataPoint dp1 = new XLinkDataPoint(INDEX_ZONE, DataPointValueType.SHORT, (short) zone);
        dps.add(dp1);
//        XlinkCloudManager.getInstance().setDeviceDatapoints(xDevice, dps, INITIAL_DATA_TIMEOUT, mDataListener);
        XlinkCloudManager.getInstance().getDeviceLocation(xDevice, new XlinkRequestCallback<DeviceApi.DeviceGeographyResponse>() {
            @Override
            public void onStart() {
                Log.e(TAG, "onStart: ");
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "onError: " + error);
                XlinkCloudManager.getInstance().setDeviceDatapoints(xDevice, dps, INITIAL_DATA_TIMEOUT, mDataListener);
            }

            @Override
            public void onSuccess(DeviceApi.DeviceGeographyResponse response) {
                Log.e(TAG, "onSuccess: " + response.lon + "  " + response.lat);
                final XLinkDataPoint dp2 = new XLinkDataPoint(INDEX_LONGITUDE, DataPointValueType.FLOAT, (float) response.lon);
                final XLinkDataPoint dp3 = new XLinkDataPoint(INDEX_LATITUDE, DataPointValueType.FLOAT, (float) response.lat);
                dps.add(dp2);
                dps.add(dp3);
                XlinkCloudManager.getInstance().setDeviceDatapoints(xDevice, dps, INITIAL_DATA_TIMEOUT, mDataListener);
            }
        });
    }

    private void initDevice(@NonNull final XDevice xDevice, int delay) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initDevice(xDevice);
            }
        }, delay);
    }

    public void setSmartConfigListener(SmartConfigListener listener) {
        mListener = listener;
    }
}
