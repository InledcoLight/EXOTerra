package com.inledco.exoterra.adddevice;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.inledco.exoterra.aliot.AliotServer;
import com.inledco.exoterra.aliot.DeviceParam;
import com.inledco.exoterra.aliot.UserApi;
import com.inledco.exoterra.bean.Result;
import com.inledco.exoterra.util.DeviceUtil;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class APConfigLinker {
    private final String TAG = "APConfigLinker";

    private final String KEY_SSID       = "ssid";
    private final String KEY_PASSWORD   = "password";
    private final String KEY_ZONE       = "zone";
    private final String KEY_TIME       = "time";

    private final int TIMEOUT = 50000;
    private final int SUBSCRIBE_TIMEOUT = 10000;

    //192.168.4.1
    private final String REMOTE_ADDRESS = "192.168.4.1";
    private final int REMOTE_IP = 0x0104A8C0;
    private final int REMOTE_PORT = 8266;
    //for udp
    private final int LOCAL_PORT = 5000;

    private final int PROGRESS_SUCCESS = 100;

    private WeakReference<AppCompatActivity> mActivity;

    private final String mProductKey;
    private final String mSsid;
    private final String mBssid;
    private final String mPassword;
    private String mAddress;
    private String mDeviceName;
    private String mDeviceSecret;

    private final boolean mSubscribe;

    private int mProgress;
    private APConfigListener mListener;

    private CountDownTimer mGetTimer;
    private boolean mGetTimeout;
    private CountDownTimer mSetTimer;
    private boolean mSetTimeout;

    private final CountDownTimer mTimer;

    private final BaseClient mClient;
    private BaseClient.Listener mClientListener;
    private String mReceive;
    private AsyncTask<Void, Integer, Result> mTask;

    public APConfigLinker(boolean subscribe, @NonNull String pkey, @NonNull String ssid, String bssid, String password, AppCompatActivity activity) {
        mActivity = new WeakReference<>(activity);
        mSubscribe = subscribe;
        mProductKey = pkey;
        mSsid = ssid;
        mBssid = bssid;
        mPassword = password;
        mClient = new UdpClient(REMOTE_ADDRESS, REMOTE_PORT, LOCAL_PORT);

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
                writeDeviceParam();
            }

            @Override
            public void onFinish() {
                mSetTimeout = true;
            }
        };

        mTimer = new CountDownTimer(TIMEOUT, 500) {
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
                    mListener.onTimeout();
                }
            }
        };

        mClientListener = new BaseClient.Listener() {
            @Override
            public void onError(String error) {
                Log.e(TAG, "onError: " + error);
            }

            @Override
            public void onReceive(byte[] bytes) {
                mReceive = new String(bytes);
                Log.e(TAG, "onReceive: " + mReceive);
            }
        };
        mClient.setListener(mClientListener);
    }

    private void readDeviceParam() {
        String[] array = new String[]{"region", "productKey", "productSecret", "deviceName", "deviceSecret", "mac"};
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
                    mAddress = deviceParam.getMac();
                    String pkey = deviceParam.getProductKey();
                    if (TextUtils.equals(mProductKey, pkey) == false) {
                        return new Result(false, "Invalid product:" + DeviceUtil.getProductName(pkey));
                    } else {
                        break;
                    }
                }
                mReceive = null;
            }
        }
        return new Result(true, null);
    }

    private void writeDeviceParam() {
        JSONObject object = new JSONObject();
        final int zone = TimeZone.getDefault().getRawOffset() / 60000;
        final long time = System.currentTimeMillis();
        object.put(KEY_SSID, mSsid);
        object.put(KEY_PASSWORD, mPassword);
        object.put(KEY_ZONE, zone);
        object.put(KEY_TIME, time);
        Map<String, Object> map = new HashMap<>();
        map.put("set", object);
        String payload = JSON.toJSONString(map);
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
        while (!mSetTimeout) {
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
                //开启客户端
                mClient.start();
                while (!mClient.isListening());

                Result result = getDeviceParam();
                if (!result.isSuccess()) {
                    return result;
                }

                delay(2000);

                result = setDeviceParam();
                if (!result.isSuccess()) {
                    return result;
                }

                if (!mSubscribe) {
                    delay(5000);
                    return new Result(true, null);
                }
                //等待手机联网成功
                while(true) {
                    try {
                        Thread.sleep(1000);
                        if (checkNetAvailable()) {
                            mClient.stop();
                            Thread.sleep(5000);
                            break;
                        }
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                return subscribeDevice();
            }

            @Override
            protected void onPostExecute(Result result) {
                super.onPostExecute(result);
                mTimer.cancel();
                if (mListener != null) {
                    if (result.isSuccess()) {
                        mProgress = PROGRESS_SUCCESS;
                        mListener.onProgressUpdate(mProgress);
                        mListener.onAPConfigSuccess(mDeviceName, mAddress);
                    } else {
                        mListener.onAPConfigFailed(result.getMessage());
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
        mTask.execute();
        mTimer.start();
    }

    public void stopTask() {
        mGetTimer.cancel();
        mSetTimer.cancel();
        mTimer.cancel();
        mClient.stop();
        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }
    }

    public void setListener(APConfigListener listener) {
        mListener = listener;
    }

    private boolean checkNetAvailable() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec("ping -c 4 47.89.235.158");
            int result = process.waitFor();
            return (result==0);
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "checkNetAvailable: " + e.getMessage());
        }
        return false;
    }

    public interface APConfigListener {
        void onProgressUpdate(int progress);

        void onTimeout();

        void onAPConfigSuccess(String deviceName, String mac);

        void onAPConfigFailed(String error);
    }
}
