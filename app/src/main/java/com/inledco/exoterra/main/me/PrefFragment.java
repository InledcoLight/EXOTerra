package com.inledco.exoterra.main.me;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.inledco.exoterra.GlobalSettings;
import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.AliotClient;
import com.inledco.exoterra.aliot.AliotServer;
import com.inledco.exoterra.aliot.HttpCallback;
import com.inledco.exoterra.aliot.UserApi;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.login.LoginActivity;
import com.inledco.exoterra.main.AuthStatus;
import com.inledco.exoterra.main.MainViewModel;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.manager.GroupManager;
import com.inledco.exoterra.manager.UserManager;
import com.inledco.exoterra.manager.UserPref;
import com.inledco.exoterra.view.AdvancedTextInputEditText;
import com.inledco.exoterra.view.PasswordEditText;

public class PrefFragment extends BaseFragment {
    private CheckedTextView pref_title_user;
    private LinearLayout pref_usr_detail;
    private TextView pref_nickname;
    private TextView pref_email;
    private TextView pref_mod_psw;
    private CheckedTextView pref_title_unit;
    private LinearLayout pref_unit_detail;
    private Switch pref_timeformat;
    private Switch pref_tempunit;
    private CheckedTextView pref_title_invite_msg;
    private Button pref_logout;

