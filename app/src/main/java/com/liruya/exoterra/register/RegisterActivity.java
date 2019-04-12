package com.liruya.exoterra.register;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.liruya.base.BaseActivity;
import com.liruya.exoterra.R;
import com.liruya.exoterra.util.RegexUtil;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkRequestCallback;
import com.liruya.loaddialog.LoadDialog;

import cn.xlink.restful.api.app.UserAuthApi;

public class RegisterActivity extends BaseActivity {

    private TextInputEditText register_et_email;
    private TextInputEditText register_et_nickname;
    private TextInputEditText register_et_password;
    private Button register_btn_signup;
    private LoadDialog mLoadDialog;
    private ProgressDialog mProgressDialog;

    private XlinkRequestCallback<UserAuthApi.EmailRegisterResponse> mRegisterCallback;

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
        register_et_email = findViewById(R.id.register_et_email);
        register_et_nickname = findViewById(R.id.register_et_nickname);
        register_et_password = findViewById(R.id.register_et_password);
        register_btn_signup = findViewById(R.id.register_btn_signup);
    }

    @Override
    protected void initData() {
        mLoadDialog = new LoadDialog(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        mRegisterCallback = new XlinkRequestCallback<UserAuthApi.EmailRegisterResponse>() {
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
            public void onSuccess(UserAuthApi.EmailRegisterResponse emailRegisterResponse) {
                dismissLoading();
                showRegisterSuccessDialog();
            }
        };
    }

    @Override
    protected void initEvent() {
        register_btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = getEmailText();
                String nickname = getNicknameText();
                String password = getPasswordText();
                if (!RegexUtil.isEmail(email)) {
                    register_et_email.setError(getString(R.string.error_email));
                    register_et_email.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(nickname)) {
                    register_et_nickname.setError(getString(R.string.error_nickname));
                    register_et_email.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(password) || password.length() < 6) {
                    register_et_password.setError(getString(R.string.error_password));
                    register_et_password.requestFocus();
                    return;
                }
                XlinkCloudManager.getInstance().register(email, nickname, password, mRegisterCallback);
            }
        });
    }

    private String getEmailText() {
        return register_et_email.getText().toString();
    }

    private String getNicknameText() {
        return register_et_nickname.getText().toString();
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
