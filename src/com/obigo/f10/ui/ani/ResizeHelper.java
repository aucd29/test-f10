/*
 * Resize.java
 * Copyright 2015 OBIGO Inc. All rights reserved.
 *             http://www.obigo.com
 */
package com.obigo.f10.ui.ani;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.obigo.f10.ui.events.OnCellDoubleTapListener;

public class ResizeHelper {
    private static final String TAG = "Resize";
    private static final int ANI_DURATION = 300;

    public static void expand(final View view, int screen, int tabPos, int toWidth, int toHeight, AnimatorEndListener l) {
        Interpolator ip = new DecelerateInterpolator();
        ObjectAnimator aniX = ObjectAnimator.ofFloat(view, "scaleX", 2f);
        aniX.setInterpolator(ip);
        aniX.setDuration(ANI_DURATION);
        aniX.addListener(l);
        aniX.start();

        // check current screen
        if (screen != 0) {
            ObjectAnimator aniY = ObjectAnimator.ofFloat(view, "scaleY", 2f);
            aniY.setDuration(ANI_DURATION);
            aniY.setInterpolator(ip);
            aniY.start();
        }

        int transX = toWidth / 4, transY = toHeight / 4;
        if (tabPos == OnCellDoubleTapListener.LEFT_TOP) {

        } else if (tabPos == OnCellDoubleTapListener.LEFT_BOTTOM) {
            transY *= -1;
        } else if (tabPos == OnCellDoubleTapListener.RIGHT_TOP) {
            transX *= -1;
        } else {
            transX *= -1;
            transY *= -1;
        }

        ObjectAnimator aniTx = ObjectAnimator.ofFloat(view,  "translationX", transX);
        aniTx.setInterpolator(ip);
        aniTx.setDuration(ANI_DURATION);
        aniTx.start();

        if (screen != 0) {
            ObjectAnimator aniTy = ObjectAnimator.ofFloat(view,  "translationY", transY);
            aniTy.setInterpolator(ip);
            aniTy.setDuration(ANI_DURATION);
            aniTy.start();
        }
    }

    public static void collapse(View view, int screen, int tabPos, int toWidth, int toHeight, AnimatorEndListener l) {
        Interpolator ip = new DecelerateInterpolator();
        ObjectAnimator aniX = ObjectAnimator.ofFloat(view, "scaleX", 0.5f);
        aniX.setInterpolator(ip);
        aniX.setDuration(ANI_DURATION);
        aniX.addListener(l);
        aniX.start();

        // check current screen
        if (screen != 0) {
            ObjectAnimator aniY = ObjectAnimator.ofFloat(view, "scaleY", 0.5f);
            aniY.setDuration(ANI_DURATION);
            aniY.setInterpolator(ip);
            aniY.start();
        }

        int transX = toWidth / 4, transY = toHeight / 4;
        if (tabPos == OnCellDoubleTapListener.LEFT_TOP) {
            transX *= -1;
            transY *= -1;
        } else if (tabPos == OnCellDoubleTapListener.LEFT_BOTTOM) {
            transX *= -1;
        } else if (tabPos == OnCellDoubleTapListener.RIGHT_TOP) {
            transY *= -1;
        }

        ObjectAnimator aniTx = ObjectAnimator.ofFloat(view,  "translationX", transX);
        aniTx.setInterpolator(ip);
        aniTx.setDuration(ANI_DURATION);
        aniTx.start();

        if (screen != 0) {
            ObjectAnimator aniTy = ObjectAnimator.ofFloat(view,  "translationY", transY);
            aniTy.setInterpolator(ip);
            aniTy.setDuration(ANI_DURATION);
            aniTy.start();
        }
    }
}