    private MainViewModel mMainViewModel;
    private AuthStatus mAuthStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initData();
        initEvent();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult: " + requestCode + " " + resultCode);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_pref;
    }

    @Override
    protected void initView(View view) {
        pref_title_user = view.findViewById(R.id.pref_title_user);
        pref_usr_detail = view.findViewById(R.id.pref_usr_detail);
        pref_nickname = view.findViewById(R.id.pref_nickname);
        pref_email = view.findViewById(R.id.pref_email);
        pref_mod_psw = view.findViewById(R.id.pref_mod_psw);
        pref_title_unit = view.findViewById(R.id.pref_title_unit);
        pref_unit_detail = view.findViewById(R.id.pref_unit_detail);
        pref_timeformat = view.findViewById(R.id.pref_timeformat);
        pref_tempunit = view.findViewById(R.id.pref_tempunit);
        pref_title_invite_msg = view.findViewById(R.id.pref_title_invite_msg);
        pref_logout = view.findViewById(R.id.pref_logout);

        pref_title_user.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.selector_add, 0);
        pref_title_unit.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.selector_add, 0);
        pref_title_invite_msg.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add_white_24dp, 0);
        pref_nickname.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
        pref_mod_psw.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
        pref_usr_detail.setVisibility(View.GONE);
        pref_unit_detail.setVisibility(View.GONE);

        Log.e(TAG, "initView: " + Integer.toHexString(pref_nickname.getCurrentHintTextColor()));
    }

    @Override
    protected void initData() {
        mMainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        mAuthStatus = mMainViewModel.getData();
        mMainViewModel.observe(this, new Observer<AuthStatus>() {
            @Override
            public void onChanged(@Nullable AuthStatus authStatus) {
                refreshData();
            }
        });

        refreshData();
    }

    @Override
    protected void initEvent() {
        pref_title_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mAuthStatus.isAuthorized()) {
                    showToast(R.string.signin_msg);
                    return;
                }
                boolean checked = !pref_title_user.isChecked();
                pref_title_user.setChecked(checked);
                pref_usr_detail.setVisibility(checked ? View.VISIBLE : View.GONE);
                if (checked) {
                    pref_nickname.setText(UserManager.getInstance().getNickname());
                    pref_email.setText(UserManager.getInstance().getEmail());
                }
            }
        });
        pref_nickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showModifyNicknameDialog();
            }
        });
        pref_mod_psw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showModifyPasswordDialog();
            }
        });
        pref_title_unit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = !pref_title_unit.isChecked();
                pref_title_unit.setChecked(checked);
                pref_unit_detail.setVisibility(checked ? View.VISIBLE : View.GONE);
            }
        });
        pref_timeformat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                GlobalSettings.setIs24HourFormat(getContext(), !isChecked);
            }
        });
        pref_tempunit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                GlobalSettings.setIsCelsius(getContext(), !isChecked);
            }
        });
        pref_title_invite_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mAuthStatus.isAuthorized()) {
                    showToast(R.string.signin_msg);
                    return;
                }
                addFragmentToStack(R.id.main_fl, new MessagesFragment());
            }
        });
        pref_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuthStatus.isAuthorized()) {
                    logout();
                } else {
                    login();
                }
            }
        });
    }

    private void refreshData() {
        pref_logout.setText(mAuthStatus.isAuthorized() ? R.string.logout : R.string.signin);
        if (mAuthStatus.isAuthorized()) {
            pref_nickname.setText(UserManager.getInstance().getNickname());
            pref_email.setText(UserManager.getInstance().getEmail());
        } else {
            pref_nickname.setText(null);
            pref_email.setText(null);
        }

        pref_timeformat.setChecked(!GlobalSettings.is24HourFormat());
        pref_tempunit.setChecked(!GlobalSettings.isCelsius());
    }

    private void showModifyNicknameDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_rename, null, false);
        final TextInputLayout til = view.findViewById(R.id.dialog_rename_til);
        final AdvancedTextInputEditText et = view.findViewById(R.id.dialog_rename_et);
        til.setHint(getString(R.string.hint_nickname));
        et.setText(UserManager.getInstance().getNickname());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.change_nickname);
        builder.setView(view);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, null);
        builder.setCancelable(false);
        final AlertDialog dialog = builder.show();
        et.requestFocus();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = et.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    til.setError(getString(R.string.input_empty));
                    return;
                }
                AliotServer.getInstance().modifyUserNickname(name, new HttpCallback<UserApi.Response>() {
                    @Override
                    public void onError(String error) {
                        dismissLoadDialog();
                        showToast(error);
                    }

                    @Override
                    public void onSuccess(UserApi.Response result) {
                        Log.e(TAG, "onSuccess: " + JSON.toJSONString(result));
                        UserManager.getInstance().getUser().nickname = name;
                        dismissLoadDialog();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pref_nickname.setText(name);
                                dialog.dismiss();
                            }
                        });
                    }
                });
                showLoadDialog();
            }
        });
    }

    private void showModifyPasswordDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_modify_password, null, false);
        final TextInputLayout til1 = view.findViewById(R.id.modify_psw_til1);
        final TextInputLayout til2 = view.findViewById(R.id.modify_psw_til2);
        final TextInputLayout til3 = view.findViewById(R.id.modify_psw_til3);
        final PasswordEditText et_old = view.findViewById(R.id.modify_psw_old);
        final PasswordEditText et_new = view.findViewById(R.id.modify_psw_new);
        final PasswordEditText et_confirm = view.findViewById(R.id.modify_psw_confirm);
        et_old.setIcon(R.drawable.ic_lock_white_24dp, R.drawable.design_ic_visibility, R.drawable.design_ic_visibility_off);
        et_new.setIcon(R.drawable.ic_lock_white_24dp, R.drawable.design_ic_visibility, R.drawable.design_ic_visibility_off);
        et_confirm.setIcon(R.drawable.ic_lock_white_24dp, R.drawable.design_ic_visibility, R.drawable.design_ic_visibility_off);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.modify_password);
        builder.setView(view);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, null);
        builder.setCancelable(false);
        final AlertDialog dialog = builder.show();
        et_old.requestFocus();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldpsw = et_old.getText().toString();
                if (TextUtils.isEmpty(oldpsw)) {
                    til1.setError(getString(R.string.error_password));
                    et_old.requestFocus();
                    return;
                }
                final String newpsw = et_new.getText().toString();
                if (TextUtils.isEmpty(newpsw) || newpsw.length() < getResources().getInteger(R.integer.password_text_length_min)) {
                    til2.setError(getString(R.string.error_password));
                    et_new.requestFocus();
                    return;
                }
                final String confirm = et_confirm.getText().toString();
                if (TextUtils.equals(newpsw, confirm) == false) {
                    til3.setError(getString(R.string.password_inconsistent));
                    et_confirm.requestFocus();
                    return;
                }
                AliotServer.getInstance().modifyPassword(oldpsw, newpsw, new HttpCallback<UserApi.Response>() {
                    @Override
                    public void onError(String error) {
                        dismissLoadDialog();
                        showToast(error);
                    }

                    @Override
                    public void onSuccess(UserApi.Response result) {
                        UserPref.savePassword(getContext(), newpsw);
                        dismissLoadDialog();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                            }
                        });
                    }
                });
                showLoadDialog();
            }
        });
    }

    private void logout() {
        AliotServer.getInstance().logout(new HttpCallback<UserApi.Response>() {
            @Override
            public void onError(String error) {

            }

            @Override
            public void onSuccess(UserApi.Response result) {

            }
        });
        AliotClient.getInstance().deinit();
        mAuthStatus.setAuthorized(false);
        UserPref.clearAuthorization(getContext());
        DeviceManager.getInstance().clear();
        GroupManager.getInstance().clear();
        UserManager.getInstance().deinit();

        pref_title_user.setChecked(false);
        pref_usr_detail.setVisibility(View.GONE);
        pref_logout.setText(R.string.signin);

//        Intent intent = new Intent(getContext(), LoginActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
    }

    private void login() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        getActivity().startActivityForResult(intent, 1);
    }
}
