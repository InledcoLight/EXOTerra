package com.inledco.exoterra.xlink;

public interface IXlinkRequestCallback<T> {
    void onStart();

    void onError(String error);

    void onSuccess(T t);
}
