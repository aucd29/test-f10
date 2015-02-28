/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.obigo.f10.ui.drag;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;


/**
 * A ViewGroup that coordinated dragging across its dscendants
 */
public class DragLayer extends FrameLayout implements IDragController {
    private static final int SCROLL_DELAY = 600;
    private static final int SCROLL_ZONE = 20;
    private static final int VIBRATE_DURATION = 35;
    private static final int ANIMATION_SCALE_UP_DURATION = 110;

    private static final boolean PROFILE_DRAWING_DURING_DRAG = false;

    // Number of pixels to add to the dragged item for scaling
    private static final float DRAG_SCALE = 24.0f;

    private boolean mDragging = false;
    private boolean mShouldDrop;
    private float mLastMotionX;
    private float mLastMotionY;

    /**
     * The bitmap that is currently being dragged
     */
    private Bitmap mDragBitmap = null;
    private View mOriginator;

    private int mBitmapOffsetX;
    private int mBitmapOffsetY;

    /**
     * X offset from where we touched on the cell to its upper-left corner
     */
    private float mTouchOffsetX;

    /**
     * Y offset from where we touched on the cell to its upper-left corner
     */
    private float mTouchOffsetY;

    /**
     * Utility rectangle
     */
    private Rect mDragRect = new Rect();

    /**
     * Where the drag originated
     */
    private IDragSource mDragSource;

    /**
     * The data associated with the object being dragged
     */
    private Object mDragInfo;

    private final Rect mRect = new Rect();
    private final int[] mDropCoordinates = new int[2];

    private final Vibrator mVibrator;

    private DragListener mListener;

    private DragScroller mDragScroller;

    private static final int SCROLL_OUTSIDE_ZONE = 0;
    private static final int SCROLL_WAITING_IN_ZONE = 1;

    private static final int SCROLL_LEFT = 0;
    private static final int SCROLL_RIGHT = 1;

    // WebLauncher
    private static final int SCROLL_UP = 2;
    private static final int SCROLL_DOWN = 3;

    private int mScrollState = SCROLL_OUTSIDE_ZONE;

    private ScrollRunnable mScrollRunnable = new ScrollRunnable();
    private View mIgnoredDropTarget;

    private RectF mDragRegion;
    private boolean mEnteredRegion;
    private DropTarget mLastDropTarget;

    private final Paint mTrashPaint = new Paint();
    private Paint mDragPaint;

    private static final int ANIMATION_STATE_STARTING = 1;
    private static final int ANIMATION_STATE_RUNNING = 2;
    private static final int ANIMATION_STATE_DONE = 3;

    private static final int ANIMATION_TYPE_SCALE = 1;

    private float mAnimationFrom;
    private float mAnimationTo;
    private int mAnimationDuration;
    private long mAnimationStartTime;
    private int mAnimationType;
    private int mAnimationState = ANIMATION_STATE_DONE;

