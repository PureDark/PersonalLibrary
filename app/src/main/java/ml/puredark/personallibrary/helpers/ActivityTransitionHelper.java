package ml.puredark.personallibrary.helpers;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.bartoszlipinski.viewpropertyobjectanimator.ViewPropertyObjectAnimator;
import com.transitionseverywhere.utils.ViewGroupOverlayUtils;

/**
 * Use Overlay to support animation that lets view moves out of its parent
 * @author PureDark
 * @createDate 2015/10/22
 */
public class ActivityTransitionHelper {
    private final static String ATH_PREFIX = "ATH_";
    private Activity activity;
    private View fromView;
    private Intent fromIntent;              //intent from pre activity
    private View toView;                    //target view show in this activity
    private ViewGroup background;           //root view of this activity
    private ViewGroup animationView;        //animation view of this activity
    private int leftDelta;
    private int topDelta;
    private float widthDelta;
    private float heightDelta;
    private CustomAnimator customAnimator;

    public static abstract class CustomAnimator{
        protected static final AccelerateInterpolator ACCELERATE = new AccelerateInterpolator();
        protected static final AccelerateDecelerateInterpolator ACCELERATE_DECELERATE = new AccelerateDecelerateInterpolator();
        protected static final DecelerateInterpolator DECELERATE = new DecelerateInterpolator();
        protected static final LinearInterpolator LINEAR = new LinearInterpolator();
        public static final int ANIM_DURATION_LONG = 500;
        public static final int ANIM_DURATION_MEDIUM = 400;
        public static final int ANIM_DURATION_SHORT = 300;
        public CustomAnimatorListener callBack;
        public CustomAnimator(){};
        public abstract void start();
        public abstract void reverse();
        public void start(CustomAnimatorListener callBack){
            this.callBack = callBack;
            start();
        };
        public void reverse(CustomAnimatorListener callBack){
            this.callBack = callBack;
            reverse();
        };
    }
    public interface CustomAnimatorListener{
        void onAnimationEnd();
    }

    public ActivityTransitionHelper(Activity activity) {
        this.activity = activity;
    }

    public static ActivityTransitionHelper with(Activity activity) {
        return new ActivityTransitionHelper(activity);
    }

    public ActivityTransitionHelper intent(Intent intent) {
        this.fromIntent = intent;
        return this;
    }

    /**
     * Add from view
     * @param fromView
     * @return
     */
    public ActivityTransitionHelper fromView(View fromView) {
        this.fromView = fromView;
        return this;
    }

    public void startActivity(Intent intent) {
        int[] screenLocation = new int[2];
        fromView.getLocationOnScreen(screenLocation);
        intent.putExtra(ATH_PREFIX + "left", screenLocation[0]).
                putExtra(ATH_PREFIX + "top", screenLocation[1]).
                putExtra(ATH_PREFIX + "width", fromView.getMeasuredWidth()).
                putExtra(ATH_PREFIX + "height", fromView.getMeasuredHeight());
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }
    public void startActivityForResult(Intent intent, int requestCode) {
        int[] screenLocation = new int[2];
        fromView.getLocationOnScreen(screenLocation);
        intent.putExtra(ATH_PREFIX + "left", screenLocation[0]).
                putExtra(ATH_PREFIX + "top", screenLocation[1]).
                putExtra(ATH_PREFIX + "width", fromView.getMeasuredWidth()).
                putExtra(ATH_PREFIX + "height", fromView.getMeasuredHeight());
        activity.startActivityForResult(intent, requestCode);
        activity.overridePendingTransition(0, 0);

    }

    /**
     * Add target view
     * @param toView
     * @return
     */
    public ActivityTransitionHelper toView(View toView) {
        this.toView = toView;
        return this;
    }

    /**
     * Add root view of this layout
     * @param background
     * @return
     */
    public ActivityTransitionHelper background(ViewGroup background) {
        this.background = background;
        return this;
    }
    /**
     * Add animation view which the animation draws on
     * @param animationView
     * @return
     */
    public ActivityTransitionHelper animationView(ViewGroup animationView) {
        this.animationView = animationView;
        return this;
    }

    public ActivityTransitionHelper customAnimator(CustomAnimator customAnimator){
        this.customAnimator = customAnimator;
        return this;
    };

