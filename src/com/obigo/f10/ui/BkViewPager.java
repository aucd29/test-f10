/*
 * BkViewPager.java
 * Copyright 2013 Burke Choi All rights reserved.
 *             http://www.sarangnamu.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.obigo.f10.ui;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;
import android.widget.ViewSwitcher;

import com.obigo.f10.CellLayout;

public class BkViewPager extends ViewGroup {
    private static final String TAG = "BkViewPager";

    protected static final int INVALID_SCREEN = -1;
    protected static final int SNAP_VELOCITY = 800;
    private int mDefaultScreen;

    protected boolean mFirstLayout = true;

    protected int mCurrentScreen;
    protected int mNextScreen = INVALID_SCREEN;
    protected Scroller mScroller;
    protected VelocityTracker mVelocityTracker;

    protected float mLastMotionX;
    protected float mLastMotionY;

    protected final static int TOUCH_STATE_REST = 0;
    protected final static int TOUCH_STATE_SCROLLING = 1;

    protected int mTouchState = TOUCH_STATE_REST;
    protected boolean mAllowLongPress = true;

    protected int mTouchSlop;
    protected int mMaximumVelocity;

    private static final int INVALID_POINTER = -1;

    protected int mActivePointerId = INVALID_POINTER;
    private static final float NANOTIME_DIV = 1000000000.0f;
    private static final float SMOOTHING_SPEED = 0.75f;
    private static final float SMOOTHING_CONSTANT = (float) (0.016 / Math.log(SMOOTHING_SPEED));
    private float mSmoothingTime;
    private float mTouchX;

    private WorkspaceOvershootInterpolator mScrollInterpolator;

    private static final float BASELINE_FLING_VELOCITY = 2500.f;
    private static final float FLING_VELOCITY_INFLUENCE = 0.4f;

    private boolean mScrollByChildWidth = false;
    private boolean mBeastMode = false;
    private boolean mEdgeEventMode = false;

    private float mEdgeMotion = 130.0f;
    private float mEdgeMotionX, mEdgeMotionY;

    public static final int EDGE_EVENT_TO_LEFT = 1;
    public static final int EDGE_EVENT_TOP_RIGHT_TO_BOTTOM = 2;
    public static final int EDGE_EVENT_TOP_LEFT_TO_BOTTOM = 3;

    private static class WorkspaceOvershootInterpolator implements Interpolator {
        private static final float DEFAULT_TENSION = 0.0f; //1.3f; // modified by burke
        private float mTension;

        public WorkspaceOvershootInterpolator() {
            mTension = DEFAULT_TENSION;
        }

        public void setDistance(int distance) {
            mTension = distance > 0 ? DEFAULT_TENSION / distance : DEFAULT_TENSION;
        }

        public void disableSettle() {
            mTension = 0.f;
        }

        @Override
        public float getInterpolation(float t) {
            // _o(t) = t * t * ((tension + 1) * t + tension)
            // o(t) = _o(t - 1) + 1
            t -= 1.0f;
            return t * t * ((mTension + 1) * t + mTension) + 1.0f;
        }
    }

    private OnScreenSwitchListener mOnScreenSwitchListener;

    public BkViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BkViewPager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setHapticFeedbackEnabled(false);

        init();
    }

    private void init() {
        mDefaultScreen = 0;

        mScrollInterpolator = new WorkspaceOvershootInterpolator();
        mScroller = new Scroller(getContext(), mScrollInterpolator);
        mCurrentScreen = mDefaultScreen;

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    boolean isDefaultScreenShowing() {
        return mCurrentScreen == mDefaultScreen;
    }

    public int getCurrentScreen() {
        return mCurrentScreen;
    }

    public void setCurrentScreen(int currentScreen) {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }

        clearVacantCache();
        mCurrentScreen = Math.max(0, Math.min(currentScreen, getScreenCount() - 1));
//        mPreviousIndicator.setLevel(mCurrentScreen);
//        mNextIndicator.setLevel(mCurrentScreen);
        scrollTo(mCurrentScreen * getWidth(), 0);
        invalidate();
    }

    private void clearVacantCache() {
//        if (mVacantCache != null) {
//            mVacantCache.clearVacantCells();
//            mVacantCache = null;
//        }
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        mTouchX = x;
        mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mTouchX = mScroller.getCurrX();
            setScrollX(mScroller.getCurrX());
            mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
            setScrollY(mScroller.getCurrY());
            postInvalidate();
        } else if (mNextScreen != INVALID_SCREEN) {
            mCurrentScreen = Math.max(0, Math.min(mNextScreen, getScreenCount() - 1));
//            mPreviousIndicator.setLevel(mCurrentScreen);
//            mNextIndicator.setLevel(mCurrentScreen);
//            Launcher.setScreen(mCurrentScreen);
            mNextScreen = INVALID_SCREEN;
            clearChildrenCache();
        } else if (mTouchState == TOUCH_STATE_SCROLLING) {
            final float now = System.nanoTime() / NANOTIME_DIV;
            final float e = (float) Math.exp((now - mSmoothingTime) / SMOOTHING_CONSTANT);
            final float dx = mTouchX - getScrollX();
            setScrollX((int)(getScrollX() + dx * e));

            mSmoothingTime = now;

            if (dx > 1.f || dx < -1.f) {
                postInvalidate();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
        }

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
        }

        // The children are given the same width and height as the workspace
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }

        if (mFirstLayout) {
            setHorizontalScrollBarEnabled(false);
            scrollTo(mCurrentScreen * width, 0);
            setHorizontalScrollBarEnabled(true);
            mFirstLayout = false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childLeft = 0;

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                final int childWidth = child.getMeasuredWidth();
                child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
                childLeft += childWidth;
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_MOVE: {
            final int pointerIndex = ev.findPointerIndex(mActivePointerId);
            final float x = ev.getX(pointerIndex);
            final float y = ev.getY(pointerIndex);
            final int xDiff = (int) Math.abs(x - mLastMotionX);
            final int yDiff = (int) Math.abs(y - mLastMotionY);

            final int touchSlop = mTouchSlop;
            boolean xMoved = xDiff > touchSlop;
            boolean yMoved = yDiff > touchSlop;

            if (xMoved || yMoved) {
                if (xMoved) {
                    // Scroll if the user moved far enough along the X axis
                    mTouchState = TOUCH_STATE_SCROLLING;
                    mLastMotionX = x;
                    mEdgeMotionX = x;
                    mEdgeMotionY = y;

//                    Log.d(TAG, "mEdgeMotionX = " + mEdgeMotionX);
                    mTouchX = getScrollX();
                    mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                    enableChildrenCache(mCurrentScreen - 1, mCurrentScreen + 1);
                }
                // Either way, cancel any pending longpress
                if (mAllowLongPress) {
                    mAllowLongPress = false;
                    // Try canceling the long press. It could also have been scheduled
                    // by a distant descendant, so use the mAllowLongPress flag to block
                    // everything
                    final View currentScreen = getChildAt(mCurrentScreen);
                    currentScreen.cancelLongPress();
                }
            }
            break;
        }

        case MotionEvent.ACTION_DOWN: {
            final float x = ev.getX();
            final float y = ev.getY();
            // Remember location of down touch
            mLastMotionX = x;
            mLastMotionY = y;
            mActivePointerId = ev.getPointerId(0);
            mAllowLongPress = true;
            mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
            Log.d(TAG, "@@ inter down");
            break;
        }

        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            Log.d(TAG, "vp inter up");
            if (mTouchState != TOUCH_STATE_SCROLLING) {
                final CellLayout currentScreen = (CellLayout)getChildAt(mCurrentScreen);
//                if (!currentScreen.lastDownOnOccupiedCell()) {
//                    getLocationOnScreen(mTempCell);
//                    // Send a tap to the wallpaper if the last down was on empty space
//                    final int pointerIndex = ev.findPointerIndex(mActivePointerId);
//                    mWallpaperManager.sendWallpaperCommand(getWindowToken(),
//                            "android.wallpaper.tap",
//                            mTempCell[0] + (int) ev.getX(pointerIndex),
//                            mTempCell[1] + (int) ev.getY(pointerIndex), 0, null);
//                }
            }

            // Release the drag
            clearChildrenCache();
            mTouchState = TOUCH_STATE_REST;
            mActivePointerId = INVALID_POINTER;
            mAllowLongPress = false;

            if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
            break;

        case MotionEvent.ACTION_POINTER_UP:
            onSecondaryPointerUp(ev);
            break;
        }

        return mTouchState != TOUCH_STATE_REST;
    }

    void enableChildrenCache(int fromScreen, int toScreen) {
//        if (fromScreen > toScreen) {
//            final int temp = fromScreen;
//            fromScreen = toScreen;
//            toScreen = temp;
//        }
//
//        final int count = getChildCount();
//
//        fromScreen = Math.max(fromScreen, 0);
//        toScreen = Math.min(toScreen, count - 1);
//
//        for (int i = fromScreen; i <= toScreen; i++) {
//            final CellLayout layout = (CellLayout) getChildAt(i);
//            layout.setChildrenDrawnWithCacheEnabled(true);
//            layout.setChildrenDrawingCacheEnabled(true);
//        }
    }

    void clearChildrenCache() {
//        final int count = getChildCount();
//        for (int i = 0; i < count; i++) {
//            final CellLayout layout = (CellLayout) getChildAt(i);
//            layout.setChildrenDrawnWithCacheEnabled(false);
//        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }

        mVelocityTracker.addMovement(ev);

        final int action = ev.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }

            mLastMotionX = ev.getX();
            mActivePointerId = ev.getPointerId(0);
            if (mTouchState == TOUCH_STATE_SCROLLING) {
                enableChildrenCache(mCurrentScreen - 1, mCurrentScreen + 1);
            }
            break;

        case MotionEvent.ACTION_MOVE:
            if (mTouchState == TOUCH_STATE_SCROLLING) {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(pointerIndex);
                final float deltaX = mLastMotionX - x;
                final float y = ev.getY(pointerIndex);
                final float deltaY = mLastMotionY - y;

//                Log.d(TAG, "touch mEdgeMotionX " + mEdgeMotionX);
//                Log.d(TAG, "touch mEdgeMotionY " + mEdgeMotionY);
                Log.d(TAG, "deltaX " + deltaX + ", deltaY " + deltaY);

                final int halfWidth = getWidth() / 2;

                if (mEdgeEventMode && mEdgeMotionX < mEdgeMotion && deltaY > -30 && deltaY < 30) {
                    onEdgeEvent(EDGE_EVENT_TO_LEFT);
                } else if (mEdgeEventMode && mEdgeMotionY < mEdgeMotion && mEdgeMotionX > halfWidth && deltaY < -30) {
                    onEdgeEvent(EDGE_EVENT_TOP_RIGHT_TO_BOTTOM);
                } else if (mEdgeEventMode && mEdgeMotionY < mEdgeMotion && mEdgeMotionX < halfWidth && deltaY < -30 ) {
                    onEdgeEvent(EDGE_EVENT_TOP_LEFT_TO_BOTTOM);
                } else {
                    mLastMotionX = x;

                    if (deltaX < 0) {
                        if (mTouchX > 0) {
                            mTouchX += Math.max(-mTouchX, deltaX);
                            mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                            invalidate();
                        }
                    } else if (deltaX > 0) {
                        final float availableToScroll = getChildAt(getChildCount() - 1).getRight() - mTouchX - getWidth();
                        if (availableToScroll > 0) {
                            mTouchX += Math.min(availableToScroll, deltaX);
                            mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                            invalidate();
                        }
                    } else {
                        awakenScrollBars();
                    }
                }
            }
            break;

        case MotionEvent.ACTION_UP:
            if (mTouchState == TOUCH_STATE_SCROLLING) {
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                final int velocityX = (int) velocityTracker.getXVelocity(mActivePointerId);

                final int screenWidth = mScrollByChildWidth ? getWidth() / 2 : getWidth();
                final int whichScreen = (getScrollX() + (screenWidth / 2)) / screenWidth;
                final float scrolledPos = (float) getScrollX() / screenWidth;

//                Log.d(TAG, "touch up scrolling " + screenWidth +
//                        ", whichscreen " + whichScreen + ", scrolledPos " + scrolledPos);

                if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
                    // Fling hard enough to move left.
                    // Don't fling across more than one screen at a time.
                    final int bound = scrolledPos < whichScreen ? mCurrentScreen - 1 : mCurrentScreen;

                    if (mBeastMode && (velocityX < -11000 || velocityX > 11000)) {
                        snapToScreen(0);
                    } else {
                        snapToScreen(Math.min(whichScreen, bound), velocityX, true);
                    }
                } else if (velocityX < -SNAP_VELOCITY && mCurrentScreen < getScreenCount() - 1) {
                    // Fling hard enough to move right
                    // Don't fling across more than one screen at a time.
                    final int bound = scrolledPos > whichScreen ? mCurrentScreen + 1 : mCurrentScreen;

                    if (mBeastMode && (velocityX < -11000 || velocityX > 11000)) {
                        snapToDestination();
                    } else {
                        snapToScreen(Math.max(whichScreen, bound), velocityX, true);
                    }
                } else {
                    snapToScreen(whichScreen, 0, true);
                }

                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
            } else {
                Log.d(TAG, "@@ vp up ");
            }

            mTouchState = TOUCH_STATE_REST;
            mActivePointerId = INVALID_POINTER;
            break;
        case MotionEvent.ACTION_CANCEL:
            mTouchState = TOUCH_STATE_REST;
            mActivePointerId = INVALID_POINTER;
            break;
        case MotionEvent.ACTION_POINTER_UP:
            onSecondaryPointerUp(ev);
            break;
        }

        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
            MotionEvent.ACTION_POINTER_INDEX_SHIFT;
            final int pointerId = ev.getPointerId(pointerIndex);
            if (pointerId == mActivePointerId) {
                final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                mLastMotionX = ev.getX(newPointerIndex);
                mLastMotionY = ev.getY(newPointerIndex);
                mActivePointerId = ev.getPointerId(newPointerIndex);
                if (mVelocityTracker != null) {
                    mVelocityTracker.clear();
                }
            }
    }

    public void snapToScreen(int whichScreen) {
        snapToScreen(whichScreen, 0, false);
    }

    public int getScreenCount() {
        return getChildCount();
    }

    protected void snapToScreen(int whichScreen, int velocity, boolean settle) {
        whichScreen = Math.max(0, Math.min(whichScreen, getScreenCount() - 1));

        clearVacantCache();
        enableChildrenCache(mCurrentScreen, whichScreen);

        mNextScreen = whichScreen;

//        mPreviousIndicator.setLevel(mNextScreen);
//        mNextIndicator.setLevel(mNextScreen);

        View focusedChild = getFocusedChild();
        if (focusedChild != null && whichScreen != mCurrentScreen && focusedChild == getChildAt(mCurrentScreen)) {
            focusedChild.clearFocus();
        }

        final int screenDelta = Math.max(1, Math.abs(whichScreen - mCurrentScreen));
        final int newX = whichScreen * (mScrollByChildWidth ? getWidth() / 2 : getWidth());
        final int delta = newX - getScrollX();
        int duration = (screenDelta + 1) * 100;

        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }

        if (settle) {
            mScrollInterpolator.setDistance(screenDelta);
        } else {
            mScrollInterpolator.disableSettle();
        }

        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration += (duration / (velocity / BASELINE_FLING_VELOCITY)) * FLING_VELOCITY_INFLUENCE;
        } else {
            duration += 100;
        }

        awakenScrollBars(duration);
        mScroller.startScroll(getScrollX(), 0, delta, 0, duration);
        invalidate();
    }

    public void snapToDestination() {
//        Log.d(TAG, "count child " + getChildCount());
        snapToScreen(getScreenCount() - 1, 0, false);
    }

    /**
     * Sets the {@link ViewSwitcher.OnScreenSwitchListener}.
     *
     * @param onScreenSwitchListener The listener for switch events.
     */
    public void setOnScreenSwitchListener(OnScreenSwitchListener onScreenSwitchListener) {
        mOnScreenSwitchListener = onScreenSwitchListener;
    }

    public boolean isScrollFinished() {
        return mScroller.isFinished();
    }

    public boolean allowLongPress() {
        return mAllowLongPress;
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // OnScreenSwitchListener
    //
    ////////////////////////////////////////////////////////////////////////////////////

    public static interface OnScreenSwitchListener {
        public void onScreenSwitched(int screen);
        public void onScreenScrollStart();
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // SavedState
    //
    ////////////////////////////////////////////////////////////////////////////////////

    public static class SavedState extends BaseSavedState {
        int currentScreen = -1;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentScreen = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(currentScreen);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final SavedState state = new SavedState(super.onSaveInstanceState());
        state.currentScreen = mCurrentScreen;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        if (savedState.currentScreen != -1) {
            mCurrentScreen = savedState.currentScreen;
            setCurrentScreen(mCurrentScreen);
        }
    }

    public void setScrollByChildWidth(boolean childWidth) {
        mScrollByChildWidth = childWidth;
    }

    public void setBeastSwipeMode(boolean beastMode) {
        mBeastMode = beastMode;
    }

    public void setEdgeEventMode(boolean edgeMode) {
        mEdgeEventMode = edgeMode;
    }

    public void onEdgeEvent(int mode) {
        Log.d(TAG, "@@ EDGE EVENT CALLED " + mode);
    }
}