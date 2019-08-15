package com.liruya.exoterra.register;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.liruya.base.BaseImmersiveActivity;
import com.liruya.exoterra.R;
import com.liruya.exoterra.util.RegexUtil;
import com.liruya.exoterra.view.AdvancedTextInputEditText;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkRequestCallback;
import com.liruya.loaddialog.LoadDialog;

import cn.xlink.restful.api.app.UserAuthApi;

public class RegisterActivity extends BaseImmersiveActivity {

    private TextInputLayout register_til_email;
    private AdvancedTextInputEditText register_et_email;
    private TextInputLayout register_til_verifycode;
    private AdvancedTextInputEditText register_et_verifycode;
    private TextInputLayout register_til_password;
    private AdvancedTextInputEditText register_et_password;
    private Button register_btn_send;
    private Button register_btn_signup;
    private Button register_btn_signin;
    private LoadDialog mLoadDialog;
    private ProgressDialog mProgressDialog;

    private XlinkRequestCallback<String> mVerifycodeCallback;
    private XlinkRequestCallback<UserAuthApi.EmailVerifyCodeRegisterResponse> mRegisterCallback;

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
        register_til_email = findViewById(R.id.register_til_email);
        register_et_email = findViewById(R.id.register_et_email);
        register_til_verifycode = findViewById(R.id.register_til_verifycode);
        register_et_verifycode = findViewById(R.id.register_et_verifycode);
        register_til_password = findViewById(R.id.register_til_password);
        register_et_password = findViewById(R.id.register_et_password);
        register_btn_send = findViewById(R.id.register_btn_send);
        register_btn_signup = findViewById(R.id.register_btn_signup);
        register_btn_signin = findViewById(R.id.register_btn_signin);

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
            public void onStart() {

            }

            @Override
            public void onError(String error) {
                Toast.makeText(RegisterActivity.this, "发送邮箱验证码失败", Toast.LENGTH_SHORT)
                     .show();
            }

            @Override
            public void onSuccess(String s) {
                Toast.makeText(RegisterActivity.this, "发送邮箱验证码成功. " + s, Toast.LENGTH_SHORT)
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
                Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_SHORT)
                     .show();
            }

            @Override
            public void onSuccess(UserAuthApi.EmailVerifyCodeRegisterResponse response) {
                dismissLoading();
                showRegisterSuccessDialog();
            }
        };
    }

    @Override
    protected void initEvent() {
        register_btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = getEmailText();
                if (!RegexUtil.isEmail(email)) {
                    register_til_email.setError(getString(R.string.error_email));
                    register_et_email.requestFocus();
                    return;
                }
                XlinkCloudManager.getInstance().requestRegisterEmailVerifycode(email, mVerifycodeCallback);
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

        register_btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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

    private void showRegisterSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_register_success)
               .setMessage(R.string.msg_active_account)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       backtoLoginActivity();
                   }
               })
               .setCancelable(false)
               .show();
    }

    private void backtoLoginActivity() {
        String email = getEmailText();
        String password = getPasswordText();
        Intent intent = new Intent();
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        setResult(1, intent);
        finish();
    }
}
