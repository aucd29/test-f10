/*
 * Workspace.java
 * Copyright 2015 OBIGO Inc. All rights reserved.
 *             http://www.obigo.com
 */
package com.obigo.f10;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.obigo.f10.ui.BkViewPager;
import com.obigo.f10.ui.Capture;
import com.obigo.f10.ui.ani.AnimatorEndListener;
import com.obigo.f10.ui.ani.ResizeHelper;
import com.obigo.f10.ui.drag.DragController;
import com.obigo.f10.ui.drag.DragScroller;
import com.obigo.f10.ui.drag.DragSource;
import com.obigo.f10.ui.drag.DragView;
import com.obigo.f10.ui.drag.DropTarget;
import com.obigo.f10.ui.events.OnCellDoubleTapListener;


public class Workspace extends BkViewPager implements OnCellDoubleTapListener, DropTarget, DragSource, DragScroller {
    private static final String TAG = "Workspace";

    private MainActivity mActivity;
    private int mMaxCellCount = 15;

    private boolean mFullScreenMode = false;
    private boolean mAnimating = false;
    private int mDoubleTapPosition;
    private View mDragView = null;

    private DragController mDragController;
    private OnLongClickListener mLongClickListener;

    public Workspace(Context context, AttributeSet attrs) {
        super(context, attrs);

        initWorkspace();
        initLayout();
    }

    private void initWorkspace() {
//        setBackgroundColor(0x7f000000);
    }

    private void initLayout() {
        setHapticFeedbackEnabled(false);
        setScrollByChildWidth(true);
        setBeastSwipeMode(true);
        setEdgeEventMode(true);

        for (int i=0; i<mMaxCellCount; ++i) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.workspace_screen, this, false);

            if (view instanceof CellLayout) {
                CellLayout cell = (CellLayout) view;
                cell.setHalfMode(true);
                cell.setOnCellDoubleTapListener(this);
                cell.setTag("" + i);

                TextView tv = new TextView(getContext());
                tv.setText("pos " + i);
                cell.addView(tv);

//                if (i != 0) {
//                    ObigoView oview = new ObigoView(getContext());
//                    cell.addView(oview);
////                        Log.d(TAG, "added obigo view " + i + " : " + j);
//                }
            }

            int x = i % 5;
            switch (x) {
            case 0:
                view.setBackgroundColor(0xffff0000);
                break;
            case 1:
                view.setBackgroundColor(0xff00ff00);
                break;
            case 2:
                view.setBackgroundColor(0xff0000ff);
                break;
            case 3:
                view.setBackgroundColor(0xffff00ff);
                break;
            case 4:
                view.setBackgroundColor(0xff00ffff);
                break;
            }

