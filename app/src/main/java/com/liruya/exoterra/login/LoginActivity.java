package com.liruya.exoterra.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.liruya.base.BaseImmersiveActivity;
import com.liruya.exoterra.R;
import com.liruya.exoterra.foundback.FoundbackActivity;
import com.liruya.exoterra.main.MainActivity;
import com.liruya.exoterra.manager.UserManager;
import com.liruya.exoterra.register.RegisterActivity;
import com.liruya.exoterra.util.RegexUtil;
import com.liruya.exoterra.view.AdvancedTextInputEditText;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.loaddialog.LoadDialog;

import cn.xlink.restful.api.app.UserAuthApi;
import cn.xlink.sdk.core.XLinkCoreException;
import cn.xlink.sdk.v5.listener.XLinkTaskListener;
import cn.xlink.sdk.v5.module.main.XLinkSDK;

public class LoginActivity extends BaseImmersiveActivity {

    private TextInputLayout login_til_email;
    private AdvancedTextInputEditText login_et_email;
    private TextInputLayout login_til_password;
    private AdvancedTextInputEditText login_et_password;
    private Button login_btn_signin;
    private Button login_btn_forget;
    private Button login_btn_signup;
    private Button login_btn_skip;
    private LoadDialog mLoadDialog;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onStart() {
        super.onStart();

        initData();
        initEvent();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 1) {
            if (data != null) {
                String email = data.getStringExtra("email");
                String password = data.getStringExtra("password");
                login_et_email.setText(email);
                login_et_password.setText(password);
            }
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_login;
    }

    @Override
    protected void initView() {
        login_til_email = findViewById(R.id.login_til_email);
        login_et_email = findViewById(R.id.login_et_email);
        login_til_password = findViewById(R.id.login_til_password);
        login_et_password = findViewById(R.id.login_et_password);
        login_btn_signin = findViewById(R.id.login_btn_signin);
        login_btn_forget = findViewById(R.id.login_btn_forget);
        login_btn_signup = findViewById(R.id.login_btn_signup);
        login_btn_skip = findViewById(R.id.login_btn_skip);

        login_et_email.bindTextInputLayout(login_til_email);
        login_et_password.bindTextInputLayout(login_til_password);
    }

    @Override
    protected void initData() {
        XLinkSDK.start();

        String email = UserManager.getAccount(this);
        if (TextUtils.isEmpty(email)) {
            login_et_email.requestFocus();
        } else {
            login_et_email.setText(email);
            login_et_password.requestFocus();
        }
        mLoadDialog = new LoadDialog(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);
    }

    @Override
    protected void initEvent() {
        login_btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = getEmailText();
                final String password = getPasswordText();
                if (!RegexUtil.isEmail(email)) {
                    login_til_email.setError(getString(R.string.error_email));
                    login_et_email.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(password) || password.length() < 6) {
                    login_til_password.setError(getString(R.string.error_password));
                    login_et_password.requestFocus();
                    return;
                }
                Log.e(TAG, "onClick: login....");
                XlinkCloudManager.getInstance().login(email, password, 5000, new XLinkTaskListener<UserAuthApi.UserAuthResponse>() {
                    @Override
                    public void onError(XLinkCoreException e) {
                        Log.e(TAG, "onError: ");
                        Toast.makeText(LoginActivity.this, e.getErrorName(), Toast.LENGTH_SHORT)
                             .show();
                        dismissLoading();
                    }

                    @Override
                    public void onStart() {
                        Log.e(TAG, "onStart: ");
                        showLoading();
                    }

                    @Override
                    public void onComplete(UserAuthApi.UserAuthResponse response) {
                        Log.e(TAG, "onComplete: ");
                        dismissLoading();
                        UserManager.setAccount(LoginActivity.this, email);
                        UserManager.setPassword(LoginActivity.this, password);
                        UserManager.setUserId(LoginActivity.this, response.userId);
                        UserManager.setAuthorize(LoginActivity.this, response.authorize);
                        UserManager.setRefreshToken(LoginActivity.this, response.refreshToken);
                        gotoMainActivity();
                    }
                });
            }
        });
        login_btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoRegisterActivity();
            }
        });
        login_btn_forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoFoundbackActivity();
            }
        });
        login_btn_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoMainActivity();
            }
        });
    }

    private String getEmailText() {
        return login_et_email.getText().toString();
    }

    private String getPasswordText() {
        return login_et_password.getText().toString();
    }

    public void showLoading() {
//        mLoadDialog.show();
        mProgressDialog.show();
    }

    public void dismissLoading() {
//        mLoadDialog.dismiss();
        mProgressDialog.dismiss();
    }

    public void gotoMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void gotoRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivityForResult(intent, 1);
    }

    public void gotoFoundbackActivity() {
        Intent intent = new Intent(this, FoundbackActivity.class);
        intent.putExtra("email", getEmailText());
        startActivityForResult(intent, 1);
    }
}
