package com.liruya.exoterra.adddevice;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UdpClient {
    private final String TAG = "UdpClient";

    private final int UDP_SEND_BUFFER_SIZE = 1024;
    private final int UDP_RECEIVE_BUFFER_SIZE = 1024;

    private DatagramSocket mSocket;

    private int mRemoteIp;
    private int mRemotePort;
    private int mLocalPort;
    private boolean mListening;
    private ExecutorService mExecutorService;
    private UdpClientListener mListener;

    public UdpClient(int remoteIp, int remotePort, int localPort) {
        if (remotePort < 0 || remotePort > 65535) {
            throw new RuntimeException("Invalid remote port.");
        }
        if (localPort < 0 || localPort > 65535) {
            throw new RuntimeException("Invalid local port.");
        }
        mRemoteIp = remoteIp;
        mRemotePort = remotePort;
        mLocalPort = localPort;
        mExecutorService = Executors.newCachedThreadPool();
    }

    public int getRemoteIp() {
        return mRemoteIp;
    }

    public void setRemoteIp(int remoteIp) {
        mRemoteIp = remoteIp;
    }

    public int getRemotePort() {
        return mRemotePort;
    }

    public void setRemotePort(int remotePort) {
        mRemotePort = remotePort;
    }

    public int getLocalPort() {
        return mLocalPort;
    }

    public void setLocalPort(int localPort) {
        mLocalPort = localPort;
    }

    private String ipstr(int ip) {
        int[] addr = new int[4];
        addr[0] = ip&0xFF;
        addr[1] = (ip>>8)&0xFF;
        addr[2] = (ip>>16)&0xFF;
        addr[3] = (ip>>24)&0xFF;
        StringBuilder sb = new StringBuilder();
        sb.append(addr[0]).append(".")
          .append(addr[1]).append(".")
          .append(addr[2]).append(".")
          .append(addr[3]);
        return new String(sb);
    }

    public void setListener(UdpClientListener listener) {
        mListener = listener;
    }

    public synchronized void start() {
        if (mListening) {
            return;
        }
        try {
            mSocket = new DatagramSocket(mLocalPort);
            mSocket.setSendBufferSize(UDP_SEND_BUFFER_SIZE);
            mSocket.setReceiveBufferSize(UDP_RECEIVE_BUFFER_SIZE);
        }
        catch (SocketException e) {
            e.printStackTrace();
            if (mListener != null) {
                mListener.onError(e.getMessage());
            }
        }
        mListening = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                receive();
            }
        }).start();
    }

    public synchronized void stop() {
        mSocket.close();
        mListening = false;
    }

    public synchronized void send(@NonNull final byte[] bytes) {
        if (!mListening) {
            return;
        }
        Log.e(TAG, "send: " + new String(bytes) + "  " + ipstr(mRemoteIp));
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress address = InetAddress.getByName(ipstr(mRemoteIp));
                    DatagramPacket packet = new DatagramPacket( bytes, bytes.length, address, mRemotePort );
                    mSocket.send(packet);

                    if (mListener != null) {
                        mListener.onSend();
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

    public synchronized void send(@NonNull final String data) {
        send(data.getBytes());
    }

    public void receive() {
        try {
            byte[] rxBuffer = new byte[UDP_RECEIVE_BUFFER_SIZE];
            InetAddress address = InetAddress.getByName( ipstr(mRemoteIp) );
            DatagramPacket rcvPacket = new DatagramPacket( rxBuffer, rxBuffer.length );
            while (mListening) {
                mSocket.receive(rcvPacket);
                if (rcvPacket.getAddress().equals(address) && rcvPacket.getLength() > 0)
                {
                    if (mListener != null) {
                        byte[] bytes = Arrays.copyOf(rxBuffer, rcvPacket.getLength());
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

    public interface UdpClientListener {
        void onSend();

        void onReceive(byte[] bytes);

        void onError(String error);
    }
}
