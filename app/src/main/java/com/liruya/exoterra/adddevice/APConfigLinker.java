package com.liruya.exoterra.adddevice;

import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkRequestCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.TimeZone;

import cn.xlink.restful.api.app.DeviceApi;

public class APConfigLinker{
    private final String TAG = "APConfigLinker";

    private final int TIMEOUT = 50000;


    private final boolean USE_TCP = true;
    //192.168.4.1
    private final int REMOTE_IP = 0x0104A8C0;
    private final int REMOTE_PORT = 8266;
    //for udp
    private final int LOCAL_PORT = 5000;

    private final String mProductId;
    private final String mSsid;
    private final String mBssid;
    private final String mPassword;
    private String mAddress;
    private String mSN;

    private UdpClient mUdpClient;
    private UdpClient.UdpClientListener mUdpClientListener;

    private TcpClient mTcpClient;
    private TcpClient.TcpClientListener mTcpClientListener;
    private final XlinkRequestCallback<DeviceApi.SnSubscribeResponse> mSnSubscribeCallback;

    private int mProgress;
    private final CountDownTimer mTimer;
    private APConfigListener mListener;
    private final Handler mHandler;

    private final CountDownTimer mAckTimer;

    private final Thread mSubscribeThread;

    public APConfigLinker(@NonNull String pid, @NonNull String ssid, @NonNull String bssid, @NonNull String password) {
        mProductId = pid;
        mSsid = ssid;
        mBssid = bssid;
        mPassword = password;
        mHandler = new Handler();
        mAckTimer = new CountDownTimer(2000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                sendRouterInfo();
            }
        };
        mSubscribeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    boolean flag = checkNetAvailable();
                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (flag) {
                        try {
                            Thread.sleep(5000);
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        XlinkCloudManager.getInstance().subscribeDeviceBySn(mProductId, mSN, mSnSubscribeCallback);
                        break;
                    }
                }
            }
        });
        if (USE_TCP) {
            mTcpClient = new TcpClient(REMOTE_IP, REMOTE_PORT);
            mTcpClientListener = new TcpClient.TcpClientListener() {
                @Override
                public void onConnected() {
                    sendRouterInfo();
                }

                @Override
                public void onDisconnected() {

                }

                @Override
                public void onSend() {

                }

                @Override
                public void onReceive(byte[] bytes) {
                    decode(new String(bytes));
                }

                @Override
                public void onError(final String error) {
                }
            };
            mTcpClient.setListener(mTcpClientListener);
        } else {
            mUdpClient = new UdpClient(REMOTE_IP, REMOTE_PORT, LOCAL_PORT);
            mUdpClientListener = new UdpClient.UdpClientListener() {
                @Override
                public void onSend() {

                }

                @Override
                public void onReceive(byte[] bytes) {
                    decode(new String(bytes));
                }

                @Override
                public void onError(String error) {

                }
            };
            mUdpClient.setListener(mUdpClientListener);
        }
        mSnSubscribeCallback = new XlinkRequestCallback<DeviceApi.SnSubscribeResponse>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onError(final String error) {
                mTimer.cancel();
                if (mListener != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onSubscribeFailed(error);
                        }
                    });
                }
            }

            @Override
            public void onSuccess(final DeviceApi.SnSubscribeResponse response) {
                mTimer.cancel();
                mProgress = 100;
                if (mListener != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onProgressUpdate(mProgress);
                            mListener.onAPConfigSuccess();
                        }
                    });
                }
            }
        };
        mTimer = new CountDownTimer(TIMEOUT, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (mProgress < 99) {
                    mProgress++;
                }
                if (mListener != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onProgressUpdate(mProgress);
                        }
                    });
                }
            }

            @Override
            public void onFinish() {
                mSubscribeThread.interrupt();
                if (mListener != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onTimeout();
                        }
                    });
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
            Log.e(TAG, "sendRouterInfo: " + jsonObject.toString());
            if (USE_TCP) {
                mTcpClient.send(jsonObject.toString());
            } else {
                mUdpClient.send(jsonObject.toString());
            }
            mAckTimer.start();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendOK() {
        if (USE_TCP) {
            mTcpClient.send("OK!\n");
        } else {
            mUdpClient.send("OK!\n");
        }
    }

    private void decode(@NonNull String rcv) {
        Log.e(TAG, "decode: " + rcv);
        try {
            JSONObject jsonObject = new JSONObject(rcv);
            if (jsonObject != null && jsonObject.has("mac") && jsonObject.has("sn")) {
                String mac = jsonObject.getString("mac");
                Log.e(TAG, "decode: " + mac + " " + mac.matches("[0-9A-Fa-f]{12}"));
                if (mac != null && mac.matches("^[0-9A-Fa-f]{12}$")) {
                    mAckTimer.cancel();
                    mAddress = mac;
                    mSN = jsonObject.getString("sn");
                    sendOK();
                    mSubscribeThread.start();
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        mTimer.start();
        if (USE_TCP) {
            mTcpClient.connect();
        } else {
            mUdpClient.start();
            sendRouterInfo();
        }
    }

    public void stop() {
        if (USE_TCP) {
            mTcpClient.disconnect();
        } else {
            mUdpClient.stop();
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

        void onSubscribeFailed(String error);

        void onTimeout();

        void onAPConfigSuccess();
    }
}
