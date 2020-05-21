package org.openmrs.mobile.utilities;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class SwipeDisabledViewPager extends ViewPager {
    public SwipeDisabledViewPager(Context context) {
        super(context);
    }

    public SwipeDisabledViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // returning false will not propagate the swipe event
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }
}