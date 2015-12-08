package ml.puredark.personallibrary.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.telly.mrvector.MrVector;
import com.transitionseverywhere.utils.ViewGroupOverlayUtils;

import net.steamcrafted.materialiconlib.MaterialIconView;

import io.codetail.widget.RevealFrameLayout;
import ml.puredark.personallibrary.PLApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.beans.Book;
import ml.puredark.personallibrary.customs.MyCoordinatorLayout;
import ml.puredark.personallibrary.customs.MyFloatingActionButton;
import ml.puredark.personallibrary.helpers.ActivityTransitionHelper;
import ml.puredark.personallibrary.helpers.ActivityTransitionHelper.CustomAnimator;
import ml.puredark.personallibrary.helpers.ActivityTransitionHelper.CustomAnimatorListener;
import ml.puredark.personallibrary.helpers.FastBlur;
import ml.puredark.personallibrary.utils.SharedPreferencesUtil;

public class BookDetailActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {
    private ImageView bookCover;
    private MyCoordinatorLayout mCoordinatorLayout;
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout toolbarLayout;
    private Toolbar toolbar;
    private ActivityTransitionHelper transitionHelper;
    private boolean scaned = false;

    //动画相关元素
    private ViewGroup rootView;
    private FloatingActionButton fabAction;
    private FloatingActionMenu fabMenu;
    private RevealFrameLayout animationView;
    private View revealView, extendBar, blank;
    private ImageView backButton;
    private DrawerArrowDrawable backButtonIcon;
    //是否动画中
    private boolean animating = false;

    //此次实例展示的图书
    private Book book;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
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
        backButton = (ImageView) findViewById(R.id.back_button);

        LinearLayout titleBar = (LinearLayout) findViewById(R.id.title_bar);
        TextView bookTitle = (TextView) findViewById(R.id.book_title);
        TextView bookSummary = (TextView) findViewById(R.id.book_summary);
        bookCover = (ImageView) findViewById(R.id.book_cover);
        final ImageView backdrop = (ImageView) findViewById(R.id.backdrop);
        View hover = findViewById(R.id.hover);
        NestedScrollView summaryLayout = (NestedScrollView) findViewById(R.id.summary_layout);
        fabAction = (FloatingActionButton) findViewById(R.id.fab_action);
        fabMenu = (FloatingActionMenu) findViewById(R.id.fab_menu);

