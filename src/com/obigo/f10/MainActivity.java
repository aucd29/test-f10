/*
 * ObigoView.java
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
import com.obigo.f10.ui.ani.Translation;
import com.obigo.f10.ui.drag.DragLayer;

public class MainActivity extends FragmentActivity implements View.OnClickListener, OnLongClickListener{
    private static final String TAG = "MainActivity";

    private static final int APPLIST_MOVE_X = 400;
    private static final int SETTING_MOVE_Y = 200;
    private static final int DELZONE_MOVE_Y = 40;

    private DragLayer mDragLayer;
    private Workspace mWorkspace;
    private DeleteZone mDeleteZone;
    private FrameLayout mAppList;
    private FrameLayout mSetting;
    private FrameLayout mExpandLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDragLayer    = (DragLayer) findViewById(R.id.drag_layer);
        mWorkspace    = (Workspace) mDragLayer.findViewById(R.id.workspace);
        mDeleteZone   = (DeleteZone) mDragLayer.findViewById(R.id.delete_zone);
        mAppList      = (FrameLayout) mDragLayer.findViewById(R.id.applist);
        mSetting      = (FrameLayout) mDragLayer.findViewById(R.id.setting);
        mExpandLayout = (FrameLayout) mDragLayer.findViewById(R.id.expand);

        mWorkspace.setDragger(mDragLayer);
        mWorkspace.setMainActivity(this);
        mWorkspace.setOnLongClickListener(this);

        mDeleteZone.setDragController(mDragLayer);
        mDragLayer.setDragScoller(mWorkspace);
        mDragLayer.setDragListener(mDeleteZone);
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
        Log.d(TAG, "on long click");

        if (v instanceof CellLayout) {
            Log.d(TAG, "long click cell layout");
            showDeleteZone();
        } else {
            switch (v.getId()) {
            case R.id.workspace:
                Log.d(TAG, "workspace long click");
                break;
            }
        }

        return false;
    }

    @Override
    public void onClick(View v) {
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
            Translation.startX(mAppList, APPLIST_MOVE_X, null);
        }
    }

    public void hideAppList() {
        Translation.startX(mAppList, APPLIST_MOVE_X * -1, new AnimatorEndListener(mAppList));
    }

    public void showSettingMenu() {
        if (mSetting.getVisibility() == View.INVISIBLE) {
            mSetting.setVisibility(View.VISIBLE);
            Translation.startY(mSetting, SETTING_MOVE_Y, null);
        }
    }

    public void hideSettingMenu() {
        Translation.startY(mSetting, SETTING_MOVE_Y * -1, new AnimatorEndListener(mSetting));
    }

    public void showDeleteZone() {
        if (mDeleteZone.getVisibility() == View.INVISIBLE) {
            mDeleteZone.performHapticFeedback( HapticFeedbackConstants.LONG_PRESS );

            mDeleteZone.setVisibility(View.VISIBLE);
            Translation.startY(mDeleteZone, DELZONE_MOVE_Y, null);
        }
    }

    public void hideDeleteZone() {
        Translation.startY(mDeleteZone, DELZONE_MOVE_Y * -1, new AnimatorEndListener(mDeleteZone));
    }

    public View getExpandLayout() {
        return mExpandLayout;
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/
}
