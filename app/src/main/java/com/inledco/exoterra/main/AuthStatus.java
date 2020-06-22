package com.inledco.exoterra.main;

public class AuthStatus {
    private boolean authorized;

    private boolean iotInited;

    private boolean iotConnected;

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public boolean isIotInited() {
        return iotInited;
    }

    public void setIotInited(boolean iotInited) {
        this.iotInited = iotInited;
    }

    public boolean isIotConnected() {
        return iotConnected;
    }

    public void setIotConnected(boolean iotConnected) {
        this.iotConnected = iotConnected;
    }
}
