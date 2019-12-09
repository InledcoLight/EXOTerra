package com.inledco.exoterra.bean;

public class LocalDevicePref {
    private final String pid;
    private final String mac;
    private final String name;
    private final long addTime;

    public LocalDevicePref(String pid, String mac, String name, long addTime) {
        this.pid = pid;
        this.mac = mac;
        this.name = name;
        this.addTime = addTime;
    }

    public String getPid() {
        return pid;
    }

    public String getMac() {
        return mac;
    }

    public String getName() {
        return name;
    }

    public long getAddTime() {
        return addTime;
    }

    public String getTag() {
        return pid + "_" + mac;
    }
}
