package com.inledco.exoterra.scan;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.inledco.exoterra.aliot.Device;
import com.inledco.exoterra.udptcp.UdpClient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class ScanDeviceTask {
    private final String TAG = "ScanDeviceTask";

    private final String BROADCAST_IP = "255.255.255.255";

//    private final int FRAME_INTERVAL = 16;
//    private long mReceiveTime;

//    private final int localPort;
    private final int remotePort;
    private final int timeout;
    private final int interval;
    private String payload;

    private boolean finished;

    private String mReceive;
    private final UdpClient mClient;
    private final CountDownTimer mTimer;

    private AsyncTask<String, Device, Void> mTask;

    public ScanDeviceTask(int remotePort) {
        this(remotePort, 10000, 1000);
    }

    public ScanDeviceTask(int remotePort, int timeout, int interval) {
        this.remotePort = remotePort;
        this.timeout = timeout;
        this.interval = interval;

        mClient = new UdpClient(BROADCAST_IP, remotePort);
        mTimer = new CountDownTimer(timeout, interval) {
            @Override
            public void onTick(long millisUntilFinished) {
                mClient.send(payload);
            }

            @Override
            public void onFinish() {
                finished = true;
            }
        };
    }

    public int getRemotePort() {
        return remotePort;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getInterval() {
        return interval;
    }

    public void start(String... params) {
        Map<String, String[]> map = new HashMap<>();
        map.put("scan", params);
        payload = JSON.toJSONString(map);
        Log.e(TAG, "start: " + payload);
        finished = false;
        mTask = new AsyncTask<String, Device, Void>() {
            @Override
            protected Void doInBackground(String... strings) {
                Log.e(TAG, "doInBackground: ");
                final Set<String> localDevices = new HashSet<>();
                mClient.setListener(new UdpClient.Listener() {
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "onError: " + error);
                    }

                    @Override
                    public void onReceive(String ip, int port, byte[] bytes) {
                        if (port != remotePort) {
                            return;
                        }
//                        String data = new String(bytes);
//                        if (System.currentTimeMillis() - mReceiveTime < FRAME_INTERVAL && mReceive != null) {
//                            mReceive = mReceive.concat(data);
//                        } else {
//                            mReceive = data;
//                        }
//                        mReceiveTime = System.currentTimeMillis();
                        mReceive = new String(bytes);
                        Log.e(TAG, "onReceive: " + ip + ":" + port + " " + mReceive);
                        try {
                            ScanResult result = JSON.parseObject(mReceive, ScanResult.class);
                            if (result != null) {
                                Device device = result.scan_resp;
                                if (device != null && !localDevices.contains(device.getTag())) {
                                    localDevices.add(device.getTag());
                                    device.setIp(ip);
                                    device.setPort(port);
                                    publishProgress(device);
                                }
                            }
                            mReceive = null;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "onReceive: " + e.getMessage());
                        }
                    }
                });
                mClient.start();
                while (!finished) {
                    if (isCancelled()) {
                        break;
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Device... values) {
                super.onProgressUpdate(values);
                if (values != null && values.length == 1) {
                    onDeviceScanned(values[0]);
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mClient.stop();
                mTask = null;
                onFinished();
            }
        };
        mTask.execute();
        mTimer.start();
        Log.e(TAG, "start: ");
    }

    public void stop() {
        Log.e(TAG, "stop: ");
        mTimer.cancel();
        mClient.stop();
        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }
    }

    public abstract void onDeviceScanned(Device device);

    public abstract void onFinished();
}
