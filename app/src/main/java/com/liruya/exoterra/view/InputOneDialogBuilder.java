package com.liruya.exoterra.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.View;

public class InputOneDialogBuilder extends AlertDialog.Builder {
    private final TextInputLayout mTextInputLayout;

    public InputOneDialogBuilder(@NonNull Context context) {
        super(context);
        mTextInputLayout = new TextInputLayout(context);
    }

    public InputOneDialogBuilder(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mTextInputLayout = new TextInputLayout(context);
        setView(mTextInputLayout);
    }

    @Override
    public AlertDialog.Builder setView(View view) {
        return super.setView(view);
    }

    public TextInputLayout getTextInputLayout() {
        return mTextInputLayout;
    }
}
