package com.inledco.exoterra.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.inledco.exoterra.R;

/**
 * Created by liruya on 2018/4/23.
 */

public class MultiCircleProgress extends View {
    private final String TAG = "MultiCircleProgress";

    private final int CIRCLE_COUNT_DEFAULT = 1;
    private final int START_ANGLE_DEFAULT = 0;
    private final int END_ANGLE_DEFAULT = 360;
    private final float CIRCLE_WIDTH_DEFAULT = 2;
    private final float DIVIDER_WIDTH_DEFAULT = 0;
    private final int CENTERTEXT_SIZE_DEFAULT = 14;
    private final int CENTERTEXT_COLOR_DEFAULT = 0xFFFFFFFF;
    private final int BACKGROUND_CLOR_DEFAULT = 0xFF9E9E9E;
    private final int CIRCLE_COLOR_DEFAULT = 0xFFFFFFFF;
    private final int DIVIDER_COLOR_DEFAULT = 0xFF9E9E9E;
    private final int CIRCLE_COUNT_MAX = 6;

    private int startAngle;
    private int sweepAngle;
    private int progress;
    private int progressMax;
    private int circleColor;
    private int dividerColor;
    private int bgColor;
    private float circleWidth;
    private float dividerWidth;

    private int mCircleCount;
    private int[] mCircleColor;
    private int[] mCircleBackgroundColor;
    private int[] mProgress;
    private int[] mProgressMax;
    private float mCenterTextSize;
    private int mCenterTextColor;
    private String mCenterText;

    public MultiCircleProgress(Context context) {
        super(context);
    }

