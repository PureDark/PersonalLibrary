package ml.puredark.personallibrary.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.transitionseverywhere.ChangeBounds;
import com.transitionseverywhere.Fade;
import com.transitionseverywhere.Transition;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import io.codetail.widget.RevealFrameLayout;
import ml.puredark.personallibrary.PersonalLibraryApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.beans.Book;
import ml.puredark.personallibrary.customs.MyCoordinatorLayout;
import ml.puredark.personallibrary.customs.MyFloatingActionButton;
import ml.puredark.personallibrary.helpers.ActivityTransitionHelper;
import ml.puredark.personallibrary.helpers.ActivityTransitionHelper.CustomAnimator;
import ml.puredark.personallibrary.helpers.ActivityTransitionHelper.CustomAnimatorListener;
import ml.puredark.personallibrary.helpers.FastBlur;
import ml.puredark.personallibrary.utils.SharedPreferencesUtil;

public class BookDetailActivity extends AppCompatActivity {
    private TextView bookTitle, bookSummary;
    private ImageView bookCover, backdrop;
    private View hover;
    private LinearLayout titleBar;
    private MyCoordinatorLayout mCoordinatorLayout;
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout toolbarLayout;
    private NestedScrollView summaryLayout;
    private ActivityTransitionHelper transitionHelper;
    private boolean scaned = false;

    //动画相关元素
    private ViewGroup rootView;
    private MyFloatingActionButton fabAction;
    private RevealFrameLayout animationView;
    private View revealView, extendBar, blank;
    //是否动画中
    private boolean animating = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mCoordinatorLayout = (MyCoordinatorLayout) findViewById(R.id.coordinator_layout);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        rootView = (ViewGroup) findViewById(R.id.root_view);
        animationView = (RevealFrameLayout) findViewById(R.id.animation_view);
        revealView = findViewById(R.id.reveal_view);
        extendBar = findViewById(R.id.extend_bar);
        blank = findViewById(R.id.blank);

        titleBar = (LinearLayout) findViewById(R.id.title_bar);
        bookTitle = (TextView) findViewById(R.id.book_title);
        bookSummary = (TextView) findViewById(R.id.book_summary);
        bookCover = (ImageView) findViewById(R.id.book_cover);
        backdrop = (ImageView) findViewById(R.id.backdrop);
        hover = findViewById(R.id.hover);
        summaryLayout = (NestedScrollView) findViewById(R.id.summary_layout);
        fabAction = (MyFloatingActionButton) findViewById(R.id.fab_action);


