package com.inledco.exoterra.aliot;

import com.inledco.exoterra.base.BaseViewModel;
import com.inledco.exoterra.util.DeviceUtil;

import java.util.List;

public class DeviceViewModel<T extends Device> extends BaseViewModel<T> {
    public void setProperty(KeyValue... attrs) {
        String pkey = getData().getProductKey();
        String dname = getData().getDeviceName();
        String pname = DeviceUtil.getProductName(pkey).toLowerCase();
        AliotClient.getInstance().setProperty(pname, dname, attrs);
    }

    public void setProperty(List<KeyValue> attrs) {
        String pkey = getData().getProductKey();
        String dname = getData().getDeviceName();
        String pname = DeviceUtil.getProductName(pkey).toLowerCase();
        AliotClient.getInstance().setProperty(pname, dname, attrs);
    }

    public void getProperty(String... keys) {
        String pkey = getData().getProductKey();
        String dname = getData().getDeviceName();
        String pname = DeviceUtil.getProductName(pkey).toLowerCase();
        AliotClient.getInstance().getProperty(pname, dname, keys);
    }

    public void getAllProperties() {
        String pkey = getData().getProductKey();
        String dname = getData().getDeviceName();
        String pname = DeviceUtil.getProductName(pkey).toLowerCase();
        AliotClient.getInstance().getAllProperties(pname, dname);
    }

    public void upgradeFirmware(int version, String url) {
        String pkey = getData().getProductKey();
        String dname = getData().getDeviceName();
        String pname = DeviceUtil.getProductName(pkey).toLowerCase();
        AliotClient.getInstance().upgradeFirmware(pname, dname, version, url);
    }

    public void setZone(int zone) {
        KeyValue attrZone = getData().setZone(zone);
        setProperty(attrZone);
    }

    public void setSunrise(int sunrise) {
        KeyValue attrSunrise = getData().setSunrise(sunrise);
        setProperty(attrSunrise);
    }

    public void setSunset(int sunset) {
        KeyValue attrSunset = getData().setSunrise(sunset);
        setProperty(attrSunset);
    }
    public void setDaytime(int sunrise, int sunset) {
        KeyValue attrSunrise = getData().setSunrise(sunrise);
        KeyValue attrSunset = getData().setSunrise(sunset);
        setProperty(attrSunrise, attrSunset);
    }

}
