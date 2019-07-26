package com.liruya.exoterra.adddevice;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.liruya.esptouch.EsptouchLinker;
import com.liruya.exoterra.bean.Result;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkRequestCallback;
import com.liruya.exoterra.xlink.XlinkTaskCallback;

import java.lang.ref.WeakReference;
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
    private final int SUBSCRIBE_DEVICE_TIMEOUT  = 30000;
    private final int INITIAL_DATA_TIMEOUT      = 10000;
    private final int WAIT_TIME                 = 5000;

    private final int INDEX_ZONE                = 1;
    private final int INDEX_LONGITUDE           = 2;
    private final int INDEX_LATITUDE            = 3;

    private final int PROGRESS_ESPTOUCH         = 48;
    private final int PROGRESS_SCAN             = 60;
    private final int PROGRESS_REGISTER         = 64;
    private final int PROGRESS_SUBSCRIBE        = 88;
    private final int PROGRESS_INITIALIZE       = 100;

    private final boolean mAllowUserRegisterDevice = false;

    /**
     * 配网60s  扫描15s  注册5s  (等待5s)  订阅25s  (等待5s)  初始化10s
     * 48%  60%  64%  88%  100%
     */

    private int mProgress;
    private final CountDownTimer mTimer;

    private final String mProductId;
    private String mAddress;
    private final String mSsid;
    private final String mBssid;
    private final String mPassword;
    private XDevice mXDevice;
    private final EsptouchLinker mEsptouchLinker;

    private WeakReference<AppCompatActivity> mActivity;

    private IEsptouchTask mEsptouchTask;

//    private final IEsptouchLinkListener mLinkListener;
//
//    private boolean mScanned;
//    private final XLinkScanDeviceListener mScanListener;
//    private final IXlinkRegisterDeviceCallback mRegisterLisener;
//    private final XlinkTaskCallback<XDevice> mSubscribeListener;
//    private final XlinkTaskCallback<XDevice> mDataListener;

    private final Handler mHandler;
    private SmartConfigListener mListener;

    private AsyncTask<Void, Integer, Result> mTask;

    public SmartConfigLinker(AppCompatActivity activity, String pid, String ssid, String bssid, String psw) {
        mProductId = pid;
        mSsid = ssid;
        mBssid = bssid;
        mPassword = psw;
        mHandler = new Handler();
        mActivity = new WeakReference<>(activity);
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
//        mLinkListener = new IEsptouchLinkListener() {
//            @Override
//            public void onWifiChanged(WifiInfo wifiInfo) {
//
//            }
//
//            @Override
//            public void onLocationChanged() {
//
//            }
//
//            @Override
//            public void onStart() {
//                mTimer.start();
//            }
//
//            @Override
//            public void onFailed() {
//                mTimer.cancel();
//                if (mListener != null) {
//                    mListener.onEsptouchFailed();
//                }
//            }
//
//            @Override
//            public void onLinked(EsptouchResult result) {
//                Log.e(TAG, "onLinked: " + result.getAddress() + " " + result.getInetAddress().getHostAddress() );
//            }
//
//            @Override
//            public void onCompleted(List<EsptouchResult> results) {
//                if (results == null || results.size() == 0) {
//                    mTimer.cancel();
//                    if (mListener != null) {
//                        mListener.onEsptouchFailed();
//                    }
//                } else {
//                    mProgress = PROGRESS_ESPTOUCH;
//                    mAddress = results.get(0).getAddress();
//                    scan();
//                    if (mListener != null) {
//                        mListener.onProgressUpdate(mProgress);
//                        mListener.onEsptouchSuccess();
//                    }
//                }
//            }
//        };
//        mScanListener = new XLinkScanDeviceListener() {
//            @Override
//            public void onScanResult(XDevice xDevice) {
//                if (xDevice != null && TextUtils.equals(mAddress.toUpperCase(), xDevice.getMacAddress().toUpperCase())) {
//                    mScanned = true;
//                    mProgress = PROGRESS_SCAN;
//                    register(xDevice);
//                    if (mListener != null) {
//                        mListener.onProgressUpdate(mProgress);
//                        mListener.onScanSuccess();
//                    }
//                }
//            }
//
//            @Override
//            public void onError(XLinkCoreException e) {
//                if (mScanned == false) {
//                    mTimer.cancel();
//                    if (mListener != null) {
//                        mListener.onScanError(e.getErrorName());
//                    }
//                }
//            }
//
//            @Override
//            public void onStart() {
//
//            }
//
//            @Override
//            public void onComplete(Void aVoid) {
//
//            }
//        };
//        mRegisterLisener = new IXlinkRegisterDeviceCallback() {
//            @Override
//            public void onDeviceAlreadyExists(XDevice xDevice) {
//                mProgress = PROGRESS_REGISTER;
//                subscribe(xDevice);
//                if (mListener != null) {
//                    mListener.onProgressUpdate(mProgress);
//                    mListener.onRegisterSuccess();
//                }
//            }
//
//            @Override
//            public void onStart() {
//
//            }
//
//            @Override
//            public void onError(String error) {
//                mTimer.cancel();
//                if (mListener != null) {
//                    mListener.onRegisterError(error);
//                }
//            }
//
//            @Override
//            public void onSuccess(XDevice xDevice) {
//                mProgress = PROGRESS_REGISTER;
//                initDevice(xDevice, WAIT_TIME + SUBSCRIBE_DEVICE_TIMEOUT);
//                if (mListener != null) {
//                    mListener.onProgressUpdate(mProgress);
//                    mListener.onRegisterSuccess();
//                    mListener.onSubscribeSuccess();
//                }
//            }
//        };
//        mSubscribeListener = new XlinkTaskCallback<XDevice>() {
//            @Override
//            public void onError(String error) {
//                mTimer.cancel();
//                if (mListener != null) {
//                    mListener.onSubscribeError(error);
//                }
//            }
//
//            @Override
//            public void onStart() {
//
//            }
//
//            @Override
//            public void onComplete(XDevice xDevice) {
//
//                mProgress = PROGRESS_SUBSCRIBE;
//                initDevice(xDevice, WAIT_TIME);
//                if (mListener != null) {
//                    mListener.onProgressUpdate(mProgress);
//                    mListener.onSubscribeSuccess();
//                }
//            }
//        };
//        mDataListener = new XlinkTaskCallback<XDevice>() {
//            @Override
//            public void onError(String error) {
//                mTimer.cancel();
//                if (mListener != null) {
//                    mListener.onInitDeviceError(error);
//                }
//            }
//
//            @Override
//            public void onStart() {
//            }
//
//            @Override
//            public void onComplete(XDevice xDevice) {
//                mTimer.cancel();
//                mProgress = PROGRESS_INITIALIZE;
//                if (mListener != null) {
//                    mListener.onProgressUpdate(mProgress);
//                    mListener.onInitDeviceSuccess();
//                }
//            }
//        };
//        mEsptouchLinker.setEsptouchLinkListener(mLinkListener);
    }

