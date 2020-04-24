package com.inledco.exoterra.main.groups;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.GridLayout;

import com.inledco.exoterra.R;
import com.inledco.exoterra.util.GroupUtil;

import java.util.List;

public abstract class GroupIconDialog extends BottomSheetDialog {
    private String mSelectName;
    private int mSelectRes;
    private CheckedTextView mSelect;

    public GroupIconDialog(@NonNull Context context) {
        super(context);
    }

    public GroupIconDialog(@NonNull Context context, int theme) {
        super(context, theme);
    }

    protected GroupIconDialog(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void init(String nm) {
        if (GroupUtil.contains(nm)) {
            mSelectName = nm;
        } else {
            mSelectName = GroupUtil.getDefaultIconName();
        }
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_group_icons, null, false);
        GridLayout gl = view.findViewById(R.id.dialog_groupicon_gl);
        Button btn_cancel = view.findViewById(R.id.dialog_groupicon_cancel);
        Button btn_ok = view.findViewById(R.id.dialog_groupicon_ok);
        final List<String> names = GroupUtil.getGroupIconNames();
        final List<Integer> icons = GroupUtil.getGroupIcons();
        final int size = GroupUtil.getGroupIconsCount();
        int idx = 0;
        for (int i = 0; i < gl.getChildCount(); i++) {
            View child = gl.getChildAt(i);
            if (child instanceof CheckedTextView) {
                final CheckedTextView icon = (CheckedTextView) child;
                if (idx < size) {
                    final String name = names.get(idx);
                    final int res = icons.get(idx);
                    idx++;
                    icon.setTextSize(0);
                    icon.setCompoundDrawablesWithIntrinsicBounds(0, res, 0, 0);
                    if (TextUtils.equals(mSelectName, name)) {
                        icon.setChecked(true);
                        mSelectRes = res;
                        mSelect = icon;
                    }
                    icon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!icon.isChecked()) {
                                mSelect.setChecked(false);
                                icon.setChecked(true);
                                mSelectName = name;
                                mSelectRes = res;
                                mSelect = icon;
                            }
                        }
                    });
                }
            }
        }
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                onChoose(mSelectName, mSelectRes);
            }
        });
        setContentView(view);
        setCancelable(false);
    }

    public abstract void onChoose(String name, int res);
}