    private InputMethodManager mInputMethodManager;

//public DragLayer(Context context) {
//    super(context);
//
//    mVibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
//}
//
//public DragLayer(Context context, AttributeSet attrs, int defStyle) {
//    super(context, attrs, defStyle);
//
//    mVibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
//}
    /**
     * Used to create a new DragLayer from XML.
     *
     * @param context The application's context.
     * @param attrs The attribtues set containing the Workspace's customization values.
     */
    public DragLayer(Context context, AttributeSet attrs) {
        super(context, attrs);

        // FIX, FOR ANDROID 4.1, Burke
        mVibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);

//        IResource res = WebLauncherRuntime.getInstance().getIResource();
//        final int srcColor = context.getResources().getColor(res.colorDeleteColorFilterID());
//        mTrashPaint.setColorFilter(new PorterDuffColorFilter(srcColor, PorterDuff.Mode.SRC_ATOP));
//
//        // Make estimated paint area in gray
//        int snagColor = context.getResources().getColor(res.colorSnagCalloutColorID());
//        Paint estimatedPaint = new Paint();
//        estimatedPaint.setColor(snagColor);
//        estimatedPaint.setStrokeWidth(3);
//        estimatedPaint.setAntiAlias(true);
    }

    @Override
    public void startDrag(View v, IDragSource source, Object dragInfo, int dragAction) {
        if (PROFILE_DRAWING_DURING_DRAG) {
            android.os.Debug.startMethodTracing("Launcher");
        }

        // Hide soft keyboard, if visible
        if (mInputMethodManager == null) {
            mInputMethodManager = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        mInputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);

        if (mListener != null) {
            mListener.onDragStart(v, source, dragInfo, dragAction);
        }

        Rect r = mDragRect;
        r.set(v.getScrollX(), v.getScrollY(), 0, 0);

        offsetDescendantRectToMyCoords(v, r);
        mTouchOffsetX = mLastMotionX - r.left;
        mTouchOffsetY = mLastMotionY - r.top;

        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap viewBitmap = v.getDrawingCache();
        int width = viewBitmap.getWidth();
        int height = viewBitmap.getHeight();

        Matrix scale = new Matrix();
        float scaleFactor = v.getWidth();
        scaleFactor = (scaleFactor + DRAG_SCALE) /scaleFactor;
        scale.setScale(scaleFactor, scaleFactor);

        mAnimationTo = 1.0f;
        mAnimationFrom = 1.0f / scaleFactor;
        mAnimationDuration = ANIMATION_SCALE_UP_DURATION;
        mAnimationState = ANIMATION_STATE_STARTING;
        mAnimationType = ANIMATION_TYPE_SCALE;

        mDragBitmap = Bitmap.createBitmap(viewBitmap, 0, 0, width, height, scale, true);
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        final Bitmap dragBitmap = mDragBitmap;
        mBitmapOffsetX = (dragBitmap.getWidth() - width) / 2;
        mBitmapOffsetY = (dragBitmap.getHeight() - height) / 2;

        if (dragAction == DRAG_ACTION_MOVE) {
            v.setVisibility(GONE);
        }

        mDragPaint = null;
        mDragging = true;
        mShouldDrop = true;
        mOriginator = v;
        mDragSource = source;
        mDragInfo = dragInfo;

        mVibrator.vibrate(VIBRATE_DURATION);

        mEnteredRegion = false;

        invalidate();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mDragging || super.dispatchKeyEvent(event);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
//        if (WebbyConstants.CIRCULAR_LOOP) {
//            drawAlphaBackground(canvas);
//        }

        super.dispatchDraw(canvas);

        if (mDragging && mDragBitmap != null) {
            if (mAnimationState == ANIMATION_STATE_STARTING) {
                mAnimationStartTime = SystemClock.uptimeMillis();
                mAnimationState = ANIMATION_STATE_RUNNING;
            }

            if (mAnimationState == ANIMATION_STATE_RUNNING) {
                float normalized = (float) (SystemClock.uptimeMillis() - mAnimationStartTime) /
                        mAnimationDuration;
                if (normalized >= 1.0f) {
                    mAnimationState = ANIMATION_STATE_DONE;
                }
                normalized = Math.min(normalized, 1.0f);
                final float value = mAnimationFrom  + (mAnimationTo - mAnimationFrom) * normalized;

                switch (mAnimationType) {
                case ANIMATION_TYPE_SCALE:
                    final Bitmap dragBitmap = mDragBitmap;
                    canvas.save();
//                    canvas.translate(mScrollX + mLastMotionX - mTouchOffsetX - mBitmapOffsetX,
//                            mScrollY + mLastMotionY - mTouchOffsetY - mBitmapOffsetY);

                      canvas.translate(getScrollX() + mLastMotionX - mTouchOffsetX - mBitmapOffsetX,
                              getScrollY() + mLastMotionY - mTouchOffsetY - mBitmapOffsetY);


                    canvas.translate((dragBitmap.getWidth() * (1.0f - value)) / 2,
                            (dragBitmap.getHeight() * (1.0f - value)) / 2);
                    canvas.scale(value, value);
                    canvas.drawBitmap(dragBitmap, 0.0f, 0.0f, mDragPaint);
                    canvas.restore();
                    break;
                }
            } else {
                // Draw actual icon being dragged
                canvas.drawBitmap(mDragBitmap,
                        getScrollX() + mLastMotionX - mTouchOffsetX - mBitmapOffsetX,
                        getScrollY() + mLastMotionY - mTouchOffsetY - mBitmapOffsetY, mDragPaint);
            }
        }
    }

    private void endDrag() {
        if (mDragging) {
            mDragging = false;
            if (mDragBitmap != null) {
                mDragBitmap.recycle();
            }
            if (mOriginator != null) {
                mOriginator.setVisibility(VISIBLE);
            }
            if (mListener != null) {
                mListener.onDragEnd();
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();

        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
        case MotionEvent.ACTION_MOVE:
            break;

        case MotionEvent.ACTION_DOWN:
            // Remember location of down touch
            mLastMotionX = x;
            mLastMotionY = y;
            mLastDropTarget = null;
            break;

        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            if (mShouldDrop && drop(x, y)) {
                mShouldDrop = false;
            }
            endDrag();
            break;
        }

        return mDragging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mDragging) {
            return false;
        }

        final int action = ev.getAction();
        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
        case MotionEvent.ACTION_DOWN:

            // Remember where the motion event started
            mLastMotionX = x;
            mLastMotionY = y;

            if ((x < SCROLL_ZONE) || (x > getWidth() - SCROLL_ZONE)) {
                mScrollState = SCROLL_WAITING_IN_ZONE;
                postDelayed(mScrollRunnable, SCROLL_DELAY);
            } else {
                mScrollState = SCROLL_OUTSIDE_ZONE;
            }

            break;
        case MotionEvent.ACTION_MOVE:
            final int scrollX = getScrollX();
            final int scrollY = getScrollY();

            final float touchX = mTouchOffsetX;
            final float touchY = mTouchOffsetY;

            final int offsetX = mBitmapOffsetX;
            final int offsetY = mBitmapOffsetY;

            int left = (int) (scrollX + mLastMotionX - touchX - offsetX);
            int top = (int) (scrollY + mLastMotionY - touchY - offsetY);

            final Bitmap dragBitmap = mDragBitmap;
            final int width = dragBitmap.getWidth();
            final int height = dragBitmap.getHeight();

            final Rect rect = mRect;
            rect.set(left - 1, top - 1, left + width + 1, top + height + 1);

            mLastMotionX = x;
            mLastMotionY = y;

            left = (int) (scrollX + x - touchX - offsetX);
            top = (int) (scrollY + y - touchY - offsetY);

            // Invalidate current icon position
            rect.union(left - 1, top - 1, left + width + 1, top + height + 1);

            final int[] coordinates = mDropCoordinates;
            DropTarget dropTarget = findDropTarget((int) x, (int) y, coordinates);
            if (dropTarget != null) {
                if (mLastDropTarget == dropTarget) {
                    dropTarget.onDragOver(mDragSource, coordinates[0], coordinates[1],
                            (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
                } else {
                    if (mLastDropTarget != null) {
                        mLastDropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1],
                                (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
                    }
                    dropTarget.onDragEnter(mDragSource, coordinates[0], coordinates[1],
                            (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
                }
            } else {
                if (mLastDropTarget != null) {
                    mLastDropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1],
                            (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
                }
            }

            invalidate(rect);

            mLastDropTarget = dropTarget;

            boolean inDragRegion = false;
            if (mDragRegion != null) {
                final RectF region = mDragRegion;
                final boolean inRegion = region.contains(ev.getRawX(), ev.getRawY());
                if (!mEnteredRegion && inRegion) {
                    mDragPaint = mTrashPaint;
                    mEnteredRegion = true;
                    inDragRegion = true;
                } else if (mEnteredRegion && !inRegion) {
                    mDragPaint = null;
                    mEnteredRegion = false;
                }
            }

            if (!inDragRegion && x < SCROLL_ZONE) {
                if (mScrollState == SCROLL_OUTSIDE_ZONE) {
                    mScrollState = SCROLL_WAITING_IN_ZONE;
                    mScrollRunnable.setDirection(SCROLL_LEFT);
                    postDelayed(mScrollRunnable, SCROLL_DELAY);
                }
            } else if (!inDragRegion && x > getWidth() - SCROLL_ZONE) {
                if (mScrollState == SCROLL_OUTSIDE_ZONE) {
                    mScrollState = SCROLL_WAITING_IN_ZONE;
                    mScrollRunnable.setDirection(SCROLL_RIGHT);
                    postDelayed(mScrollRunnable, SCROLL_DELAY);
                }
            }
            // WebLauncher
            // additions
            else if (!inDragRegion && y < SCROLL_ZONE) {
                if (mScrollState == SCROLL_OUTSIDE_ZONE) {
                    mScrollState = SCROLL_WAITING_IN_ZONE;
                    mScrollRunnable.setDirection(SCROLL_UP);
                    postDelayed(mScrollRunnable, SCROLL_DELAY);
                }
            }
            else if (!inDragRegion && y > getHeight() - SCROLL_ZONE) {
                if (mScrollState == SCROLL_OUTSIDE_ZONE) {
                    mScrollState = SCROLL_WAITING_IN_ZONE;
                    mScrollRunnable.setDirection(SCROLL_DOWN);
                    postDelayed(mScrollRunnable, SCROLL_DELAY);
                }
            }
            // WebLauncher(end)
            else {
                if (mScrollState == SCROLL_WAITING_IN_ZONE) {
                    mScrollState = SCROLL_OUTSIDE_ZONE;
                    mScrollRunnable.setDirection(SCROLL_RIGHT);
                    removeCallbacks(mScrollRunnable);
                }
            }

            break;
        case MotionEvent.ACTION_UP:
            removeCallbacks(mScrollRunnable);
            if (mShouldDrop) {
                drop(x, y);
                mShouldDrop = false;
            }
            endDrag();

            break;
        case MotionEvent.ACTION_CANCEL:
            endDrag();
        }

        return true;
    }

    private boolean drop(float x, float y) {
        invalidate();

        final int[] coordinates = mDropCoordinates;
        DropTarget dropTarget = findDropTarget((int) x, (int) y, coordinates);

        if (dropTarget != null) {
            dropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1],
                    (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
            if (dropTarget.acceptDrop(mDragSource, coordinates[0], coordinates[1],
                    (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo)) {
                dropTarget.onDrop(mDragSource, coordinates[0], coordinates[1],
                        (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
                mDragSource.onDropCompleted((View) dropTarget, true);
                return true;
            } else {
                mDragSource.onDropCompleted((View) dropTarget, false);
                return true;
            }
        }
        return false;
    }

    DropTarget findDropTarget(int x, int y, int[] dropCoordinates) {
        return findDropTarget(this, x, y, dropCoordinates);
    }

    private DropTarget findDropTarget(ViewGroup container, int x, int y, int[] dropCoordinates) {
        final Rect r = mDragRect;
        final int count = container.getChildCount();
        final int scrolledX = x + container.getScrollX();
        final int scrolledY = y + container.getScrollY();
        final View ignoredDropTarget = mIgnoredDropTarget;

        for (int i = count - 1; i >= 0; i--) {
            final View child = container.getChildAt(i);
            if (child.getVisibility() == VISIBLE && child != ignoredDropTarget) {
                child.getHitRect(r);
                if (r.contains(scrolledX, scrolledY)) {
                    DropTarget target = null;
                    if (child instanceof ViewGroup) {
                        x = scrolledX - child.getLeft();
                        y = scrolledY - child.getTop();
                        target = findDropTarget((ViewGroup) child, x, y, dropCoordinates);
                    }
                    if (target == null) {
                        if (child instanceof DropTarget) {
                            // Only consider this child if they will accept
                            DropTarget childTarget = (DropTarget) child;
                            if (childTarget.acceptDrop(mDragSource, x, y, 0, 0, mDragInfo)) {
                                dropCoordinates[0] = x;
                                dropCoordinates[1] = y;
                                return (DropTarget) child;
                            } else {
                                return null;
                            }
                        }
                    } else {
                        return target;
                    }
                }
            }
        }

        return null;
    }

    public void setDragScoller(DragScroller scroller) {
        mDragScroller = scroller;
    }

    public void setDragListener(DragListener l) {
        mListener = l;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void removeDragListener(DragListener l) {
        mListener = null;
    }

    /**
     * Specifies the view that must be ignored when looking for a drop target.
     *
     * @param view The view that will not be taken into account while looking
     *        for a drop target.
     */
    void setIgnoredDropTarget(View view) {
        mIgnoredDropTarget = view;
    }

    /**
     * Specifies the delete region.
     *
     * @param region The rectangle in screen coordinates of the delete region.
     */
    public void setDeleteRegion(RectF region) {
        mDragRegion = region;
    }

    private class ScrollRunnable implements Runnable {
        private int mDirection;

        ScrollRunnable() {
        }

        @Override
        public void run() {
            if (mDragScroller != null) {
                if (mDirection == SCROLL_LEFT) {
                    mDragScroller.scrollLeft();
                } else if (mDirection == SCROLL_RIGHT) {
                    mDragScroller.scrollRight();
                }

                // WebLauncher
                // additions
//                else if (mDirection == SCROLL_UP) {
//                    mDragScroller.scrollUp();
//                } else if (mDirection == SCROLL_DOWN) {
//                    mDragScroller.scrollDown();
//                }
                // WebLauncher(end)

                mScrollState = SCROLL_OUTSIDE_ZONE;
            }
        }

        void setDirection(int direction) {
            mDirection = direction;
        }
    }

    /**
     * WebLauncher
     * additional methods for Launcher2 style
     * @author john
     */

    /**
     * Starts a drag.
     *
     * @param b The bitmap to display as the drag image.  It will be re-scaled to the
     *          enlarged size.
     * @param screenX The x position on screen of the left-top of the bitmap.
     * @param screenY The y position on screen of the left-top of the bitmap.
     * @param textureLeft The left edge of the region inside b to use.
     * @param textureTop The top edge of the region inside b to use.
     * @param textureWidth The width of the region inside b to use.
     * @param textureHeight The height of the region inside b to use.
     * @param source An object representing where the drag originated
     * @param dragInfo The data associated with the object that is being dragged
     * @param dragAction The drag action: either {@link #DRAG_ACTION_MOVE} or
     *        {@link #DRAG_ACTION_COPY}
     */
    @Override
    public void startDrag(Bitmap b, int screenX, int screenY,
            int textureLeft, int textureTop, int textureWidth, int textureHeight,
            IDragSource source, Object dragInfo, int dragAction) {
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // circular loop
    //
    ////////////////////////////////////////////////////////////////////////////////////

    // circular loop 형태로 cell 을 진행시 wallpaper 도 동일하게 circular 형태로
    // 되어야 하므로 이를 지원하기 위해서 dispatchDraw 에서 bitmap 을 그리도록 한다.
    // 가장 처음 cell 과 마지막 cell 을 겹친 후 마지막 cell 을 fade 시키도록 한다.


//    private Launcher launcher = null;
    private boolean previewArea = false;

    public static int alpha = 255;
    private static boolean showBackground = false;
    private static final String TAG = "DragLayer";


//    private void drawAlphaBackground(Canvas canvas) {
//        if (launcher == null) {
//            return ;
//        }
//
//        if (previewArea) {
//            // wallpaper 의 전체 크기는 960 이다.
//            // 한 cell 의 이동 값은 480 / cell count 값이다.
//            Drawable wp = WallpaperManager.getInstance(mContext).getDrawable();
//            final int count = launcher.getWorkspace().getChildCount() - 1;
//            final int top = getHeight() - launcher.getDefaultHeight();
//            final int curr = launcher.getWorkspace().getCurrentScreen();
//            final int offset = (wp.getIntrinsicWidth() - getWidth()) / count * curr;
//
//            wp.setAlpha(255);
//            wp.setBounds(-offset, top, wp.getIntrinsicWidth() - offset, getHeight());
//            wp.draw(canvas);
//
//            previewArea = false;
//        } else {
//            if (!showBackground) {
//                return ;
//            }
//
//            Drawable wp = WallpaperManager.getInstance(mContext).getDrawable();
//            final int top = getHeight() - launcher.getDefaultHeight();
//
//            wp.setBounds(0, top, wp.getIntrinsicWidth(), getHeight());
//            wp.setAlpha(255);
//            wp.draw(canvas);
//
//            //Log.d(TAG, "alpha " + alpha);
//
//            wp.setBounds(-wp.getIntrinsicWidth() + getWidth(), top, getWidth(), getHeight());
//            wp.setAlpha(alpha);
//            wp.draw(canvas);
//        }
//    }

//    public void setLauncher(Launcher l) {
//        launcher = l;
//    }

    public static void setAlpha(float x, final float width) {
        if (x == 0) {
            return ;
        }

        boolean right = false;
        if (x < 0) {
            x *= -1f;
            right = true;
        }

        float percent = x / width * 100f;
        float alp = percent * 255 / 100f;

        if (right) {
            alpha = (int)alp;
        } else {
            alpha = 255 - (int)alp;
        }
    }

    public static boolean getBackgoundValue() {
        return showBackground;
    }

    public static synchronized void showBackground(boolean res) {
        showBackground = res;

//        if (Option.DEBUG_WEBBY_DRAGLAYER) {
//            if (showBackground) {
//                Log.d(TAG, "showBackground " + showBackground);
//            } else {
//                Log.d(TAG, "showBackground " + showBackground);
//            }
//        }

        if (!res) {
            alpha = 255;
        }
    }

    public void setPreviewAreaBackground() {
        previewArea = true;
    }
}


