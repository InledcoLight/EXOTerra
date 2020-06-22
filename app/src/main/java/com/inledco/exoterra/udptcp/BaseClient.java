package com.inledco.exoterra.udptcp;

import android.support.annotation.NonNull;

import com.inledco.exoterra.util.RegexUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseClient {
    protected String mRemoteAddress;
    protected int mRemotePort;
    protected boolean mListening;
    protected final ExecutorService mExecutorService;

    protected final Object mLock;

    public BaseClient() {
        mExecutorService = Executors.newCachedThreadPool();
        mLock = new Object();
    }

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

    public void send(@NonNull String value) {
        send(value.getBytes());
    }

    public abstract void start();
    public abstract void stop();
    public abstract void send(@NonNull byte[] bytes);
    public abstract void receive();
}
