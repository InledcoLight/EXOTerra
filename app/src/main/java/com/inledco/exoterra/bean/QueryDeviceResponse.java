package com.inledco.exoterra.bean;

public class QueryDeviceResponse {
    private int count;
    private QueryDevice[] list;

    public boolean isValid() {
        if (count == 1 && list != null && list.length == 1) {
            return true;
        }
        return false;
    }

    public QueryDevice getQueryDevice() {
        if (isValid()) {
            return list[0];
        }
        return null;
    }

    public class QueryDevice {
        private String mac;
        private String sn;

        public String getMac() {
            return mac;
        }

        public String getSn() {
            return sn;
        }
    }
}
