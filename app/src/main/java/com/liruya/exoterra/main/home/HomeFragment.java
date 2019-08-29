package com.liruya.exoterra.main.home;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.liruya.exoterra.R;
import com.liruya.exoterra.base.BaseFragment;

public class HomeFragment extends BaseFragment implements View.OnClickListener {

    private ImageView home_iv_guide;
    private ImageButton home_ib_strip;
    private ImageButton home_ib_socket;
    private ImageButton home_ib_monsoon;

    private ValueAnimator mAnimator;
    private ValueAnimator.AnimatorUpdateListener mUpdateListener;

    private boolean mLedOn;
    private boolean mSocketOn;
    private boolean mMonsoonOn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initData();
        initEvent();
        return view;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_home;
    }

    @Override
    protected void initView(View view) {
        home_iv_guide = view.findViewById(R.id.home_iv_guide);
        home_ib_strip = view.findViewById(R.id.home_ib_strip);
        home_ib_socket = view.findViewById(R.id.home_ib_socket);
        home_ib_monsoon = view.findViewById(R.id.home_ib_monsoon);
    }

    @Override
    protected void initData() {
        mAnimator = ObjectAnimator.ofInt(0, 120);
        mAnimator.setDuration(120);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                switch (value/40) {
                    case 1:
                        home_iv_guide.setImageResource(R.mipmap.ic_monsoon_1);
                        break;
                    case 2:
                        home_iv_guide.setImageResource(R.mipmap.ic_monsoon_2);
                        break;
                    case 0:
                    default:
                        home_iv_guide.setImageResource(R.mipmap.ic_monsoon_0);
                        break;
                }
            }
        };
    }

    @Override
    protected void initEvent() {
        home_ib_strip.setOnClickListener(this);
        home_ib_socket.setOnClickListener(this);
        home_ib_monsoon.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        mAnimator.cancel();
        mAnimator.removeAllUpdateListeners();
        switch (v.getId()) {
            case R.id.home_ib_strip:
                mLedOn = !mLedOn;
                mSocketOn = false;
                mMonsoonOn = false;
                home_iv_guide.setImageResource(mLedOn ? R.mipmap.ic_strip_on: R.mipmap.ic_guide_default);
                break;
            case R.id.home_ib_socket:
                mSocketOn = !mSocketOn;
                mMonsoonOn = false;
                mLedOn = false;
                home_iv_guide.setImageResource(mSocketOn ? R.mipmap.ic_socket_on: R.mipmap.ic_guide_default);
                break;
            case R.id.home_ib_monsoon:
                mMonsoonOn = !mMonsoonOn;
                mSocketOn = false;
                mLedOn = false;
                if (mMonsoonOn) {
                    mAnimator.addUpdateListener(mUpdateListener);
                    mAnimator.start();
                } else {
                    home_iv_guide.setImageResource(R.mipmap.ic_guide_default);
                }
                break;
        }
        home_ib_strip.setImageResource(mLedOn ? R.drawable.ic_strip : R.drawable.ic_strip_gray);
        home_ib_socket.setImageResource(mSocketOn ? R.drawable.ic_socket : R.drawable.ic_socket_gray);
        home_ib_monsoon.setImageResource(mMonsoonOn ? R.drawable.ic_monsoon : R.drawable.ic_monsoon_gray);
    }
}
