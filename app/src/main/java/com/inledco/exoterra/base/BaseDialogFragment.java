package com.inledco.exoterra.base;

import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.inledco.exoterra.R;

public abstract class BaseDialogFragment extends DialogFragment {
    protected final String TAG = this.getClass().getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutRes(), container, false);

        initView(view);
        return view;
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) {
            if (enter) {        //
                return AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right);
            } else {
                //                return AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right);
            }
        } else if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE) {
            if (enter) {
                //                return AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_right);
            } else {
                return AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_right);
            }
        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    protected final boolean isMainThread() {
        return Looper.getMainLooper().getThread().getId() == Thread.currentThread().getId();
    }

    protected final void showToast(final String msg) {
        if (isMainThread()) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT)
                 .show();
            Log.e(TAG, "showToast: " + msg);
        } else {
            if (getActivity() == null) {
                return;
            }
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), msg, Toast.LENGTH_LONG)
                     .show();
                Log.e(TAG, "showToast: " + msg);
            });
        }
    }

    protected final void showToast(@StringRes int res) {
        showToast(getString(res));
    }

    protected abstract @LayoutRes int getLayoutRes();

    protected abstract void initView(View view);

    protected abstract void initData();

    protected abstract void initEvent();
}