        book = (Book) PLApplication.temp;
        Bitmap cover = PLApplication.bitmap;
        fabAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferencesUtil.saveData(getBaseContext(), "isbn13_" + book.isbn13, new Gson().toJson(book));
                setResult(RESULT_OK);
                finishActivity();
            }
        });

        final Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        scaned = intent.getBooleanExtra("scaned", false);
        if(scaned)
            fabMenu.setVisibility(View.GONE);
        else
            fabAction.setVisibility(View.GONE);

        Object data = PLApplication.temp;
        if(data==null||!(data instanceof Book)){
            setResult(RESULT_CANCELED);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return;
        }

        /* 修改界面文字 */
        bookTitle.setText(book.title);
        bookSummary.setText(book.summary);
        bookCover.setImageBitmap(cover);
        backdrop.setImageBitmap(cover);
        String author = (book.author.length>0)?book.author[0]:(book.translator.length>0)?book.translator[0]+"[译]":"";
        ((TextView)findViewById(R.id.author)).setText(author);
        ((TextView)findViewById(R.id.pages)).setText(book.pages + "页");
        ((TextView)findViewById(R.id.price)).setText(book.price);
        ((TextView)findViewById(R.id.pubdate)).setText(book.pubdate + "出版");
        ((TextView)findViewById(R.id.isbn)).setText(book.isbn13);

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
        if(!scaned) {
            mCoordinatorLayout.setAlpha(0);
            animating = true;
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    transitionHelper = ActivityTransitionHelper
                                        .with(BookDetailActivity.this)
                                        .intent(intent)
                                        .toView(bookCover)
                                        .background(mCoordinatorLayout)
                                        .animationView(rootView)
                                        .customAnimator(new AnimationOnActivityStart())
                                        .startTransition(savedInstanceState);
                }
            }, 200);
        }

        /* 修改UI颜色 */
        titleBar.setBackgroundColor(bundle.getInt("titleBarColor"));
        bookTitle.setTextColor(bundle.getInt("titleTextColor"));
        toolbarLayout.setContentScrimColor(bundle.getInt("topColor"));
        hover.setBackgroundColor(bundle.getInt("topColor"));
        summaryLayout.setBackgroundColor(bundle.getInt("bottomColor"));
        bookSummary.setTextColor(bundle.getInt("bottomTextColor"));
        setInfoIconColor(bundle.getInt("topTextColor"));
        setInfoTextColor(bundle.getInt("topTextColor"));

        final int topColor = bundle.getInt("topColor");
        final int topTextColor = bundle.getInt("topTextColor");
        final int fabColor = bundle.getInt("fabColor");
        float[] hsv = new float[3];
        Color.colorToHSV(fabColor, hsv);
        hsv[2] *= 0.8f;
        final int fabColorPressed = Color.HSVToColor(hsv);
        setFloatingActionButtonColors(fabAction, fabColor, fabColorPressed);
        setFloatingActionMenuColors(fabMenu, fabColor, fabColorPressed);
        new Thread(new Runnable() {
            @Override
            public void run() {
                /* 给背景封面加上高斯模糊 */
                final Bitmap overlay = FastBlur.doBlur(PLApplication.bitmap.copy(Bitmap.Config.ARGB_8888, true), 2, true);
                final Drawable iconStar = MrVector.inflate(getResources(), R.drawable.ic_star_white_24dp);
                final Drawable iconPencil = MrVector.inflate(getResources(),R.drawable.ic_pencil_white_24dp);
                final Drawable iconPaperclip = MrVector.inflate(getResources(),R.drawable.ic_paperclip_white_24dp);
                final Drawable iconLibraryBooks = MrVector.inflate(getResources(),R.drawable.ic_library_books_white_24dp);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        backdrop.setImageBitmap(overlay);
                        fabAction.setImageDrawable(iconStar);
                        ((FloatingActionButton)fabMenu.getChildAt(0)).setImageDrawable(iconPencil);
                        ((FloatingActionButton)fabMenu.getChildAt(1)).setImageDrawable(iconPaperclip);
                        ((FloatingActionButton)fabMenu.getChildAt(2)).setImageDrawable(iconLibraryBooks);
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

        //写书评按钮
        fabMenu.getChildAt(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(BookDetailActivity.this, WriteMarkActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("bid", book.id);
                bundle.putString("isbn13", book.isbn13);
                bundle.putInt("topColor", topColor);
                bundle.putInt("topTextColor", topTextColor);
                bundle.putInt("fabColor", fabColor);
                bundle.putInt("fabColorPressed", fabColorPressed);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
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

    private void setInfoIconColor(int color){
        LinearLayout book_info_layout = (LinearLayout) findViewById(R.id.book_info_layout);
        int count = book_info_layout.getChildCount();
        LinearLayout childAt;
        for(int i=0;i<count;i++) {
            childAt = (LinearLayout) book_info_layout.getChildAt(i);
            ((MaterialIconView)childAt.getChildAt(0)).setColor(color);
        }
    }
    private void setInfoTextColor(int color){
        LinearLayout book_info_layout = (LinearLayout) findViewById(R.id.book_info_layout);
        int count = book_info_layout.getChildCount();
        LinearLayout childAt;
        for(int i=0;i<count;i++) {
            childAt = (LinearLayout) book_info_layout.getChildAt(i);
            ((TextView)childAt.getChildAt(1)).setTextColor(color);
        }
    }

    private void setFloatingActionButtonColors(FloatingActionButton fab, int primaryColor, int rippleColor) {
        fab.setColorNormal(primaryColor);
        fab.setColorPressed(rippleColor);
    }

    private void setFloatingActionMenuColors(FloatingActionMenu fab, int primaryColor, int rippleColor) {
        fab.setMenuButtonColorNormal(primaryColor);
        fab.setMenuButtonColorPressed(rippleColor);
        for(int i=0;i<3;i++) {
            ((FloatingActionButton) fab.getChildAt(i)).setColorNormal(primaryColor);
            ((FloatingActionButton) fab.getChildAt(i)).setColorPressed(rippleColor);
        }
    }

    public void setToolbarCollapsible(){
        mAppBarLayout.setExpanded(true, true);
        mCoordinatorLayout.setAllowForScrool(true);
    }
    public void setToolbarUncollapsible(){
        mAppBarLayout.setExpanded(false, true);
        mCoordinatorLayout.setAllowForScrool(false);
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
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if(verticalOffset!=0){
            if(scaned)
                fabAction.hide(true);
            else
                fabMenu.hideMenu(true);
        }else{
            if(scaned)
                fabAction.show(true);
            else
                fabMenu.showMenu(true);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mAppBarLayout.addOnOffsetChangedListener(this);
    }
    @Override
    public void onPause() {
        super.onPause();
        mAppBarLayout.removeOnOffsetChangedListener(this);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mAppBarLayout.removeOnOffsetChangedListener(this);
    }

    private class AnimationOnActivityStart extends CustomAnimator {

        public AnimationOnActivityStart(){}

        public void start(){
            animating = true;
            final ViewGroup bookParent = (ViewGroup) bookCover.getParent();
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
                            ((ViewGroup) bookCover.getParent()).removeView(bookCover);
                            bookParent.addView(bookCover);
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
            objectAnimator.setInterpolator((show)?ACCELERATE_DECELERATE:ACCELERATE);
            return objectAnimator;
        }

        ObjectAnimator getExtendBarAnimator(boolean show){
            int startX = (show)?animationView.getRight():0;
            int endX = (show)?0:animationView.getRight();
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(extendBar, "left",
                    startX, endX);
            objectAnimator.setDuration(CustomAnimator.ANIM_DURATION_MEDIUM);
            objectAnimator.setInterpolator((show)?ACCELERATE_DECELERATE:ACCELERATE);
            return objectAnimator;
        }

        ObjectAnimator getContentAnimator(boolean show){
            int startX = (show)?0:animationView.getRight();
            int endX = (show)?animationView.getRight():0;
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(blank, "right",
                    startX, endX);
            objectAnimator.setDuration(CustomAnimator.ANIM_DURATION_MEDIUM);
            objectAnimator.setInterpolator((show) ? ACCELERATE_DECELERATE : ACCELERATE);
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
