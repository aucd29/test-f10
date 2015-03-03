/*
 * OnDoubleTapListener.java
 * Copyright 2015 OBIGO Inc. All rights reserved.
 *             http://www.obigo.com
 */
package com.obigo.f10.ui.events;

import android.view.View;


public interface OnCellDoubleTapListener {
    public static final int LEFT_TOP = 0;
    public static final int LEFT_BOTTOM = 1;
    public static final int RIGHT_TOP = 2;
    public static final int RIGHT_BOTTOM = 3;

    public void onDoubleTap(View view);
}
