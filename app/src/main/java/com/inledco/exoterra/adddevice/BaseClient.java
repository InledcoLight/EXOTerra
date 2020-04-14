package com.inledco.exoterra.adddevice;

import android.support.annotation.NonNull;

import com.inledco.exoterra.util.RegexUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseClient {
    protected String mRemoteAddress;
    protected int mRemotePort;
    protected boolean mListening;
    protected final ExecutorService mExecutorService;

    protected Listener mListener;

    protected final Object mLock;

    public BaseClient(String remoteAddress, int remotePort) {
        if (!RegexUtil.isIP(remoteAddress)) {
            throw new RuntimeException("Invalid ip address.");
        }
        if (remotePort < 0 || remotePort > 65535) {
            throw new RuntimeException("Invalid remote port.");
        }
        mRemoteAddress = remoteAddress;
        mRemotePort = remotePort;
        mExecutorService = Executors.newCachedThreadPool();
        mLock = new Object();
    }

    public String getRemoteAddress() {
        return mRemoteAddress;
    }

    public void setRemoteIp(String remoteAddress) {
        if (!mListening && RegexUtil.isIP(remoteAddress)) {
            mRemoteAddress = remoteAddress;
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

    public void setListener(Listener listener) {
        mListener = listener;
    }

    protected void send(@NonNull String value) {
        send(value.getBytes());
    }

    protected abstract void start();
    protected abstract void stop();
    protected abstract void send(@NonNull byte[] bytes);
    protected abstract void receive();

    public interface Listener {
        void onError(String error);
        void onReceive(byte[] bytes);
    }
}
