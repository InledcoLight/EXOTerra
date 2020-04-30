package com.inledco.exoterra.adddevice;

public interface SmartConfigListener {
    void onProgressUpdate(int progress);

    void onError(String error);

    void onSuccess(String deviceName, String mac);

    void onEsptouchSuccess();

    void onSubscribe(String pkey, String dname);
}
