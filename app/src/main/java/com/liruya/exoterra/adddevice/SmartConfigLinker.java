package com.liruya.exoterra.adddevice;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.liruya.exoterra.bean.Result;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkTaskHandler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import cn.xlink.sdk.core.XLinkCoreException;
import cn.xlink.sdk.core.model.DataPointValueType;
import cn.xlink.sdk.core.model.XLinkDataPoint;
import cn.xlink.sdk.v5.listener.XLinkScanDeviceListener;
import cn.xlink.sdk.v5.model.XDevice;

public class SmartConfigLinker {
    private final String TAG = "SmartConfigLinker";

    private final int SCAN_DEVICE_TIMEOUT       = 8000;
    private final int SCAN_RETRY_INTERVAL       = 1000;
    private final int ADD_DEVICE_TIMEOUT        = 10000;
    private final int SET_ZONE_TIMEOUT          = 5000;
    private final int SUBSCRIBE_DEVICE_TIMEOUT  = 15000;

    private final int INDEX_ZONE                = 1;
    private final int INDEX_LONGITUDE           = 2;
    private final int INDEX_LATITUDE            = 3;

    private final int PROGRESS_ESPTOUCH         = 60;
    private final int PROGRESS_SCAN             = 68;
    private final int PROGRESS_ADDDEVICE        = 78;
    private final int PROGRESS_SET_ZONE         = 85;
    private final int PROGRESS_SUCCESS          = 100;

    private int mProgress;
    private final CountDownTimer mTimer;

    private final String mProductId;
    private String mAddress;
    private final String mSsid;
    private final String mBssid;
    private final String mPassword;
    private XDevice mXDevice;

    private WeakReference<AppCompatActivity> mActivity;

    private final Object mLock;

    private IEsptouchTask mEsptouchTask;

    private SmartConfigListener mListener;

    private AsyncTask<Void, Integer, Result> mTask;

