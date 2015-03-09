/*
 * MainActivity.java
 * Copyright 2015 OBIGO Inc. All rights reserved.
 *             http://www.obigo.com
 */
package com.obigo.f10;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.FrameLayout;

import com.obigo.f10.ui.ani.AnimatorEndListener;
import com.obigo.f10.ui.ani.TranslationHelper;
import com.obigo.f10.ui.drag.DragController;
import com.obigo.f10.ui.drag.DragLayer;

public class MainActivity extends FragmentActivity implements View.OnClickListener, OnLongClickListener {
    private static final String TAG = "MainActivity";

    private static final int APPLIST_MOVE_X = -400;
    private static final int SETTING_MOVE_Y = -200;
//    private static final int DELZONE_MOVE_Y = -40;

    private DragController mDragController;

    private DragLayer mDragLayer;
    private Workspace mWorkspace;
    private FrameLayout mAppList;
    private FrameLayout mSetting;
    private FrameLayout mExpandLayout;
    private DeleteZone mDeleteZone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDragController = new DragController(getApplicationContext());

        mDragLayer     = (DragLayer) findViewById(R.id.drag_layer);
        mWorkspace     = (Workspace) mDragLayer.findViewById(R.id.workspace);
        mAppList       = (FrameLayout) mDragLayer.findViewById(R.id.applist);
        mSetting       = (FrameLayout) mDragLayer.findViewById(R.id.setting);
        mExpandLayout  = (FrameLayout) mDragLayer.findViewById(R.id.expand);
        mDeleteZone    = (DeleteZone) mDragLayer.findViewById(R.id.delete_zone);

        mDeleteZone.setMainActivity(this);
//        mDeleteZone.setDragController(mDragController);

        mWorkspace.setOnLongClickListener(this);
        mWorkspace.setMainActivity(this);

        mDragLayer.setDragController(mDragController);
        mWorkspace.setDragController(mDragController);
        mWorkspace.setMainActivity(this);

        mDragController.setDragScoller(mWorkspace);
//        mDragController.setDragListener(mDeleteZone);
        mDragController.setScrollView(mDragLayer);
        mDragController.setMoveTarget(mWorkspace);

        mDragController.addDropTarget(mWorkspace);
//        mDragController.addDropTarget(mDeleteZone);

//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        ActionBar actionBar = getActionBar();
//        actionBar.hide();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            if ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) {
                if (mWorkspace.isAnimating()) {
                    return ;
                }

                if (isWorkspaceLocked()) {
                    onBackPressed();
                } else if (mWorkspace.isFullScreenMode()) {
                    mWorkspace.resetScreenMode();
                } else {
                    mWorkspace.snapToScreen(0);
                }
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mWorkspace.isFullScreenMode()) {
            return false;
        }

        if (v instanceof CellLayout && mWorkspace.getChildAt(0).equals(v)) {
            return false;
        }

        Log.d(TAG, "@@ ACTIVITY LONG CLICK");
        if (v instanceof ObigoView && mWorkspace.allowLongPress()) {
            Log.d(TAG, "@@ OBIGO VIEW LONG CLICK");

            View view = (View) v.getParent();
            v.cancelLongPress();
            v.clearFocus();

            mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
            mWorkspace.startDrag(view);
        } else if (v instanceof CellLayout && mWorkspace.allowLongPress()) {
            Log.d(TAG, "@@ CELL LAYOUT LONG CLICK");

            mWorkspace.startDrag(v);
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "@@ onclick");
    }

    @Override
    public void onBackPressed() {
        if (mAppList.getVisibility() == View.VISIBLE) {
            hideAppList();
        } else if (mSetting.getVisibility() == View.VISIBLE) {
            hideSettingMenu();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // HIDE OBJECT ANIMATION
    //
    ////////////////////////////////////////////////////////////////////////////////////

    public boolean isWorkspaceLocked() {
        if (mAppList.getVisibility() == View.VISIBLE ||
            mSetting.getVisibility() == View.VISIBLE ||
            mDeleteZone.getVisibility() == View.VISIBLE) {
            return true;
        }

        return false;
    }

    public boolean isDeleteZone() {
        if (mDeleteZone.getVisibility() == View.VISIBLE) {
            return true;
        }

        return false;
    }

    public void showAppList() {
        if (mAppList.getVisibility() == View.INVISIBLE) {
            mAppList.setVisibility(View.VISIBLE);
            TranslationHelper.startX(mAppList, 0, null);
        }
    }

    public void hideAppList() {
        TranslationHelper.startX(mAppList, APPLIST_MOVE_X, new AnimatorEndListener(mAppList));
    }

    public void showSettingMenu() {
        if (mSetting.getVisibility() == View.INVISIBLE) {
            mSetting.setVisibility(View.VISIBLE);
            TranslationHelper.startY(mSetting, 0, null);
        }
    }

    public void hideSettingMenu() {
        TranslationHelper.startY(mSetting, SETTING_MOVE_Y, new AnimatorEndListener(mSetting));
    }

//    public void showDeleteZone() {
//        if (mDeleteZone.getVisibility() == View.INVISIBLE) {
//            mDeleteZone.setVisibility(View.VISIBLE);
//            TranslationHelper.startY(mDeleteZone, 0, null);
//        }
//    }
//
//    public void hideDeleteZone() {
//        TranslationHelper.startY(mDeleteZone, DELZONE_MOVE_Y, new AnimatorEndListener(mDeleteZone));
//    }

    public View getExpandLayout() {
        return mExpandLayout;
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/
}
