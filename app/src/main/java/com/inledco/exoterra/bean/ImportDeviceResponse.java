package com.inledco.exoterra.bean;

import com.google.gson.JsonObject;

public class ImportDeviceResponse {
    private String import_record_id;
    private Resp[] resp;

    public boolean isValid() {
        if (resp != null && resp.length == 1) {
            return true;
        }
        return false;
    }

    public Resp getResp() {
        if (isValid()) {
            return resp[0];
        }
        return null;
    }

    public class Resp {
        private JsonObject info;
        private int errcode;
        private String errmsg;

        public JsonObject getInfo() {
            return info;
        }

        public int getErrcode() {
            return errcode;
        }

        public String getErrmsg() {
            return errmsg;
        }
    }
}
