package com.intellisoft.nacare.widgets;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.recyclerview.widget.RecyclerView;

public class CustomRecyclerView extends RecyclerView {

    private float mInitialTouchX;
    private float mInitialTouchY;

    public CustomRecyclerView(Context context) {
        super(context);
    }

    public CustomRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Record the initial touch position
                mInitialTouchX = event.getX();
                mInitialTouchY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                // Calculate the distance moved since the initial touch position
                float xDiff = Math.abs(event.getX() - mInitialTouchX);
                float yDiff = Math.abs(event.getY() - mInitialTouchY);
                if (xDiff > yDiff) {
                    // If the user is attempting to swipe horizontally, prevent the RecyclerView from scrolling
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }
}
