package com.liruya.exoterra.adddevice;

import com.liruya.exoterra.BaseViewModel;

public class ConnectNetViewModel extends BaseViewModel<ConnectNetBean> {

    public void start() {
        ConnectNetBean bean = getData();
        if (bean == null) {
            return;
        }
    }
}
