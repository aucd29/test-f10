/*
 * Workspace.java
 * Copyright 2015 OBIGO Inc. All rights reserved.
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
import com.obigo.f10.ui.events.OnCellDoubleTapListener;


public class Workspace extends BkViewPager implements DropTarget, IDragSource , DragScroller, OnCellDoubleTapListener {
    private static final String TAG = "Workspace";

    private MainActivity mActivity;
    private int mMaxCellCount = 15;

    private IDragController mDragger;
    private OnLongClickListener mLongClickListener;
    private boolean mFullSizeMode = false;
    private int mDoubleTapPosition;

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
                CellLayout cell = (CellLayout) view;
                cell.setHalfMode(true);
                cell.setOnCellDoubleTapListener(this);

                if (i != 0) {
                    for (int j=0; j<2; ++j) {
                        ObigoView oview = new ObigoView(getContext());

                        cell.addView(oview);
//                        Log.d(TAG, "added obigo view " + i + " : " + j);
                    }
                }
            }

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
    }

    public void setMainActivity(MainActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onEdgeEvent(int mode) {
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

    public void setFullSizeCell(boolean fullsize) {
        setBeastSwipeMode(!fullsize);
        setEdgeEventMode(!fullsize);
        setChildWidth(!fullsize);
        mFullSizeMode = fullsize;

        requestLayout();
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // LAYOUT OVERRIDE
    //
    ////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (!mFullSizeMode) {
            width /= 2;
        }

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
        }

        int height = MeasureSpec.getSize(heightMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
        }

        // The children are given the same width and height as the workspace
        final int count = getChildCount();
        if (mFullSizeMode) {
            for (int i = 0; i < count; i++) {
                getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
            }
        } else {
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                lp.width = width;
                lp.height = i == 0 ? height : height / 2;
                child.setLayoutParams(lp);

                int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
                int childheightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);

                getChildAt(i).measure(childWidthMeasureSpec, childheightMeasureSpec);
            }
        }

        if (mFirstLayout) {
            setHorizontalScrollBarEnabled(false);
            scrollTo(mCurrentScreen * width, 0);
            setHorizontalScrollBarEnabled(true);
            mFirstLayout = false;
        } else {
            scrollTo(mCurrentScreen * width, 0);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childLeft = 0;
        int childTop = 0;

        final int count = getChildCount();

        if (mFullSizeMode) {
            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                if (child.getVisibility() != View.GONE) {
                    final int childWidth = child.getMeasuredWidth();

                    child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
                    childLeft += childWidth;
                }
            }

//            Log.d(TAG, "===================================================================");
//            Log.d(TAG, "width " + getWidth());
//            Log.d(TAG, "measure size " + getMeasuredWidth());
//            Log.d(TAG, "===================================================================");
        } else {
            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                if (child.getVisibility() != View.GONE) {
                    final int childWidth = child.getMeasuredWidth();
                    final int childHeight= child.getMeasuredHeight();

                    if (i == 0) {
                        child.layout(childLeft, 0, childLeft + childWidth, childHeight);
                        childLeft += childWidth;
                    } else {
                        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);

                        if ((i % 2) == 1) {
                            childTop = childHeight;
                        } else {
                            childLeft += childWidth;
                            childTop = 0;
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_UP:
            if (mTouchState != TOUCH_STATE_SCROLLING && !mFullSizeMode) {
                setPositionDoubleTap(ev.getX(), ev.getY());
            }

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

        switch (ev.getAction()) {
        case MotionEvent.ACTION_UP:
            if (mTouchState != TOUCH_STATE_SCROLLING) {
                Log.d(TAG, "x " + ev.getX() + ", y " + ev.getY());
            }
            break;
        }

        return super.onTouchEvent(ev);
    }

    @Override
    public int getScreenCount() {
        if (mFullSizeMode) {
            return getChildCount();
        } else {
            if (getChildCount() == 1) {
                return 1;
            } else {
                return getChildCount() / 2;
            }
        }
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mLongClickListener = l;

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).setOnLongClickListener(l);
        }
    }

    private void setPositionDoubleTap(float x, float y) {
        float width  = getWidth() / 2;
        float height = getHeight() / 2;

        Log.d(TAG, "x " + x + ",y " + y);

        if (x < width && y < height) {
            mDoubleTapPosition = OnCellDoubleTapListener.TOP_LEFT;
        } else if (x > width && y < height) {
            mDoubleTapPosition = OnCellDoubleTapListener.TOP_RIGHT;
        } else if (x < width && y > height) {
            mDoubleTapPosition = OnCellDoubleTapListener.BOTTOM_LEFT;
        } else {
            mDoubleTapPosition = OnCellDoubleTapListener.BOTTOM_RIGHT;
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

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // interface OnCellDoubleTapListener
    //
    ////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onDoubleTap(View view) {
        Log.d(TAG, "double tap pos " + mDoubleTapPosition);
        if (!mFullSizeMode) {
            for (int i=0; i<getChildCount(); ++i) {
                if (getChildAt(i).equals(view)) {
                    mCurrentScreen = i;
                }
            }

            setFullSizeCell(true);
        } else {
            Log.d(TAG, "current screen " + mCurrentScreen);
            if (mCurrentScreen > 0 && mDoubleTapPosition == OnCellDoubleTapListener.TOP_LEFT) {
                mCurrentScreen = mCurrentScreen / 2 + 1;

                Log.d(TAG, "current size " + mCurrentScreen);
            } else if (mDoubleTapPosition == OnCellDoubleTapListener.BOTTOM_RIGHT) {
                mCurrentScreen = mCurrentScreen / 2 - 1;
            } else {
                mCurrentScreen = mCurrentScreen / 2;
            }

            setFullSizeCell(false);
        }
    }
}
