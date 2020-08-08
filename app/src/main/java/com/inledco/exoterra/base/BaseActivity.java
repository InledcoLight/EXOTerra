package com.inledco.exoterra.base;

import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.widget.Toast;

import com.inledco.exoterra.R;
import com.inledco.exoterra.view.MessageDialog;
import com.liruya.loaddialog.LoadDialog;

public abstract class BaseActivity extends AppCompatActivity {

    protected final String TAG = this.getClass().getSimpleName();

    private LoadDialog mLoadDialog;
    private MessageDialog mDialog;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRes());

        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissLoadDialog();
    }

    @Override
    public void overridePendingTransition(int enterAnim, int exitAnim) {
        super.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    protected void replaceFragment(@IdRes int layout, @NonNull final Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                                   .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                   .replace(layout, fragment, fragment.getClass().getSimpleName())
                                   .commitAllowingStateLoss();
    }

    protected void addFragment(@IdRes int layout, @NonNull final Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                                   .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                   .add(layout, fragment, fragment.getClass().getSimpleName())
                                   .commitAllowingStateLoss();
    }

    protected void addFragmentToStack(@IdRes int layout, @NonNull final Fragment fragment) {
        String name = fragment.getClass().getSimpleName();
        getSupportFragmentManager().beginTransaction()
                                   .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                   .add(layout, fragment, name)
                                   .addToBackStack(name)
                                   .commitAllowingStateLoss();
    }

    protected MessageDialog getMessageDialog() {
        if (mDialog == null) {
            mDialog = new MessageDialog(this, true);
        }
        return mDialog;
    }

    //    protected void showMessageDialog(final String title, final String msg) {
//        if (mDialog == null) {
//            mDialog = new MessageDialog(this);
//        }
//        mDialog.setTitle(title)
//               .setMessage(msg)
//               .show();
//    }
//
//    protected void dismissMessageDialog() {
//        if (mDialog != null) {
//            mDialog.dismiss();
//        }
//    }

    protected void showToast(final String msg) {
        boolean isMainThread = Looper.getMainLooper().getThread().getId() == Thread.currentThread().getId();
        if (isMainThread) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT)
                 .show();
        } else {runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(BaseActivity.this, msg, Toast.LENGTH_LONG)
                         .show();
                }
            });
        }
    }

    protected void showToast(@StringRes int res) {
        showToast(getString(res));
    }

    protected void showLoadDialog() {

        boolean isMainThread = Looper.getMainLooper().getThread().getId() == Thread.currentThread().getId();
        if (isMainThread) {
            if (mLoadDialog == null) {
                mLoadDialog = new LoadDialog(this);
                mLoadDialog.setCancelable(true);
                mLoadDialog.setCanceledOnTouchOutside(false);
            }
            mLoadDialog.show();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mLoadDialog == null) {
                        mLoadDialog = new LoadDialog(BaseActivity.this);
                        mLoadDialog.setCancelable(true);
                        mLoadDialog.setCanceledOnTouchOutside(false);
                    }
                    mLoadDialog.show();
                }
            });
        }
    }

    protected void dismissLoadDialog() {
        if (mLoadDialog != null) {
            boolean isMainThread = Looper.getMainLooper().getThread().getId() == Thread.currentThread().getId();
            if (isMainThread) {
                mLoadDialog.dismiss();
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLoadDialog.dismiss();
                    }
                });
            }
        }
    }

    protected abstract @LayoutRes int getLayoutRes();

    protected abstract void initView();

    protected abstract void initData();

    protected abstract void initEvent();
}
