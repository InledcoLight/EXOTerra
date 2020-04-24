package com.inledco.exoterra.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class HorizontalMatrixImageView extends AppCompatImageView {
    private int imageWidth;
    private int imageHeight;

    public HorizontalMatrixImageView(Context context) {
        super(context);
    }

    public HorizontalMatrixImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalMatrixImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setScaleType(ScaleType.FIT_XY);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        float scale = (float) width / imageWidth;
        int height = (int) (imageHeight * scale);
        setMeasuredDimension(getMeasuredWidth(), height + getPaddingTop() + getPaddingBottom());
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
