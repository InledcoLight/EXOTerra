package com.inledco.exoterra.aliot.bean;

import com.aliyun.alink.dm.model.RequestModel;

import java.util.ArrayList;
import java.util.List;

public class UpdateLabelRequest extends RequestModel<List<DeviceLabel>> {
    public UpdateLabelRequest(int id) {
        this.id = String.valueOf(id);
        version = "1.0";
        method = "thing.deviceinfo.update";
        params = new ArrayList<>();
    }
}
