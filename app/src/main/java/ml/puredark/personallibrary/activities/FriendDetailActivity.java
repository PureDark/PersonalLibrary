package ml.puredark.personallibrary.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.transitionseverywhere.utils.ViewGroupOverlayUtils;

import java.util.ArrayList;
import java.util.List;

import io.codetail.widget.RevealFrameLayout;
import ml.puredark.personallibrary.PLApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.adapters.BookListAdapter;
import ml.puredark.personallibrary.adapters.BookMarkAdapter;
import ml.puredark.personallibrary.adapters.FragmentViewPagerAdapter;
import ml.puredark.personallibrary.beans.Friend;
import ml.puredark.personallibrary.customs.MyCoordinatorLayout;
import ml.puredark.personallibrary.fragments.BookListFragment;
import ml.puredark.personallibrary.fragments.BookMarkListFragment;
import ml.puredark.personallibrary.helpers.ActivityTransitionHelper;
import ml.puredark.personallibrary.helpers.ActivityTransitionHelper.CustomAnimator;
import ml.puredark.personallibrary.helpers.ActivityTransitionHelper.CustomAnimatorListener;
import ml.puredark.personallibrary.helpers.FastBlur;

public class FriendDetailActivity extends MyActivity {
    private ImageView avatar;
    private MyCoordinatorLayout mCoordinatorLayout;
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout toolbarLayout;
    private Toolbar toolbar;
    private ActivityTransitionHelper transitionHelper;
    private TextView nickname;
    private TextView signature;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private BookListAdapter mBookAdapter;
    private BookMarkAdapter mMarkAdapter;
    //动画相关元素
    private ViewGroup rootView;
    private RevealFrameLayout animationView;
    private View revealView, extendBar, blank;
    private ImageView backButton;
    private DrawerArrowDrawable backButtonIcon;
    //是否动画中
    private boolean animating = false;
    //此次实例展示的好友
    private Friend friend;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_detail);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mCoordinatorLayout = (MyCoordinatorLayout) findViewById(R.id.coordinator_layout);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        setSupportActionBar(toolbar);

        rootView = (ViewGroup) findViewById(R.id.root_view);
        animationView = (RevealFrameLayout) findViewById(R.id.animation_view);
        revealView = findViewById(R.id.reveal_view);
        extendBar = findViewById(R.id.extend_bar);
        blank = findViewById(R.id.blank);
        backButton = (ImageView) findViewById(R.id.back_button);

        avatar = (ImageView) findViewById(R.id.avatar);
        signature = (TextView) findViewById(R.id.signature);
        final ImageView backdrop = (ImageView) findViewById(R.id.backdrop);
        View hover = findViewById(R.id.hover);

        friend = (Friend) PLApplication.temp;

        final Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        Object data = PLApplication.temp;
        if(data==null||!(data instanceof Friend)){
            setResult(RESULT_CANCELED);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return;
        }
        final Bitmap avatarBitmap = PLApplication.bitmap;
        if(avatarBitmap!=null)
            avatar.setImageBitmap(avatarBitmap);
        /* 修改界面文字 */
        toolbarLayout.setTitle(friend.nickname);
        signature.setText(friend.signature);
        /* 为返回按钮加载图标 */
        backButtonIcon = new DrawerArrowDrawable(this);
        backButtonIcon.setColor(getResources().getColor(R.color.white));
        backButton.setImageDrawable(backButtonIcon);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finishActivity();
            }
        });

        /* 修改动画元素颜色 */
        blank.setBackgroundColor(bundle.getInt("bottomColor"));
        revealView.setBackgroundColor(bundle.getInt("topColor"));
        extendBar.setBackgroundColor(bundle.getInt("titleBarColor"));
        mCoordinatorLayout.setAlpha(0);
        animating = true;
        new Handler().postDelayed(new Runnable() {
            public void run() {
                transitionHelper = ActivityTransitionHelper
                        .with(FriendDetailActivity.this)
                        .intent(intent)
                        .toView(avatar)
                        .background(mCoordinatorLayout)
                        .animationView(rootView)
                        .customAnimator(new AnimationOnActivityStart())
                        .startTransition(savedInstanceState);
            }
        }, 200);


        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        /* 修改UI颜色 */
        mTabLayout.setBackgroundColor(bundle.getInt("titleBarColor"));
        mTabLayout.setTabTextColors(bundle.getInt("titleTextColor"),getResources().getColor(R.color.white));
        toolbarLayout.setContentScrimColor(bundle.getInt("topColor"));
        hover.setBackgroundColor(bundle.getInt("topColor"));

        new Thread(new Runnable() {
            @Override
            public void run() {
                /* 给背景封面加上高斯模糊 */
                final Bitmap overlay = (avatarBitmap!=null)?FastBlur.doBlur(avatarBitmap.copy(Bitmap.Config.ARGB_8888, true), 2, true):null;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        backdrop.setImageBitmap(overlay);
                        /* 让背景的封面大图来回缓慢移动 */
                        float targetY = (backdrop.getHeight()>backdrop.getWidth())?-0.4f:0f;
                        Animation translateAnimation = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, 0f,
                                TranslateAnimation.RELATIVE_TO_SELF, 0f,
                                TranslateAnimation.RELATIVE_TO_SELF, 0f,
                                TranslateAnimation.RELATIVE_TO_SELF, targetY);
                        translateAnimation.setDuration(30000);
                        translateAnimation.setRepeatCount(-1);
                        translateAnimation.setRepeatMode(Animation.REVERSE);
                        translateAnimation.setInterpolator(new LinearInterpolator());
                        backdrop.startAnimation(translateAnimation);
                    }
                });
            }
        }).start();

        Bundle newbund = new Bundle();
        newbund.putInt("bid", 0);
        newbund.putInt("uid", friend.uid);
        newbund.putBoolean("openActivity", true);
        List<Fragment> fragments = new ArrayList<Fragment>();
        BookListFragment bookList = new BookListFragment();
        bookList.setArguments(newbund);
        fragments.add(bookList);
        Fragment bookMarkList = new BookMarkListFragment();
        bookMarkList.setArguments(newbund);
        fragments.add(bookMarkList);
        List<String> titles = new ArrayList<String>();
        titles.add("TA的书籍");
        titles.add("TA的书评");
        FragmentViewPagerAdapter mFragmentAdapter = new FragmentViewPagerAdapter(getSupportFragmentManager(), fragments, titles);
        mTabLayout.setTabsFromPagerAdapter(mFragmentAdapter);
        mViewPager.setAdapter(mFragmentAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    public void finishActivity(){
        if(transitionHelper!=null) {
            mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (verticalOffset == 0)
                        transitionHelper.exitActivity();
                }
            });
            mAppBarLayout.setExpanded(true, true);
        }else
            finish();
    }

    public void showSnackBar(String content){
        Snackbar snackbar = Snackbar.make(
                findViewById(R.id.container),
                content,
                Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(ContextCompat.getColor(PLApplication.mContext, R.color.colorAccentDark));
        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        if(animating)return;
        if(transitionHelper!=null)
            finishActivity();
        else
            super.onBackPressed();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(animating)return false;
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setCurrFragment(int curr) {
    }

    @Override
    public void replaceFragment(Fragment fragment) {
    }

    private class AnimationOnActivityStart extends CustomAnimator {

        public AnimationOnActivityStart(){}

        public void start(){
            animating = true;
            final ViewGroup bookParent = (ViewGroup) avatar.getParent();
            final ViewGroup backParent = (ViewGroup) backButton.getParent();
            ViewGroupOverlayUtils.addOverlay(rootView, backButton, (int)backButton.getX(), (int)backButton.getY());

            ValueAnimator backAnimator = getArrowAnimator(true);
            ObjectAnimator headerAnimator = getHeaderAnimator(true);
            ObjectAnimator extendBarAnimator = getExtendBarAnimator(true);
            ObjectAnimator contentAnimator = getContentAnimator(true);
            android.animation.ObjectAnimator coverAnim = transitionHelper.getToViewAnimator(this, true, false);
            final android.animation.ObjectAnimator bgAnim = transitionHelper.getBackgoundAnimator(this, true,
                    new CustomAnimatorListener() {
                        @Override
                        public void onAnimationEnd() {
                            ((ViewGroup) avatar.getParent()).removeView(avatar);
                            bookParent.addView(avatar);
                            ((ViewGroup) backButton.getParent()).removeView(backButton);
                            backParent.addView(backButton);
                        }
            });

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(headerAnimator, extendBarAnimator, contentAnimator, backAnimator);
            animatorSet.addListener(new SimpleListener() {

                @Override
                public void onAnimationStart(com.nineoldandroids.animation.Animator animation) {
                    blank.setVisibility(View.VISIBLE);
                    extendBar.setVisibility(View.VISIBLE);
                    revealView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(com.nineoldandroids.animation.Animator animation) {
                    bgAnim.addListener(new SimpleListener(){
                        @Override
                        public void onAnimationEnd(android.animation.Animator animation) {
                            animating = false;
                        }
                    });
                    bgAnim.start();
                    if (callBack != null)
                        callBack.onAnimationEnd();
                }
            });
            animatorSet.start();
            coverAnim.start();
        }

        public void reverse(){
            animating = true;
            ViewGroupOverlayUtils.addOverlay(rootView, backButton, (int)backButton.getX(), (int)backButton.getY());

            final ValueAnimator backAnimator = getArrowAnimator(false);
            final ObjectAnimator headerAnimator = getHeaderAnimator(false);
            final ObjectAnimator extendBarAnimator = getExtendBarAnimator(false);
            final ObjectAnimator contentAnimator = getContentAnimator(false);
            final android.animation.ObjectAnimator coverAnim = transitionHelper.getToViewAnimator(this, false);
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
                    coverAnim.start();
                }
            });
            bgAnim.start();
            backAnimator.start();
        }

        ValueAnimator getArrowAnimator(boolean show){
            float start = (show)?0f:1f;
            float end = (show)?1f:0f;
            ValueAnimator animator = ValueAnimator.ofFloat(start, end);
            animator.setDuration(CustomAnimator.ANIM_DURATION_LONG);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    backButtonIcon.setProgress((Float) animation.getAnimatedValue());
                }
            });
            return animator;
        }

        ObjectAnimator getHeaderAnimator(boolean show){
            int startX = (show)?0:animationView.getRight();
            int endX = (show)?animationView.getRight():0;
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(revealView, "right",
                    startX, endX);
            objectAnimator.setDuration(CustomAnimator.ANIM_DURATION_MEDIUM);
            if(show)
                objectAnimator.setInterpolator(ACCELERATE_DECELERATE);
            else
                objectAnimator.setInterpolator(ACCELERATE);
            return objectAnimator;
        }

        ObjectAnimator getExtendBarAnimator(boolean show){
            int startX = (show)?animationView.getRight():0;
            int endX = (show)?0:animationView.getRight();
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(extendBar, "left",
                    startX, endX);
            objectAnimator.setDuration(CustomAnimator.ANIM_DURATION_MEDIUM);
            if(show)
                objectAnimator.setInterpolator(ACCELERATE_DECELERATE);
            else
                objectAnimator.setInterpolator(ACCELERATE);
            return objectAnimator;
        }

        ObjectAnimator getContentAnimator(boolean show){
            int startX = (show)?0:animationView.getRight();
            int endX = (show)?animationView.getRight():0;
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(blank, "right",
                    startX, endX);
            objectAnimator.setDuration(CustomAnimator.ANIM_DURATION_MEDIUM);
            if(show)
                objectAnimator.setInterpolator(ACCELERATE_DECELERATE);
            else
                objectAnimator.setInterpolator(ACCELERATE);
            return objectAnimator;
        }
    }

    private static class SimpleListener implements ObjectAnimator.AnimatorListener,
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
