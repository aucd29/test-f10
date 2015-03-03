/*
 * Capture.java
 * Copyright 2015 OBIGO Inc. All rights reserved.
 *             http://www.obigo.com
 */
package com.obigo.f10.ui;

import android.graphics.Bitmap;
import android.view.View;

public class Capture {
    public static Bitmap get(View view) {
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();

        return bmp;
    }
}
