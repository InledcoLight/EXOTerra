package com.inledco.exoterra.event;

public class HomeChangedEvent {
    private String mHomeid;

    public HomeChangedEvent(String homeid) {
        mHomeid = homeid;
    }

    public String getHomeid() {
        return mHomeid;
    }
}
