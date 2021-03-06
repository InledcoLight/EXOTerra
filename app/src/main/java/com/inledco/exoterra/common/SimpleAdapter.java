package com.inledco.exoterra.common;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public abstract class SimpleAdapter<T, VH extends RecyclerView.ViewHolder> extends BaseAdapter<VH> {
    protected Context mContext;
    protected List<T> mData;

    public SimpleAdapter(@NonNull Context context, List<T> data) {
        mContext = context;
        mData = data;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    protected View createView(@NonNull ViewGroup viewGroup) {
        return LayoutInflater.from(mContext).inflate(getItemLayoutResId(), viewGroup, false);
    }

    protected abstract @LayoutRes int getItemLayoutResId();
}
