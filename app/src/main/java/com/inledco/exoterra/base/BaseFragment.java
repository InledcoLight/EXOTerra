package com.inledco.exoterra.base;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.inledco.exoterra.R;

public abstract class BaseFragment extends Fragment {

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

    protected void replaceFragment(@IdRes int layout, @NonNull final Fragment fragment) {
        getActivity().getSupportFragmentManager().beginTransaction()
                     .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                     .replace(layout, fragment, fragment.getClass().getSimpleName())
                     .commitAllowingStateLoss();
    }

    protected void addFragment(@IdRes int layout, @NonNull final Fragment fragment) {
        getActivity().getSupportFragmentManager()
                     .beginTransaction()
                     .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                     .add(layout, fragment, fragment.getClass().getSimpleName())
                     .commitAllowingStateLoss();
    }

    protected void addFragmentToStack(@IdRes int layout, @NonNull final Fragment fragment) {
        String name = fragment.getClass().getSimpleName();
        getActivity().getSupportFragmentManager().beginTransaction()
                     .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                     .add(layout, fragment, name)
                     .addToBackStack(name)
                     .commitAllowingStateLoss();
    }

    protected abstract @LayoutRes int getLayoutRes();

    protected abstract void initView(View view);

    protected abstract void initData();

    protected abstract void initEvent();
}
