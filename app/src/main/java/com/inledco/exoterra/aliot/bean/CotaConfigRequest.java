package com.inledco.exoterra.aliot.bean;

import com.aliyun.alink.dm.model.RequestModel;

public class CotaConfigRequest extends RequestModel<CotaConfig> {
    public CotaConfigRequest(int id) {
        this.id = String.valueOf(id);
        version = "1.0";
        method = "thing.config.get";
        params = new CotaConfig();
    }
}
