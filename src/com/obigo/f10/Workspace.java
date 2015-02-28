/*
 * Workspace.java
 * Copyright 2013 OBIGO Inc. All rights reserved.
 *             http://www.obigo.com
 */
package com.obigo.f10;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import com.obigo.f10.ui.BkViewPager;
import com.obigo.f10.ui.drag.DragScroller;
import com.obigo.f10.ui.drag.DropTarget;
import com.obigo.f10.ui.drag.IDragController;
import com.obigo.f10.ui.drag.IDragSource;

public class Workspace extends BkViewPager implements DropTarget, IDragSource , DragScroller {
    private static final String TAG = "Workspace";

    private MainActivity mActivity;
    private int mMaxCellCount = 10;

    private IDragController mDragger;
    private OnLongClickListener mLongClickListener;

    public Workspace(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWorkspace();
        initLayout();
    }

    private void initWorkspace() {
        setBackgroundColor(0x7f000000);
    }

    private void initLayout() {
        setHapticFeedbackEnabled(false);
        setChildWidth(true);
        setBeastSwipeMode(true);
        setEdgeEventMode(true);

        for (int i=0; i<mMaxCellCount; ++i) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.workspace_screen, this, false);

            if (view instanceof CellLayout) {
                ((CellLayout) view).setHalfMode(true);
            }

//            WebView view = new WebView(getContext());
//            view.loadUrl("http://sarangnamu.net");
//            view.setVerticalScrollBarEnabled(false);
//            view.setHorizontalScrollBarEnabled(false);

            int x = i % 5;
            switch (x) {
            case 0:
                view.setBackgroundColor(0x7fff0000);
                break;
            case 1:
                view.setBackgroundColor(0x7f00ff00);
                break;
            case 2:
                view.setBackgroundColor(0x7f0000ff);
                break;
            case 3:
                view.setBackgroundColor(0x7fff00ff);
                break;
            case 4:
                view.setBackgroundColor(0x7f00ffff);
                break;
            }

            addView(view);
        }

//        postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                snapToScreen(3);
//            }
//        }, 1000);
    }


    public void setMainActivity(MainActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onEdgeEventMode(int mode) {
        if (mActivity != null) {
            switch (mode) {
            case EDGE_EVENT_LEFT:
                mActivity.showAppList();
                break;

            case EDGE_EVENT_TOP:
                mActivity.showSettingMenu();
                break;

            default:
                break;
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_UP:
            if (mActivity != null && mActivity.isDeleteZone()) {
              mActivity.hideDeleteZone();
            }
            break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mActivity != null && mActivity.isWorkspaceLocked()) {
            return false;
        }

        return super.onTouchEvent(ev);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mLongClickListener = l;

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).setOnLongClickListener(l);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // DRAG
    //
    ////////////////////////////////////////////////////////////////////////////////////

    public void setDragger(IDragController dragger) {
        mDragger = dragger;
    }

    @Override
    public void scrollLeft() {
        Log.d(TAG, "scroll left");
    }

    @Override
    public void scrollRight() {
        Log.d(TAG, "scroll right");
    }

    @Override
    public void onDropCompleted(View target, boolean success) {
    }

    @Override
    public void onDrop(IDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
    }

    @Override
    public void onDragEnter(IDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
    }

    @Override
    public void onDragOver(IDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
    }

    @Override
    public void onDragExit(IDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
    }

    @Override
    public boolean acceptDrop(IDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
        return false;
    }
}
