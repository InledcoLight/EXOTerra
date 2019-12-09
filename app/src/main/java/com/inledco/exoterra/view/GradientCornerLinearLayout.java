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
import android.widget.LinearLayout;

import com.inledco.exoterra.R;

public class GradientCornerLinearLayout extends LinearLayout {
    private final int HORIZONTAL = 0;
    private final int VERTICAL = 1;

    private float mStrokeWidth;
    private float mRadiusTopLeft;
    private float mRadiusTopRight;
    private float mRadiusBottomRight;
    private float mRadiusBottomLeft;
    private boolean mSemiCircle;
    private int mGradientStart;
    private int mGradientEnd;
    private int mDirection;

    private Paint mPaint;

    public GradientCornerLinearLayout(Context context) {
        this(context, null, 0);
    }

    public GradientCornerLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GradientCornerLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GradientCornerView);
        mStrokeWidth = a.getDimension(R.styleable.GradientCornerView_strokeWidth, 0);
        if (a.hasValue(R.styleable.GradientCornerView_radius)) {
            float radius = a.getDimension(R.styleable.GradientCornerView_radius, 0);
            mRadiusTopLeft = radius;
            mRadiusTopRight = radius;
            mRadiusBottomRight = radius;
            mRadiusBottomLeft = radius;
        }
        mRadiusTopLeft = a.getDimension(R.styleable.GradientCornerView_radiusTopLeft, mRadiusTopLeft);
        mRadiusTopRight = a.getDimension(R.styleable.GradientCornerView_radiusTopRight, mRadiusTopRight);
        mRadiusBottomRight = a.getDimension(R.styleable.GradientCornerView_radiusBottomRight, mRadiusBottomRight);
        mRadiusBottomLeft = a.getDimension(R.styleable.GradientCornerView_radiusBottomLeft, mRadiusBottomLeft);
        mSemiCircle = a.getBoolean(R.styleable.GradientCornerView_semiCircle, false);
        mGradientStart = a.getColor(R.styleable.GradientCornerView_gradientStart, 0);
        mGradientEnd = a.getColor(R.styleable.GradientCornerView_gradientEnd, 0);
        mDirection = a.getInt(R.styleable.GradientCornerView_direction, HORIZONTAL);
        a.recycle();

        setWillNotDraw(false);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float left = mStrokeWidth/2;
        float right = getWidth()-mStrokeWidth/2;
        float top = mStrokeWidth/2;
        float bottom = getHeight()-mStrokeWidth/2;
        if (mSemiCircle) {
            float width = right - left;
            float height = bottom - top;
            float radius;
            if (width <= height) {
                radius = width/2;
            } else {
                radius = height/2;
            }
            mRadiusTopLeft = radius;
            mRadiusTopRight = radius;
            mRadiusBottomRight = radius;
            mRadiusBottomLeft = radius;
        }

        //TopLeft corner
        RectF rectF1 = new RectF(left, top, left+2*mRadiusTopLeft, top+2*mRadiusTopLeft);
        //TopRight corner
        RectF rectF2 = new RectF(right-2*mRadiusTopRight, top, right, top+2*mRadiusTopRight);
        //BottomRight corner
        RectF rectF3 = new RectF(right-2*mRadiusBottomRight, bottom-2*mRadiusBottomRight, right, bottom);
        //BottomLeft corner
        RectF rectF4 = new RectF(left, bottom-2*mRadiusBottomLeft, left+2*mRadiusBottomLeft, bottom);

        //渐变效果
        LinearGradient lg;
        if (mDirection == VERTICAL) {
            lg = new LinearGradient(0, 0, 0, getHeight(), mGradientStart, mGradientEnd, Shader.TileMode.CLAMP);
        } else {
            lg = new LinearGradient(0, 0, getWidth(), 0, mGradientStart, mGradientEnd, Shader.TileMode.CLAMP);
        }
        mPaint.setShader(lg);

        canvas.drawArc(rectF1, 180, 90, false, mPaint);
        canvas.drawArc(rectF2, 270, 90, false, mPaint);
        canvas.drawArc(rectF3, 0, 90, false, mPaint);
        canvas.drawArc(rectF4, 90, 90, false, mPaint);
        canvas.drawLine(left+mRadiusTopLeft, top, right-mRadiusTopRight, top, mPaint);
        canvas.drawLine(right, top+mRadiusTopRight, right, bottom-mRadiusBottomRight, mPaint);
        canvas.drawLine(left+mRadiusBottomLeft, bottom, right-mRadiusBottomRight, bottom, mPaint);
        canvas.drawLine(left, top+mRadiusTopLeft, left, bottom-mRadiusBottomLeft, mPaint);
    }
}
