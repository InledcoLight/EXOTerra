package com.liruya.exoterra.adddevice;

import android.support.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseClient {
    protected int mRemoteIp;
    protected int mRemotePort;
    protected boolean mListening;
    protected final ExecutorService mExecutorService;

    protected BaseClientListener mListener;

    protected final Object mLock;

    public BaseClient(int remoteIp, int remotePort) {
        if (remotePort < 0 || remotePort > 65535) {
            throw new RuntimeException("Invalid remote port.");
        }
        mRemoteIp = remoteIp;
        mRemotePort = remotePort;
        mExecutorService = Executors.newCachedThreadPool();
        mLock = new Object();
    }

    public int getRemoteIp() {
        return mRemoteIp;
    }

    public void setRemoteIp(int remoteIp) {
        if (!mListening) {
            mRemoteIp = remoteIp;
        }
    }

    public int getRemotePort() {
        return mRemotePort;
    }

    public void setRemotePort(int remotePort) {
        if (!mListening) {
            mRemotePort = remotePort;
        }
    }

    public boolean isListening() {
        return mListening;
    }

    protected final String ipstr(int ip) {
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

    public void setListener(BaseClientListener listener) {
        mListener = listener;
    }

    protected void send(@NonNull String value) {
        send(value.getBytes());
    }

    protected abstract void start();
    protected abstract void stop();
    protected abstract void send(@NonNull byte[] bytes);
    protected abstract void receive();

    public interface BaseClientListener {
        void onError(String error);
        void onReceive(byte[] bytes);
    }
}
