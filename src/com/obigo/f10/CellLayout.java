/*
 * CellLayout.java
 * Copyright 2015 OBIGO Inc. All rights reserved.
 *             http://www.obigo.com
 */
package com.obigo.f10;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.obigo.f10.ui.events.OnCellDoubleTapListener;
import com.obigo.f10.ui.events.OnIgnoreGestureListener;

public class CellLayout extends ViewGroup implements OnDoubleTapListener {
    private static final String TAG = "CellLayout";

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

        setDrawingCacheEnabled(false);
        setFocusable(true);
        setFocusableInTouchMode(true);

        mDectector = new GestureDetector(getContext(), new OnIgnoreGestureListener());
        mDectector.setOnDoubleTapListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);

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
            lp.height = heightSpecSize;

            child.setLayoutParams(lp);
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
            break;
        case MotionEvent.ACTION_UP:
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
//        Log.d(TAG, "@@ cell on double tap");

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
