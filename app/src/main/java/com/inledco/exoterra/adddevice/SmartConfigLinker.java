package com.inledco.exoterra.adddevice;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.inledco.exoterra.R;
import com.inledco.exoterra.bean.ImportDeviceResponse;
import com.inledco.exoterra.bean.QueryDeviceResponse;
import com.inledco.exoterra.bean.Result;
import com.inledco.exoterra.manager.HomeManager;
import com.inledco.exoterra.xlink.RoomApi;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkResult;
import com.inledco.exoterra.xlink.XlinkTaskHandler;

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
        mAddress = esptouchResult.getBssid().toUpperCase();
        return new Result(true, null);
    }

    private Result scan() {
        final TaskResult result = new TaskResult();
        XLinkScanDeviceListener listener = new XLinkScanDeviceListener() {
            @Override
            public void onScanResult(XDevice xDevice) {
                if (xDevice != null && TextUtils.equals(mAddress, xDevice.getMacAddress().toUpperCase())) {
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

    private boolean checkDeviceRegistered() {
        QueryDeviceResponse response = XlinkCloudManager.getInstance().queryDeviceBlock(mProductId, mAddress);
        if (response == null || !response.isValid()) {
            return false;
        }
        return true;
    }

    private Result registerDevice() {
        Result result = new Result();
        ImportDeviceResponse response = XlinkCloudManager.getInstance()
                                                         .registerDeviceBlock(mProductId, null, mAddress, mAddress.toLowerCase());
        if (response != null && response.isValid()) {
            if (response.getResp().getErrcode() == 0) {
                result.setSuccess(true);
            } else {
                result.setError(response.getResp().getErrmsg());
            }
        } else {
            result.setError(mActivity.get().getString(R.string.error_regdev_failed));
        }
        return result;
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
        XlinkTaskHandler<XDevice> listener = new XlinkTaskHandler<XDevice>() {
            @Override
            public void onComplete(XDevice device) {
                super.onComplete(device);
                mXDevice = device;
            }
        };
        XlinkCloudManager.getInstance().subscribeDevice(mXDevice, null, SUBSCRIBE_DEVICE_TIMEOUT, listener);
        while (!listener.isOver());
        Result res = new Result(listener.isSuccess(), listener.getError());
        return res;
    }

    private Result addDeviceToHomeAndRoom() {
        Result result = new Result();
        final String homeid = HomeManager.getInstance().getCurrentHomeId();
        final int devid = mXDevice.getDeviceId();

        // 将设备添加到当前Home
        XlinkResult<String> result1 = XlinkCloudManager.getInstance().addDeviceToHome(homeid, devid);
        Log.e(TAG, "addDeviceToHomeAndRoom: addtohome " + result1.isSuccess() + " " + result1.getError());
        if (!result1.isSuccess()) {
            // 如果添加设备失败 取消订阅设备 避免重新添加时报错
            XlinkCloudManager.getInstance().unsubscribeDevice(devid);
            result.setError(result1.getError());
            return result;
        }

        // 创建Room name = 设备id
        XlinkResult<RoomApi.RoomResponse> result2 = XlinkCloudManager.getInstance().createRoom(homeid, String.valueOf(devid));
        Log.e(TAG, "addDeviceToHomeAndRoom: addroom " + result2.isSuccess() + " " + result2.getError());
        if (!result2.isSuccess()) {
            // 如果创建Room失败 从Home中删除设备 避免重新添加时报错
            XlinkCloudManager.getInstance().deleteDeviceFromHome(homeid, devid);
            result.setError(result2.getError());
            return result;
        }

        // 将设备添加到Room
        final String roomid = result2.getResult().id;
        XlinkResult<String> result3 = XlinkCloudManager.getInstance().addRoomDevice(homeid, roomid, devid);
        Log.e(TAG, "addDeviceToHomeAndRoom: addtoroom " + result3.isSuccess() + " " + result3.getError());
        if (!result3.isSuccess()) {
            // 如果添加到Room失败 从Home删除设备 并删除Room 避免重新添加时报错
            XlinkCloudManager.getInstance().deleteDeviceFromHome(homeid, devid);
            XlinkCloudManager.getInstance().deleteRoom(homeid, roomid);
            result.setError(result3.getError());
            return result;
        }
        result.setSuccess(true);
        return result;
    }

    private void delay(long ms) {
        if (ms < 0) {
            return;
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startTask() {
        mXDevice = null;
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

                if (!checkDeviceRegistered()) {
                    result = registerDevice();
                    if (!result.isSuccess()) {
                        return result;
                    }
                }

                result = addDevice();
                if (!result.isSuccess()) {
                    return result;
                }
                publishProgress(PROGRESS_ADDDEVICE);

                delay(2000);

                result = setZone();
                if (!result.isSuccess()) {
                    return result;
                }
                while (mProgress < PROGRESS_SET_ZONE);
                publishProgress(PROGRESS_SET_ZONE);

                result = subscribe();
                if (!result.isSuccess()) {
                    return result;
                }
                delay(1000);
                return addDeviceToHomeAndRoom();
            }

            @Override
            protected void onPostExecute(Result result) {
                super.onPostExecute(result);
                mTimer.cancel();
                mEsptouchTask.interrupt();
                mEsptouchTask = null;
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
                if (mProgress < values[0]) {
                    mProgress = values[0];
                }
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
