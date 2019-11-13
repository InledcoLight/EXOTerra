package com.inledco.exoterra.test;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TestActivity extends BaseActivity {
    private Toolbar test_toolbar;
    private TextView test_result;
    private Button test_btn1;
    private Button test_btn2;
    private Button test_btn3;
    private Button test_btn4;
    private Button test_btn5;

    private ProgressDialog mProgressDialog;

    private final String[] urls = new String[5];

    private AsyncTask<String, Void, String> mTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
        initEvent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_test;
    }

    @Override
    protected void initView() {
        test_toolbar = findViewById(R.id.test_toolbar);
        test_result = findViewById(R.id.test_result);
        test_btn1 = findViewById(R.id.test_btn1);
        test_btn2 = findViewById(R.id.test_btn2);
        test_btn3 = findViewById(R.id.test_btn3);
        test_btn4 = findViewById(R.id.test_btn4);
        test_btn5 = findViewById(R.id.test_btn5);
    }

    @Override
    protected void initData() {
        urls[0] = "api2.xlink.cn";
        urls[1] = "cm2.xlink.cn";
        urls[2] = "mqtt.xlink.cn";
        urls[3] = "www.exo-terra.com";
        urls[4] = "www.google.com";

        mProgressDialog = new ProgressDialog(TestActivity.this);
        mProgressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void initEvent() {
        test_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        test_btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTask != null) {
                    return;
                }
                mProgressDialog.show();
                test_result.setText("");
                mTask = new TestNetworkTask() {
                    @Override
                    protected void onPostExecute(String s) {
                        super.onPostExecute(s);
                        test_result.setText(s);
                        mTask = null;
                        mProgressDialog.dismiss();
                    }
                };
                mTask.execute(urls[0]);
            }
        });

        test_btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTask != null) {
                    return;
                }
                mProgressDialog.show();
                test_result.setText("");
                mTask = new TestNetworkTask() {
                    @Override
                    protected void onPostExecute(String s) {
                        super.onPostExecute(s);
                        test_result.setText(s);
                        mTask = null;
                        mProgressDialog.dismiss();
                    }
                };
                mTask.execute(urls[1]);
            }
        });

        test_btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTask != null) {
                    return;
                }
                mProgressDialog.show();
                test_result.setText("");
                mTask = new TestNetworkTask() {
                    @Override
                    protected void onPostExecute(String s) {
                        super.onPostExecute(s);
                        test_result.setText(s);
                        mTask = null;
                        mProgressDialog.dismiss();
                    }
                };
                mTask.execute(urls[2]);
            }
        });

        test_btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTask != null) {
                    return;
                }
                mProgressDialog.show();
                test_result.setText("");
                mTask = new TestNetworkTask() {
                    @Override
                    protected void onPostExecute(String s) {
                        super.onPostExecute(s);
                        test_result.setText(s);
                        mTask = null;
                        mProgressDialog.dismiss();
                    }
                };
                mTask.execute(urls[3]);
            }
        });

        test_btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTask != null) {
                    return;
                }
                mProgressDialog.show();
                test_result.setText("");
                mTask = new TestNetworkTask() {
                    @Override
                    protected void onPostExecute(String s) {
                        super.onPostExecute(s);
                        test_result.setText(s);
                        mTask = null;
                        mProgressDialog.dismiss();
                    }
                };
                mTask.execute(urls[4]);
            }
        });
    }

    private class TestNetworkTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            if (strings == null && strings.length < 1) {
                return null;
            }
            String url = strings[0];
            Runtime runtime = Runtime.getRuntime();
            try {
                Process process = runtime.exec("ping -c 4 " + url);
                InputStream is = process.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                is.close();
                process.destroy();
                return sb.toString();
            }
            catch (IOException e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }
    }
}
