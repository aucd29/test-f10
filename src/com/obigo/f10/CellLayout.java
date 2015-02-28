package com.obigo.f10;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class CellLayout extends ViewGroup {
    private static final String TAG = "CellLayout";

    private boolean mPortrait;
    private boolean mHalfMode = false;

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
    }

    protected void initLayout() {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);

        // added burke
        if (mHalfMode) {
            widthSpecSize /= 2;
        }

        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            throw new RuntimeException("CellLayout cannot have UNSPECIFIED dimensions");
        }

//        final int shortAxisCells = mShortAxisCells;
//        final int longAxisCells = mLongAxisCells;
//        final int longAxisStartPadding = mLongAxisStartPadding;
//        final int longAxisEndPadding = mLongAxisEndPadding;
//        final int shortAxisStartPadding = mShortAxisStartPadding;
//        final int shortAxisEndPadding = mShortAxisEndPadding;
//        final int cellWidth = mCellWidth;
//        final int cellHeight = mCellHeight;

        mPortrait = heightSpecSize > widthSpecSize;

//        int numShortGaps = shortAxisCells - 1;
//        int numLongGaps = longAxisCells - 1;

//        if (mPortrait) {
//            int vSpaceLeft = heightSpecSize - longAxisStartPadding - longAxisEndPadding - (cellHeight * longAxisCells);
//            mHeightGap = vSpaceLeft / numLongGaps;
//
//            int hSpaceLeft = widthSpecSize - shortAxisStartPadding - shortAxisEndPadding - (cellWidth * shortAxisCells);
//            if (numShortGaps > 0) {
//                mWidthGap = hSpaceLeft / numShortGaps;
//            } else {
//                mWidthGap = 0;
//            }
//        } else {
//            int hSpaceLeft = widthSpecSize - longAxisStartPadding - longAxisEndPadding - (cellWidth * longAxisCells);
//            mWidthGap = hSpaceLeft / numLongGaps;
//
//            int vSpaceLeft = heightSpecSize - shortAxisStartPadding - shortAxisEndPadding - (cellHeight * shortAxisCells);
//            if (numShortGaps > 0) {
//                mHeightGap = vSpaceLeft / numShortGaps;
//            } else {
//                mHeightGap = 0;
//            }
//        }

        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();

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

            Log.d(TAG, "lp width " + lp.width);

            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
            int childheightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);

            child.measure(childWidthMeasureSpec, childheightMeasureSpec);
        }

        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();

//        for (int i = 0; i < count; i++) {
//            View child = getChildAt(i);
//            if (child.getVisibility() != GONE) {
//                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
//
//                int childLeft = lp.x;
//                int childTop = lp.y;
//                child.layout(childLeft, childTop, childLeft + lp.width, childTop + lp.height);
//
//                if (lp.dropped) {
//                    lp.dropped = false;
//
//                    final int[] cellXY = mCellXY;
//                    getLocationOnScreen(cellXY);
//                    mWallpaperManager.sendWallpaperCommand(getWindowToken(), "android.home.drop",
//                            cellXY[0] + childLeft + lp.width / 2,
//                            cellXY[1] + childTop + lp.height / 2, 0, null);
//                }
//            }
//        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return true;
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

//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        final int action = ev.getAction();
//        switch (action) {
//        case MotionEvent.ACTION_DOWN:
////            Log.d(TAG, "    @@ touch down");
//            break;
//        case MotionEvent.ACTION_UP:
////            Log.d(TAG, "    @@ touch up");
//            break;
//
//
//        }
//
//        return super.onTouchEvent(ev);
//    }

    public void setHalfMode(boolean half) {
        mHalfMode = half;
    }
}
