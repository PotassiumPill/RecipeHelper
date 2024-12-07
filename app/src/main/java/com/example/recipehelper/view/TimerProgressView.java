package com.example.recipehelper.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.example.recipehelper.R;
import com.example.recipehelper.Utils;

public class TimerProgressView extends View {

    public interface OnTimerCloseListener {
        void onTimerClose(int time);
    }
    private OnTimerCloseListener mOnTimerCloseListener;
    private int mColorRegular;
    private int mColorClose;

    private int mColorBackground;
    private int mColorText;

    private int mTotal;
    private int mClose;
    private int mCurrent;

    private final StringBuffer mTempLabel = new StringBuffer(8);
    private Paint mTextPaint;
    private Paint mProgressPaint;
    private int mWidth;
    private int mHeight;
    private RectF mProgressRect;
    private boolean nearEnd;


    public TimerProgressView(Context context) {
        super(context);
        init(null);
    }

    public TimerProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public TimerProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public TimerProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        nearEnd = false;
        mColorRegular = Color.GREEN;
        mColorClose = Color.RED;
        mColorBackground = Color.BLACK;
        mColorText = Color.WHITE;
        mTotal = 60;
        mClose = 10;
        mCurrent = -1;
        if(attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs,
                    R.styleable.TimerProgressView,
                    0, 0);
            try {
                mColorBackground = typedArray.getColor(R.styleable.TimerProgressView_colorBackground, mColorBackground);
                mColorRegular = typedArray.getColor(R.styleable.TimerProgressView_progressColor, mColorRegular);
                mColorClose = typedArray.getColor(R.styleable.TimerProgressView_progressCloseColor, mColorClose);
                mColorText = typedArray.getColor(R.styleable.TimerProgressView_timeColor, mColorText);
                mTotal = Math.max(typedArray.getInt(R.styleable.TimerProgressView_maxTime, mTotal), 1);
                mClose = Math.max(typedArray.getInt(R.styleable.TimerProgressView_closeTime, mClose), 0);
                mCurrent = typedArray.getInt(R.styleable.TimerProgressView_currentTime, mCurrent);
            } catch(RuntimeException ignored) {}
            try {
                typedArray.recycle();
            } catch(RuntimeException ignored) {}
        }
        if(mCurrent < 0 || mCurrent > mTotal) {
            mCurrent = mTotal;
        }
        if(mClose > mTotal) {
            mClose = mTotal;
        }
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setColor(mColorRegular);

        mProgressRect = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float radius = (mWidth > mHeight) ? mHeight / 25f : mWidth / 25f;
        canvas.drawColor(mColorBackground);
        StringBuffer label = mTempLabel;
        label.setLength(0);
        label.append(Utils.secondsToTimeString(mCurrent));
        mTextPaint.setColor(mColorText);
        mTextPaint.setTextSize(Math.min(mWidth / label.length() * 2, mHeight / label.length() * 2));
        int xPos = (mWidth / 2);
        int yPos = (int) ((mHeight / 2) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2));
        canvas.drawText(label, 0, label.length(), xPos, yPos, mTextPaint);
        if(mCurrent > mClose) {
            mProgressPaint.setColor(mColorRegular);
        } else {
            mProgressPaint.setColor(mColorClose);
        }
        mProgressRect.set(0, mHeight * 0.95f, mWidth * (float)mCurrent /(float) mTotal, mHeight);
        canvas.drawRect(mProgressRect, mProgressPaint);
        this.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0 , 0, view.getWidth(), view.getHeight(), radius);
            }
        });
        this.setClipToOutline(true);

    }

    public void setCurrentTime(int time, boolean listenForClose) {
        mCurrent = Math.max(time, 0);
        if(mCurrent > mTotal) {
            mTotal = mCurrent;
        }
        invalidate();
        if(listenForClose) {
            if (!nearEnd && mCurrent <= mClose) {
                nearEnd = true;
                if (mOnTimerCloseListener != null) {
                    mOnTimerCloseListener.onTimerClose(mCurrent);
                }
            } else if (nearEnd && mCurrent > mClose) {
                nearEnd = false;
            }
        }
    }

    public void setMaxTime(int time) {
        mTotal = Math.max(time, 1);
        if(mCurrent > mTotal) {
            mCurrent = mTotal;
        }
        if(mClose > mTotal) {
            mClose = mTotal;
        }
        invalidate();
    }

    public void setCloseTimeThreshold(int time) {
        mClose = Math.max(time, 0);
        if(mClose > mTotal) {
            mClose = mTotal;
        }
        invalidate();
    }

    public void setColorBackground(@ColorInt int color) {
        mColorBackground = color;
        invalidate();
    }

    public void setProgressBarColor(@ColorInt int color) {
        mColorRegular = color;
        invalidate();
    }


    public void setProgressCloseBarColor(@ColorInt int color) {
        mColorClose = color;
        invalidate();
    }

    public void setTimeColor(@ColorInt int color) {
        mColorText = color;
        invalidate();
    }

    public int decrementTimer() {
        setCurrentTime(--mCurrent, true);
        return mCurrent;
    }

    public void setOnTimerCloseListener(OnTimerCloseListener onTimerCloseListener) {
        mOnTimerCloseListener = onTimerCloseListener;
    }

    public void removeOnTimerCloseListener() {
        mOnTimerCloseListener = null;
    }

    public int getCurrentTime() {
        return mCurrent;
    }

    public int getMaxTime() {
        return mTotal;
    }

    public int getCloseTimeThreshold() {
        return mClose;
    }

    public @ColorInt int getColorBackground() {
        return mColorBackground;
    }

    public @ColorInt int getProgressBarColor() {
        return mColorRegular;
    }


    public @ColorInt int getProgressCloseBarColor() {
        return mColorClose;
    }

    public @ColorInt int getTimeColor() {
        return mColorText;
    }

}
