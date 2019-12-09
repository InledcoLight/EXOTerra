package com.inledco.exoterra.event;

public class HomeDeviceChangedEvent {
    private String mHomeid;

    public HomeDeviceChangedEvent(String homeid) {
        mHomeid = homeid;
    }

    public String getHomeid() {
        return mHomeid;
    }
}
