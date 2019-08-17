package com.liruya.exoterra.view;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;

public class MessageDialog {
    private AlertDialog mDialog;

    public MessageDialog(@NonNull Context context, final boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        mDialog = builder.create();
        mDialog.setCanceledOnTouchOutside(cancelable);
    }

    public MessageDialog(@NonNull Context context) {
        this(context, false);
    }

    public MessageDialog setTitle(@NonNull final String title) {
        mDialog.setTitle(title);
        return this;
    }

    public MessageDialog setTitle(@StringRes final int res) {
        mDialog.setTitle(res);
        return this;
    }

    public MessageDialog setMessage(@NonNull final String msg) {
        mDialog.setMessage(msg);
        return this;
    }

    public MessageDialog setButton(@NonNull final String text, final DialogInterface.OnClickListener listener) {
        mDialog.setButton(DialogInterface.BUTTON_POSITIVE, text, listener);
        return this;
    }

    public void show() {
        mDialog.show();
    }
}
