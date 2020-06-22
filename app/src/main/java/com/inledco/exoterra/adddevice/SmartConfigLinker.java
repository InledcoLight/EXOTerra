package com.inledco.exoterra.adddevice;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.inledco.exoterra.aliot.AliotServer;
import com.inledco.exoterra.aliot.DeviceParam;
import com.inledco.exoterra.aliot.UserApi;
import com.inledco.exoterra.bean.ExoProduct;
import com.inledco.exoterra.bean.Result;
import com.inledco.exoterra.udptcp.UdpClient;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class SmartConfigLinker {
    private final String TAG = "SmartConfigLinker";

    private final int SUBSCRIBE_TIMEOUT         = 15000;

    private final int PROGRESS_ESPTOUCH         = 60;
    private final int PROGRESS_GETPARAM         = 70;
    private final int PROGRESS_SUBSCRIBE        = 85;
    private final int PROGRESS_SUCCESS          = 100;

    private final int REMOTE_PORT               = 8899;

    private final boolean mSubscribe;

    private int mProgress;
    private final CountDownTimer mTimer;

    private final String mProductKey;
    private final String mSsid;
    private final String mBssid;
    private final String mPassword;
    private String mAddress;
    private String mDeviceName;
    private String mDeviceSecret;

    private WeakReference<AppCompatActivity> mActivity;

    private final Object mLock;

    private IEsptouchTask mEsptouchTask;

    private SmartConfigListener mListener;

    private UdpClient mClient;
    private UdpClient.Listener mClientListener;
    private String mReceive;
    private CountDownTimer mGetTimer;
    private boolean mGetTimeout;
    private CountDownTimer mSetTimer;
    private boolean mSetTimeout;

    private AsyncTask<Void, Integer, Result> mTask;

    public SmartConfigLinker(AppCompatActivity activity, boolean subscribe, String pkey, String ssid, String bssid, String psw) {
        mSubscribe = subscribe;
        mProductKey = pkey;
        mSsid = ssid;
        mBssid = bssid;
        mPassword = psw;
        mActivity = new WeakReference<>(activity);
        mLock = new Object();

        mGetTimer = new CountDownTimer(10000, 2000) {
            @Override
            public void onTick(long millisUntilFinished) {
                readDeviceParam();
            }

            @Override
            public void onFinish() {
                mGetTimeout = true;
            }
        };

        mSetTimer = new CountDownTimer(10000, 2000) {
            @Override
            public void onTick(long millisUntilFinished) {
                writeDeviceParam(mDeviceSecret);
            }

            @Override
            public void onFinish() {
                mSetTimeout = true;
            }
        };

        mClientListener = new UdpClient.Listener() {
            @Override
            public void onError(String error) {
                Log.e(TAG, "onError: " + error);
            }

            @Override
            public void onReceive(String ip, int port, byte[] bytes) {
                mReceive = new String(bytes);
                Log.e(TAG, "onReceive: " + ip + ":" + port + " " + mReceive);
            }
        };

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
            return new Result(false, "Smartconfig failed.");
        }
        mAddress = esptouchResult.getBssid().toUpperCase();
        byte[] addr = esptouchResult.getInetAddress().getAddress();
        String remoteAddress = String.format("%1$d.%2$d.%3$d.%4$d", addr[0]&0xFF, addr[1]&0xFF, addr[2]&0xFF, addr[3]&0xFF);
        Log.e(TAG, "esptouch: " + remoteAddress);
        mClient = new UdpClient(remoteAddress, REMOTE_PORT);
        mClient.setListener(mClientListener);
        mClient.start();
        while (!mClient.isListening());
        return new Result(true, remoteAddress);
    }

    private void readDeviceParam() {
        String[] array = new String[]{"region", "productKey", "productSecret", "deviceName", "deviceSecret"};
        Map<String, Object> map = new HashMap<>();
        map.put("get", array);
        String paylod = JSON.toJSONString(map);
        Log.e(TAG, "getDeviceParam: " + paylod);
        mClient.send(paylod);
    }

    private DeviceParam parseReadParamResponse(String result) {
        try {
            JSONObject object = JSON.parseObject(result);
            if (object.containsKey("get_resp")) {
                return object.getObject("get_resp", DeviceParam.class);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Result getDeviceParam() {
        mReceive = null;
        DeviceParam deviceParam;
        mGetTimer.start();
        while (true) {
            if (mGetTimeout) {
                return new Result(false, "Get Param Timeout.");
            }
            if (mReceive != null) {
                deviceParam = parseReadParamResponse(mReceive);
                if (deviceParam != null) {
                    mGetTimer.cancel();
                    mDeviceName = deviceParam.getDeviceName();
                    mDeviceSecret = deviceParam.getDeviceSecret();
                    String pkey = deviceParam.getProductKey();
                    if (TextUtils.equals(mProductKey, pkey) == false) {
                        ExoProduct product = ExoProduct.getExoProduct(pkey);
                        String name = "";
                        if (product != null) {
                            name = product.getProductName();
                        }
                        return new Result(false, "Invalid product:" + name);
                    } else {
                        break;
                    }
                }
                mReceive = null;
            }
        }
        return new Result(true, null);
    }

    public void writeDeviceParam(String region, String pkey, String psecret, String dsecret) {
        final int zone = TimeZone.getDefault().getRawOffset() / 60000;
        final long time = System.currentTimeMillis();
        DeviceParam devParam = new DeviceParam(region, pkey, psecret, dsecret, zone, time);
        Map<String, Object> setAction = new HashMap<>();
        setAction.put("set", devParam);
        String payload = JSON.toJSONString(setAction);
        Log.e(TAG, "writeDeviceParam: " + payload);
        mClient.send(payload);
    }

    public void writeDeviceParam(String dsecret) {
        final int zone = TimeZone.getDefault().getRawOffset() / 60000;
        final long time = System.currentTimeMillis();
        DeviceParam devParam = new DeviceParam(dsecret, zone, time);
        Map<String, Object> setAction = new HashMap<>();
        setAction.put("set", devParam);
        String payload = JSON.toJSONString(setAction);
        Log.e(TAG, "writeDeviceParam: " + payload);
        mClient.send(payload);
    }

    public boolean parseWriteParamResponse(String result) {
        try {
            JSONObject object = JSON.parseObject(result);
            if (object.containsKey("set_resp")) {
                JSONObject res = object.getJSONObject("set_resp");
                if (res.containsKey("result")) {
                    String suc = res.getString("result");
                    if (TextUtils.equals(suc, "success")) {
                        return true;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Result setDeviceParam() {
        mReceive = null;
        mSetTimer.start();
        while (true) {
            if (mSetTimeout) {
                return new Result(false, "Set Param Timeout.");
            }
            if (mReceive != null) {
                if (parseWriteParamResponse(mReceive)) {
                    mSetTimer.cancel();
                    break;
                }
                mReceive = null;
            }
        }
        return new Result(true, null);
    }

    private Result subscribeDevice() {
        UserApi.SubscribeDeviceResponse response = AliotServer.getInstance()
                                                              .subscribeDevice(mProductKey, mDeviceName, mAddress);
        Log.e(TAG, "subscribeDevice: " + JSON.toJSONString(response));
        if (response == null) {
            return new Result(false, "Suscribe failed");
        }
        if (response.code != 0) {
            return new Result(false, response.msg);
        }
        if (TextUtils.equals(mDeviceSecret, response.data.device_secret)) {
            mDeviceSecret = null;
        } else {
            mDeviceSecret = response.data.device_secret;
        }
        return new Result(true, null);
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
        mAddress = null;
        mDeviceName = null;
        mDeviceSecret = null;
        mReceive = null;
        mGetTimeout = false;
        mSetTimeout = false;
        mProgress = 0;
        mTask = new AsyncTask<Void, Integer, Result>() {
            @Override
            protected Result doInBackground(Void... voids) {
                Result result = esptouch();
                if (!result.isSuccess()) {
                    return result;
                }
                publishProgress(PROGRESS_ESPTOUCH);

                if (mSubscribe) {
                    result = getDeviceParam();
                    if (!result.isSuccess()) {
                        return result;
                    }
                    publishProgress(PROGRESS_GETPARAM);
                    delay(2000);

                    result = subscribeDevice();
                    if (!result.isSuccess()) {
                        return result;
                    }
                    publishProgress(PROGRESS_SUBSCRIBE);
                }

                delay(2000);
                result = setDeviceParam();
                mClient.stop();
                return result;
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
                        mListener.onSuccess(mDeviceName, mAddress);
                    } else {
                        mListener.onError(result.getMessage() + "\n" + mAddress);
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
                    } else if (values[0] == PROGRESS_SUBSCRIBE) {
                        mListener.onSubscribe(mProductKey, mDeviceName);
                    }
                    mListener.onProgressUpdate(mProgress);
                }
            }
        };
        mTask.execute();
        mTimer.start();
    }

    public void stopTask() {
        mGetTimer.cancel();
        mSetTimer.cancel();
        mTimer.cancel();
        if (mEsptouchTask != null) {
            mEsptouchTask.interrupt();
            mEsptouchTask = null;
        }
        if (mClient != null) {
            mClient.stop();
            mClient = null;
        }
        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }
    }

    private String getString(@StringRes int resid) {
        return mActivity.get().getString(resid);
    }
}
