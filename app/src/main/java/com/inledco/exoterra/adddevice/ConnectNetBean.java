package com.inledco.exoterra.adddevice;

public class ConnectNetBean {
    private String mHomeid;
    private String mProductId;
    private String mSsid;
    private String mBssid;
    private String mPassword;
    private int mResultDevid;
    private String mResultAddress;
    private boolean mConflict;
    private boolean mCompatibleMode;
    private boolean mRunning;

    public ConnectNetBean() {

    }

    public ConnectNetBean(String homeid) {
        mHomeid = homeid;
    }

    public String getHomeid() {
        return mHomeid;
    }

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

    public String getBssid() {
        return mBssid;
    }

    public void setBssid(String bssid) {
        mBssid = bssid;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public int getResultDevid() {
        return mResultDevid;
    }

    public void setResultDevid(int resultDevid) {
        mResultDevid = resultDevid;
    }

    public String getResultAddress() {
        return mResultAddress;
    }

    public void setResultAddress(String resultAddress) {
        mResultAddress = resultAddress;
    }

    public boolean isConflict() {
        return mConflict;
    }

    public void setConflict(boolean conflict) {
        mConflict = conflict;
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
