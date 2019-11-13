package com.inledco.exoterra.event;

public class HomeMemberChangedEvent {
    private String mHomeId;

    public HomeMemberChangedEvent(String homeId) {
        mHomeId = homeId;
    }

    public String getHomeId() {
        return mHomeId;
    }
}
