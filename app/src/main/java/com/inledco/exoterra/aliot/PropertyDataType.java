package com.inledco.exoterra.aliot;

import android.text.TextUtils;

public enum PropertyDataType {
    INTEGER("int"),
    FLOAT("float"),
    DOUBLE("double"),
    ENUM("enum"),
    BOOL("bool"),
    TEXT("text"),
    DATE("date"),
    STRUCT("struct"),
    ARRAY("array");

    private final String type;

    PropertyDataType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static PropertyDataType getPropertyDataType(String type) {
        for (PropertyDataType dataType : values()) {
            if (TextUtils.equals(dataType.getType(), type)) {
                return dataType;
            }
        }
        return null;
    }
}
