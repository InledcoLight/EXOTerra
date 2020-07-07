package com.inledco.exoterra.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class VerticalMatrixImageView extends AppCompatImageView {
    private int imageWidth;
    private int imageHeight;

    public VerticalMatrixImageView(Context context) {
        super(context);
    }

    public VerticalMatrixImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalMatrixImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setScaleType(ScaleType.FIT_XY);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        float scale = (float) height / imageHeight;
        int width = (int) (imageWidth * scale);
        setMeasuredDimension(width + getPaddingLeft() + getPaddingRight(), getMeasuredHeight());
    }

    @Override
    public void setImageResource(int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), resId, options);
        imageWidth = options.outWidth;
        imageHeight = options.outHeight;

        super.setImageResource(resId);
    }
}