            addView(view);
        }
    }

    public void setMainActivity(MainActivity activity) {
        mActivity = activity;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mLongClickListener = l;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            ViewGroup view = (ViewGroup) getChildAt(i);
            view.setOnLongClickListener(l);

//            int cnt = view.getChildCount();
//            Log.d(TAG, "@@ cnt  " + cnt);
//            for (int j=0; j<cnt; ++j) {
//                view.getChildAt(j).setOnLongClickListener(l);
//            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        computeScroll();
        mDragController.setWindowToken(getWindowToken());
    }

    @Override
    public void onEdgeEvent(int mode) {
        if (mActivity != null) {
            switch (mode) {
            case EDGE_EVENT_TO_LEFT:
                mActivity.showAppList();
                break;

            case EDGE_EVENT_TOP_RIGHT_TO_BOTTOM:
                mActivity.showSettingMenu();
                break;

            case EDGE_EVENT_TOP_LEFT_TO_BOTTOM:
                break;

            default:
                break;
            }
        }
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
        if (!mFullScreenMode) {
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
        if (mFullScreenMode) {
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

        if (mFullScreenMode) {
            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                if (child.getVisibility() != View.GONE) {
                    final int childWidth = child.getMeasuredWidth();

                    child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
                    childLeft += childWidth;
                }
            }
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
            if (mTouchState != TOUCH_STATE_SCROLLING && !mFullScreenMode) {
                setPositionDoubleTap(ev.getX(), ev.getY());
            }

//            if (mActivity != null && mActivity.isDeleteZone()) {
//                mActivity.hideDeleteZone();
//            }
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
        if (mFullScreenMode) {
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
    protected void snapToScreen(int whichScreen, int velocity, boolean settle) {
        super.snapToScreen(whichScreen, velocity, settle);

        if (mFullScreenMode) {
//            Log.d(TAG, "@@ next screen " + mNextScreen + ", current " + mCurrentScreen + ", tap " + mDoubleTapPosition);

            if (mCurrentScreen < mNextScreen) {
                ++mDoubleTapPosition;
                mDoubleTapPosition %= 4;
            } else if (mCurrentScreen > mNextScreen) {
                if (--mDoubleTapPosition < 0) {
                    mDoubleTapPosition = OnCellDoubleTapListener.RIGHT_BOTTOM;
                }
            }

//            Log.d(TAG, "## next screen " + mNextScreen + ", current " + mCurrentScreen + ", tap " + mDoubleTapPosition);
        }
    }

    @Override
    public void setDragController(DragController controller) {
        mDragController = controller;
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // DOUBLE TAP EVENT (FULL SCREEN)
    //
    ////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onDoubleTap(View view) {
        if (!mFullScreenMode && !mActivity.isWorkspaceLocked()) {
            mAnimating = true;

            for (int i=0; i<getChildCount(); ++i) {
                if (getChildAt(i).equals(view)) {
                    mCurrentScreen = i;
                }
            }

            final View expandLayout = mActivity.getExpandLayout();
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(getWidth() / 2, mCurrentScreen == 0 ? getHeight() : getHeight() / 2);

            if (mCurrentScreen == 0 || mDoubleTapPosition == OnCellDoubleTapListener.LEFT_TOP) {
                lp.gravity = Gravity.LEFT | Gravity.TOP;
            } else if (mDoubleTapPosition == OnCellDoubleTapListener.LEFT_BOTTOM) {
                lp.gravity = Gravity.LEFT | Gravity.BOTTOM;
            } else if (mDoubleTapPosition == OnCellDoubleTapListener.RIGHT_TOP) {
                lp.gravity = Gravity.RIGHT | Gravity.TOP;
            } else {
                lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            }

            Drawable drawable = new BitmapDrawable(getContext().getResources(), Capture.get(view));
            expandLayout.setScaleX(1);
            expandLayout.setScaleY(1);
            expandLayout.setTranslationX(0);
            expandLayout.setTranslationY(0);
            expandLayout.setLayoutParams(lp);
            expandLayout.setBackground(drawable);
            expandLayout.setVisibility(View.VISIBLE);

            ResizeHelper.expand(expandLayout, mCurrentScreen, mDoubleTapPosition, getWidth(), getHeight(), new AnimatorEndListener(expandLayout) {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    setFullScreenMode(true);

//                    if (expandLayout.getTag() != null) {
//                        Bitmap bmp = ((Bitmap) expandLayout.getTag());
//                        bmp.recycle();
//                        bmp = null;
////
//                        expandLayout.setTag(null);
//                    }

                    mAnimating = false;
                }
            });
        }
    }

    private void setPositionDoubleTap(float x, float y) {
        float width  = getWidth() / 2;
        float height = getHeight() / 2;

        if (x < width && y < height) {
            mDoubleTapPosition = OnCellDoubleTapListener.LEFT_TOP;
        } else if (x > width && y < height) {
            mDoubleTapPosition = OnCellDoubleTapListener.RIGHT_TOP;
        } else if (x < width && y > height) {
            mDoubleTapPosition = OnCellDoubleTapListener.LEFT_BOTTOM;
        } else {
            mDoubleTapPosition = OnCellDoubleTapListener.RIGHT_BOTTOM;
        }
    }

    private void setFullScreenMode(boolean mode) {
        setBeastSwipeMode(!mode);
        setEdgeEventMode(!mode);
        setScrollByChildWidth(!mode);
        mFullScreenMode = mode;

        requestLayout();
    }

    public void resetScreenMode() {
        mAnimating = true;

        int prevScreen = mCurrentScreen;
        Drawable drawable = new BitmapDrawable(getContext().getResources(), Capture.get(getChildAt(mCurrentScreen)));

        if (mCurrentScreen > 0 && mDoubleTapPosition == OnCellDoubleTapListener.LEFT_TOP) {
            mCurrentScreen = mCurrentScreen / 2 + 1;
        } else if (mDoubleTapPosition == OnCellDoubleTapListener.RIGHT_BOTTOM) {
            mCurrentScreen = mCurrentScreen / 2 - 1;
        } else {
            mCurrentScreen = mCurrentScreen / 2;
        }

        setFullScreenMode(false);

        final View expandLayout = mActivity.getExpandLayout();
        expandLayout.setScaleX(1);
        expandLayout.setScaleY(1);
        expandLayout.setTranslationX(0);
        expandLayout.setTranslationY(0);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(getWidth(), getHeight());
        expandLayout.setLayoutParams(lp);
        expandLayout.setBackground(drawable);
        expandLayout.setVisibility(View.VISIBLE);

//        traceTapPosition(mDoubleTapPosition);
        ResizeHelper.collapse(expandLayout, prevScreen, mDoubleTapPosition, getWidth(), getHeight(), new AnimatorEndListener(expandLayout) {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setFullScreenMode(false);

                mAnimating = false;
            }
        });
    }

    public boolean isFullScreenMode() {
        return mFullScreenMode;
    }

    public boolean isAnimating() {
        return mAnimating;
    }

    private void traceTapPosition(int pos) {
        switch (pos) {
        case OnCellDoubleTapListener.LEFT_TOP:
            Log.d(TAG, "@@ tap pos LEFT TOP");
            break;
        case OnCellDoubleTapListener.RIGHT_TOP:
            Log.d(TAG, "@@ tap pos RIGHT TOP");
            break;
        case OnCellDoubleTapListener.LEFT_BOTTOM:
            Log.d(TAG, "@@ tap pos LEFT BOTTOM");
            break;
        case OnCellDoubleTapListener.RIGHT_BOTTOM:
            Log.d(TAG, "@@ tap pos RIGHT BOTTOM");
            break;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // DRAG AND DROP
    //
    ////////////////////////////////////////////////////////////////////////////////////

    public void removeDropView() {
        if (mDragView != null) {
            removeView(mDragView);
            mDragView = null;
        }
    }

    public void startDrag(View child) {
        mDragController.startDrag(child, this, child.getTag(), DragController.DRAG_ACTION_MOVE);
        invalidate();
    }

    @Override
    public void scrollLeft() {
//        clearVacantCache();
        if (mScroller.isFinished()) {
            if (mCurrentScreen > 0) {
                snapToScreen(mCurrentScreen - 1);
            }
        } else {
            if (mNextScreen > 0) {
                snapToScreen(mNextScreen - 1);
            }
        }
    }

    @Override
    public void scrollRight() {
//        clearVacantCache();
        if (mScroller.isFinished()) {
            if (mCurrentScreen < getChildCount() -1) {
                snapToScreen(mCurrentScreen + 1);
            }
        } else {
            if (mNextScreen < getChildCount() -1) {
                snapToScreen(mNextScreen + 1);
            }
        }
    }

    @Override
    public void onDropCompleted(View target, boolean success) {
//        clearVacantCache();
//
//        if (success){
//            if (target != this && mDragInfo != null) {
//                final CellLayout cellLayout = (CellLayout) getChildAt(mDragInfo.screen);
//                cellLayout.removeView(mDragInfo.cell);
//                if (mDragInfo.cell instanceof DropTarget) {
//                    mDragController.removeDropTarget((DropTarget)mDragInfo.cell);
//                }
//                //final Object tag = mDragInfo.cell.getTag();
//            }
//        } else {
//            if (mDragInfo != null) {
//                final CellLayout cellLayout = (CellLayout) getChildAt(mDragInfo.screen);
//                cellLayout.onDropAborted(mDragInfo.cell);
//            }
//        }
//
//        mDragInfo = null;
    }

    @Override
    public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
//        final CellLayout cellLayout = getCurrentDropLayout();
//        if (source != this) {
//            onDropExternal(x - xOffset, y - yOffset, dragInfo, cellLayout);
//        } else {
//            // Move internally
//            if (mDragInfo != null) {
//                final View cell = mDragInfo.cell;
//                int index = mScroller.isFinished() ? mCurrentScreen : mNextScreen;
//                if (index != mDragInfo.screen) {
//                    final CellLayout originalCellLayout = (CellLayout) getChildAt(mDragInfo.screen);
//                    originalCellLayout.removeView(cell);
//                    cellLayout.addView(cell);
//                }
//                mTargetCell = estimateDropCell(x - xOffset, y - yOffset,
//                        mDragInfo.spanX, mDragInfo.spanY, cell, cellLayout, mTargetCell);
//                cellLayout.onDropChild(cell, mTargetCell);
//
//                final ItemInfo info = (ItemInfo) cell.getTag();
//                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
//                LauncherModel.moveItemInDatabase(mLauncher, info,
//                        LauncherSettings.Favorites.CONTAINER_DESKTOP, index, lp.cellX, lp.cellY);
//            }
//        }
    }

    @Override
    public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
//        clearVacantCache();
    }

    @Override
    public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
    }

    @Override
    public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
//        clearVacantCache();
    }

    @Override
    public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
