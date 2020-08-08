package com.inledco.exoterra.aliot;

import com.aliyun.alink.linksdk.tools.AError;

public interface ILinkListener {
    void onStart();

    void onInitError(AError error);

    void onInitDone();
}