    public SmartConfigLinker(AppCompatActivity activity, String pid, String ssid, String bssid, String psw) {
        mProductId = pid;
        mSsid = ssid;
        mBssid = bssid;
        mPassword = psw;
        mActivity = new WeakReference<>(activity);
        mLock = new Object();
        mTimer = new CountDownTimer(100000, 1000) {
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
                stopTask();
                if (mListener != null) {
                    mListener.onError("Timeout");
                }
            }
        };
    }

    public void setSmartConfigListener(SmartConfigListener listener) {
        mListener = listener;
    }

    private Result esptouch() {
        synchronized (mLock) {
            mEsptouchTask = new EsptouchTask(mSsid, mBssid, mPassword, mActivity.get());
            mEsptouchTask.setPackageBroadcast(false);
        }
        IEsptouchResult esptouchResult = mEsptouchTask.executeForResult();
        if (esptouchResult == null || esptouchResult.isCancelled() || !esptouchResult.isSuc()) {
            return new Result(false, "Esptouch failed.");
        }
        mAddress = esptouchResult.getBssid();
        return new Result(true, null);
    }

    private Result scan() {
        final TaskResult result = new TaskResult();
        XLinkScanDeviceListener listener = new XLinkScanDeviceListener() {
            @Override
            public void onScanResult(XDevice xDevice) {
                if (xDevice != null && mAddress.toUpperCase().equals(xDevice.getMacAddress().toUpperCase())) {
                    mXDevice = xDevice;
                    result.setSuccess(true);
                    result.setOver(true);
                }
            }

            @Override
            public void onError(XLinkCoreException e) {
                result.setError("error code: " + e.getErrorCode() + "\n" + e.getErrorName());
                result.setSuccess(false);
                result.setOver(true);
            }

            @Override
            public void onStart() {
                Log.e(TAG, "Scan - onStart: ");
            }

            @Override
            public void onComplete(Void aVoid) {
                Log.e(TAG, "onComplete: ");
                result.setError("Scan timeout");
                result.setSuccess(false);
                result.setOver(true);
            }
        };
        XlinkCloudManager.getInstance().scanDevice(mProductId, SCAN_DEVICE_TIMEOUT, SCAN_RETRY_INTERVAL, listener);
        while(!result.isOver());
        Result res = new Result(result.isSuccess(), result.getError());
        return res;
    }

    private Result addDevice() {
        XlinkTaskHandler<XDevice> listener = new XlinkTaskHandler<>();
        XlinkCloudManager.getInstance().addDevice(mXDevice, ADD_DEVICE_TIMEOUT, listener);
        while (!listener.isOver());
        Result res = new Result(listener.isSuccess(), listener.getError());
        return res;
    }

    private Result setZone() {
        XlinkTaskHandler<XDevice> listener = new XlinkTaskHandler<>();
        final int rawZone = TimeZone.getDefault().getRawOffset()/60000;
        final int zone = (rawZone/60)*100 + (rawZone%60);
        final List<XLinkDataPoint> dps = new ArrayList<>();
        final XLinkDataPoint dp1 = new XLinkDataPoint(INDEX_ZONE, DataPointValueType.SHORT, (short) zone);
        dps.add(dp1);
        XlinkCloudManager.getInstance().setDeviceDatapoints(mXDevice, dps, SET_ZONE_TIMEOUT, listener);
        while (!listener.isOver());
        Result res = new Result(listener.isSuccess(), listener.getError());
        return res;
    }

    private Result subscribe() {
        XlinkTaskHandler<XDevice> listener = new XlinkTaskHandler<>();
        XlinkCloudManager.getInstance().subscribeDevice(mXDevice, null, SUBSCRIBE_DEVICE_TIMEOUT, listener);
        while (!listener.isOver());
        Result res = new Result(listener.isSuccess(), listener.getError());
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
                publishProgress(PROGRESS_ESPTOUCH);
                result = scan();
                if (!result.isSuccess()) {
                    return result;
                }
                publishProgress(PROGRESS_SCAN);
                result = addDevice();
                if (!result.isSuccess()) {
                    return result;
                }
                publishProgress(PROGRESS_ADDDEVICE);
                try {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                result = setZone();
                if (!result.isSuccess()) {
                    return result;
                }
                while (mProgress < PROGRESS_SET_ZONE);
                publishProgress(PROGRESS_SET_ZONE);
                result = subscribe();
                return result;
            }

            @Override
            protected void onPostExecute(Result result) {
                super.onPostExecute(result);
                mTimer.cancel();
                mEsptouchTask.interrupt();
                mEsptouchTask = null;
                Log.e(TAG, "onPostExecute: " + result.isSuccess() + " " + result.getError());
                if (mListener != null) {
                    if (result.isSuccess()) {
                        mProgress = PROGRESS_SUCCESS;
                        mListener.onProgressUpdate(mProgress);
                        mListener.onSuccess();
                    } else {
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
                    if (values[0] == PROGRESS_ESPTOUCH) {
                        mListener.onEsptouchSuccess();
                    } else if (values[0] == PROGRESS_SCAN) {
                        mListener.onDeviceScanned();
                    } else if (values[0] == PROGRESS_SET_ZONE) {
                        mListener.onDeviceInitialized();
                    }
                    mListener.onProgressUpdate(mProgress);
                }
            }
        };
        mTask.execute();
        mTimer.start();
    }

    public void stopTask() {
        mTimer.cancel();
        if (mEsptouchTask != null) {
            mEsptouchTask.interrupt();
            mEsptouchTask = null;
        }
        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }
    }

    class TaskResult {
        private boolean mOver;
        private boolean mSuccess;
        private String mError;

        public boolean isOver() {
            return mOver;
        }

        public void setOver(boolean over) {
            mOver = over;
        }

        public boolean isSuccess() {
            return mSuccess;
        }

        public void setSuccess(boolean success) {
            mSuccess = success;
        }

        public String getError() {
            return mError;
        }

        public void setError(String error) {
            mError = error;
        }
    }
}
