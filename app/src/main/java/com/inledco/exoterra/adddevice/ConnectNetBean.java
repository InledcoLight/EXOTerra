package com.inledco.exoterra.adddevice;

import com.inledco.exoterra.bean.ExoProduct;

public class ConnectNetBean {
    private String mGroupid;
    private ExoProduct mProduct;
    private String mProductKey;
    private String mSsid;
    private String mBssid;
    private String mPassword;
    private int mNetworkId = -1;

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

    public ExoProduct getProduct() {
        return mProduct;
    }

    public void setProduct(ExoProduct product) {
        mProduct = product;
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

    public int getNetworkId() {
        return mNetworkId;
    }

    public void setNetworkId(int networkId) {
        mNetworkId = networkId;
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
