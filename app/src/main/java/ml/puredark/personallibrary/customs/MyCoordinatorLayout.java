package ml.puredark.personallibrary.customs;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by PureDark on 2015/10/19.
 */
public class MyCoordinatorLayout extends CoordinatorLayout {
    private boolean allowForScroll = true;

    public MyCoordinatorLayout(Context context) {
        super(context);
    }

    public MyCoordinatorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return allowForScroll && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return allowForScroll && super.onStartNestedScroll(child, target, nestedScrollAxes);
    }


    public boolean isAllowForScroll() {
        return allowForScroll;
    }

    public void setAllowForScrool(boolean allowForScrool) {
        this.allowForScroll = allowForScrool;
    }
}