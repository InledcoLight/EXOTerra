package com.inledco.exoterra.aliot;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ADevice {
    protected String productKey;
    protected String deviceName;

    protected String requestId;
    private final Map<String, BaseProperty> items = new HashMap<>();

    public ADevice() {
    }

    public ADevice(String productKey, String deviceName) {
        this.productKey = productKey;
        this.deviceName = deviceName;
    }

    public String getTag() {
        return productKey + "_" + deviceName;
    }

    public String getProductKey() {
        return productKey;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Map<String, BaseProperty> getItems() {
        return items;
    }

    public void updateValue(String key, Object value) {
        if (items.containsKey(key)) {
            BaseProperty prop = items.get(key);
            if (prop != null) {
                prop.setValue(value);
                prop.setTime(System.currentTimeMillis());
                prop.setUpdated(true);
                return;
            }
        }
        BaseProperty property = new BaseProperty(System.currentTimeMillis(), value);
        property.setUpdated(true);
        items.put(key, property);
    }

    public void updateValues(Map<String, Object> properties) {
        for (String key : properties.keySet()) {
            Object value = properties.get(key);
            updateValue(key, value);
        }
    }

    public void updateProperty(String key, BaseProperty property) {
        if (property == null) {
            return;
        }
        if (items.containsKey(key)) {
            BaseProperty prop = items.get(key);
            if (prop != null && prop.time < property.time) {
                property.setUpdated(true);
                items.put(key, property);
                return;
            }
        } else {
            property.setUpdated(true);
            items.put(key, property);
        }
    }

    public void updateProperties(Map<String, BaseProperty> properties) {
        if (properties == null || properties.size() == 0) {
            return;
        }
        for (String key : properties.keySet()) {
            BaseProperty property = properties.get(key);
            updateProperty(key, property);
        }
    }

    public BaseProperty convertFromThingProperty(ThingProperty property) {
        if (property == null) {
            return null;
        }
        PropertyDataType type = PropertyDataType.getPropertyDataType(property.dataType);
        if (type == null) {
            return null;
        }
        try {
            BaseProperty prop = new BaseProperty();
            prop.setTime(Long.parseLong(property.time));
            if (TextUtils.isEmpty(property.value)) {
                prop.setValue(null);
                return prop;
            }
            switch (type) {
                case INTEGER:
                case ENUM:
                case BOOL:
                    prop.setValue(Integer.parseInt(property.value));
                    break;
                case FLOAT:
                    prop.setValue(Float.parseFloat(property.value));
                    break;
                case DOUBLE:
                    prop.setValue(Double.parseDouble(property.value));
                    break;
                case DATE:
                case TEXT:
                    prop.setValue(property.value);
                    break;
                case STRUCT:
                    prop.setValue(JSONObject.parseObject(property.value));
                    break;
                case ARRAY:
                    prop.setValue(JSONArray.parseArray(property.value));
                    break;
            }
            return prop;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateProperties(List<ThingProperty> properties) {
        if (properties == null || properties.size() == 0) {
            return;
        }
        for (ThingProperty prop : properties) {
            BaseProperty property = convertFromThingProperty(prop);
            updateProperty(prop.identifier, property);
        }
    }

    public Object getPropertyValue(String key) {
        if (items.containsKey(key)) {
            BaseProperty prop = items.get(key);
            if (prop != null) {
                prop.setUpdated(false);
                return prop.getValue();
            }
        }
        return null;
    }

    public long getPropertyTime(String key) {
        if (items.containsKey(key)) {
            BaseProperty prop = items.get(key);
            if (prop != null) {
                return prop.getTime();
            }
        }
        return 0;
    }
}
