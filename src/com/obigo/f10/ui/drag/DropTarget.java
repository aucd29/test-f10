/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.obigo.f10.ui.drag;


import android.graphics.Rect;

/**
 * Interface defining an object that can receive a drag.
 *
 */
public interface DropTarget {

    /**
     * Handle an object being dropped on the DropTarget
     * 
     * @param source IDragSource where the drag started
     * @param x X coordinate of the drop location
     * @param y Y coordinate of the drop location
     * @param xOffset Horizontal offset with the object being dragged where the original touch happened
     * @param yOffset Vertical offset with the object being dragged where the original touch happened
     * @param dragInfo Data associated with the object being dragged
     * 
     */
    void onDrop(IDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo);
    
    void onDragEnter(IDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo);

    void onDragOver(IDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo);

    void onDragExit(IDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo);

    /**
     * Check if a drop action can occur at, or near, the requested location.
     * This may be called repeatedly during a drag, so any calls should return
     * quickly.
     * 
     * @param source IDragSource where the drag started
     * @param x X coordinate of the drop location
     * @param y Y coordinate of the drop location
     * @param xOffset Horizontal offset with the object being dragged where the
     *            original touch happened
     * @param yOffset Vertical offset with the object being dragged where the
     *            original touch happened
     * @param dragInfo Data associated with the object being dragged
     * @return True if the drop will be accepted, false otherwise.
     */
    boolean acceptDrop(IDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo);
}
