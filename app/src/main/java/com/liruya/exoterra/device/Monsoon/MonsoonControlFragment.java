package com.liruya.exoterra.device.Monsoon;

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
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.liruya.base.BaseFragment;
import com.liruya.exoterra.AppConstants;
import com.liruya.exoterra.R;
import com.liruya.exoterra.bean.EXOMonsoon;

import java.util.List;

public class MonsoonControlFragment extends BaseFragment {
    private TextView monsoon_ctrl_status;
    private ImageButton monsoon_ctrl_on;
    private ImageButton monsoon_ctrl_off;
    private Button[] monsoon_ctrl_custom = new Button[8];

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
        monsoon_ctrl_status = view.findViewById(R.id.monsoon_ctrl_status);
        monsoon_ctrl_on = view.findViewById(R.id.monsoon_ctrl_on);
        monsoon_ctrl_off = view.findViewById(R.id.monsoon_ctrl_off);
        monsoon_ctrl_custom[0] = view.findViewById(R.id.monsoon_ctrl_custom1);
        monsoon_ctrl_custom[1] = view.findViewById(R.id.monsoon_ctrl_custom2);
        monsoon_ctrl_custom[2] = view.findViewById(R.id.monsoon_ctrl_custom3);
        monsoon_ctrl_custom[3] = view.findViewById(R.id.monsoon_ctrl_custom4);
        monsoon_ctrl_custom[4] = view.findViewById(R.id.monsoon_ctrl_custom5);
        monsoon_ctrl_custom[5] = view.findViewById(R.id.monsoon_ctrl_custom6);
        monsoon_ctrl_custom[6] = view.findViewById(R.id.monsoon_ctrl_custom7);
        monsoon_ctrl_custom[7] = view.findViewById(R.id.monsoon_ctrl_custom8);
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

        refreshData();
    }

    @Override
    protected void initEvent() {
        monsoon_ctrl_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMonsoonViewModel.setPower(AppConstants.MONSOON_POWERON);
            }
        });

        monsoon_ctrl_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMonsoonViewModel.setPower(AppConstants.MONSOON_POWEROFF);
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
                monsoon_ctrl_custom[i].setText("" + actions.get(i));
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
                monsoon_ctrl_custom[addIdx].setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_add_black_64dp, 0, 0);
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
            monsoon_ctrl_status.setText((mMonsoon.getPower()&0x80) == 0x00 ? "Off" : "On");
        }
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
            String[] values = new String[127];
            for (int i = 0; i < 59; i++) {
                values[i] = "" + (i+1) + " Sec";
            }
            for (int i = 59; i < 119; i++) {
                values[i] = "1 Min " + (i-59) + " Sec";
            }
            values[119] = "2 Min";
            values[120] = "3 Min";
            values[121] = "4 Min";
            values[122] = "5 Min";
            values[123] = "6 Min";
            values[124] = "8 Min";
            values[125] = "10 Min";
            values[126] = "15 Min";
            np.setMinValue(1);
            np.setMaxValue(127);
            np.setDisplayedValues(values);
            if (idx == actions.size()) {
                np.setValue(5);
            } else {
                np.setValue(actions.get(idx));
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle( "Set Turnon Duration" );
            builder.setView(view);
            builder.setNegativeButton(R.string.cancel, null);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (idx == actions.size()) {
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
