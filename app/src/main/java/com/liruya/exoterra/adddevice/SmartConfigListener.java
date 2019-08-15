package com.liruya.exoterra.adddevice;

public interface SmartConfigListener {
    void onProgressUpdate(int progress);

    void onError(String error);

    void onSuccess();

    void onEsptouchSuccess();

    void onDeviceScanned();

    void onDeviceInitialized();
}
