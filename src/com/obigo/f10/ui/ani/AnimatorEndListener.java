/*
 * AnimationEndListener.java
 * Copyright 2015 OBIGO All right reserved.
 *             http://www.obigo.com
 */
package com.obigo.f10.ui.ani;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.view.View;

public class AnimatorEndListener implements AnimatorListener {
    private View mView;

    public AnimatorEndListener(View view) {
        mView = view;
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }

    @Override
    public void onAnimationStart(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (mView != null) {
            mView.setVisibility(View.INVISIBLE);
        }

        animation.removeListener(this);
    }
}
