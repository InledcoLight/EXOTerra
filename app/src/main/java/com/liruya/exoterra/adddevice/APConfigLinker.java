package com.liruya.exoterra.adddevice;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;

import com.liruya.exoterra.bean.Result;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.TimeZone;

import cn.xlink.restful.api.app.DeviceApi;

public class APConfigLinker{
    private final String TAG = "APConfigLinker";

    private final int TIMEOUT = 50000;
    private final int SUBSCRIBE_TIMEOUT = 10000;

    private final boolean USE_TCP = false;
    //192.168.4.1
    private final int REMOTE_IP = 0x0104A8C0;
    private final int REMOTE_PORT = 8266;
    //for udp
    private final int LOCAL_PORT = 5000;

    private final int PROGRESS_SUCCESS = 100;

    private final String mProductId;
    private final String mSsid;
    private final String mBssid;
    private final String mPassword;
    private String mSN;

    private int mProgress;
    private APConfigListener mListener;

    private final CountDownTimer mAckTimer;
    private final CountDownTimer mTimer;

    private final BaseClient mClient;
    private String mReceive;
    private AsyncTask<Void, Integer, Result> mTask;

    public APConfigLinker(@NonNull String pid, @NonNull String ssid, @NonNull String bssid, @NonNull String password) {
        mProductId = pid;
        mSsid = ssid;
        mBssid = bssid;
        mPassword = password;
        if (USE_TCP) {
            mClient = new TcpClient(REMOTE_IP, REMOTE_PORT);
        } else {
            mClient = new UdpClient(REMOTE_IP, REMOTE_PORT, LOCAL_PORT);
        }
        mAckTimer = new CountDownTimer(2000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                sendRouterInfo();
                start();
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
    }

    private void sendRouterInfo() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("ssid", mSsid);
            jsonObject.put("psw", mPassword);
            jsonObject.put("bssid", mBssid.replace(":", ""));
            final int rawZone = TimeZone.getDefault().getRawOffset() / 60000;
            final int zone = (rawZone/60)*100 + (rawZone%60);
            jsonObject.put("zone", zone);
            mClient.send(jsonObject.toString());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendOK() {
        mClient.send("OK!\n");
    }

    private boolean decode(@NonNull String rcv) {
        try {
            JSONObject jsonObject = new JSONObject(rcv);
            if (jsonObject != null && jsonObject.has("mac") && jsonObject.has("sn")) {
                String mac = jsonObject.getString("mac");
                if (mac != null && mac.matches("^[0-9A-Fa-f]{12}$")) {
                    mSN = jsonObject.getString("sn");
                    return true;
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Result subscribeBySn() {
        XlinkResult<DeviceApi.SnSubscribeResponse> result = XlinkCloudManager.getInstance().subscribeDeviceBySn(mProductId, mSN);
        return new Result(result.isSuccess(), result.getError());
    }

    public void startTask() {
        mProgress = 0;
        final BaseClient.BaseClientListener listener = new BaseClient.BaseClientListener() {
            @Override
            public void onError(String error) {

            }

            @Override
            public void onReceive(byte[] bytes) {
                mReceive = new String(bytes);
            }
        };
        mClient.setListener(listener);
        mTask = new AsyncTask<Void, Integer, Result>() {
            @Override
            protected Result doInBackground(Void... voids) {
                //开启客户端
                mClient.start();
                while (!mClient.isListening());
                //发送配网信息,如果超时时间内未收到返回信息则重复发送
                sendRouterInfo();
                mAckTimer.start();
                //等待设备返回信息并解析
                while (true) {
                    if (mReceive != null) {
                        if (decode(mReceive)) {
                            mAckTimer.cancel();
                            break;
                        }
                        mReceive = null;
                    }
                }
                //发送配置完成信息，设备开始连接路由
                sendOK();
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
                //通过设备SN订阅设备
                return subscribeBySn();
            }

            @Override
            protected void onPostExecute(Result result) {
                super.onPostExecute(result);
                mTimer.cancel();
                if (mListener != null) {
                    if (result.isSuccess()) {
                        mProgress = PROGRESS_SUCCESS;
                        mListener.onProgressUpdate(mProgress);
                        mListener.onAPConfigSuccess();
                    } else {
                        mListener.onAPConfigFailed(result.getError());
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
        mAckTimer.cancel();
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
            Process process = runtime.exec("ping -c 4 api.xlink.cn");
            int result = process.waitFor();
            return (result==0);
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public interface APConfigListener {
        void onProgressUpdate(int progress);

        void onTimeout();

        void onAPConfigSuccess();

        void onAPConfigFailed(String error);
    }
}
