package com.inledco.exoterra.aliot;

import android.text.TextUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StatusReponse {
    private final String UTC_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private String productKey;
    private String deviceName;
    private String utcTime;
    private String status;

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

    public String getUtcTime() {
        return utcTime;
    }

    public void setUtcTime(String utcTime) {
        this.utcTime = utcTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isOnline() {
        if (TextUtils.equals(status, "online")) {
            return true;
        }
        return false;
    }

    public long getStatusUpdateTime() {
        if (TextUtils.isEmpty(utcTime)) {
            return 0;
        }
        DateFormat df = new SimpleDateFormat(UTC_TIME_FORMAT);
        try {
            Date date = df.parse(utcTime);
            if (date != null) {
                return date.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
