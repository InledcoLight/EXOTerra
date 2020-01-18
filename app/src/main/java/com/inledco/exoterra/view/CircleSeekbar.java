package com.inledco.exoterra.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.inledco.exoterra.R;

public class CircleSeekbar extends LinearLayout {
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
    private float mThumbX;
    private float mThumbY;

    private boolean mSeeking;

    private float mLastX;
    private int mOverCount;

    private OnProgressChangedListener mListener;

    public CircleSeekbar(@NonNull Context context) {
        this(context, null, 0);
    }

    public CircleSeekbar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleSeekbar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleSeekbar, defStyleAttr, 0);
        mMax = a.getInt(R.styleable.CircleSeekbar_max, 100);
        mMin = a.getInt(R.styleable.CircleSeekbar_min, 0);
        mProgress = a.getInt(R.styleable.CircleSeekbar_progress, 0);
        mUnreachedColor = a.getColor(R.styleable.CircleSeekbar_unreachedColor, 0);
        mProgressColor = a.getColor(R.styleable.CircleSeekbar_progressColor, 0);
        mThumbColor = a.getColor(R.styleable.CircleSeekbar_pointerColor, 0);
        mUnreachedWidth = a.getDimension(R.styleable.CircleSeekbar_unreachedWidth, 0);
        mProgressWidth = a.getDimension(R.styleable.CircleSeekbar_progressWidth, 0);
        mThumbRadius = a.getDimension(R.styleable.CircleSeekbar_pointerRadius, 0);
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

    public void setListener(OnProgressChangedListener listener) {
        mListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int min = Math.min(width-paddingLeft-paddingRight, height-paddingTop-paddingBottom);
        if (widthMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.AT_MOST) {
            min = Math.max(width-paddingLeft-paddingRight, height-paddingTop-paddingBottom);
        }
        setMeasuredDimension(min+paddingLeft+paddingRight, min+paddingTop+paddingBottom);
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
//        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
//        int paddingLeft = getPaddingLeft();
//        int paddingRight = getPaddingRight();
//        int paddingTop = getPaddingTop();
//        int paddingBottom = getPaddingBottom();
//        int min = Math.min(width-paddingLeft-paddingRight, height-paddingTop-paddingBottom);
//        setMeasuredDimension(min+paddingLeft+paddingRight, min+paddingTop+paddingBottom);
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float max = Math.max(mThumbRadius*2, Math.max(mUnreachedWidth, mProgressWidth));
        int w = canvas.getWidth()-getPaddingLeft()-getPaddingRight();
        int h = canvas.getHeight()-getPaddingTop()-getPaddingBottom();
        int min = Math.min(w, h);
        float radius = (min-max)/2;
        mCenterX = getPaddingLeft() + w/2;
        mCenterY = getPaddingTop() + h/2;

        float left = mCenterX - radius;
        float top = mCenterY - radius;
        float right = mCenterX + radius;
        float bottom = mCenterY + radius;

        canvas.drawCircle(mCenterX, mCenterY, radius, mWheelPaint);

        double angle = (double) (mProgress-mMin) / (mMax-mMin) * 2 * Math.PI;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        //画选中区域
        RectF rectF = new RectF(left, top, right, bottom);
        canvas.drawArc(rectF, -90, (float) (angle * RADIAN), false, mProgressPaint);

        //画锚点
        mThumbX = (float) (mCenterX + sin * radius);
        mThumbY = (float) (mCenterY - cos * radius);
        canvas.drawCircle(mThumbX, mThumbY, mThumbRadius, mThumbPaint);
    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        float max = Math.max(mThumbRadius*2, Math.max(mUnreachedWidth, mProgressWidth));
//        float left = getPaddingLeft() + max / 2;
//        float top = getPaddingTop() + max / 2;
//        float right = canvas.getWidth() - getPaddingRight() - max / 2;
//        float bottom = canvas.getHeight() - getPaddingBottom() - max / 2;
//        mCenterX = (left + right) / 2;
//        mCenterY = (top + bottom) / 2;
//        float radius = (right-left)/2;
//
//        canvas.drawCircle(mCenterX, mCenterY, radius, mWheelPaint);
//
//        double angle = (double) (mProgress-mMin) / (mMax-mMin) * 2 * Math.PI;
//        double cos = Math.cos(angle);
//        double sin = Math.sin(angle);
//        //画选中区域
//        RectF rectF = new RectF(left, top, right, bottom);
//        canvas.drawArc(rectF, -90, (float) (angle * RADIAN), false, mProgressPaint);
//
//        //画锚点
//        mThumbX = (float) (mCenterX + sin * radius);
//        mThumbY = (float) (mCenterY - cos * radius);
//        canvas.drawCircle(mThumbX, mThumbY, mThumbRadius, mThumbPaint);
//    }

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
                    mLastX = mThumbX;
                    if (mProgress == mMin) {
                        mOverCount = -1;
                    }  else if (mProgress == mMax) {
                        mOverCount = 1;
                    } else {
                        mOverCount = 0;
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mSeeking) {
                    //0度溢出检测
                    if (mScrollOnce && y <= mCenterY) {
                        if (mLastX < mCenterX && x >= mCenterX) {
                            mOverCount ++;
                            if (mOverCount >= 1) {
                                mOverCount = 1;
                                mProgress = mMax;
                            }
                        } else if (mLastX > mCenterX && x <= mCenterX) {
                            mOverCount--;
                            if (mOverCount <= -1) {
                                mOverCount = -1;
                                mProgress = mMin;
                            }
                        }
                    }
                    if (mOverCount == 0) {
                        float dx = x - mCenterX;
                        float dy = y - mCenterY;
                        double radius = Math.sqrt(dx * dx + dy * dy);
                        double cos = dy / radius;
                        double angle;
                        if (dx < 0) {
                            angle = Math.PI + Math.acos(cos);
                        } else {
                            angle = Math.PI - Math.acos(cos);
                        }
                        mProgress = (int) (mMin + angle / (2 * Math.PI) * (mMax - mMin));
                    }
                    mLastX = x;
                    invalidate();
                    if (mListener != null) {
                        mListener.onProgressChanged(mProgress);
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                mSeeking = false;
                if (mListener != null) {
                    mListener.onSeekStop();
                }
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

    public interface OnProgressChangedListener {
        void onProgressChanged(int progress);

        void onSeekStop();
    }
}
