package com.inledco.exoterra.adddevice;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.inledco.exoterra.R;
import com.inledco.exoterra.bean.ImportDeviceResponse;
import com.inledco.exoterra.bean.QueryDeviceResponse;
import com.inledco.exoterra.bean.Result;
import com.inledco.exoterra.manager.Home2Manager;
import com.inledco.exoterra.xlink.RoomApi;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
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

    private WeakReference<AppCompatActivity> mActivity;

    private final String mProductId;
    private final String mSsid;
    private final String mBssid;
    private final String mPassword;
    private String mAddress;
    private String mSN;
    private int mDeviceId;

    private final boolean mSubscribe;

    private int mProgress;
    private APConfigListener mListener;

    private final CountDownTimer mAckTimer;
    private final CountDownTimer mTimer;

    private final BaseClient mClient;
    private String mReceive;
    private AsyncTask<Void, Integer, Result> mTask;

    public APConfigLinker(boolean subscribe, @NonNull String pid, @NonNull String ssid, String bssid, String password, AppCompatActivity activity) {
        mActivity = new WeakReference<>(activity);
        mSubscribe = subscribe;
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
                    mAddress = jsonObject.getString("mac");
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
                                                         .registerDeviceBlock(mProductId, null, mAddress, mSN);
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

    private Result subscribeBySn() {
        XlinkResult<DeviceApi.SnSubscribeResponse> result = XlinkCloudManager.getInstance().subscribeDeviceBySn(mProductId, mSN);
        if (result.isSuccess()) {
            mDeviceId = result.getResult().id;
        }
        return new Result(result.isSuccess(), result.getError());
    }

    private Result addDeviceToHomeAndRoom() {
        Result result = new Result();
        final String homeid = Home2Manager.getInstance().getCurrentHomeId();

        // 将设备添加到当前Home
        XlinkResult<String> result1 = XlinkCloudManager.getInstance().addDeviceToHome(homeid, mDeviceId);
        Log.e(TAG, "addDeviceToHomeAndRoom: addtohome " + result1.isSuccess() + " " + result1.getError());
        if (!result1.isSuccess()) {
            // 如果添加设备失败 取消订阅设备 避免重新添加时报错
            XlinkCloudManager.getInstance().unsubscribeDevice(mDeviceId);
            result.setError(result1.getError());
            return result;
        }

        // 创建Room name = 设备id
        XlinkResult<RoomApi.RoomResponse> result2 = XlinkCloudManager.getInstance().createRoom(homeid, String.valueOf(mDeviceId));
        Log.e(TAG, "addDeviceToHomeAndRoom: addroom " + result2.isSuccess() + " " + result2.getError());
        if (!result2.isSuccess()) {
            // 如果创建Room失败 从Home中删除设备 避免重新添加时报错
            XlinkCloudManager.getInstance().deleteDeviceFromHome(homeid, mDeviceId);
            result.setError(result2.getError());
            return result;
        }

        // 将设备添加到Room
        final String roomid = result2.getResult().id;
        XlinkResult<String> result3 = XlinkCloudManager.getInstance().addRoomDevice(homeid, roomid, mDeviceId);
        Log.e(TAG, "addDeviceToHomeAndRoom: addtoroom " + result3.isSuccess() + " " + result3.getError());
        if (!result3.isSuccess()) {
            // 如果添加到Room失败 从Home删除设备 并删除Room 避免重新添加时报错
            XlinkCloudManager.getInstance().deleteDeviceFromHome(homeid, mDeviceId);
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
                if (!checkDeviceRegistered()) {
                    Result result = registerDevice();
                    if (!result.isSuccess()) {
                        return result;
                    }
                    delay(3000);
                }
                //通过设备SN订阅设备
                return subscribeBySn();

//                Result result = subscribeBySn();
//                if (!result.isSuccess()) {
//                    return result;
//                }
//                delay(1000);
//                return addDeviceToHomeAndRoom();
            }

            @Override
            protected void onPostExecute(Result result) {
                super.onPostExecute(result);
                mTimer.cancel();
                if (mListener != null) {
                    if (result.isSuccess()) {
                        mProgress = PROGRESS_SUCCESS;
                        mListener.onProgressUpdate(mProgress);
                        mListener.onAPConfigSuccess(mDeviceId, mAddress);
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
            Process process = runtime.exec("ping -c 4 api2.xlink.cn");
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

        void onAPConfigSuccess(int devid, String mac);

        void onAPConfigFailed(String error);
    }
}
