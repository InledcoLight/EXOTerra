package com.inledco.exoterra.aliot.bean;

import com.aliyun.alink.dm.model.RequestModel;

import java.util.ArrayList;
import java.util.List;

public class DeleteLabelRequest extends RequestModel<List<AttrKey>> {

    public DeleteLabelRequest(int id) {
        this.id = String.valueOf(id);
        version = "1.0";
        method = "thing.deviceinfo.delete";
        params = new ArrayList<>();
    }
}
