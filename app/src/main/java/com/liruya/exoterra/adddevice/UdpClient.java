package com.liruya.exoterra.adddevice;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class UdpClient extends BaseClient {
    private final String TAG = "UdpClient";

    private final int UDP_SEND_BUFFER_SIZE = 1024;
    private final int UDP_RECEIVE_BUFFER_SIZE = 1024;

    private int mLocalPort;

    private DatagramSocket mSocket;

    public UdpClient(int remoteIp, int remotePort, int localPort) {
        super(remoteIp, remotePort);
        if (localPort < 0 || localPort > 65535) {
            throw new RuntimeException("Invalid local port.");
        }
        mLocalPort = localPort;
    }

    public int getLocalPort() {
        return mLocalPort;
    }

    public void setLocalPort(int localPort) {
        if (!mListening) {
            mLocalPort = localPort;
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
                        mSocket = new DatagramSocket(mLocalPort);
                        mSocket.setSendBufferSize(UDP_SEND_BUFFER_SIZE);
                        mSocket.setReceiveBufferSize(UDP_RECEIVE_BUFFER_SIZE);

                        Thread.sleep(100);
                        mListening = true;
                    }
                    receive();
                }
                catch (SocketException e) {
                    e.printStackTrace();
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
                synchronized (mLock) {
                    mListening = false;
                    if (mSocket != null) {
                        mSocket.close();
                        mSocket = null;
                    }
                }
            }
        });
    }

    @Override
    protected synchronized void send(@NonNull final byte[] bytes) {
        if (!mListening || mSocket == null || mSocket.isClosed() || bytes.length == 0) {
            return;
        }
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (mLock) {
                        InetAddress address = InetAddress.getByName(ipstr(mRemoteIp));
                        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, mRemotePort);
                        mSocket.send(packet);
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
        byte[] rxBuffer = new byte[UDP_RECEIVE_BUFFER_SIZE];
        try {
            InetAddress address = InetAddress.getByName( ipstr(mRemoteIp) );
            DatagramPacket rcvPacket = new DatagramPacket( rxBuffer, rxBuffer.length );
            while (mListening) {
                if (mSocket == null) {
                    continue;
                }
                mSocket.receive(rcvPacket);
                int len = rcvPacket.getLength();
                if (rcvPacket.getAddress().equals(address) && len > 0)
                {
                    if (mListener != null) {
                        byte[] bytes = Arrays.copyOf(rxBuffer, len);
                        mListener.onReceive(bytes);
                    }
                }
                //重置长度 否则可能会数据截断 丢失数据
                rcvPacket.setLength(rxBuffer.length);
            }
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
            if (mListener != null) {
                mListener.onError(e.getMessage());
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