    public MultiCircleProgress(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiCircleProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultiCircleProgress);
        mCircleCount = a.getInt(R.styleable.MultiCircleProgress_circleCount, CIRCLE_COUNT_DEFAULT);
        mCircleCount = mCircleCount < 1 ? 1 : mCircleCount;
        mCircleCount = mCircleCount > 8 ? CIRCLE_COUNT_MAX : mCircleCount;
        startAngle = a.getInt(R.styleable.MultiCircleProgress_startAngle, START_ANGLE_DEFAULT);
        sweepAngle = a.getInt(R.styleable.MultiCircleProgress_sweepAngle, END_ANGLE_DEFAULT);
        progress = a.getInt(R.styleable.MultiCircleProgress_progress, 0);
        progressMax = a.getInt(R.styleable.MultiCircleProgress_max, 100);
        circleColor = a.getColor(R.styleable.MultiCircleProgress_circleColor, CIRCLE_COLOR_DEFAULT);
        dividerColor = a.getColor(R.styleable.MultiCircleProgress_dividerColor, DIVIDER_COLOR_DEFAULT);
        bgColor = a.getColor(R.styleable.MultiCircleProgress_circleBackgroundColor, BACKGROUND_CLOR_DEFAULT);
        circleWidth = a.getDimension(R.styleable.MultiCircleProgress_circleWidth, CIRCLE_WIDTH_DEFAULT);
        dividerWidth = a.getDimension(R.styleable.MultiCircleProgress_dividerWidth, DIVIDER_WIDTH_DEFAULT);
        mCenterText = a.getString(R.styleable.MultiCircleProgress_centerText);
        mCenterTextColor = a.getColor(R.styleable.MultiCircleProgress_centerTextColor, CENTERTEXT_COLOR_DEFAULT);
        mCenterTextSize = a.getDimension(R.styleable.MultiCircleProgress_centerTextSize, CENTERTEXT_SIZE_DEFAULT);
        setMinimumHeight(64);
        setMinimumWidth(64);
        init();
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height;
        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                width = widthSize;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
            default:
                width = (int) ((circleWidth + dividerWidth) * CIRCLE_COUNT_MAX * 4.5 + 16 + getPaddingLeft() + getPaddingRight());
                break;
        }
        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                height = heightSize;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
            default:
                height = (int) ((circleWidth + dividerWidth) * CIRCLE_COUNT_MAX * 4.5 + 16 + getPaddingTop() + getPaddingBottom());
                break;
        }
        if (width < height) {
            height = width;
        } else if (width > height) {
            width = height;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int bottom = getPaddingBottom();
        int rx = (getMeasuredWidth() - left - right) / 2;
        int ry = (getMeasuredHeight() - top - bottom) / 2;
        int radius = rx < ry ? rx : ry;
        int cx = left + rx;
        int cy = top + ry;
        Paint paint = new Paint();

        paint.setTextSize(mCenterTextSize);
        paint.setColor(mCenterTextColor);
        Rect bound = new Rect();
        paint.getTextBounds(mCenterText, 0, mCenterText.length(), bound);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(mCenterText, cx, cy + bound.height() / 4, paint);

        for (int i = 0; i < mCircleCount; i++) {
            if (circleWidth > 0) {
                radius = radius - (int) circleWidth / 2;
                paint.setColor(mCircleBackgroundColor[i]);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(circleWidth);
                RectF rect = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
                //                canvas.drawCircle( cx, cy, radius, paint );
                canvas.drawArc(rect, startAngle, sweepAngle, false, paint);
                paint.setColor(mCircleColor[i]);
                canvas.drawArc(rect, startAngle, sweepAngle * mProgress[i] / mProgressMax[i], false, paint);
                radius = radius - (int) circleWidth / 2;
            }
            if (dividerWidth > 0) {
                radius = radius - (int) dividerWidth / 2;
                paint.setColor(dividerColor);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(dividerWidth);
                canvas.drawCircle(cx, cy, radius, paint);
            }
        }
    }

    private void init() {
        mCircleColor = new int[mCircleCount];
        mCircleBackgroundColor = new int[mCircleCount];
        mProgress = new int[mCircleCount];
        mProgressMax = new int[mCircleCount];
        progressMax = progressMax < 0 ? 100 : progressMax;
        progress = progress < 0 ? 0 : progress;
        progress = progress > progressMax ? progressMax : progress;
        circleWidth = circleWidth < 2 ? 2 : circleWidth;
        dividerWidth = dividerWidth < 0 ? 0 : dividerWidth;
        for (int i = 0; i < mCircleCount; i++) {
            mProgress[i] = progress;
            mProgressMax[i] = progressMax;
            mCircleColor[i] = circleColor;
            mCircleBackgroundColor[i] = bgColor;
        }
    }

    public void setCircleCount(int count) {
        if (count >= 1 && count <= 8) {
            mCircleCount = count;
            init();
        }
    }

    public int getCircleCount() {
        return mCircleCount;
    }

    public void setCircleColor(int[] colors) {
        if (colors.length == mCircleCount) {
            for (int i = 0; i < mCircleCount; i++) {
                mCircleColor[i] = colors[i];
            }
        }
    }

    public void setCircleColor(int idx, int color) {
        if (idx >= 0 && idx < mCircleCount) {
            mCircleColor[idx] = color;
        }
    }

    public int[] getCircleColor() {
        return mCircleColor;
    }

    public void setCircleBackgroundColor(int[] colors) {
        if (colors.length == mCircleCount) {
            for (int i = 0; i < mCircleCount; i++) {
                mCircleBackgroundColor[i] = colors[i];
            }
        }
    }

    public void setCircleBackgroundColor(int idx, int color) {
        if (idx >= 0 && idx < mCircleCount) {
            mCircleBackgroundColor[idx] = color;
        }
    }

    public int[] getCircleBackgroundColor() {
        return mCircleBackgroundColor;
    }

    public void setDividerColor(int color) {
        dividerColor = color;
    }

    public int getDividerColor() {
        return dividerColor;
    }

    public void setProgress(int[] progress) {
        if (progress.length == mCircleCount) {
            for (int i = 0; i < mCircleCount; i++) {
                mProgress[i] = progress[i];
            }
        }
    }

    public void setProgress(int idx, int progress) {
        if (idx >= 0 && idx < mCircleCount && progress >= 0 && progress <= mProgressMax[idx]) {
            mProgress[idx] = progress;
        }
    }

    public int[] getProgress() {
        return mProgress;
    }

    public void setProgressMax(int[] progressMax) {
        if (progressMax.length == mCircleCount) {
            for (int i = 0; i < mCircleCount; i++) {
                mProgressMax[i] = progressMax[i];
            }
        }
    }

    public void setProgressMax(int idx, int progressMax) {
        if (idx >= 0 && idx < mCircleCount) {
            mProgressMax[idx] = progressMax;
        }
    }

    public int[] getProgressMax() {
        return mProgressMax;
    }

    public void setStartAngle(int start) {
        startAngle = start;
    }

    public int getStartAngle() {
        return startAngle;
    }

    public void setSweepAngle(int sweep) {
        sweepAngle = sweep;
    }

    public int getSweepAngle() {
        return sweepAngle;
    }

    public void setCenterText(String centerText) {
        mCenterText = centerText;
    }

    public String getCenterText() {
        return mCenterText;
    }
}
