package com.inledco.exoterra.udptcp;

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

    private final int UDP_SEND_BUFFER_SIZE = 2048;
    private final int UDP_RECEIVE_BUFFER_SIZE = 2048;

    private DatagramSocket mSocket;

    private Listener mListener;

    public UdpClient() {
        super();
    }

    public UdpClient(String remoteAddress, int remotePort) {
        super(remoteAddress, remotePort);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public synchronized void start() {
        if (mListening) {
            return;
        }
        mExecutorService.execute(() -> {
            try {
                synchronized (mLock) {
                    mSocket = new DatagramSocket(0);
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
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public synchronized void stop() {
        if (!mListening) {
            return;
        }
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (mLock) {
                    mListening = false;
                    mListener = null;
                    if (mSocket != null) {
                        mSocket.close();
                        mSocket = null;
                    }
                }
            }
        });
    }

    public synchronized void send(@NonNull final String ip, final int port, @NonNull final byte[] bytes) {
        if (!mListening || mSocket == null || mSocket.isClosed() || bytes.length == 0) {
            return;
        }
        mExecutorService.execute(() -> {
            try {
                synchronized (mLock) {
                    InetAddress address = InetAddress.getByName(ip);
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);
                    mSocket.send(packet);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                if (mListener != null) {
                    mListener.onError(e.getMessage());
                }
            }
        });
    }

    public synchronized void send(@NonNull final String ip, final int port, @NonNull final String data) {
        send(ip, port, data.getBytes());
    }

    @Override
    public synchronized void send(@NonNull final byte[] bytes) {
        send(mRemoteAddress, mRemotePort, bytes);
    }

    @Override
    public void receive() {
        byte[] rxBuffer = new byte[UDP_RECEIVE_BUFFER_SIZE];
        try {
//            InetAddress address = InetAddress.getByName(mRemoteAddress);
            DatagramPacket rcvPacket = new DatagramPacket(rxBuffer, rxBuffer.length);
            while (mListening) {
                if (mSocket == null) {
                    continue;
                }
                mSocket.receive(rcvPacket);
                int len = rcvPacket.getLength();
                if (len > 0 && mListener != null) {
                    InetAddress address = rcvPacket.getAddress();
                    byte[] bytes = Arrays.copyOf(rxBuffer, len);
                    mListener.onReceive(address.getHostAddress(), rcvPacket.getPort(), bytes);
                }
//                if (rcvPacket.getAddress().equals(address) && len > 0) {
//                    if (mListener != null) {
//                        byte[] bytes = Arrays.copyOf(rxBuffer, len);
//                        mListener.onReceive(bytes);
//                    }
//                }
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

    public interface Listener {
        void onError(String error);
        void onReceive(String ip, int port, byte[] bytes);
    }
}