    /**
     * @param savedInstanceState
     * If savedInstanceState != null, it means that the activity is not newly created, needn't perform the animation
     */
    public ActivityTransitionHelper startTransition(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            final int thumbnailTop = fromIntent.getIntExtra(ATH_PREFIX + "top", 0);
            final int thumbnailLeft = fromIntent.getIntExtra(ATH_PREFIX + "left", 0);
            final int thumbnailWidth = fromIntent.getIntExtra(ATH_PREFIX + "width", 0);
            final int thumbnailHeight = fromIntent.getIntExtra(ATH_PREFIX + "height", 0);
            toView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    //remove default
                    toView.getViewTreeObserver().removeOnPreDrawListener(this);
                    int viewLocation[] = new int[2];
                    toView.getLocationOnScreen(viewLocation);
                    leftDelta = thumbnailLeft - viewLocation[0];
                    topDelta = thumbnailTop - viewLocation[1];
                    widthDelta =(float) thumbnailWidth / toView.getWidth();
                    heightDelta =(float)  thumbnailHeight / toView.getHeight();

                    runEnterAnimation();
                    return true;
                }
            });
        }
        return this;
    }

    private void runEnterAnimation() {
        if(customAnimator==null)
            defaultAnimator.start();
        else
            customAnimator.start();
    }
    public void exitActivity() {
        runExitAnimation();
    }

    private void runExitAnimation(){
        CustomAnimatorListener exit = new CustomAnimatorListener() {
            @Override
            public void onAnimationEnd() {
                activity.finish();
                activity.overridePendingTransition(0, 0);
            }
        };
        if(customAnimator==null)
            defaultAnimator.reverse(exit);
        else
            customAnimator.reverse(exit);
    }

    public ObjectAnimator getToViewAnimator(CustomAnimator animator, boolean show){
        return getToViewAnimator(animator, show, null, true);
    }
    public ObjectAnimator getToViewAnimator(CustomAnimator animator, boolean show, boolean recoverToView){
        return getToViewAnimator(animator, show, null, recoverToView);
    }
    public ObjectAnimator getToViewAnimator(CustomAnimator animator, final boolean show, final CustomAnimatorListener callBack, final boolean recoverToView){
        final ViewGroup parent = (ViewGroup) toView.getParent();
        ViewGroupOverlayUtils.addOverlay(animationView, toView, (int)toView.getX(), (int)toView.getY());
//        animationView.getOverlay().add(toView);

        if(show) {
            toView.setPivotX(0);
            toView.setPivotY(0); //axis
            toView.setScaleX(widthDelta);
            toView.setScaleY(heightDelta);
            toView.setTranslationX(leftDelta);
            toView.setTranslationY(topDelta);
        }
        int translationX = (show)?0:leftDelta;
        int translationY = (show)?0:topDelta;
        float scaleX = (show)?1:widthDelta;
        float scaleY = (show)?1:heightDelta;

        ObjectAnimator viewAnim = ViewPropertyObjectAnimator
                .animate(toView)
                .withLayer().translationX(translationX).translationY(translationY)
                .scaleX(scaleX).scaleY(scaleY).setDuration(CustomAnimator.ANIM_DURATION_LONG)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if (show&&recoverToView) {
//                            animationView.getOverlay().remove(toView);
                            ((ViewGroup) toView.getParent()).removeView(toView);
                            parent.addView(toView);
                        }
                        if(callBack!=null)
                            callBack.onAnimationEnd();
                    }
                }).get();
        viewAnim.setInterpolator(CustomAnimator.DECELERATE);

        return viewAnim;
    }

    public ObjectAnimator getBackgoundAnimator(CustomAnimator animator, boolean show){
        return getBackgoundAnimator(animator, show, null);
    }
    public ObjectAnimator getBackgoundAnimator(CustomAnimator animator, boolean show, final CustomAnimatorListener callBack){
        float from = (show)?0:1;
        float to = (show)?1:0;
        ObjectAnimator bgAnim = ObjectAnimator.ofFloat(background, "alpha", from, to);
        bgAnim.setInterpolator(animator.DECELERATE);
        bgAnim.setDuration(CustomAnimator.ANIM_DURATION_SHORT);
        bgAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                if(callBack!=null)
                    callBack.onAnimationEnd();
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        return bgAnim;
    }

    private CustomAnimator defaultAnimator = new CustomAnimator(){
        @Override
        public void start() {
            ObjectAnimator viewAnim = getToViewAnimator(this, true);
            ObjectAnimator bgAnim = getBackgoundAnimator(this, true);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(viewAnim, bgAnim);
            set.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {}
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (callBack != null)
                        callBack.onAnimationEnd();
                }
                @Override
                public void onAnimationCancel(Animator animation) {}
                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
            set.start();
        }

        @Override
        public void reverse() {
            ObjectAnimator viewAnim = getToViewAnimator(this, false);
            ObjectAnimator bgAnim = getBackgoundAnimator(this, false);

            AnimatorSet set = new AnimatorSet();

            set.playTogether(viewAnim, bgAnim);
            set.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {}
                @Override
                public void onAnimationEnd(Animator animation) {
                    if(callBack!=null)
                        callBack.onAnimationEnd();
                }
                @Override
                public void onAnimationCancel(Animator animation) {}
                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
            set.start();
        }
    };

}
