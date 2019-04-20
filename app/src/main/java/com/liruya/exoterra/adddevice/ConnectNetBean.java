package com.liruya.exoterra.adddevice;

public class ConnectNetBean {
    private String mProductId;
    private String mSsid;
    private String mGateway;
    private String mPassword;
    private boolean mCompatibleMode;
    private boolean mRunning;

    public String getProductId() {
        return mProductId;
    }

    public void setProductId(String productId) {
        mProductId = productId;
    }

    public String getSsid() {
        return mSsid;
    }

    public void setSsid(String ssid) {
        mSsid = ssid;
    }

    public String getGateway() {
        return mGateway;
    }

    public void setGateway(String gateway) {
        mGateway = gateway;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public boolean isCompatibleMode() {
        return mCompatibleMode;
    }

    public void setCompatibleMode(boolean compatibleMode) {
        mCompatibleMode = compatibleMode;
    }

    public boolean isRunning() {
        return mRunning;
    }

    public void setRunning(boolean running) {
        mRunning = running;
    }
}
