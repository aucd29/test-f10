/*
 * Resize.java
 * Copyright 2015 OBIGO Inc. All rights reserved.
 *             http://www.obigo.com
 */
package com.obigo.f10.ui.ani;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class ResizeHelper {
    private static final String TAG = "Resize";
    private static final int ANI_DURATION = 400;

    public static void expand(final View view, int screen, AnimatorEndListener l) {
        scale(view, screen, 2f, l);
    }

    public static void collapse(View view, int screen, AnimatorEndListener l) {
        scale(view, screen, 0.5f, l);
    }

    private static void scale(View view, int screen, float scaleValue, AnimatorEndListener l) {
        AnimatorSet aniset = new AnimatorSet();
        aniset.setInterpolator(new DecelerateInterpolator());
        aniset.setDuration(ANI_DURATION);

        ObjectAnimator aniX = ObjectAnimator.ofFloat(view, "scaleX", scaleValue);
        aniX.addListener(l);

        if (screen == 0) {
            aniset.play(aniX);
        } else {
            aniset.playTogether(aniX, ObjectAnimator.ofFloat(view, "scaleY", scaleValue));
        }

        aniset.start();
    }
}
