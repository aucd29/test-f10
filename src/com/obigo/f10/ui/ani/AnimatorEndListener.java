/*
 * AnimationEndListener.java
 * Copyright 2015 OBIGO All right reserverd.
 *             http://www.obigo.com
 */
package com.obigo.f10.ui.ani;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;

public abstract class AnimatorEndListener implements AnimatorListener {
    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }

    @Override
    public void onAnimationStart(Animator animation) {
    }
}
