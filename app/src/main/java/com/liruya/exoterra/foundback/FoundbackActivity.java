package com.liruya.exoterra.foundback;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

public class FoundbackActivity extends BaseImmersiveActivity {

    private final int VERIFY_CODE_SEND_INTERVAL = 120000;

    private TextInputLayout foundback_til_email;
    private AdvancedTextInputEditText foundback_et_email;
    private TextInputLayout foundback_til_password;
    private AdvancedTextInputEditText foundback_et_password;
    private TextInputLayout foundback_til_verifycode;
    private AdvancedTextInputEditText foundback_et_verifycode;
    private Button foundback_btn_send;
    private Button foundback_btn_found;
    private LoadDialog mLoadDialog;
    private ProgressDialog mProgressDialog;

    private CountDownTimer mSendVerifycodeTimer;
    private XlinkRequestCallback<String> mRequestVerifycodeCallback;
    private XlinkRequestCallback<String> mFoundbackPasswordCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
        initEvent();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_foundback;
    }

    @Override
    protected void initView() {
        foundback_til_email = findViewById(R.id.foundback_til_email);
        foundback_et_email = findViewById(R.id.foundback_et_email);
        foundback_til_password = findViewById(R.id.foundback_til_password);
        foundback_et_password = findViewById(R.id.foundback_et_password);
        foundback_til_verifycode = findViewById(R.id.foundback_til_verifycode);
        foundback_et_verifycode = findViewById(R.id.foundback_et_verifycode);
        foundback_btn_send = findViewById(R.id.foundback_btn_send);
        foundback_btn_found = findViewById(R.id.foundback_btn_found);

        foundback_et_email.bindTextInputLayout(foundback_til_email);
        foundback_et_verifycode.bindTextInputLayout(foundback_til_verifycode);
        foundback_et_password.bindTextInputLayout(foundback_til_password);
    }

    @Override
    protected void initData() {
        mLoadDialog = new LoadDialog(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        foundback_et_email.requestFocus();
        Intent intent = getIntent();
        if (intent != null) {
            String email = intent.getStringExtra("email");
            if (!TextUtils.isEmpty(email)) {
                foundback_et_email.setText(email);
                foundback_et_password.requestFocus();
            }
        }
        mSendVerifycodeTimer = new CountDownTimer(VERIFY_CODE_SEND_INTERVAL, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                foundback_btn_send.setText("" + millisUntilFinished/1000);
            }

            @Override
            public void onFinish() {
                foundback_btn_send.setText(R.string.send_verifycode);
                foundback_btn_send.setEnabled(true);
            }
        };
        mRequestVerifycodeCallback = new XlinkRequestCallback<String>() {
            @Override
            public void onStart() {
                showLoading();
            }

            @Override
            public void onError(String error) {
                dismissLoading();
                Toast.makeText(FoundbackActivity.this, error, Toast.LENGTH_SHORT)
                     .show();
            }

            @Override
            public void onSuccess(String s) {
                dismissLoading();
                foundback_btn_send.setEnabled(false);
                mSendVerifycodeTimer.start();
            }
        };
        mFoundbackPasswordCallback = new XlinkRequestCallback<String>() {
            @Override
            public void onStart() {
                showLoading();
            }

            @Override
            public void onError(String error) {
                dismissLoading();
                Toast.makeText(FoundbackActivity.this, error, Toast.LENGTH_SHORT)
                     .show();
            }

            @Override
            public void onSuccess(String s) {
                dismissLoading();
                showFoundbackSuccessDialog();
            }
        };
    }

    @Override
    protected void initEvent() {
        foundback_et_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                foundback_til_email.setError(null);
            }
        });
        foundback_et_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                foundback_til_password.setError(null);
            }
        });
        foundback_et_verifycode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                foundback_til_verifycode.setError(null);
            }
        });
        foundback_btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = getEmailText();
                if (RegexUtil.isEmail(email)) {
                    XlinkCloudManager.getInstance()
                                     .requestEmailFoundbackPasswordVerifyCode(email, mRequestVerifycodeCallback);
                } else {
                    foundback_til_email.setError(getString(R.string.error_email));
                }
            }
        });
        foundback_btn_found.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = getEmailText();
                String password = getPasswordText();
                String verifycode = getVerifycodeText();
                if (!RegexUtil.isEmail(email)) {
                    foundback_til_email.setError(getString(R.string.error_email));
                    foundback_et_email.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(password) || password.length() < 6) {
                    foundback_til_password.setError(getString(R.string.error_password));
                    foundback_et_password.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(verifycode) || verifycode.length() != 6) {
                    foundback_til_verifycode.setError(getString(R.string.error_verifycode));
                    foundback_et_verifycode.requestFocus();
                    return;
                }
                XlinkCloudManager.getInstance().foundbackPassword(email, password, verifycode, mFoundbackPasswordCallback);
            }
        });
    }

    private String getEmailText() {
        return foundback_et_email.getText().toString();
    }

    private String getPasswordText() {
        return foundback_et_password.getText().toString();
    }

    private String getVerifycodeText() {
        return foundback_et_verifycode.getText().toString();
    }

    private void showLoading() {
//        mLoadDialog.show();
        mProgressDialog.show();
    }

    private void dismissLoading() {
//        mLoadDialog.dismiss();
        mProgressDialog.dismiss();
    }

    private void showFoundbackSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_foundback_success)
               .setMessage(R.string.msg_foundback_success)
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
