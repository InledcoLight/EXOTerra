package com.inledco.exoterra.adddevice;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

public class TcpClient extends BaseClient {
    private static final String TAG = "TcpClient";

    private final int TCP_SEND_MAX_LENGTH = 1460;
    private final int TCP_SEND_BUFFER_SIZE = 2048;
    private final int TCP_RECEIVE_BUFFER_SIZE = 2048;

    private int mConnectTimeout;

    private Socket mSocket;
    private BufferedInputStream mInputStream;
    private BufferedOutputStream mOutputStream;

    public TcpClient(String remoteAddress, int remotePort, int connectTimeout) {
        super(remoteAddress, remotePort);
        mConnectTimeout = connectTimeout;
    }

    public TcpClient(String remoteAddress, int remotePort) {
        this(remoteAddress, remotePort, 2000);
    }

    public int getConnectTimeout() {
        return mConnectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        if (!mListening) {
            mConnectTimeout = connectTimeout;
        }
    }

    @Override
    protected synchronized void start() {
        if (mListening) {
            return;
        }
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (mLock) {
                        mSocket = new Socket();
                        mSocket.setTcpNoDelay(true);
                        mSocket.setKeepAlive(true);
                        mSocket.setReceiveBufferSize(TCP_RECEIVE_BUFFER_SIZE);
                        mSocket.setSendBufferSize(TCP_SEND_BUFFER_SIZE);
                        mSocket.connect(new InetSocketAddress(mRemoteAddress, mRemotePort), mConnectTimeout);
                        mInputStream = new BufferedInputStream(mSocket.getInputStream());
                        mOutputStream = new BufferedOutputStream(mSocket.getOutputStream());

                        Thread.sleep(100);
                        mListening = true;
                    }
                    receive();
                }
                catch (SocketException e) {
                    e.printStackTrace();
                    Log.e(TAG, "run: " + e.getMessage());
                    if (mListener != null) {
                        mListener.onError(e.getMessage());
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "run: " + e.getMessage());
                    if (mListener != null) {
                        mListener.onError(e.getMessage());
                    }
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected synchronized void stop() {
        if (!mListening) {
            return;
        }
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (mLock) {
                        mListening = false;
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
                        }
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                    if (mListener != null) {
                        mListener.onError(e.getMessage());
                    }
                }
            }
        });
    }

    @Override
    protected synchronized void send(@NonNull final byte[] bytes) {
        if (!mListening || mSocket == null || mSocket.isClosed() || mOutputStream == null || bytes.length == 0) {
            return;
        }
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (mLock) {
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
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                    if (mListener != null) {
                        mListener.onError(e.getMessage());
                    }
                }
            }
        });
    }

    @Override
    protected void receive() {
        byte[] rxBuffer = new byte[TCP_RECEIVE_BUFFER_SIZE];
        while (mListening) {
            if (mInputStream != null) {
                try {
                    int len = mInputStream.read(rxBuffer);
                    if (len > 0) {
                        byte[] bytes = Arrays.copyOf(rxBuffer, len);
                        if (mListener != null) {
                            mListener.onReceive(bytes);
                        }
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                    if (mListener != null) {
                        mListener.onError(e.getMessage());
                    }
                }
            }
        }
    }
}
