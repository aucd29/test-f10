package com.obigo.f10;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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

//        mDragLayer  = (DragLayer) findViewById(R.id.drag_layer);
//        mWorkspace  = (Workspace) mDragLayer.findViewById(R.id.workspace);
//        mDeleteZone = (DeleteZone) mDragLayer.findViewById(R.id.delete_zone);

//        mWorkspace.setDragger(mDragLayer);
//        mWorkspace.setMainActivity(this);
//        mWorkspace.setOnLongClickListener(this);
//
//        mDeleteZone.setDragController(mDragLayer);
//        mDragLayer.setDragScoller(mWorkspace);
//        mDragLayer.setDragListener(mDeleteZone);
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