//        final CellLayout layout = getCurrentDropLayout();
//        final CellLayout.CellInfo cellInfo = mDragInfo;
//        final int spanX = cellInfo == null ? 1 : cellInfo.spanX;
//        final int spanY = cellInfo == null ? 1 : cellInfo.spanY;
//
//        if (mVacantCache == null) {
//            final View ignoreView = cellInfo == null ? null : cellInfo.cell;
//            mVacantCache = layout.findAllVacantCells(null, ignoreView);
//        }
//
//        return mVacantCache.findCellForSpan(mTempEstimate, spanX, spanY, false);
        return false;
    }

    @Override
    public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo, Rect recycle) {
//        final CellLayout layout = getCurrentDropLayout();
//
//        final CellLayout.CellInfo cellInfo = mDragInfo;
//        final int spanX = cellInfo == null ? 1 : cellInfo.spanX;
//        final int spanY = cellInfo == null ? 1 : cellInfo.spanY;
//        final View ignoreView = cellInfo == null ? null : cellInfo.cell;
//
//        final Rect location = recycle != null ? recycle : new Rect();
//
//        // Find drop cell and convert into rectangle
//        int[] dropCell = estimateDropCell(x - xOffset, y - yOffset,
//                spanX, spanY, ignoreView, layout, mTempCell);
//
//        if (dropCell == null) {
//            return null;
//        }
//
//        layout.cellToPoint(dropCell[0], dropCell[1], mTempEstimate);
//        location.left = mTempEstimate[0];
//        location.top = mTempEstimate[1];
//
//        layout.cellToPoint(dropCell[0] + spanX, dropCell[1] + spanY, mTempEstimate);
//        location.right = mTempEstimate[0];
//        location.bottom = mTempEstimate[1];
//
//        return location;

        return null;
    }
}
