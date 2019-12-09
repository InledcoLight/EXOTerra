package com.inledco.exoterra.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.inledco.exoterra.R;

public class GradientSemiCircleButton extends android.support.v7.widget.AppCompatButton {
    private int mGradientStart;
    private int mGradientEnd;

    private Paint mPaint;

    public GradientSemiCircleButton(Context context) {
        this(context, null, 0);
    }

    public GradientSemiCircleButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GradientSemiCircleButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GradientSemiCircleView);
        mGradientStart = a.getColor(R.styleable.GradientSemiCircleView_colorStart, 0);
        mGradientEnd = a.getColor(R.styleable.GradientSemiCircleView_colorEnd, 0);
        a.recycle();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        RectF rectF1;
        RectF rectF2;
        RectF rectF3;
        LinearGradient lg;
        int radius;

        if (height <= width) {
            radius = height/2;
            rectF1 = new RectF(0, 0, height, height);                //左半圆
            rectF2 = new RectF(radius, 0, width-radius, height);    //中间矩形
            rectF3 = new RectF(width - height, 0, width, height);    //右半圆
            lg = new LinearGradient(0, 0, width, 0, mGradientStart, mGradientEnd, Shader.TileMode.CLAMP);
            mPaint.setShader(lg);
            canvas.drawArc(rectF1, 90, 180, true, mPaint);
            canvas.drawRect(rectF2, mPaint);
            canvas.drawArc(rectF3, 270, 180, true, mPaint);
        } else {
            radius = width/2;
            rectF1 = new RectF(0, 0, width, width);                     //上半圆
            rectF2 = new RectF(0, radius, width, height-radius);     //中间矩形
            rectF3 = new RectF(0, height-width, width, height);         //下半圆
            lg = new LinearGradient(0, 0, 0, height, mGradientStart, mGradientEnd, Shader.TileMode.CLAMP);
            mPaint.setShader(lg);
            canvas.drawArc(rectF1, 90, 180, true, mPaint);
            canvas.drawRect(rectF2, mPaint);
            canvas.drawArc(rectF3, 270, 180, true, mPaint);
        }

        super.onDraw(canvas);
    }
}
