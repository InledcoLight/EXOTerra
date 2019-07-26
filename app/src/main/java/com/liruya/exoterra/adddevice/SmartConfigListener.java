package com.liruya.exoterra.adddevice;

public interface SmartConfigListener {
    void onProgressUpdate(int progress);

    void onError(String error);

    void onSuccess();

    void onEsptouchFailed();

    void onEsptouchSuccess();

    void onRegisterError(String error);

    void onRegisterSuccess();

    void onScanError(String error);

    void onScanSuccess();

    void onSubscribeError(String error);

    void onSubscribeSuccess();

    void onInitDeviceError(String error);

    void onInitDeviceSuccess();
}
