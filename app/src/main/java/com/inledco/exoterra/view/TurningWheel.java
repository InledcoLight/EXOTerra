package com.inledco.exoterra.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.inledco.exoterra.R;

public class TurningWheel extends LinearLayout {
    private final String TAG = "TurningWheel";

    private final double RADIAN = 180 / Math.PI;

    private int mMax;
    private int mMin;
    private int mProgress;
    private int mUnreachedColor;
    private int mProgressColor;
    private float mUnreachedWidth;
    private float mProgressWidth;
    private boolean mSeekable;
    private boolean mScrollOnce;
    private Drawable mWheelDrawable;
    private Drawable mPointerDrawable;

    private Paint mWheelPaint;
    private Paint mProgressPaint;

    private float mCenterX;
    private float mCenterY;
    private float mPointerX;
    private float mPointerY;

    private boolean mSeeking;

    private float mLastX;
    private int mOverCount;

    private OnProgressChangedListener mListener;

    public TurningWheel(@NonNull Context context) {
        this(context, null, 0);
    }

    public TurningWheel(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TurningWheel(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TurningWheel, defStyleAttr, 0);
        mMax = a.getInt(R.styleable.TurningWheel_max, 100);
        mMin = a.getInt(R.styleable.TurningWheel_min, 0);
        mProgress = a.getInt(R.styleable.TurningWheel_progress, 0);
        mUnreachedColor = a.getColor(R.styleable.TurningWheel_unreachedColor, 0);
        mProgressColor = a.getColor(R.styleable.TurningWheel_progressColor, 0);
        mUnreachedWidth = a.getDimension(R.styleable.TurningWheel_unreachedWidth, 0);
        mProgressWidth = a.getDimension(R.styleable.TurningWheel_progressWidth, 0);
        mSeekable = a.getBoolean(R.styleable.TurningWheel_seekable, false);
        mScrollOnce = a.getBoolean(R.styleable.TurningWheel_scrollOnce, true);
        mWheelDrawable = a.getDrawable(R.styleable.TurningWheel_wheelDrawable);
        mPointerDrawable = a.getDrawable(R.styleable.TurningWheel_pointerDrawable);

        a.recycle();

        if (mWheelDrawable == null || mPointerDrawable == null) {
            throw new RuntimeException("Need wheelDrawable or thumbDrawable!");
        }

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
    }

    public void setListener(OnProgressChangedListener listener) {
        mListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int width = getMeasuredWidth();
//        int height = getMeasuredHeight();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
//        int w = width - paddingLeft - paddingRight;
//        int h = height - paddingTop - paddingBottom;
//        int min = Math.min(w, h);
//        setMeasuredDimension(min+paddingLeft+paddingRight, min+paddingTop+paddingBottom);

        int max = (int) Math.max(mUnreachedWidth, mProgressWidth);
        int min = Math.min(mWheelDrawable.getIntrinsicWidth(), mWheelDrawable.getIntrinsicHeight());
        setMeasuredDimension(min+max+paddingLeft+paddingRight, min+max+paddingTop+paddingBottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float max = Math.max(mUnreachedWidth, mProgressWidth);
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
        double angle = (double) (mProgress-mMin) / (mMax-mMin) * 2 * Math.PI;

        canvas.rotate((float) (angle*RADIAN), mCenterX, mCenterY);

        mWheelDrawable.setBounds((int) (left+max/2), (int)(top+max/2), (int)(right-max/2), (int)(bottom-max/2));
        final int count = canvas.save();
        mWheelDrawable.draw(canvas);
        canvas.restoreToCount(count);

        int tw = mPointerDrawable.getIntrinsicWidth();
        int th = mPointerDrawable.getIntrinsicHeight();
        mPointerDrawable.setBounds((int) (mCenterX - tw / 2), (int) (top + max / 2 + 16), (int) (mCenterX + tw / 2), (int) (top + max / 2 + 16 + th));
        final int cnt2 = canvas.save();
        mPointerDrawable.draw(canvas);
        canvas.restoreToCount(cnt2);

        canvas.rotate((float) (0-angle*RADIAN), mCenterX, mCenterY);
        canvas.drawCircle(mCenterX, mCenterY, radius, mWheelPaint);

        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        //画选中区域
        RectF rectF = new RectF(left, top, right, bottom);
        canvas.drawArc(rectF, -90, (float) (angle * RADIAN), false, mProgressPaint);

        float thumbRadius = radius - max/2 - 16 - th/2;
        mPointerX = (float) (mCenterX + sin * thumbRadius);
        mPointerY = (float) (mCenterX - cos * thumbRadius);
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
                    mLastX = mPointerX;
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
        int w = mPointerDrawable.getIntrinsicWidth();
        int h = mPointerDrawable.getIntrinsicHeight();
        float dx = mPointerX - x;
        float dy = mPointerY - y;
        if (dx*dx + dy*dy < (w*w + h*h)/4) {
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

    public interface OnProgressChangedListener {
        void onProgressChanged(int progress);

        void onSeekStop();
    }
}
