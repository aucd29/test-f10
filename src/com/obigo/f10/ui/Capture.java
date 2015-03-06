/*
 * Capture.java
 * Copyright 2015 OBIGO Inc. All rights reserved.
 *             http://www.obigo.com
 */
package com.obigo.f10.ui;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

public class Capture {
    private static final String TAG = "Capture";

    public static Bitmap get(View view) {
        view.clearFocus();
        view.setPressed(false);

        boolean willNotCache = view.willNotCacheDrawing();
        view.setWillNotCacheDrawing(false);

        view.invalidate();
        view.buildDrawingCache();

        int color = view.getDrawingCacheBackgroundColor();
        view.setDrawingCacheBackgroundColor(0);
        if (color != 0) {
            view.destroyDrawingCache();
        }

        Bitmap cachedBmp = view.getDrawingCache();
        if (cachedBmp == null) {
            Log.e(TAG, "Capture get", new RuntimeException());
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(cachedBmp);

        view.destroyDrawingCache();
        view.setWillNotCacheDrawing(willNotCache);
        view.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }
}
