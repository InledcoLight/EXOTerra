package com.liruya.exoterra.base;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;

public class BaseViewModel<T> extends ViewModel {
    protected final String TAG = this.getClass().getSimpleName();

    private final MutableLiveData<T> mLiveData = new MutableLiveData<>();
    private T mData;

    public void setData(T data) {
        mData = data;
    }

    public T getData() {
        return mData;
    }

    public void observe(LifecycleOwner owner, Observer<T> observer) {
        mLiveData.observe(owner, observer);
    }

    public void postValue() {
        mLiveData.postValue(mData);
    }

    public void postValue(T t) {
        mLiveData.postValue(t);
    }
}