//    public void start() {
//        mScanned = false;
//        mProgress = 0;
//        if (mListener != null) {
//            mListener.onProgressUpdate(mProgress);
//        }
//        mEsptouchLinker.start(mSsid, mBssid, mPassword, 1, false);
//    }
//
//    public void stop() {
//        mTimer.cancel();
//        mEsptouchLinker.stop();
//    }
//
//    private void scan() {
//        XlinkCloudManager.getInstance()
//                         .scanDevice(mProductId, SCAN_DEVICE_TIMEOUT, SCAN_RETRY_INTERVAL, mScanListener);
//    }
//
//    private void register(@NonNull final XDevice xDevice) {
//        XlinkCloudManager.getInstance().registerDevice(xDevice, mRegisterLisener);
//    }
//
//    private void subscribe(@NonNull final XDevice xDevice) {
//        /**
//         * 增加延时避免订阅失败
//         */
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                XlinkCloudManager.getInstance().subscribeDevice(xDevice, null, SUBSCRIBE_DEVICE_TIMEOUT, mSubscribeListener);
//            }
//        }, WAIT_TIME);
//    }

//    private void initDevice(@NonNull final XDevice xDevice) {
//        final int rawZone = TimeZone.getDefault().getRawOffset()/60000;
//        final int zone = (rawZone/60)*100 + (rawZone%60);
//        final List<XLinkDataPoint> dps = new ArrayList<>();
//        final XLinkDataPoint dp1 = new XLinkDataPoint(INDEX_ZONE, DataPointValueType.SHORT, (short) zone);
//        dps.add(dp1);
//        XlinkCloudManager.getInstance().getDeviceLocation(xDevice, new XlinkRequestCallback<DeviceApi.DeviceGeographyResponse>() {
//            @Override
//            public void onStart() {
//            }
//
//            @Override
//            public void onError(String error) {
//                XlinkCloudManager.getInstance().setDeviceDatapoints(xDevice, dps, INITIAL_DATA_TIMEOUT, mDataListener);
//            }
//
//            @Override
//            public void onSuccess(DeviceApi.DeviceGeographyResponse response) {
//                final XLinkDataPoint dp2 = new XLinkDataPoint(INDEX_LONGITUDE, DataPointValueType.FLOAT, (float) response.lon);
//                final XLinkDataPoint dp3 = new XLinkDataPoint(INDEX_LATITUDE, DataPointValueType.FLOAT, (float) response.lat);
//                dps.add(dp2);
//                dps.add(dp3);
//                XlinkCloudManager.getInstance().setDeviceDatapoints(xDevice, dps, INITIAL_DATA_TIMEOUT, mDataListener);
//            }
//        });
//    }
//
//    private void initDevice(@NonNull final XDevice xDevice, int delay) {
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                initDevice(xDevice);
//            }
//        }, delay);
//    }

    public void setSmartConfigListener(SmartConfigListener listener) {
        mListener = listener;
    }

    private Result esptouch() {
        mEsptouchTask = new EsptouchTask(mSsid, mBssid, mPassword, mActivity.get());
        mEsptouchTask.setPackageBroadcast(false);
        IEsptouchResult esptouchResult = mEsptouchTask.executeForResult();
        if (esptouchResult == null || esptouchResult.isCancelled() || !esptouchResult.isSuc()) {
            return new Result(false, "Esptouch failed.");
        }
        mAddress = esptouchResult.getBssid();
        Log.e(TAG, "esptouch: " + mAddress);
        return new Result(true, null);
    }

    private Result scan() {
        final boolean[] result = new boolean[] {false, false};
        final String[] error = new String[]{null};
        XLinkScanDeviceListener listener = new XLinkScanDeviceListener() {
            @Override
            public void onScanResult(XDevice xDevice) {
                if (xDevice != null && mAddress.toUpperCase().equals(xDevice.getMacAddress().toUpperCase())) {
                    result[0] = true;
                    result[1] = true;
                    mXDevice = xDevice;
                }
            }

            @Override
            public void onError(XLinkCoreException e) {
                result[0] = true;
                result[1] = false;
                error[0] = e.getErrorName();
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(Void aVoid) {

            }
        };
        XlinkCloudManager.getInstance().scanDevice(mProductId, SCAN_DEVICE_TIMEOUT, SCAN_RETRY_INTERVAL, listener);
        while(!result[0]);
        Result res = new Result(result[1], error[0]);
        Log.e(TAG, "scan: " + error[0] + " " + res.getError() + " " + mXDevice.getSN());
        return res;
    }

    private Result addDevice() {
        final boolean[] result = new boolean[]{false, false};
        final String[] error = new String[]{null};
        XlinkTaskCallback<XDevice> listener = new XlinkTaskCallback<XDevice>() {
            @Override
            public void onError(String er) {
                result[0] = true;
                result[1] = false;
                error[0] = er;
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(XDevice device) {
                result[0] = true;
                result[1] = true;
            }
        };
        XlinkCloudManager.getInstance().addDevice(mXDevice, 10000, listener);
        while (!result[0]);
        Result res = new Result(result[1], error[0]);
        Log.e(TAG, "addDevice: " + error[0] + " " + res.getError());
        return res;
    }

    private Result setZone() {
        final boolean[] result = new boolean[]{false, false};
        final String[] error = new String[]{null};
        XlinkTaskCallback<XDevice> listener = new XlinkTaskCallback<XDevice>() {
            @Override
            public void onError(String er) {
                result[0] = true;
                result[1] = false;
                error[0] = er;
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(XDevice device) {
                result[0] = true;
                result[1] = true;
            }
        };
        final int rawZone = TimeZone.getDefault().getRawOffset()/60000;
        final int zone = (rawZone/60)*100 + (rawZone%60);
        final List<XLinkDataPoint> dps = new ArrayList<>();
        final XLinkDataPoint dp1 = new XLinkDataPoint(INDEX_ZONE, DataPointValueType.SHORT, (short) zone);
        dps.add(dp1);
        XlinkCloudManager.getInstance().setDeviceDatapoints(mXDevice, dps, listener);
        while (!result[0]);
        Result res = new Result(result[1], error[0]);
        Log.e(TAG, "setZone: " + error[0] + "  " + res.getError());
        return res;
    }

    private Result removeDevice() {
        final boolean[] result = new boolean[]{false, false};
        final String[] error = new String[]{null};
        XlinkTaskCallback<String> listener = new XlinkTaskCallback<String>() {
            @Override
            public void onError(String er) {
                result[0] = true;
                result[1] = false;
                error[0] = er;
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(String str) {
                result[0] = true;
                result[1] = true;
            }
        };
        XlinkCloudManager.getInstance().unsubscribeDevice(mXDevice, listener);
        while (!result[0]);
        Result res = new Result(result[1], error[0]);
        Log.e(TAG, "addDevice: " + error[0] + " " + res.getError());
        return res;
    }

    private Result subscribe() {
        final boolean[] result = new boolean[]{false, false};
        final String[] error = new String[]{null};
        XlinkTaskCallback<XDevice> listener = new XlinkTaskCallback<XDevice>() {
            @Override
            public void onError(String er) {
                result[0] = true;
                result[1] = false;
                error[0] = er;
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(XDevice device) {
                result[0] = true;
                result[1] = true;
            }
        };
        XlinkCloudManager.getInstance().subscribeDevice(mXDevice, null, SUBSCRIBE_DEVICE_TIMEOUT, listener);
        while (!result[0]);
        Result res = new Result(result[1], error[0]);
        Log.e(TAG, "subscribe: " + error[0] + " " + res.getError());
        return res;
    }

    private Result subscribeBysn() {
        final boolean[] result = new boolean[]{false, false};
        final String[] error = new String[]{null};
        XlinkRequestCallback<DeviceApi.SnSubscribeResponse> listener = new XlinkRequestCallback<DeviceApi.SnSubscribeResponse>() {
            @Override
            public void onError(String er) {
                result[0] = true;
                result[1] = false;
                error[0] = er;
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(DeviceApi.SnSubscribeResponse response) {
                result[0] = true;
                result[1] = true;
            }
        };
        XlinkCloudManager.getInstance().subscribeDeviceBySn(mProductId, mAddress.toLowerCase(), listener);
        while (!result[0]);
        Result res = new Result(result[1], error[0]);
        Log.e(TAG, "subscribeBysn: " + error[0] + " " + res.getError());
        return res;
    }

    public void startTask() {
        mProgress = 0;
        mTask = new AsyncTask<Void, Integer, Result>() {
            @Override
            protected Result doInBackground(Void... voids) {
                Result result = esptouch();
                if (!result.isSuccess()) {
                    return result;
                }
                publishProgress(48);
//                result = scan();
//                if (!result.isSuccess()) {
//                    return result;
//                }
//                publishProgress(60);
//                result = addDevice();
//                if (!result.isSuccess()) {
//                    return result;
//                }
//                publishProgress(72);
//                result = setZone();
//                if (!result.isSuccess()) {
//                    return result;
//                }
////                publishProgress(76);
//                while (mProgress < 76);
//                result = subscribe();
                return result;
            }

            @Override
            protected void onPostExecute(Result result) {
                super.onPostExecute(result);
                mTimer.cancel();
                mEsptouchTask.interrupt();
                Log.e(TAG, "onPostExecute: " + result.isSuccess() + " " + result.getError());
                if (result.isSuccess()) {
                    if (mListener != null) {
                        mProgress = 100;
                        mListener.onProgressUpdate(mProgress);
                        mListener.onSuccess();
                    }
                } else {
                    if (mListener != null) {
                        mListener.onError(result.getError());
                    }
                }
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                if (values == null || values.length == 0) {
                    return;
                }
                mProgress = values[0];
                if (mListener != null) {
                    mListener.onProgressUpdate(mProgress);
                }
            }
        };
        try {
            Log.e(TAG, "startTask: 111");
            mTask.execute();
            Log.e(TAG, "startTask: 222");
        } catch (Exception e) {
            e.printStackTrace();
        }
        mTimer.start();
    }

    public void stopTask() {
//        mTimer.cancel();
//        mEsptouchTask.interrupt();
//        if (mTask != null) {
//            mTask.cancel(true);
//            mTask = null;
//        }
    }
}
