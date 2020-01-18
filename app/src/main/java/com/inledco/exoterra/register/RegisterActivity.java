package com.inledco.exoterra.register;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseActivity;
import com.inledco.exoterra.manager.VerifycodeManager;
import com.inledco.exoterra.util.RegexUtil;
import com.inledco.exoterra.view.AdvancedTextInputEditText;
import com.inledco.exoterra.view.MessageDialog;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;
import com.liruya.loaddialog.LoadDialog;

import cn.xlink.restful.api.app.UserAuthApi;

public class RegisterActivity extends BaseActivity {
    private Toolbar register_toolbar;
    private TextInputLayout register_til_email;
    private AdvancedTextInputEditText register_et_email;
    private TextInputLayout register_til_verifycode;
    private AdvancedTextInputEditText register_et_verifycode;
    private TextInputLayout register_til_password;
    private AdvancedTextInputEditText register_et_password;
    private Button register_btn_signup;
    private LoadDialog mLoadDialog;
    private ProgressDialog mProgressDialog;

    private XlinkRequestCallback<String> mVerifycodeCallback;
    private XlinkRequestCallback<UserAuthApi.EmailVerifyCodeRegisterResponse> mRegisterCallback;

    private boolean showPassword;

    private VerifycodeManager mVerifycodeManager;

    @Override
    protected void onStart() {
        super.onStart();

        initData();
        initEvent();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_register;
    }

    @Override
    protected void initView() {
        register_toolbar = findViewById(R.id.register_toolbar);
        register_til_email = findViewById(R.id.register_til_email);
        register_et_email = findViewById(R.id.register_et_email);
        register_til_verifycode = findViewById(R.id.register_til_verifycode);
        register_et_verifycode = findViewById(R.id.register_et_verifycode);
        register_til_password = findViewById(R.id.register_til_password);
        register_et_password = findViewById(R.id.register_et_password);
        register_btn_signup = findViewById(R.id.register_btn_signup);

        setSupportActionBar(register_toolbar);
        register_et_email.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email_white_24dp, 0, 0, 0);
        register_et_verifycode.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_verify_white_24dp, 0, R.drawable.ic_send_white_24dp, 0);
        register_et_password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_white_24dp, 0, R.drawable.design_ic_visibility_off, 0);
        register_et_email.bindTextInputLayout(register_til_email);
        register_et_verifycode.bindTextInputLayout(register_til_verifycode);
        register_et_password.bindTextInputLayout(register_til_password);
    }

    @Override
    protected void initData() {
        mLoadDialog = new LoadDialog(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        mVerifycodeCallback = new XlinkRequestCallback<String>() {
            @Override
            public void onError(String error) {
                Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_SHORT)
                     .show();
                getMessageDialog().setTitle(R.string.title_verifycode_sent_failed)
                                  .setMessage(error)
                                  .show();
            }

            @Override
            public void onSuccess(String s) {
                getMessageDialog().setTitle(R.string.title_verifycode_sent)
                                  .setMessage(R.string.msg_get_verifycode)
                                  .show();
            }
        };

        mRegisterCallback = new XlinkRequestCallback<UserAuthApi.EmailVerifyCodeRegisterResponse>() {
            @Override
            public void onStart() {
                showLoading();
            }

            @Override
            public void onError(String error) {
                dismissLoading();
                new MessageDialog(RegisterActivity.this).setTitle(R.string.signup_failed)
                                                     .setMessage(error)
                                                     .setButton(getString(R.string.ok), null)
                                                     .show();
            }

            @Override
            public void onSuccess(UserAuthApi.EmailVerifyCodeRegisterResponse response) {
                dismissLoading();
                backtoLoginActivity(true);
            }
        };
    }

    @Override
    protected void initEvent() {
        register_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        register_et_verifycode.setDrawableRightClickListener(new AdvancedTextInputEditText.DrawableRightClickListener() {
            @Override
            public void onDrawableRightClick() {
                String email = getEmailText();
                if (!RegexUtil.isEmail(email)) {
                    register_til_email.setError(getString(R.string.error_email));
                    register_et_email.requestFocus();
                    return;
                }
                if (mVerifycodeManager == null) {
                    mVerifycodeManager = new VerifycodeManager(RegisterActivity.this);
                }
                long expired = mVerifycodeManager.getRegisterExpired(email);
                if (expired > 0) {
                    Toast.makeText(RegisterActivity.this, "Please wait for " + expired + " second.", Toast.LENGTH_SHORT)
                         .show();
                    return;
                }
                XlinkCloudManager.getInstance().requestRegisterEmailVerifycode(email, mVerifycodeCallback);
                mVerifycodeManager.addRegisterVerifycode(email);
            }
        });

        register_et_password.setDrawableRightClickListener(new AdvancedTextInputEditText.DrawableRightClickListener() {
            @Override
            public void onDrawableRightClick() {
                showPassword = !showPassword;
                if (showPassword) {
                    register_et_password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_white_24dp, 0, R.drawable.design_ic_visibility, 0);
                    register_et_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    register_et_password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_white_24dp, 0, R.drawable.design_ic_visibility_off, 0);
                    register_et_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        register_btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = getEmailText();
                if (!RegexUtil.isEmail(email)) {
                    register_til_email.setError(getString(R.string.error_email));
                    register_et_email.requestFocus();
                    return;
                }
                String verifycode = getVerifycodeText();
                if (TextUtils.isEmpty(verifycode)) {
                    register_til_verifycode.setError(getString(R.string.error_verifycode));
                    register_et_verifycode.requestFocus();
                    return;
                }
                String password = getPasswordText();
                if (TextUtils.isEmpty(password) || password.length() < 6) {
                    register_til_password.setError(getString(R.string.error_password));
                    register_et_password.requestFocus();
                    return;
                }
                XlinkCloudManager.getInstance().registerEmailByVerifycode(email, password, verifycode, mRegisterCallback);
            }
        });
    }

    private String getEmailText() {
        return register_et_email.getText().toString();
    }

    private String getVerifycodeText() {
        return register_et_verifycode.getText().toString();
    }

    private String getPasswordText() {
        return register_et_password.getText().toString();
    }

    private void showLoading() {
//        mLoadDialog.show();
        mProgressDialog.show();
    }

    private void dismissLoading() {
//        mLoadDialog.dismiss();
        mProgressDialog.dismiss();
    }

//    private void showRegisterSuccessDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle(R.string.title_register_success)
//               .setMsg(R.string.msg_active_account)
//               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                   @Override
//                   public void onClick(DialogInterface dialog, int which) {
//                       backtoLoginActivity(true);
//                   }
//               })
//               .setCancelable(false)
//               .show();
//    }

    private void backtoLoginActivity(boolean login) {
        String email = getEmailText();
        String password = getPasswordText();
        Intent intent = new Intent();
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        intent.putExtra("login", login);
        setResult(1, intent);
        finish();
    }
}
