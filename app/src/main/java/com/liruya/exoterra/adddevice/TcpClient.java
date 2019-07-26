package com.liruya.exoterra.adddevice;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpClient {
    private static final String TAG = "TcpClient";

    private final int TCP_SEND_MAX_LENGTH = 1460;
    private final int TCP_SEND_BUFFER_SIZE = 2048;
    private final int TCP_RECEIVE_BUFFER_SIZE = 2048;

    private final int MSG_TYPE_CONNECTED = 1;
    private final int MSG_TYPE_DISCONNECTED = 2;
    private final int MSG_TYPE_ERROR = 3;
    private final int MSG_TYPE_SEND = 4;
    private final int MSG_TYPE_RECEIVE = 5;

    private final ExecutorService mExecutorService;
    private Socket mSocket;
    private final int mRemoteIp;
    private final int mRemotePort;
    private final int mConnectTimeout;
    private BufferedInputStream mInputStream;
    private BufferedOutputStream mOutputStream;
    private final byte[] mRxBuffer;
    private boolean mListening;
    private TcpClientListener mListener;
    private final Handler mHandler;

    public TcpClient(int remoteIp, int remotePort, int connectTimeout) {
        if (remotePort < 0 || remotePort > 65535) {
            throw new RuntimeException("Invalid remote port.");
        }
        mRemoteIp = remoteIp;
        mRemotePort = remotePort;
        mConnectTimeout = connectTimeout;
        mRxBuffer = new byte[TCP_RECEIVE_BUFFER_SIZE];
        mExecutorService = Executors.newCachedThreadPool();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (mListener != null) {
                    switch (msg.what) {
                        case MSG_TYPE_CONNECTED:
                            mListener.onConnected();
                            break;
                        case MSG_TYPE_DISCONNECTED:
                            mListener.onDisconnected();
                            break;
                        case MSG_TYPE_ERROR:
                            mListener.onError((String) msg.obj);
                            break;
                        case MSG_TYPE_SEND:
                            mListener.onSend();
                            break;
                        case MSG_TYPE_RECEIVE:
                            mListener.onReceive((byte[]) msg.obj);
                            break;
                    }
                }
            }
        };
    }

    public TcpClient(int remoteIp, int remotePort) {
        this(remoteIp, remotePort, 2000);
    }

    public void setListener(TcpClientListener listener) {
        mListener = listener;
    }

    public synchronized void disconnect() {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                if (mListening) {
                    mListening = false;
                    try {
                        if (mInputStream != null) {
                            mInputStream.close();
                            mInputStream = null;
                        }
                        if (mOutputStream != null) {
                            mOutputStream.close();
                            mOutputStream = null;
                        }
                        if (mSocket != null) {
                            mSocket.close();
                            mSocket = null;
                            mHandler.sendEmptyMessage(MSG_TYPE_DISCONNECTED);
                        }
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "run: " + e.getMessage());
                    }
                }
            }
        });
    }

    public synchronized void connect() {
        if (mSocket == null) {
            mSocket = new Socket();
        }
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocket.setTcpNoDelay(true);
                    mSocket.setKeepAlive(true);
                    mSocket.setReceiveBufferSize(TCP_RECEIVE_BUFFER_SIZE);
                    mSocket.setSendBufferSize(TCP_SEND_BUFFER_SIZE);
                    String remoteIp = "" + (mRemoteIp & 0xFF) + "." + ((mRemoteIp & 0xFF00) >> 8) + "." + ((mRemoteIp & 0xFF0000) >> 16) + "." + ((mRemoteIp & 0xFF000000) >> 24);
                    mSocket.connect(new InetSocketAddress(remoteIp, mRemotePort), mConnectTimeout);
                    mInputStream = new BufferedInputStream(mSocket.getInputStream());
                    mOutputStream = new BufferedOutputStream(mSocket.getOutputStream());
                    mListening = true;
                    mHandler.sendEmptyMessageDelayed(MSG_TYPE_CONNECTED, 100);
                    receive();
                }
                catch (SocketException e) {
                    e.printStackTrace();
                    Log.e(TAG, "run1: " + e.getMessage());
                    Message msg = new Message();
                    msg.what = MSG_TYPE_ERROR;
                    msg.obj = e.getMessage();
                    mHandler.sendMessage(msg);
                }
                catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "run2: " + e.getMessage());
                    Message msg = new Message();
                    msg.what = MSG_TYPE_ERROR;
                    msg.obj = e.getMessage();
                    mHandler.sendMessage(msg);
                }
            }
        });
    }

    private void receive() {
        while (mListening) {
            try {
                int len = mInputStream.read(mRxBuffer);
                if (len > 0) {
                    byte[] bytes = Arrays.copyOf(mRxBuffer, len);
                    Message msg = new Message();
                    msg.what = MSG_TYPE_RECEIVE;
                    msg.obj = bytes;
                    mHandler.sendMessage(msg);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "receive: " + e.getMessage());
                Message msg = new Message();
                msg.what = MSG_TYPE_ERROR;
                msg.obj = e.getMessage();
                mHandler.sendMessage(msg);
            }
        }
    }

    public synchronized void send(@NonNull final byte[] bytes) {
        if (mSocket == null || mSocket.isClosed() || mOutputStream == null) {
            return;
        }
        if (bytes.length > 0) {
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        int index = 0;
                        while (index < bytes.length) {
                            int len = bytes.length - index;
                            if (len > TCP_SEND_MAX_LENGTH) {
                                len = TCP_SEND_MAX_LENGTH;
                            }
                            mOutputStream.write(bytes, index, len);
                            mOutputStream.flush();
                            index += len;
                        }
                        mHandler.sendEmptyMessage(MSG_TYPE_SEND);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "run: " + e.getMessage());
                        Message msg = new Message();
                        msg.what = MSG_TYPE_ERROR;
                        msg.obj = e.getMessage();
                        mHandler.sendMessage(msg);
                    }
                }
            });
        }
    }

    public synchronized void send(@NonNull final String value) {
        send(value.getBytes());
    }

    public interface TcpClientListener {
        void onConnected();

        void onDisconnected();

        void onSend();

        void onReceive(byte[] bytes);

        void onError(String error);
    }
}
