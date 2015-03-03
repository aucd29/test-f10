/*
 * CellLayout.java
 * Copyright 2015 OBIGO Inc. All rights reserved.
 *             http://www.obigo.com
 */
package com.obigo.f10;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.obigo.f10.ui.events.OnCellDoubleTapListener;
import com.obigo.f10.ui.events.OnIgnoreGestureListener;

public class CellLayout extends ViewGroup implements OnDoubleTapListener {
    private static final String TAG = "CellLayout";

    private boolean mPortrait;
    private boolean mHalfMode = false;
    private GestureDetector mDectector;
    private OnCellDoubleTapListener mDblTapListener;

    public CellLayout(Context context) {
        this(context, null);
    }

    public CellLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CellLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

//        setAlwaysDrawnWithCacheEnabled(false);
        setFocusable(true);
        setFocusableInTouchMode(true);
        mDectector = new GestureDetector(getContext(), new OnIgnoreGestureListener());
        mDectector.setOnDoubleTapListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);

//        // added burke
//        if (mHalfMode) {
//            widthSpecSize /= 2;
//        }

        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            throw new RuntimeException("CellLayout cannot have UNSPECIFIED dimensions");
        }

        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            lp.width = widthSpecSize;
//            lp.height = heightSpecSize / 2;
            lp.height = heightSpecSize;
            child.setLayoutParams(lp);

//            if (mPortrait) {
//                lp.setup(cellWidth, cellHeight, mWidthGap, mHeightGap, shortAxisStartPadding, longAxisStartPadding);
//            } else {
//                lp.setup(cellWidth, cellHeight, mWidthGap, mHeightGap, longAxisStartPadding, shortAxisStartPadding);
//            }
//
//            if (lp.regenerateId) {
//                child.setId(((getId() & 0xFF) << 16) | (lp.cellX & 0xFF) << 8 | (lp.cellY & 0xFF));
//                lp.regenerateId = false;
//            }

//            Log.d(TAG, "lp width " + lp.width);
//            Log.d(TAG, "lp height " + lp.height);

//            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
//            int childheightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);

            child.measure(widthMeasureSpec, heightMeasureSpec);
        }

        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        int childTop = 0;

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
//                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
//
//                int childLeft = lp.x;
//                int childTop = lp.y;
//                Log.d(TAG, "cell - width " + child.getMeasuredWidth());
//                Log.d(TAG, "cell - height " + child.getMeasuredHeight());

//                Log.d(TAG, "left 0, top " + childTop + ", width " + child.getMeasuredWidth() + ", height " + (childTop + child.getMeasuredHeight()));

                child.layout(0, childTop, child.getMeasuredWidth(), childTop + child.getMeasuredHeight());
                childTop += child.getMeasuredHeight();
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();

        switch (action) {
        case MotionEvent.ACTION_MOVE:
            return true;
        case MotionEvent.ACTION_DOWN:
//            Log.d(TAG, "    @@ inter down");
//            super.onInterceptTouchEvent(ev);
            break;
        case MotionEvent.ACTION_UP:
//            Log.d(TAG, "    @@ inter up");
//            super.onInterceptTouchEvent(ev);
            break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mDectector != null) {
//            Log.d(TAG, "@@ cell layout touch event (id: " + getId() + ")");
            mDectector.onTouchEvent(ev);
        }
//        final int action = ev.getAction();
//        switch (action) {
//        case MotionEvent.ACTION_DOWN:
////            Log.d(TAG, "    @@ touch down");
//            break;
//        case MotionEvent.ACTION_UP:
////            Log.d(TAG, "    @@ touch up");
//            break;
//        }

        return super.onTouchEvent(ev);
    }

    public void setHalfMode(boolean half) {
        mHalfMode = half;
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // interface OnCellDoubleTapListener
    //
    ////////////////////////////////////////////////////////////////////////////////////

    public void setOnCellDoubleTapListener(OnCellDoubleTapListener l) {
        mDblTapListener = l;
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // OnDoubleTapListener
    //
    ////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (mDblTapListener != null) {
            mDblTapListener.onDoubleTap(this);
        }

        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }
}
