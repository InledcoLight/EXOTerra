package com.inledco.exoterra.event;

public class SimpleEvent {
    private Object Message;

    public SimpleEvent(Object message) {
        Message = message;
    }

    public Object getMessage() {
        return Message;
    }
}
