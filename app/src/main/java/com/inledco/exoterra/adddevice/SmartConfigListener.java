package com.inledco.exoterra.adddevice;

public interface SmartConfigListener {
    void onProgressUpdate(int progress);

    void onError(String error);

    void onSuccess(int devid, String mac);

    void onEsptouchSuccess();

    void onDeviceScanned();

    void onDeviceInitialized();
}
