/*
 * ObigoView.java
 * Copyright 2015 OBIGO Inc. All rights reserved.
 *             http://www.obigo.com
 */
package com.obigo.f10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

public class ObigoView extends WebView {
//public class ObigoView extends FrameLayout {
    private boolean mUseScreenCache = false;
    public boolean mNeedCapture = false;

    Bitmap mCacheBt = null;
    Canvas mCacheCanvas = null;

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

    protected void initLayout() {
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
//        lp.weight = 1;
//        setLayoutParams(lp);

        loadUrl("file:///android_asset/dumy.html");
//        loadUrl("http://sarangnamu.net");
    }

    @Override
    public boolean onInterceptTouchEvent(android.view.MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mNeedCapture = true;
        }

        return true;
    }

//    @Override
//    protected void dispatchDraw(Canvas canvas) {
//        super.dispatchDraw(canvas);
//
//        if (mUseScreenCache == false) {
//            return;
//        }
//    }

//    @Override
//    protected void onDraw (Canvas canvas) {
//        if (mUseScreenCache == false) {
//            super.onDraw(canvas);
//            return;
//        }
//
//        Workspace ws = (Workspace) getParent();
//
////        if (ws.isFastDraw() && !mLoading && !mNeedCapture) {
//        if (!mNeedCapture) {
//            super.onDraw(canvas);
////            if (!mFirstTouch) {
////                drawCacheCavas(canvas);
////                mFirstTouch = false;
////            }
////        } else if (mLoading || mNeedCapture) {
//        } else if (mNeedCapture) {
//            super.onDraw(canvas);
//            drawCacheCavas(canvas);
//            mNeedCapture = false;
//        } else if (mCacheBt != null && mCacheCanvas != null) {
//            canvas.drawBitmap(mCacheBt, 0, 0, null);
//        } else {
//            super.onDraw(canvas);
//            drawCacheCavas(canvas);
//        }
//    }

//    private void drawCacheCavas(Canvas canvas) {
//        if (mCacheBt == null) {
//            Bitmap.Config c = Bitmap.Config.RGB_565;
//            mCacheBt = Bitmap.createBitmap(getWidth(), getHeight(), c);
//        }
//
//        if (mCacheCanvas == null) {
//            mCacheCanvas = new Canvas();
//            mCacheCanvas.setBitmap(mCacheBt);
//        }
//
////        super.onDraw(mCacheCanvas);
//
//        canvas.drawBitmap(mCacheBt, 0, 0, null);
//    }
}
