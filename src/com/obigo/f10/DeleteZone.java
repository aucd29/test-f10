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

package com.obigo.f10;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.obigo.f10.ui.drag.DragLayer;
import com.obigo.f10.ui.drag.DropTarget;
import com.obigo.f10.ui.drag.IDragController;
import com.obigo.f10.ui.drag.IDragSource;

public class DeleteZone extends ImageView implements DropTarget, IDragController.DragListener {
    private static final int ORIENTATION_HORIZONTAL = 1;
    private static final int TRANSITION_DURATION = 250;
    private static final int ANIMATION_DURATION = 200;

    private final int[] mLocation = new int[2];

    private boolean mTrashMode;

    private AnimationSet mInAnimation;
    private AnimationSet mOutAnimation;
    private Animation mHandleInAnimation;
    private Animation mHandleOutAnimation;

    private int mOrientation;
    private DragLayer mDragLayer;

    private final RectF mRegion = new RectF();
    private TransitionDrawable mTransition;
    private View mHandle;
    private final Paint mTrashPaint = new Paint();

    public DeleteZone(Context context) {
        super(context);
    }

    public DeleteZone(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DeleteZone(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

//        IResource res = WebLauncherRuntime.getInstance().getIResource();
//        final int srcColor = context.getResources().getColor(res.colorDeleteColorFilterID());
//        mTrashPaint.setColorFilter(new PorterDuffColorFilter(srcColor, PorterDuff.Mode.SRC_ATOP));
//
//        TypedArray a = context.obtainStyledAttributes(attrs, res.styleableDeleteZoneID(), defStyle, 0);
//        mOrientation = a.getInt(res.styleableDeleteZoneDirectionID(), ORIENTATION_HORIZONTAL);
//        a.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTransition = (TransitionDrawable) getDrawable();
    }

    @Override
    public boolean acceptDrop(IDragSource source, int x, int y, int xOffset, int yOffset,
            Object dragInfo) {
        return true;
    }

    public Rect estimateDropLocation(IDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo, Rect recycle) {
        return null;
    }

    @Override
    public void onDrop(IDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
//        final ItemInfo item = (ItemInfo) dragInfo;
//
//        if (item.container == -1) {
//            return;
//        }
//
//        ILauncher lc = WebLauncherRuntime.getInstance().getILauncher();
//
//        if (item.container == lc.getLauncherSettingFavoritesInt(WebbyConstants.Favorites.CONTAINER_DESKTOP)) {
//            if (lc.isLauncherAppWidgetInfo(item)) {
//                lc.setLauncherModelRemoveDesktopAppWidget(item);
//            } else {
//                lc.setLauncherModelRemoveDesktopItem(item);
//            }
//        } else {
//            if (source instanceof UserFolder) {
//                lc.setLauncherModelRemoveUserFolderItem((UserFolder)source, item);
//            }
//        }
//
//        if (item instanceof UserFolderInfo) {
//            final UserFolderInfo userFolderInfo = (UserFolderInfo)item;
//            lc.setLauncherModelDeleteUserFolderContentsFromDatabase(userFolderInfo);
//        } else if (lc.isLauncherAppWidgetInfo(item)) {
//            lc.setLauncherAppWidgetInfoDeleteAppWidgetId(item);
//        }
//
//        lc.setLauncherModelDeleteItemFromDatabase(item);
    }

    @Override
    public void onDragEnter(IDragSource source, int x, int y, int xOffset, int yOffset,
            Object dragInfo) {
        mTransition.reverseTransition(TRANSITION_DURATION);
    }

    @Override
    public void onDragOver(IDragSource source, int x, int y, int xOffset, int yOffset,
            Object dragInfo) {
    }

    @Override
    public void onDragExit(IDragSource source, int x, int y, int xOffset, int yOffset,
            Object dragInfo) {
        mTransition.reverseTransition(TRANSITION_DURATION);
    }

    @Override
    public void onDragStart(View v, IDragSource source, Object info, int dragAction) {
//        final ItemInfo item = (ItemInfo) info;
//        if (item != null) {
//            mTrashMode = true;
//            createAnimations();
//            final int[] location = mLocation;
//            getLocationOnScreen(location);
//            mRegion.set(location[0], location[1], location[0] + mRight - mLeft,
//                    location[1] + mBottom - mTop);
//            mDragLayer.setDeleteRegion(mRegion);
//            mTransition.resetTransition();
//            startAnimation(mInAnimation);
//            mHandle.startAnimation(mHandleOutAnimation);
//            setVisibility(VISIBLE);
//        }
    }

    @Override
    public void onDragEnd() {
        if (mTrashMode) {
            mTrashMode = false;
            mDragLayer.setDeleteRegion(null);
            startAnimation(mOutAnimation);
            mHandle.startAnimation(mHandleInAnimation);
            setVisibility(GONE);
        }
    }

    private void createAnimations() {
        if (mInAnimation == null) {
            mInAnimation = new FastAnimationSet();
            final AnimationSet animationSet = mInAnimation;
            animationSet.setInterpolator(new AccelerateInterpolator());
            animationSet.addAnimation(new AlphaAnimation(0.0f, 1.0f));
            if (mOrientation == ORIENTATION_HORIZONTAL) {
                animationSet.addAnimation(new TranslateAnimation(Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f));
            } else {
                animationSet.addAnimation(new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                        1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f));
            }
            animationSet.setDuration(ANIMATION_DURATION);
        }
        if (mHandleInAnimation == null) {
            mHandleInAnimation = new AlphaAnimation(0.0f, 1.0f);
            mHandleInAnimation.setDuration(ANIMATION_DURATION);
        }
        if (mOutAnimation == null) {
            mOutAnimation = new FastAnimationSet();
            final AnimationSet animationSet = mOutAnimation;
            animationSet.setInterpolator(new AccelerateInterpolator());
            animationSet.addAnimation(new AlphaAnimation(1.0f, 0.0f));
            if (mOrientation == ORIENTATION_HORIZONTAL) {
                animationSet.addAnimation(new FastTranslateAnimation(Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 1.0f));
            } else {
                animationSet.addAnimation(new FastTranslateAnimation(Animation.RELATIVE_TO_SELF,
                        0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f));
            }
            animationSet.setDuration(ANIMATION_DURATION);
        }
        if (mHandleOutAnimation == null) {
            mHandleOutAnimation = new AlphaAnimation(1.0f, 0.0f);
            mHandleOutAnimation.setFillAfter(true);
            mHandleOutAnimation.setDuration(ANIMATION_DURATION);
        }
    }

    public void setDragController(DragLayer dragLayer) {
        mDragLayer = dragLayer;
    }

    public void setHandle(View view) {
        mHandle = view;
    }

    private static class FastTranslateAnimation extends TranslateAnimation {
        public FastTranslateAnimation(int fromXType, float fromXValue, int toXType, float toXValue,
                int fromYType, float fromYValue, int toYType, float toYValue) {
            super(fromXType, fromXValue, toXType, toXValue,
                    fromYType, fromYValue, toYType, toYValue);
        }

        @Override
        public boolean willChangeTransformationMatrix() {
            return true;
        }

        @Override
        public boolean willChangeBounds() {
            return false;
        }
    }

    private static class FastAnimationSet extends AnimationSet {
        FastAnimationSet() {
            super(false);
        }

        @Override
        public boolean willChangeTransformationMatrix() {
            return true;
        }

        @Override
        public boolean willChangeBounds() {
            return false;
        }
    }
}
