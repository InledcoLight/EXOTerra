package com.inledco.exoterra.register;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.AliotServer;
import com.inledco.exoterra.aliot.HttpCallback;
import com.inledco.exoterra.aliot.UserApi;
import com.inledco.exoterra.base.BaseActivity;
import com.inledco.exoterra.manager.VerifycodeManager;
import com.inledco.exoterra.util.RegexUtil;
import com.inledco.exoterra.util.SizeUtil;
import com.inledco.exoterra.view.AdvancedTextInputEditText;
import com.inledco.exoterra.view.MessageDialog;
import com.inledco.exoterra.view.PasswordEditText;

public class RegisterActivity extends BaseActivity {
    private TextInputLayout register_til_email;
    private AdvancedTextInputEditText register_et_email;
    private TextInputLayout register_til_verifycode;
    private AdvancedTextInputEditText register_et_verifycode;
    private TextInputLayout register_til_nickname;
    private AdvancedTextInputEditText register_et_nickname;
    private TextInputLayout register_til_password;
    private PasswordEditText register_et_password;
    private TextInputLayout register_til_confirm;
    private PasswordEditText register_et_confirm;
    private CheckBox register_cb;
    private TextView register_tv_agree;
    private Button register_btn_send;
    private Button register_btn_signup;
    private Button register_btn_back;

    private VerifycodeManager mVerifycodeManager;

    private HttpCallback<UserApi.Response> mVerifycodeCallback;
    private HttpCallback<UserApi.Response> mRegisterCallback;

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
//        register_til_nickname = findViewById(R.id.register_til_nickname);
//        register_et_nickname = findViewById(R.id.register_et_nickname);
        register_til_email = findViewById(R.id.register_til_email);
        register_et_email = findViewById(R.id.register_et_email);
        register_til_verifycode = findViewById(R.id.register_til_verifycode);
        register_et_verifycode = findViewById(R.id.register_et_verifycode);
        register_til_password = findViewById(R.id.register_til_password);
        register_et_password = findViewById(R.id.register_et_password);
        register_til_confirm = findViewById(R.id.register_til_confirm);
        register_et_confirm = findViewById(R.id.register_et_confirm);
        register_cb = findViewById(R.id.register_cb);
        register_tv_agree = findViewById(R.id.register_tv_agree);
        register_btn_signup = findViewById(R.id.register_btn_signup);
        register_btn_back = findViewById(R.id.register_btn_back);

//        register_et_nickname.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_white_24dp, 0, 0, 0);
        register_et_email.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email_white_24dp, 0, 0, 0);
        register_et_verifycode.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_verify_white_24dp, 0, 0, 0);
        register_et_password.setIcon(R.drawable.ic_lock_white_24dp, R.drawable.design_ic_visibility, R.drawable.design_ic_visibility_off);
        register_et_confirm.setIcon(R.drawable.ic_lock_white_24dp, R.drawable.design_ic_visibility, R.drawable.design_ic_visibility_off);


        String text = "Have read and accept User Agreement";
        SpannableString ss = new SpannableString(text);
        int start = text.indexOf("User Agreement");
        int end = start + "User Agreement".length();
        final ClickableSpan cs1 = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {

            }
        };
        ss.setSpan(cs1, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        register_tv_agree.setText(ss);
        register_tv_agree.setMovementMethod(LinkMovementMethod.getInstance());

        register_btn_send = new Button(this);
        register_btn_send.setText(R.string.send_verifycode);
        register_btn_send.setTextColor(getResources().getColor(R.color.colorAccent));
        register_btn_send.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        int[] attrs = new int[] {android.R.attr.selectableItemBackground};
        TypedArray ta = obtainStyledAttributes(attrs);
        register_btn_send.setBackground(ta.getDrawable(0));
        ta.recycle();
        View view = register_til_verifycode.getChildAt(0);
        if (view != null && view instanceof FrameLayout) {
            FrameLayout fl = (FrameLayout) view;
            fl.addView(register_btn_send);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) register_btn_send.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.rightMargin = SizeUtil.dp2px(8);
            lp.gravity = Gravity.RIGHT|Gravity.CENTER_VERTICAL;
        }
    }

    @Override
    protected void initData() {
        mVerifycodeCallback = new HttpCallback<UserApi.Response>() {
            @Override
            public void onError(final String error) {
                dismissLoadDialog();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getMessageDialog().setTitle(R.string.title_verifycode_sent_failed)
                                          .setMessage(error)
                                          .show();
                    }
                });
            }

            @Override
            public void onSuccess(UserApi.Response result) {
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

        mRegisterCallback = new HttpCallback<UserApi.Response>() {
            @Override
            public void onError(final String error) {
                dismissLoadDialog();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new MessageDialog(RegisterActivity.this).setTitle(R.string.signup_failed)
                                                                .setMessage(error)
                                                                .setButton(getString(R.string.ok), null)
                                                                .show();
                    }
                });
            }

            @Override
            public void onSuccess(UserApi.Response result) {
                dismissLoadDialog();
                backtoLoginActivity(true);
            }
        };
    }

    @Override
    protected void initEvent() {
        register_btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        register_btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                AliotServer.getInstance().getEmailVerifycode(email, mVerifycodeCallback);
                showLoadDialog();
                mVerifycodeManager.addRegisterVerifycode(email);
            }
        });

        register_btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nickname = "";
//                String nickname = getNicknameText();
//                if (TextUtils.isEmpty(nickname)) {
//                    register_til_nickname.setError(getString(R.string.error_empty));
//                    register_et_nickname.requestFocus();
//                    return;
//                }
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
                if (TextUtils.isEmpty(password) || password.length() < getResources().getInteger(R.integer.password_text_length_min)) {
                    register_til_password.setError(getString(R.string.error_password));
                    register_et_password.requestFocus();
                    return;
                }
                String confirm = getConfirmPasswordText();
                if (TextUtils.equals(password, confirm) == false) {
                    register_til_confirm.setError(getString(R.string.password_inconsistent));
                    register_et_confirm.requestFocus();
                    return;
                }
                AliotServer.getInstance().register(email, password, verifycode, nickname, mRegisterCallback);
                showLoadDialog();
            }
        });
    }

    private String getEmailText() {
        return register_et_email.getText().toString();
    }

    private String getVerifycodeText() {
        return register_et_verifycode.getText().toString();
    }

    private String getNicknameText() {
        return register_et_nickname.getText().toString();
    }

    private String getPasswordText() {
        return register_et_password.getText().toString();
    }

    private String getConfirmPasswordText() {
        return register_et_confirm.getText().toString();
    }

    private void showRegisterSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_register_success)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       backtoLoginActivity(false);
                   }
               })
               .setCancelable(false)
               .show();
    }

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