        final Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        /* 修改动画元素颜色 */
        blank.setBackgroundColor(bundle.getInt("bottomColor"));
        revealView.setBackgroundColor(bundle.getInt("topColor"));
        extendBar.setBackgroundColor(bundle.getInt("titleBarColor"));
        scaned = intent.getBooleanExtra("scaned", false);
        if(!scaned) {
            mCoordinatorLayout.setAlpha(0);
            animating = true;
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    transitionHelper = ActivityTransitionHelper.with(BookDetailActivity.this)
                            .intent(intent)
                            .toView(bookCover)
                            .background(mCoordinatorLayout)
                            .animationView(rootView)
                            .customAnimator(new AnimationOnActivityStart())
                            .startTransition(savedInstanceState);
                }
            }, 200);
        }

        Object data = PersonalLibraryApplication.temp;
        if(data==null||!(data instanceof Book)){
            setResult(RESULT_CANCELED);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return;
        }
        final Book book = (Book)PersonalLibraryApplication.temp;
        Bitmap cover = PersonalLibraryApplication.bitmap;
        fabAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferencesUtil.saveData(getBaseContext(), "isbn13_"+book.isbn13, new Gson().toJson(book));
                setResult(RESULT_OK);
                if(transitionHelper!=null)
                    transitionHelper.exitActivity();
                else {
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        });

        bookTitle.setText(book.title);
        bookSummary.setText(book.summary);
        bookCover.setImageBitmap(cover);
        backdrop.setImageBitmap(cover);

        Drawable fabIcon = MaterialDrawableBuilder.with(this)
                .setIcon(MaterialDrawableBuilder.IconValue.STAR)
                .setSizeDp(24)
                .setColor(Color.WHITE)
                .build();
        fabAction.setImageDrawable(fabIcon);

        /* 让背景的封面大图上下来回缓慢移动 */
        Animation translateAnimation = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, 0f,
        TranslateAnimation.RELATIVE_TO_SELF, 0f,
        TranslateAnimation.RELATIVE_TO_SELF, 0f,
        TranslateAnimation.RELATIVE_TO_SELF, -0.4f);
        translateAnimation.setDuration(30000);
        translateAnimation.setRepeatCount(-1);
        translateAnimation.setRepeatMode(Animation.REVERSE);
        translateAnimation.setInterpolator(new LinearInterpolator());
        backdrop.startAnimation(translateAnimation);

        /* 修改UI颜色 */
        titleBar.setBackgroundColor(bundle.getInt("titleBarColor"));
        bookTitle.setTextColor(bundle.getInt("titleTextColor"));
        toolbarLayout.setContentScrimColor(bundle.getInt("topColor"));
        hover.setBackgroundColor(bundle.getInt("topColor"));
        summaryLayout.setBackgroundColor(bundle.getInt("bottomColor"));
        bookSummary.setTextColor(bundle.getInt("bottomTextColor"));
        int fabColor = bundle.getInt("fabColor");
        float[] hsv = new float[3];
        Color.colorToHSV(fabColor, hsv);
        hsv[2] *= 0.8f;
        int fabColorPressed = Color.HSVToColor(hsv);
        setFloatingActionButtonColors(fabAction, fabColor, fabColorPressed);
        /* 给背景封面加上高斯模糊 */
        Bitmap overlay = PersonalLibraryApplication.bitmap.copy(Bitmap.Config.ARGB_8888, true);
        overlay = FastBlur.doBlur(overlay, 2, true);
        backdrop.setImageBitmap(overlay);

    }

    @Override
    public void onBackPressed() {
        if(animating)return;
        if(transitionHelper!=null)
            transitionHelper.exitActivity();
        else
            super.onBackPressed();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(animating)return false;
        return super.dispatchTouchEvent(event);
    }

    private void setFloatingActionButtonColors(MyFloatingActionButton fab, int primaryColor, int rippleColor) {
        int[][] states = {
                {android.R.attr.state_enabled},
                {android.R.attr.state_pressed},
        };
        int[] colors = {
                primaryColor,
                rippleColor,
        };
        ColorStateList colorStateList = new ColorStateList(states, colors);
        fab.setBackgroundTintList(colorStateList);
    }


    public void setToolbarCollapsible(){
        mAppBarLayout.setExpanded(true, true);
        mCoordinatorLayout.setAllowForScrool(true);
    }
    public void setToolbarUncollapsible(){
        mAppBarLayout.setExpanded(false, true);
        mCoordinatorLayout.setAllowForScrool(false);
    }

    private class AnimationOnActivityStart extends CustomAnimator {

        public AnimationOnActivityStart(){}

        public void start(){
            animating = true;
            final ViewGroup parent = (ViewGroup) bookCover.getParent();
            ObjectAnimator headerAnimator = getHeaderAnimator(true);
            ObjectAnimator extendBarAnimator = getExtendBarAnimator(true);
            ObjectAnimator contentAnimator = getContentAnimator(true);
            android.animation.ObjectAnimator viewAnim = transitionHelper.getToViewAnimator(this, true, false);
            final android.animation.ObjectAnimator bgAnim = transitionHelper.getBackgoundAnimator(this, true,
                    new CustomAnimatorListener() {
                        @Override
                        public void onAnimationEnd() {
                            ((ViewGroup) bookCover.getParent()).removeView(bookCover);
                            parent.addView(bookCover);
                        }
            });

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(headerAnimator, extendBarAnimator, contentAnimator);
            animatorSet.addListener(new SimpleListener() {

                @Override
                public void onAnimationStart(com.nineoldandroids.animation.Animator animation) {
                    blank.setVisibility(View.VISIBLE);
                    extendBar.setVisibility(View.VISIBLE);
                    revealView.setVisibility(View.VISIBLE);
                }
                @Override
                public void onAnimationEnd(com.nineoldandroids.animation.Animator animation) {
                    bgAnim.start();
                    animating = false;
                    if (callBack != null)
                        callBack.onAnimationEnd();
                }
            });
            animatorSet.start();
            viewAnim.start();
        }

        public void reverse(){
            animating = true;
            final ObjectAnimator headerAnimator = getHeaderAnimator(false);
            final ObjectAnimator extendBarAnimator = getExtendBarAnimator(false);
            final ObjectAnimator contentAnimator = getContentAnimator(false);
            final android.animation.ObjectAnimator viewAnim = transitionHelper.getToViewAnimator(this, false);
            android.animation.ObjectAnimator bgAnim = transitionHelper.getBackgoundAnimator(this, false);
            bgAnim.addListener(new SimpleListener() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(headerAnimator, extendBarAnimator, contentAnimator);
                    animatorSet.addListener(new SimpleListener() {
                        @Override
                        public void onAnimationEnd(com.nineoldandroids.animation.Animator animation) {
                            animating = false;
                            if (callBack != null)
                                callBack.onAnimationEnd();
                        }
                    });
                    animatorSet.start();
                    viewAnim.start();
                }
            });
            bgAnim.start();
        }

        ObjectAnimator getHeaderAnimator(boolean show){
            int startX = (show)?0:animationView.getRight();
            int endX = (show)?animationView.getRight():0;
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(revealView, "right",
                    startX, endX);
            objectAnimator.setDuration(CustomAnimator.ANIM_DURATION_LONG);
            objectAnimator.setInterpolator((show)?DECELERATE:ACCELERATE);
            return objectAnimator;
        }

        ObjectAnimator getExtendBarAnimator(boolean show){
            int startX = (show)?animationView.getRight():0;
            int endX = (show)?0:animationView.getRight();
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(extendBar, "left",
                    startX, endX);
            objectAnimator.setDuration(CustomAnimator.ANIM_DURATION_LONG);
            objectAnimator.setInterpolator((show)?DECELERATE:ACCELERATE);
            return objectAnimator;
        }

        ObjectAnimator getContentAnimator(boolean show){
            int startX = (show)?0:animationView.getRight();
            int endX = (show)?animationView.getRight():0;
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(blank, "right",
                    startX, endX);
            objectAnimator.setDuration(CustomAnimator.ANIM_DURATION_LONG);
            objectAnimator.setInterpolator((show)?DECELERATE:ACCELERATE);
            return objectAnimator;
        }
    }

    private static class SimpleListener implements com.nineoldandroids.animation.ObjectAnimator.AnimatorListener,
                                                    android.animation.Animator.AnimatorListener{
        @Override
        public void onAnimationStart(com.nineoldandroids.animation.Animator animation) {}
        @Override
        public void onAnimationEnd(com.nineoldandroids.animation.Animator animation) {}
        @Override
        public void onAnimationCancel(com.nineoldandroids.animation.Animator animation) {}
        @Override
        public void onAnimationRepeat(com.nineoldandroids.animation.Animator animation) {}

        @Override
        public void onAnimationStart(android.animation.Animator animation) {}
        @Override
        public void onAnimationEnd(android.animation.Animator animation) {}
        @Override
        public void onAnimationCancel(android.animation.Animator animation) {}
        @Override
        public void onAnimationRepeat(android.animation.Animator animation) {}
    }

}
