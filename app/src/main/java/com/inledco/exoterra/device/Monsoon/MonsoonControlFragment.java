package com.inledco.exoterra.device.Monsoon;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.EXOMonsoon;

import java.util.List;

public class MonsoonControlFragment extends BaseFragment {
    private Button[] monsoon_ctrl_custom = new Button[8];
    private TextView monsoon_key;

    private final String[] mPeriods = new String[120];
    private final String[] mKeyActions = new String[120];

    private MonsoonViewModel mMonsoonViewModel;
    private EXOMonsoon mMonsoon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initData();
        initEvent();
        return view;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_monsoon_control;
    }

    @Override
    protected void initView(View view) {
        monsoon_ctrl_custom[0] = view.findViewById(R.id.monsoon_ctrl_custom1);
        monsoon_ctrl_custom[1] = view.findViewById(R.id.monsoon_ctrl_custom2);
        monsoon_ctrl_custom[2] = view.findViewById(R.id.monsoon_ctrl_custom3);
        monsoon_ctrl_custom[3] = view.findViewById(R.id.monsoon_ctrl_custom4);
        monsoon_ctrl_custom[4] = view.findViewById(R.id.monsoon_ctrl_custom5);
        monsoon_ctrl_custom[5] = view.findViewById(R.id.monsoon_ctrl_custom6);
        monsoon_ctrl_custom[6] = view.findViewById(R.id.monsoon_ctrl_custom7);
        monsoon_ctrl_custom[7] = view.findViewById(R.id.monsoon_ctrl_custom8);
        monsoon_key = view.findViewById(R.id.monsoon_key);
        monsoon_key.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
    }

    @Override
    protected void initData() {
        mMonsoonViewModel = ViewModelProviders.of(getActivity())
                                              .get(MonsoonViewModel.class);
        mMonsoon = mMonsoonViewModel.getData();
        mMonsoonViewModel.observe(this, new Observer<EXOMonsoon>() {
            @Override
            public void onChanged(@Nullable EXOMonsoon exoMonsoon) {
                refreshData();
            }
        });

        String minute = getString(R.string.minute);
        String second = getString(R.string.second);
        String spray = getString(R.string.spray);
        for (int i = 0; i < 59; i++) {
            mPeriods[i] = "" + (i+1) + " " + second;
        }
        mPeriods[59] = "1 " + minute;
        for (int i = 60; i < 119; i++) {
            mPeriods[i] = "1 " + minute + " " + (i-59) + " " + second;
        }
        mPeriods[119] = "2 " + minute;

        for (int i = 0; i < 59; i++) {
            mKeyActions[i] = spray + " " + (i+1) + " " + second;
        }
        mKeyActions[59] = spray + " 1 " + minute;
        for (int i = 60; i < 119; i++) {
            mKeyActions[i] = spray + " 1 " + minute + " " + (i-59) + " " + second;
        }
        mKeyActions[119] = spray + " 2 " + minute;
        refreshData();
    }

    @Override
    protected void initEvent() {
        monsoon_key.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showKeyActionDialog();
            }
        });
    }

    private void refreshData() {
        if (mMonsoon != null) {
            final List<Byte> actions = mMonsoon.getCustomActions();
            int addIdx = 0;
            if (actions != null) {
                addIdx = actions.size();
            }
            for (int i = 0; i < addIdx && i < monsoon_ctrl_custom.length; i++) {
                monsoon_ctrl_custom[i].setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.ic_monsoon_pulse_64, 0, 0);
                monsoon_ctrl_custom[i].setVisibility(View.VISIBLE);
                monsoon_ctrl_custom[i].setText((actions.get(i) <= 0 || actions.get(i) > 120) ? "" : mPeriods[actions.get(i)-1]);
                final int idx = i;
                monsoon_ctrl_custom[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMonsoonViewModel.setPower((byte) (actions.get(idx) + 0x80));
                    }
                });
                monsoon_ctrl_custom[i].setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        showDialog(idx);
                        return false;
                    }
                });
            }
            if (addIdx < monsoon_ctrl_custom.length) {
                monsoon_ctrl_custom[addIdx].setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_add_white_48dp, 0, 0);
                monsoon_ctrl_custom[addIdx].setVisibility(View.VISIBLE);
                monsoon_ctrl_custom[addIdx].setText("");
                final int idx = addIdx;
                monsoon_ctrl_custom[addIdx].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditCustomActionDialog(idx);
                    }
                });
                monsoon_ctrl_custom[addIdx].setOnLongClickListener(null);
                for (int i = addIdx + 1; i < monsoon_ctrl_custom.length; i++) {
                    monsoon_ctrl_custom[i].setVisibility(View.INVISIBLE);
                    monsoon_ctrl_custom[i].setOnClickListener(null);
                    monsoon_ctrl_custom[i].setOnLongClickListener(null);
                }
            }

            monsoon_key.setText(getKeyActionText());
        }
    }

    private String getKeyActionText() {
        if (mMonsoon == null || mMonsoon.getKeyAction() < 1 || mMonsoon.getKeyAction() > 120) {
            return "";
        }
        return mKeyActions[mMonsoon.getKeyAction()-1];
    }

    private void showKeyActionDialog() {
        if (mMonsoon == null || mMonsoon.getKeyAction() < 1 || mMonsoon.getKeyAction() > 120) {
            return;
        }
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_key_action, null, false);
        final NumberPicker np = view.findViewById(R.id.dialog_key_action_np);
        np.setDisplayedValues(mKeyActions);
        np.setMinValue(1);
        np.setMaxValue(120);
        np.setValue(mMonsoon.getKeyAction());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(view);
        builder.setTitle(R.string.device_key_action);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mMonsoonViewModel.setKeyAction((byte) np.getValue());
            }
        });
        builder.show();
    }

    private void showEditCustomActionDialog(final int idx) {
        if (idx < 0 || idx >= 8) {
            return;
        }
        if (mMonsoon != null) {
            final List<Byte> actions = mMonsoon.getCustomActions();
            if (idx > actions.size()) {
                return;
            }
            View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_custom_actions, null, false);
            final NumberPicker np = view.findViewById(R.id.dialog_custom_np);
            np.setMinValue(1);
            np.setMaxValue(120);
            np.setDisplayedValues(mPeriods);
            if (idx >= actions.size()) {
                np.setValue(5);
            } else {
                np.setValue(actions.get(idx));
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle( "Set Spray Duration" );
            builder.setView(view);
            builder.setNegativeButton(R.string.cancel, null);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (idx >= actions.size()) {
                        actions.add((byte) np.getValue());
                    }
                    else {
                        actions.set(idx, (byte) np.getValue());
                    }
                    mMonsoonViewModel.setCustomActions(actions);
                }
            });
            builder.show();
        }
    }

    private void showDialog(final int idx) {
        if (idx < 0 || idx >= 8 ) {
            return;
        }
        if (mMonsoon != null) {
            final List<Byte> actions = mMonsoon.getCustomActions();
            if (actions == null) {
                return;
            }
            if (idx < actions.size()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setNeutralButton(R.string.cancel, null);
                builder.setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        actions.remove(idx);
                        mMonsoonViewModel.setCustomActions(actions);
                    }
                });
                builder.setPositiveButton("Modify", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showEditCustomActionDialog(idx);
                    }
                });
                builder.show();
            }
        }
    }
}
