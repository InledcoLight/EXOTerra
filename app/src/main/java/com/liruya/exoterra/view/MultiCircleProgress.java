package com.liruya.exoterra.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.liruya.exoterra.R;

/**
 * Created by liruya on 2018/4/23.
 */

public class MultiCircleProgress extends View
{
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
    private int[] mDividerColor;
    private int[] mProgress;
    private int[] mProgressMax;
    private int[] mStartAngle;
    private int[] mSweepAngle;
    private float[] mCircleWidth;
    private float[] mDividerWidth;
    private float mCenterTextSize;
    private int mCenterTextColor;
    private String mCenterText;

    public MultiCircleProgress(Context context)
    {
        super( context );
    }

    public MultiCircleProgress(Context context, @Nullable AttributeSet attrs)
    {
        this( context, attrs, 0 );
    }

    public MultiCircleProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super( context, attrs, defStyleAttr );
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultiCircleProgress);
        mCircleCount = a.getInt( R.styleable.MultiCircleProgress_circleCount, CIRCLE_COUNT_DEFAULT );
        mCircleCount = mCircleCount < 1 ? 1 : mCircleCount;
        mCircleCount = mCircleCount > 8 ? 8 : mCircleCount;
        startAngle = a.getInt( R.styleable.MultiCircleProgress_startAngle, START_ANGLE_DEFAULT );
        sweepAngle = a.getInt( R.styleable.MultiCircleProgress_sweepAngle, END_ANGLE_DEFAULT );
        progress = a.getInt( R.styleable.MultiCircleProgress_progress, 0 );
        progressMax = a.getInt( R.styleable.MultiCircleProgress_max, 100 );
        circleColor = a.getColor( R.styleable.MultiCircleProgress_circleColor, CIRCLE_COLOR_DEFAULT );
        dividerColor = a.getColor( R.styleable.MultiCircleProgress_dividerColor, DIVIDER_COLOR_DEFAULT );
        bgColor = a.getColor( R.styleable.MultiCircleProgress_circleBackgroundColor, BACKGROUND_CLOR_DEFAULT );
        circleWidth = a.getDimension( R.styleable.MultiCircleProgress_circleWidth, CIRCLE_WIDTH_DEFAULT );
        dividerWidth = a.getDimension( R.styleable.MultiCircleProgress_dividerWidth, DIVIDER_WIDTH_DEFAULT );
        mCenterText = a.getString( R.styleable.MultiCircleProgress_centerText );
        mCenterTextColor = a.getColor( R.styleable.MultiCircleProgress_centerTextColor, CENTERTEXT_COLOR_DEFAULT );
        mCenterTextSize = a.getDimension( R.styleable.MultiCircleProgress_centerTextSize, CENTERTEXT_SIZE_DEFAULT );
        init();
        a.recycle();
    }

    @Override
    protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec )
    {
        super.onMeasure( widthMeasureSpec, heightMeasureSpec );
        int widthMode = MeasureSpec.getMode( widthMeasureSpec );
        int widthSize = MeasureSpec.getSize( widthMeasureSpec );
        int heightMode = MeasureSpec.getMode( heightMeasureSpec );
        int heightSize = MeasureSpec.getSize( heightMeasureSpec );
        int width = widthSize + getPaddingLeft() + getPaddingRight();
        int height = heightSize + getPaddingTop() + getPaddingBottom();
        if ( widthMode == MeasureSpec.EXACTLY )
        {
            width = widthSize;
        }
        if ( heightMode == MeasureSpec.EXACTLY )
        {
            height = heightSize;
        }
//        if (width < getMinimumWidth()) {
//            width = getMinimumWidth();
//        }
//        if (height < getMinimumHeight()) {
//            height = getMinimumHeight();
//        }
        setMeasuredDimension( width, height );
    }

    @Override
    protected void onDraw( Canvas canvas )
    {
        super.onDraw( canvas );
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int bottom = getPaddingBottom();
        int rx = ( getMeasuredWidth() - left - right ) / 2;
        int ry = ( getMeasuredHeight() - top - bottom ) / 2;
        int radius = rx < ry ? rx : ry;
        int cx = left + rx;
        int cy = top + ry;
        Paint paint = new Paint();

        paint.setTextSize( mCenterTextSize );
        paint.setColor( mCenterTextColor );
        Rect bound = new Rect();
        paint.getTextBounds( mCenterText, 0, mCenterText.length(), bound );
        paint.setTextAlign( Paint.Align.CENTER );
        canvas.drawText( mCenterText, cx, cy + bound.height()/4, paint );

        for ( int i = 0; i < mCircleCount; i++ )
        {
            if ( mCircleWidth[i] > 0 )
            {
                radius = radius - (int) mCircleWidth[i] / 2;
                paint.setColor( mCircleBackgroundColor[i] );
                paint.setStyle( Paint.Style.STROKE );
                paint.setStrokeWidth( mCircleWidth[i] );
                RectF rect = new RectF( cx - radius, cy - radius, cx + radius, cy + radius );
//                canvas.drawCircle( cx, cy, radius, paint );
                canvas.drawArc( rect, mStartAngle[i], mSweepAngle[i], false, paint );
                paint.setColor( mCircleColor[i] );
                canvas.drawArc( rect, mStartAngle[i], mSweepAngle[i] * mProgress[i] / mProgressMax[i], false, paint );
                radius = radius - (int) mCircleWidth[i] / 2;
            }
            if ( mDividerWidth[i] > 0 )
            {
                radius = radius - (int) mDividerWidth[i] / 2;
                paint.setColor( mDividerColor[i] );
                paint.setStyle( Paint.Style.STROKE );
                paint.setStrokeWidth( mDividerWidth[i] );
                canvas.drawCircle( cx, cy, radius, paint );
            }
        }
    }

    private void init()
    {
        mCircleColor = new int[mCircleCount];
        mDividerColor = new int[mCircleCount];
        mCircleBackgroundColor = new int[mCircleCount];
        mProgress = new int[mCircleCount];
        mProgressMax = new int[mCircleCount];
        mStartAngle = new int[mCircleCount];
        mSweepAngle = new int[mCircleCount];
        mCircleWidth = new float[mCircleCount];
        mDividerWidth = new float[mCircleCount];
        progressMax = progressMax < 0 ? 100 : progressMax;
        progress = progress < 0 ? 0 : progress;
        progress = progress > progressMax ? progressMax : progress;
        circleWidth = circleWidth < 2 ? 2 : circleWidth;
        dividerWidth = dividerWidth < 0 ? 0 : dividerWidth;
        for ( int i = 0; i < mCircleCount; i++ )
        {
            mStartAngle[i] = startAngle;
            mSweepAngle[i] = sweepAngle;
            mProgress[i] = progress;
            mProgressMax[i] = progressMax;
            mCircleColor[i] = circleColor;
            mDividerColor[i] = dividerColor;
            mCircleBackgroundColor[i] = bgColor;
            mCircleWidth[i] = circleWidth;
            mDividerWidth[i] = dividerWidth;
        }

    }

    public void setCircleCount( int count )
    {
        if ( count >= 1 && count <= 8 )
        {
            mCircleCount = count;
            init();
        }
    }

    public int getCircleCount()
    {
        return mCircleCount;
    }

    public void setCircleColor( int[] colors )
    {
        if ( colors.length == mCircleCount )
        {
            for ( int i = 0; i < mCircleCount; i++ )
            {
                mCircleColor[i] = colors[i];
            }
        }
    }

    public void setCircleColor( int idx, int color )
    {
        if ( idx >= 0 && idx < mCircleCount )
        {
            mCircleColor[idx] = color;
        }
    }

    public int[] getCircleColor()
    {
        return mCircleColor;
    }

    public void setCircleBackgroundColor( int[] colors )
    {
        if ( colors.length == mCircleCount )
        {
            for ( int i = 0; i < mCircleCount; i++ )
            {
                mCircleBackgroundColor[i] = colors[i];
            }
        }
    }

    public void setCircleBackgroundColor( int idx, int color )
    {
        if ( idx >= 0 && idx < mCircleCount )
        {
            mCircleBackgroundColor[idx] = color;
        }
    }

    public int[] getCircleBackgroundColor()
    {
        return mCircleBackgroundColor;
    }

    public void setDividerColor( int[] colors )
    {
        if ( colors.length == mCircleCount )
        {
            for ( int i = 0; i < mCircleCount; i++ )
            {
                mDividerColor[i] = colors[i];
            }
        }
    }

    public void setDividerColor( int idx, int color )
    {
        if ( idx >= 0 && idx < mCircleCount )
        {
            mDividerColor[idx] = color;
        }
    }

    public int[] getDividerColor()
    {
        return mDividerColor;
    }

    public void setProgress( int[] progress )
    {
        if ( progress.length == mCircleCount )
        {
            for ( int i = 0; i < mCircleCount; i++ )
            {
                mProgress[i] = progress[i];
            }
        }
    }

    public void setProgress( int idx, int progress )
    {
        if ( idx >= 0 && idx < mCircleCount && progress >= 0 && progress <= mProgressMax[idx] )
        {
            mProgress[idx] = progress;
        }
    }

    public int[] getProgress()
    {
        return mProgress;
    }

    public void setProgressMax( int[] progressMax )
    {
        if ( progressMax.length == mCircleCount )
        {
            for ( int i = 0; i < mCircleCount; i++ )
            {
                mProgressMax[i] = progressMax[i];
            }
        }
    }

    public void setProgressMax( int idx, int progressMax )
    {
        if ( idx >= 0 && idx < mCircleCount )
        {
            mProgressMax[idx] = progressMax;
        }
    }

    public int[] getProgressMax()
    {
        return mProgressMax;
    }

    public void setStartAngle( int[] startAngle )
    {
        if ( startAngle.length == mCircleCount )
        {
            for ( int i = 0; i < mCircleCount; i++ )
            {
                mStartAngle[i] = startAngle[i];
            }
        }
    }

    public void setStartAngle( int idx, int startAngle )
    {
        if ( idx >= 0 && idx < mCircleCount )
        {
            mStartAngle[idx] = startAngle;
        }
    }

    public int[] getStartAngle()
    {
        return mStartAngle;
    }

    public void setSweepAngle( int[] sweepAngle )
    {
        if ( sweepAngle.length == mCircleCount )
        {
            for ( int i = 0; i < mCircleCount; i++ )
            {
                mSweepAngle[i] = sweepAngle[i];
            }
        }
    }

    public void setEndAngle( int idx, int endAngle )
    {
        if ( idx >= 0 && idx < mCircleCount )
        {
            mStartAngle[idx] = endAngle;
        }
    }

    public int[] getSweepAngle()
    {
        return mSweepAngle;
    }

    public void setCenterText( String centerText )
    {
        mCenterText = centerText;
    }

    public String getCenterText()
    {
        return mCenterText;
    }
}
