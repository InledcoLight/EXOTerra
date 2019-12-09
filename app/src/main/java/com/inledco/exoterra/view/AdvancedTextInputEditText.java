package com.inledco.exoterra.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class AdvancedTextInputEditText extends TextInputEditText {
    private final String TAG = "AdvancedTextInputEditTe";

    private boolean mBind;
    private TextInputLayout mParentTextInputLayout;

    private DrawableRightClickListener mDrawableRightClickListener;

    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mParentTextInputLayout != null) {
                mParentTextInputLayout.setError(null);
            }
        }
    };
    public AdvancedTextInputEditText(Context context) {
        super(context);
        addTextChangedListener(mTextWatcher);
    }

    public AdvancedTextInputEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        addTextChangedListener(mTextWatcher);
    }

    public AdvancedTextInputEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addTextChangedListener(mTextWatcher);
    }

    public void bindTextInputLayout(@NonNull final TextInputLayout textInputLayout) {
        if (!mBind) {
            mParentTextInputLayout = textInputLayout;
            mBind = true;
        }
    }

    public void setDrawableRightClickListener(DrawableRightClickListener listener) {
        mDrawableRightClickListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //Left Top Right Bottom 0-3
                Drawable drawableLeft = getCompoundDrawables()[0];
                Drawable drawableRight = getCompoundDrawables()[2];
                if (drawableLeft != null && event.getX() <= getPaddingLeft() + drawableLeft.getBounds().width()) {
                    return false;
                }
                if (drawableRight != null && event.getX() >= getRight() - getPaddingRight() - drawableRight.getBounds().width()) {
                    if (mDrawableRightClickListener != null) {
                        mDrawableRightClickListener.onDrawableRightClick();
                    }
                    return false;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public interface DrawableRightClickListener {
        void onDrawableRightClick();
    }
}
