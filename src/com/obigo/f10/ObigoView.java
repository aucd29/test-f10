/*
 * ObigoView.java
 * Copyright 2015 OBIGO Inc. All rights reserved.
 *             http://www.obigo.com
 */
package com.obigo.f10;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.MotionEvent;
import android.webkit.WebView;

import com.obigo.f10.ui.events.OnCellDoubleTapListener;
import com.obigo.f10.ui.events.OnIgnoreGestureListener;

public class ObigoView extends WebView implements OnDoubleTapListener {
    private static final String TAG = "ObigoView";

    private GestureDetector mDectector;
    private OnCellDoubleTapListener mDblTapListener;

    public ObigoView(Context context) {
        super(context);
        initLayout();
    }

    public ObigoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout();
    }

    public ObigoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initLayout();
    }

    private static int COUNT = 0;

    protected void initLayout() {
        setDrawingCacheEnabled(false);
        setFocusable(true);
        setFocusableInTouchMode(true);

        setHapticFeedbackEnabled(false);
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);

        getSettings().setSupportMultipleWindows(false);
        getSettings().setJavaScriptEnabled(true);
        getSettings().setBuiltInZoomControls(false);
        getSettings().setSupportZoom(false);
        getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

//        setBackgroundColor(Color.BLACK);
        mDectector = new GestureDetector(getContext(), new OnIgnoreGestureListener());
        mDectector.setOnDoubleTapListener(this);

        switch (COUNT++) {
        case 0:
            loadUrl("file:///android_asset/dumy.html");
            break;
        case 1:
            loadUrl("http://m.daum.net");
            break;
        case 2:
            loadUrl("http://m.naver.com");
            break;
        }

    }

//    @Override
//    public boolean onInterceptTouchEvent(android.view.MotionEvent ev) {
//        switch (ev.getAction()) {
//        case MotionEvent.ACTION_DOWN:
//            Log.d(TAG, "## inter down");
//            return false;
//        case MotionEvent.ACTION_UP:
//            Log.d(TAG, "## inter up");
//            return false;
//        }
//
//        return true;
//    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mDectector != null) {
            mDectector.onTouchEvent(ev);
        }

//        switch (ev.getAction()) {
//        case MotionEvent.ACTION_DOWN:
//            Log.d(TAG, "## touch down");
//            break;
//        case MotionEvent.ACTION_UP:
//            Log.d(TAG, "## touch up");
//            break;
//        }

        return super.onTouchEvent(ev);
    }

    public void setOnCellDoubleTapListener(OnCellDoubleTapListener l) {
        mDblTapListener = l;
    }

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
