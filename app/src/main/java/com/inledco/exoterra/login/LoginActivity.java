package com.inledco.exoterra.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.alibaba.fastjson.JSON;
import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.AliotClient;
import com.inledco.exoterra.aliot.ExoSocket;
import com.inledco.exoterra.aliot.KeyValue;
import com.inledco.exoterra.base.BaseActivity;
import com.inledco.exoterra.foundback.FoundbackActivity;
import com.inledco.exoterra.main.MainActivity;
import com.inledco.exoterra.manager.UserManager;
import com.inledco.exoterra.register.RegisterActivity;
import com.inledco.exoterra.util.RegexUtil;
import com.inledco.exoterra.view.AdvancedTextInputEditText;
import com.liruya.loaddialog.LoadDialog;

public class LoginActivity extends BaseActivity {

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

    private boolean showPassword;

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
                boolean login = data.getBooleanExtra("login", false);
                login_et_email.setText(email);
                login_et_password.setText(password);
                if (login) {
                    login_btn_signin.performClick();
                }
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

        login_et_email.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email_white_24dp, 0, 0, 0);
        login_et_password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_white_24dp, 0, R.drawable.design_ic_visibility_off, 0);
        login_et_email.bindTextInputLayout(login_til_email);
        login_et_password.bindTextInputLayout(login_til_password);

        AliotClient.getInstance().init(LoginActivity.this, "test001", "zoi45K3ZgWcNeZGjdAIoDQlAKMZ1z3ij");
    }

    @Override
    protected void initData() {
        final String email = UserManager.getAccount(this);
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
        login_et_password.setDrawableRightClickListener(new AdvancedTextInputEditText.DrawableRightClickListener() {
            @Override
            public void onDrawableRightClick() {
                showPassword = !showPassword;
                if (showPassword) {
                    login_et_password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_white_24dp, 0, R.drawable.design_ic_visibility, 0);
                    login_et_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    login_et_password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_white_24dp, 0, R.drawable.design_ic_visibility_off, 0);
                    login_et_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        login_btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoMainActivity();

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
//                XlinkCloudManager.getInstance().login(email, password, 5000, new XlinkTaskCallback<UserAuthApi.UserAuthResponse>() {
//                    @Override
//                    public void onError(String error) {
//                        new MessageDialog(LoginActivity.this).setTitle(R.string.signin_failed)
//                                                             .setMessage(error)
//                                                             .setButton(getString(R.string.ok), null)
//                                                             .show();
//                        dismissLoading();
//                    }
//
//                    @Override
//                    public void onStart() {
//                        showLoading();
//                    }
//
//                    @Override
//                    public void onComplete(UserAuthApi.UserAuthResponse response) {
//                        dismissLoading();
//                        UserManager.setAccount(LoginActivity.this, email);
//                        UserManager.setPassword(LoginActivity.this, password);
//                        UserManager.setUserId(LoginActivity.this, response.userId);
//                        UserManager.setToken(LoginActivity.this, response.authorize);
//                        gotoMainActivity();
//                    }
//                });
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
//                gotoMainActivity();
//                AliotServer.getInstance().test();
//                UserApi.UserRegisterRequest request = new UserApi.UserRegisterRequest();
//                request.email = "123";
//                Log.e(TAG, "onClick: " + JSON.toJSONString(request));
//                test();
//                test();
            }
        });
    }

    private void test() {
        ExoSocket.SensorConfig cfg = new ExoSocket.SensorConfig();
        KeyValue attr1 = new KeyValue("attr1", cfg);
        Log.e(TAG, "test: " + JSON.toJSONString(attr1));

        ExoSocket.SensorConfig[] cfgs = new ExoSocket.SensorConfig[2];
        cfgs[0] = new ExoSocket.SensorConfig();
        cfgs[1] = new ExoSocket.SensorConfig();
        KeyValue attrs = new KeyValue("attrs", cfgs);
        Log.e(TAG, "test: " + JSON.toJSONString(attrs));

        AliotClient.getInstance().setProperty("123", "12345", attr1, attrs);
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
