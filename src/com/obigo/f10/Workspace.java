/*
 * Workspace.java
 * Copyright 2013 OBIGO Inc. All rights reserved.
 *             http://www.obigo.com
 */
package com.obigo.f10;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.obigo.f10.ui.BkViewPager;
import com.obigo.f10.ui.drag.IDragController;

//public class Workspace extends BkViewPager implements DropTarget, IDragSource , DragScroller {
public class Workspace extends BkViewPager {
    private static final String TAG = "Workspace";

    private MainActivity mActivity;
    private int mMaxCellCount = 10;

    private IDragController mDragger;

//    public Workspace(Context context) {
//        super(context);
//        initWorkspace();
//        initLayout();
//    }

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



    ////////////////////////////////////////////////////////////////////////////////////
    //
    // DRAG
    //
    ////////////////////////////////////////////////////////////////////////////////////

    public void setDragger(IDragController dragger) {
        mDragger = dragger;
    }
//
//    @Override
//    public void scrollLeft() {
//        Log.d(TAG, "scroll left");
//    }
//
//    @Override
//    public void scrollRight() {
//        Log.d(TAG, "scroll right");
//    }
//
//    @Override
//    public void onDropCompleted(View target, boolean success) {
//    }
//
//    @Override
//    public void onDrop(IDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
//    }
//
//    @Override
//    public void onDragEnter(IDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
//    }
//
//    @Override
//    public void onDragOver(IDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
//    }
//
//    @Override
//    public void onDragExit(IDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
//    }
//
//    @Override
//    public boolean acceptDrop(IDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
//        return false;
//    }
}
