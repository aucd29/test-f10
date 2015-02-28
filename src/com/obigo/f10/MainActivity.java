package com.obigo.f10;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnLongClickListener;

import com.obigo.f10.ui.drag.DragLayer;

public class MainActivity extends FragmentActivity implements View.OnClickListener, OnLongClickListener{
    private static final String TAG = "MainActivity";

    private DragLayer mDragLayer;
    private Workspace mWorkspace;
    private DeleteZone mDeleteZone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDragLayer  = (DragLayer) findViewById(R.id.drag_layer);
        mWorkspace  = (Workspace) mDragLayer.findViewById(R.id.workspace);
        mDeleteZone = (DeleteZone) mDragLayer.findViewById(R.id.delete_zone);

//        mWorkspace.setDragger(mDragLayer);
//        mWorkspace.setMainActivity(this);
//        mWorkspace.setOnLongClickListener(this);
//
//        mDeleteZone.setDragController(mDragLayer);
//        mDragLayer.setDragScoller(mWorkspace);
//        mDragLayer.setDragListener(mDeleteZone);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            if ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) {
                mWorkspace.snapToScreen(0);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_HOME:
            Log.d(TAG, "@@ home key");
            break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    @Override
    public void onClick(View v) {
    }

    public boolean isWorkspaceLocked() {
        return false;
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/
}
