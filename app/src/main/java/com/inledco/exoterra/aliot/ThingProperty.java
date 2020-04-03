package com.inledco.exoterra.aliot;

import android.support.annotation.NonNull;

public class ThingProperty extends BaseProperty {
    private final Class clazz;

    public ThingProperty(@NonNull final Class clazz) {
        this.clazz = clazz;
    }

    @Override
    public void setValue(Object value) {
        if (value != null && clazz.equals(value.getClass())) {
            super.setValue(value);
        }
    }

    @Override
    public Object getValue() {
        if (value != null && clazz.equals(value.getClass())) {
            return super.getValue();
        }
        return null;
    }
}
