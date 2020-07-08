package com.inledco.exoterra.foundback;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.AliotServer;
import com.inledco.exoterra.aliot.HttpCallback;
import com.inledco.exoterra.aliot.UserApi;
import com.inledco.exoterra.base.BaseActivity;
import com.inledco.exoterra.manager.VerifycodeManager;
import com.inledco.exoterra.util.RegexUtil;
import com.inledco.exoterra.util.SizeUtil;
import com.inledco.exoterra.view.AdvancedTextInputEditText;
import com.inledco.exoterra.view.PasswordEditText;

public class FoundbackActivity extends BaseActivity {
    private TextInputLayout foundback_til_email;
    private AdvancedTextInputEditText foundback_et_email;
    private TextInputLayout foundback_til_verifycode;
    private AdvancedTextInputEditText foundback_et_verifycode;
    private TextInputLayout foundback_til_password;
    private PasswordEditText foundback_et_password;
    private TextInputLayout foundback_til_confirm;
    private PasswordEditText foundback_et_confirm;
    private Button foundback_btn_send;
    private Button foundback_btn_found;
    private Button foundback_btn_back;

    private HttpCallback<UserApi.Response> mVerifycodeCallback;
    private HttpCallback<UserApi.Response> mFoundbackPasswordCallback;

    private VerifycodeManager mVerifycodeManager;

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

    @SuppressLint ("ResourceAsColor")
    @Override
    protected void initView() {
        foundback_til_email = findViewById(R.id.foundback_til_email);
        foundback_et_email = findViewById(R.id.foundback_et_email);
        foundback_til_verifycode = findViewById(R.id.foundback_til_verifycode);
        foundback_et_verifycode = findViewById(R.id.foundback_et_verifycode);
        foundback_til_password = findViewById(R.id.foundback_til_password);
        foundback_et_password = findViewById(R.id.foundback_et_password);
        foundback_til_confirm = findViewById(R.id.foundback_til_confirm);
        foundback_et_confirm = findViewById(R.id.foundback_et_confirm);
//        foundback_btn_send = findViewById(R.id.foundback_btn_send);
        foundback_btn_found = findViewById(R.id.foundback_btn_found);
        foundback_btn_back = findViewById(R.id.foundback_btn_back);

        foundback_et_email.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email_white_24dp, 0, 0, 0);
        foundback_et_verifycode.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_verify_white_24dp, 0, 0, 0);
        foundback_et_password.setIcon(R.drawable.ic_lock_white_24dp, R.drawable.design_ic_visibility, R.drawable.design_ic_visibility_off);
        foundback_et_confirm.setIcon(R.drawable.ic_lock_white_24dp, R.drawable.design_ic_visibility, R.drawable.design_ic_visibility_off);

        foundback_btn_send = new Button(this);
        foundback_btn_send.setText(R.string.send_verifycode);
        foundback_btn_send.setTextColor(getResources().getColor(R.color.colorAccent));
        foundback_btn_send.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        int[] attrs = new int[] {android.R.attr.selectableItemBackground};
        TypedArray ta = obtainStyledAttributes(attrs);
        foundback_btn_send.setBackground(ta.getDrawable(0));
        ta.recycle();
        View view = foundback_til_verifycode.getChildAt(0);
        if (view != null && view instanceof FrameLayout) {
            FrameLayout fl = (FrameLayout) view;
            fl.addView(foundback_btn_send);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) foundback_btn_send.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.rightMargin = SizeUtil.dp2px(8);
            lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        }
    }

    @Override
    protected void initData() {
        foundback_et_email.requestFocus();
        Intent intent = getIntent();
        if (intent != null) {
            String email = intent.getStringExtra("email");
            if (!TextUtils.isEmpty(email)) {
                foundback_et_email.setText(email);
                foundback_et_password.requestFocus();
            }
        }
        mVerifycodeCallback = new HttpCallback<UserApi.Response>() {
            @Override
            public void onError(final String error) {
                Log.e(TAG, "onError: " + error);
                dismissLoadDialog();
                showToast(error);
            }

            @Override
            public void onSuccess(UserApi.Response result) {
                Log.e(TAG, "onSuccess: " + JSON.toJSONString(result));
                dismissLoadDialog();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getMessageDialog().setTitle(R.string.title_verifycode_sent)
                                          .setMessage(R.string.msg_get_verifycode)
                                          .show();
                    }
                });
            }
        };
        mFoundbackPasswordCallback = new HttpCallback<UserApi.Response>() {
            @Override
            public void onError(final String error) {
                Log.e(TAG, "onError: " + error);
                dismissLoadDialog();
                showToast(error);
            }

            @Override
            public void onSuccess(UserApi.Response result) {
                Log.e(TAG, "onSuccess: " + JSON.toJSONString(result));
                dismissLoadDialog();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showFoundbackSuccessDialog();
                    }
                });
            }
        };
    }

    @Override
    protected void initEvent() {
        foundback_btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        foundback_btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = getEmailText();
                if (!RegexUtil.isEmail(email)) {
                    foundback_til_email.setError(getString(R.string.error_email));
                    foundback_et_email.requestFocus();
                    return;
                }
                if (mVerifycodeManager == null) {
                    mVerifycodeManager = new VerifycodeManager(FoundbackActivity.this);
                }
                long expired = mVerifycodeManager.getResetExpired(email);
                if (expired > 0) {
                    Toast.makeText(FoundbackActivity.this, "Please wait for " + expired + " second.", Toast.LENGTH_SHORT)
                         .show();
                    return;
                }
                AliotServer.getInstance().getEmailVerifycode(email, mVerifycodeCallback);
                showLoadDialog();
                mVerifycodeManager.addResetVerifycode(email);
            }
        });

        foundback_btn_found.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = getEmailText();
                if (!RegexUtil.isEmail(email)) {
                    foundback_til_email.setError(getString(R.string.error_email));
                    foundback_et_email.requestFocus();
                    return;
                }
                String verifycode = getVerifycodeText();
                if (TextUtils.isEmpty(verifycode) || verifycode.length() != getResources().getInteger(R.integer.verifycode_text_length_max)) {
                    foundback_til_verifycode.setError(getString(R.string.error_verifycode));
                    foundback_et_verifycode.requestFocus();
                    return;
                }
                String password = getPasswordText();
                if (TextUtils.isEmpty(password) || password.length() < getResources().getInteger(R.integer.password_text_length_min)) {
                    foundback_til_password.setError(getString(R.string.error_password));
                    foundback_et_password.requestFocus();
                    return;
                }
                String confirm = getConfirmPasswordText();
                if (TextUtils.equals(password, confirm) == false) {
                    foundback_til_confirm.setError(getString(R.string.password_inconsistent));
                    foundback_et_confirm.requestFocus();
                    return;
                }
                AliotServer.getInstance().resetPassword(email, verifycode, password, mFoundbackPasswordCallback);
                showLoadDialog();
            }
        });
    }

    private String getEmailText() {
        return foundback_et_email.getText().toString();
    }

    private String getPasswordText() {
        return foundback_et_password.getText().toString();
    }

    private String getConfirmPasswordText() {
        return foundback_et_confirm.getText().toString();
    }

    private String getVerifycodeText() {
        return foundback_et_verifycode.getText().toString();
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
        intent.putExtra("login", false);
        setResult(1, intent);
        finish();
    }
}
