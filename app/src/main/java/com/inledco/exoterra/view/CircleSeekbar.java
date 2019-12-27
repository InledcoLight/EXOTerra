package com.inledco.exoterra.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.inledco.exoterra.R;

public class CircleSeekbar extends FrameLayout {
    private final String TAG = "CircleSeekbar";

    private final double RADIAN = 180 / Math.PI;

    private int mMax;
    private int mMin;
    private int mProgress;
    private int mUnreachedColor;
    private int mProgressColor;
    private int mThumbColor;
    private float mUnreachedWidth;
    private float mProgressWidth;
    private float mThumbRadius;
    private boolean mSeekable;
    private boolean mScrollOnce;

    private Paint mWheelPaint;
    private Paint mProgressPaint;
    private Paint mThumbPaint;

    private float mCenterX;
    private float mCenterY;
    private double mAngle;
    private float mThumbX;
    private float mThumbY;

    private boolean mSeeking;

    private double mLastAngle;

    public CircleSeekbar(@NonNull Context context) {
        this(context, null, 0);
    }

    public CircleSeekbar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleSeekbar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleSeekbar, defStyleAttr, 0);
        mMax = a.getInt(R.styleable.CircleSeekbar_maxProgress, 100);
        mMin = a.getInt(R.styleable.CircleSeekbar_minProgress, 0);
        mProgress = a.getInt(R.styleable.CircleSeekbar_curProgress, 0);
        mUnreachedColor = a.getColor(R.styleable.CircleSeekbar_unreachedColor, 0);
        mProgressColor = a.getColor(R.styleable.CircleSeekbar_progressColor, 0);
        mThumbColor = a.getColor(R.styleable.CircleSeekbar_pointerColor, 0);
        mUnreachedWidth = a.getDimension(R.styleable.CircleSeekbar_unreachedWidth, 0);
        mProgressWidth = a.getDimension(R.styleable.CircleSeekbar_unreachedWidth, 0);
        mThumbRadius = a.getDimension(R.styleable.CircleSeekbar_unreachedWidth, 0);
        mSeekable = a.getBoolean(R.styleable.CircleSeekbar_seekable, false);
        mScrollOnce = a.getBoolean(R.styleable.CircleSeekbar_scrollOnce, true);

        a.recycle();

        setWillNotDraw(false);

        /**
         * 圆环画笔
         */
        mWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWheelPaint.setColor(mUnreachedColor);
        mWheelPaint.setStyle(Paint.Style.STROKE);
        mWheelPaint.setStrokeWidth(mUnreachedWidth);
        /**
         * 选中区域画笔
         */
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setColor(mProgressColor);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mProgressWidth);
        /**
         * 锚点画笔
         */
        mThumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mThumbPaint.setColor(mThumbColor);
        mThumbPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int min = Math.min(width-paddingLeft-paddingRight, height-paddingTop-paddingBottom);
        setMeasuredDimension(min+paddingLeft+paddingRight, min+paddingTop+paddingBottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float max = Math.max(mThumbRadius*2, Math.max(mUnreachedWidth, mProgressWidth));
        float left = getPaddingLeft() + max / 2;
        float top = getPaddingTop() + max / 2;
        float right = canvas.getWidth() - getPaddingRight() - max / 2;
        float bottom = canvas.getHeight() - getPaddingBottom() - max / 2;
        mCenterX = (left + right) / 2;
        mCenterY = (top + bottom) / 2;
        float radius = (right-left)/2;

        canvas.drawCircle(mCenterX, mCenterY, radius, mWheelPaint);

        mAngle = (double) (mProgress-mMin) / (mMax-mMin) * 2 * Math.PI;
        double cos = Math.cos(mAngle);
        double sin = Math.sin(mAngle);
        //画选中区域
        RectF rectF = new RectF(left, top, right, bottom);
        canvas.drawArc(rectF, -90, (float) (mAngle * RADIAN), false, mProgressPaint);

        //画锚点
        mThumbX = (float) (mCenterX + sin * radius);
        mThumbY = (float) (mCenterY - cos * radius);
        canvas.drawCircle(mThumbX, mThumbY, mThumbRadius, mThumbPaint);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mSeekable) {
            float x = ev.getX();
            float y = ev.getY();
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (isTouchDownPointer(x, y)) {
                        mSeeking = true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mSeeking = false;
                    break;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mSeeking) {
                    mLastAngle = mAngle;
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mSeeking) {
                    float dx = x - mCenterX;
                    float dy = y - mCenterY;
                    double radius = Math.sqrt(dx * dx + dy * dy);
                    double cos = dy / radius;
                    if (dx < 0) {
                        mAngle = Math.PI + Math.acos(cos);
                    } else {
                        mAngle = Math.PI - Math.acos(cos);
                    }
                    if (mScrollOnce) {
                        if (mLastAngle > 3*Math.PI/2 && mAngle < Math.PI/2) {
                            mAngle = 0;
                        } else if (mLastAngle < Math.PI/2 && mAngle > 3*Math.PI/2) {
                            mAngle = 0;
                        } else {
                            mLastAngle = mAngle;
                        }
                    } else {
                        mLastAngle = mAngle;
                    }
                    Log.e(TAG, "onTouchEvent: " + mScrollOnce + " " + mLastAngle + " " + mAngle);
                    mProgress = (int) (mMin + mAngle / (2 * Math.PI) * (mMax - mMin));
                    invalidate();
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                mSeeking = false;
                break;
        }
        return super.onTouchEvent(event);
    }

    private boolean isTouchDownPointer(float x, float y) {
        float max = Math.max(mThumbRadius*2, Math.max(mUnreachedWidth, mProgressWidth));
        float dx = mThumbX - x;
        float dy = mThumbY - y;
        if (dx*dx + dy*dy < max*max*2) {
            return true;
        }
        return false;
    }

    public int getMax() {
        return mMax;
    }

    public void setMax(int max) {
        mMax = max;
        invalidate();
    }

    public int getMin() {
        return mMin;
    }

    public void setMin(int min) {
        mMin = min;
        invalidate();
    }

    public int getProgress() {
        return mProgress;
    }

    public void setProgress(int progress) {
        mProgress = progress;
        invalidate();
    }

    public int getUnreachedColor() {
        return mUnreachedColor;
    }

    public void setUnreachedColor(int unreachedColor) {
        mUnreachedColor = unreachedColor;
        mWheelPaint.setColor(mUnreachedColor);
        invalidate();
    }

    public int getProgressColor() {
        return mProgressColor;
    }

    public void setProgressColor(int progressColor) {
        mProgressColor = progressColor;
        mProgressPaint.setColor(mProgressColor);
        invalidate();
    }

    public int getThumbColor() {
        return mThumbColor;
    }

    public void setThumbColor(int thumbColor) {
        mThumbColor = thumbColor;
        mThumbPaint.setColor(mThumbColor);
        invalidate();
    }
}
