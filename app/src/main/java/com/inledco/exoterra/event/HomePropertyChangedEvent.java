package com.inledco.exoterra.event;

public class HomePropertyChangedEvent {
    private String homeid;

    public HomePropertyChangedEvent(String homeid) {
        this.homeid = homeid;
    }

    public String getHomeid() {
        return homeid;
    }
}
