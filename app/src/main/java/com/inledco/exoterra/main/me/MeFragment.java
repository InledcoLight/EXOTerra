package com.inledco.exoterra.main.me;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.login.LoginActivity;
import com.inledco.exoterra.manager.UserManager;
import com.inledco.exoterra.view.AdvancedTextInputEditText;

public class MeFragment extends BaseFragment {
    private ImageView me_icon_usr;
    private TextView me_tv_signin;
    private LinearLayout me_usr_detail;
    private TextView me_tv_nickname;
    private TextView me_tv_email;
    private ImageButton me_mod_psw;
    private Button me_btn_logout;

    private boolean showOldPassword;
    private boolean showNewPassword;

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
        return R.layout.fragment_me;
    }

    @Override
    protected void initView(View view) {
        me_icon_usr = view.findViewById(R.id.me_icon_usr);
        me_tv_signin = view.findViewById(R.id.me_tv_signin);
        me_usr_detail = view.findViewById(R.id.me_usr_detail);
        me_tv_nickname = view.findViewById(R.id.me_tv_nickname);
        me_tv_email = view.findViewById(R.id.me_tv_email);
        me_mod_psw = view.findViewById(R.id.me_mod_psw);
        me_btn_logout = view.findViewById(R.id.me_btn_logout);

        me_tv_nickname.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);

        boolean login = false;
        me_tv_signin.setVisibility(login ? View.INVISIBLE : View.VISIBLE);
        me_usr_detail.setVisibility(login ? View.VISIBLE : View.INVISIBLE);
        if (login) {
            me_tv_nickname.setText(UserManager.getNickname());
            me_tv_email.setText(UserManager.getEmail());
        }
        me_btn_logout.setVisibility(login ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void initData() {
//        if (XLinkUserManager.getInstance().isUserAuthorized() && TextUtils.isEmpty(UserManager.getEmail())) {
//            XlinkCloudManager.getInstance()
//                             .getUserInfo(XLinkUserManager.getInstance()
//                                                          .getUid(), new XlinkRequestCallback<UserApi.UserInfoResponse>() {
//                                 @Override
//                                 public void onError(String error) {
//
//                                 }
//
//                                 @Override
//                                 public void onSuccess(UserApi.UserInfoResponse response) {
//                                     UserManager.setEmail(response.email);
//                                     UserManager.setNickname(response.nickname);
//                                     me_tv_nickname.setText(response.nickname);
//                                     me_tv_email.setText(response.email);
//                                     me_tv_nickname.setVisibility(TextUtils.isEmpty(response.nickname) ? View.GONE : View.VISIBLE);
//                                 }
//                             });
//        }
    }

    @Override
    protected void initEvent() {
//        me_icon_usr.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!XLinkUserManager.getInstance().isUserAuthorized()) {
//                    login();
//                    getActivity().finish();
//                } else {
//                    addFragmentToStack(R.id.main_fl, new MessagesFragment());
//                }
//            }
//        });
//        me_tv_nickname.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showModifyNicknameDialog();
//            }
//        });
//        me_mod_psw.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showModifyPasswordDialog();
//            }
//        });
//        me_btn_logout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int usrid = UserManager.getUserId(getContext());
//                XlinkCloudManager.getInstance().unregisterFCMMessageService(usrid, new XlinkRequestCallback<String>() {
//                    @Override
//                    public void onError(String error) {
//                        Log.e(TAG, "onError: " + error);
//                    }
//
//                    @Override
//                    public void onSuccess(String s) {
//
//                    }
//                });
//                XLinkSDK.logoutAndStop();
//                logout();
//            }
//        });
    }

    private void showModifyNicknameDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_rename, null, false);
        final TextInputLayout til = view.findViewById(R.id.dialog_rename_til);
        final AdvancedTextInputEditText et = view.findViewById(R.id.dialog_rename_et);
        et.bindTextInputLayout(til);
        til.setHint(getString(R.string.hint_nickname));
        et.setText(UserManager.getNickname());
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
//                XlinkCloudManager.getInstance().modifyNickname(name, new XlinkRequestCallback<String>() {
//                    @Override
//                    public void onError(String error) {
//                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
//                             .show();
//                    }
//
//                    @Override
//                    public void onSuccess(String s) {
//                        me_tv_nickname.setText(name);
//                        UserManager.setNickname(name);
//                        dialog.dismiss();
//                    }
//                });
            }
        });
    }

    private void showModifyPasswordDialog() {
        showOldPassword = false;
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_modify_password, null, false);
        final TextInputLayout til1 = view.findViewById(R.id.modify_psw_til1);
        final TextInputLayout til2 = view.findViewById(R.id.modify_psw_til2);
        final AdvancedTextInputEditText et_old = view.findViewById(R.id.modify_psw_old);
        final AdvancedTextInputEditText et_new = view.findViewById(R.id.modify_psw_new);
        et_old.bindTextInputLayout(til1);
        et_new.bindTextInputLayout(til2);
        et_old.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_white_24dp, 0, R.drawable.design_ic_visibility_off, 0);
        et_new.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_white_24dp, 0, R.drawable.design_ic_visibility_off, 0);
        et_old.setDrawableRightClickListener(new AdvancedTextInputEditText.DrawableRightClickListener() {
            @Override
            public void onDrawableRightClick() {
                showOldPassword = !showOldPassword;
                if (showOldPassword) {
                    et_old.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_white_24dp, 0, R.drawable.design_ic_visibility, 0);
                    et_old.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    et_old.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_white_24dp, 0, R.drawable.design_ic_visibility_off, 0);
                    et_old.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        et_new.setDrawableRightClickListener(new AdvancedTextInputEditText.DrawableRightClickListener() {
            @Override
            public void onDrawableRightClick() {
                showNewPassword = ! showNewPassword;
                if (showNewPassword) {
                    et_new.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_white_24dp, 0, R.drawable.design_ic_visibility, 0);
                    et_new.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    et_new.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_white_24dp, 0, R.drawable.design_ic_visibility_off, 0);
                    et_new.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
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
                String newpsw = et_new.getText().toString();
                if (TextUtils.isEmpty(newpsw) || newpsw.length() < 6) {
                    til2.setError(getString(R.string.error_password));
                    et_new.requestFocus();
                    return;
                }
//                XlinkCloudManager.getInstance().modifyPassword(oldpsw, newpsw, new XlinkRequestCallback<String>() {
//                    @Override
//                    public void onError(String error) {
//                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
//                             .show();
//                    }
//
//                    @Override
//                    public void onSuccess(String s) {
//                        dialog.dismiss();
//                    }
//                });
            }
        });
    }

    private void logout() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void login() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
    }
}
