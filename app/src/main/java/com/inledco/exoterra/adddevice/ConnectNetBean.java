package com.inledco.exoterra.adddevice;

public class ConnectNetBean {
    private String mGroupid;
    private String mProductKey;
    private String mSsid;
    private String mBssid;
    private String mPassword;

    private String mDeviceName;
    private String mAddress;
    private String mName;

    private boolean mConflict;
    private boolean mCompatibleMode;
    private boolean mRunning;

    public ConnectNetBean() {

    }

    public ConnectNetBean(String groupid) {
        mGroupid = groupid;
    }

    public String getGroupid() {
        return mGroupid;
    }

    public String getProductKey() {
        return mProductKey;
    }

    public void setProductKey(String productKey) {
        mProductKey = productKey;
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

    public String getDeviceName() {
        return mDeviceName;
    }

    public void setDeviceName(String deviceName) {
        mDeviceName = deviceName;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
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
