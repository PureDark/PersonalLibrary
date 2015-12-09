package ml.puredark.personallibrary.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.telly.mrvector.MrVector;
import com.transitionseverywhere.utils.ViewGroupOverlayUtils;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.ArrayList;
import java.util.List;

import io.codetail.widget.RevealFrameLayout;
import ml.puredark.personallibrary.PLApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.User;
import ml.puredark.personallibrary.adapters.BookListAdapter;
import ml.puredark.personallibrary.adapters.ViewPagerAdapter;
import ml.puredark.personallibrary.beans.Book;
import ml.puredark.personallibrary.beans.BookListItem;
import ml.puredark.personallibrary.beans.Friend;
import ml.puredark.personallibrary.customs.MyCoordinatorLayout;
import ml.puredark.personallibrary.dataprovider.BookListDataProvider;
import ml.puredark.personallibrary.helpers.ActivityTransitionHelper;
import ml.puredark.personallibrary.helpers.ActivityTransitionHelper.CustomAnimator;
import ml.puredark.personallibrary.helpers.ActivityTransitionHelper.CustomAnimatorListener;
import ml.puredark.personallibrary.helpers.DoubanRestAPI;
import ml.puredark.personallibrary.helpers.FastBlur;
import ml.puredark.personallibrary.helpers.PLServerAPI;
import ml.puredark.personallibrary.utils.SharedPreferencesUtil;

public class FriendActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {
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
    private boolean bookItemClicked = false;

    //此次实例展示的好友
    private Friend friend;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_friend_detail);

        Log.i("Kevin", "startH1");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mCoordinatorLayout = (MyCoordinatorLayout) findViewById(R.id.coordinator_layout);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        Log.i("Kevin", "startH2");
        rootView = (ViewGroup) findViewById(R.id.root_view);
        animationView = (RevealFrameLayout) findViewById(R.id.animation_view);
        revealView = findViewById(R.id.reveal_view);
        extendBar = findViewById(R.id.extend_bar);
        blank = findViewById(R.id.blank);
        backButton = (ImageView) findViewById(R.id.back_button);

        avatar = (ImageView) findViewById(R.id.avatar);
        nickname = (TextView) findViewById(R.id.name);
        signature = (TextView) findViewById(R.id.signature);
        final ImageView backdrop = (ImageView) findViewById(R.id.backdrop);
        View hover = findViewById(R.id.hover);

        Log.i("Kevin","startH3");
        friend = (Friend) PLApplication.temp;

        final Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String uid = intent.getStringExtra("uid");

        Log.i("Kevin","startH4");

        Object data = PLApplication.temp;
        if(data==null||!(data instanceof Friend)){
            Log.i("Kevin","startH5");
            setResult(RESULT_CANCELED);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return;
        }
        Log.i("Kevin", friend.nickname + "test");
        /* 修改界面文字 */
        nickname.setText(friend.nickname);
        signature.setText(friend.signature);
        String url =  PLApplication.serverHost + "/images/users/avatars/" + friend.uid + ".png";
        if(avatar.getTag() != url) {
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)//设置下载的图片是否缓存在内存中
                    .cacheOnDisc(false)//设置下载的图片是否缓存在SD卡中
                    .displayer(new FadeInBitmapDisplayer(300))//是否图片加载好后渐入的动画时间
                    .build();//构建完成
            ImageLoader.getInstance().displayImage(null, avatar,options);
            ImageLoader.getInstance().displayImage(url,avatar,options);
            avatar.setTag(url);
        }
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
                                        .with(FriendActivity.this)
                                        .intent(intent)
                                        .toView(avatar)
                                        .background(mCoordinatorLayout)
                                        .animationView(rootView)
                                        .customAnimator(new AnimationOnActivityStart())
                                        .startTransition(savedInstanceState);
                }
            }, 200);
        }
        /* 加载ViewPager */

        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        getBookList(friend.getId());
        mBookAdapter = new BookListAdapter(new BookListDataProvider(null));
        mBookAdapter.setOnItemClickListener(new BookListAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(final View view, int postion) {
                if (bookItemClicked == false) {
                    bookItemClicked = true;
                    final BookListItem item = (BookListItem) mBookAdapter.getDataProvider().getItem(postion);
                    String bookString = (String) SharedPreferencesUtil.getData(PLApplication.mContext, "isbn13_" + item.isbn13, "");
                    if (!bookString.equals("")) {
                        Book book = new Gson().fromJson(bookString, Book.class);
                        book.id = item.getId();
                        book.uid = User.getUid();
                        mActivity.startBookDetailActivity(book, view);
                    } else {
                        DoubanRestAPI.getBookByISBN(item.isbn13, new MainActivity.CallBack() {
                            @Override
                            public void action(final Object obj) {
                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        if (obj instanceof Book) {
                                            Book book = (Book) obj;
                                            book.id = item.getId();
                                            book.uid = User.getUid();
                                            mActivity.startBookDetailActivity(book, view);
                                            SharedPreferencesUtil.saveData(PLApplication.mContext, "isbn13_" + book.isbn13, new Gson().toJson(book));
                                        }
                                    }
                                }, 500);
                            }
                        });
                    }
                }
            }
        });
        List<View> views = new ArrayList<>();
        final View viewBookList = getLayoutInflater().inflate(R.layout.view_book_list, null);
        //final View viewMaskList = getLayoutInflater().inflate(R.layout.view_mark_list, null);
        views.add(viewBookList);
        //views.add(viewMaskList);
        List<String> titles = new ArrayList<String>();
        titles.add("书籍");
        //titles.add("书评");
        ViewPagerAdapter mAdapter = new ViewPagerAdapter(views, titles);
        mTabLayout.setTabsFromPagerAdapter(mAdapter);
        mViewPager.setAdapter(mAdapter);
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
    public void getBookList(int uid){
        PLServerAPI.getBookList(uid,null, null, new PLServerAPI.onResponseListener() {
            @Override
            public void onSuccess(Object data) {
                List<BookListItem> books = (List<BookListItem>) data;
                mBookAdapter.setDataProvider(new BookListDataProvider(books));
                mBookAdapter.notifyDataSetChanged();
            }
            @Override
            public void onFailure(PLServerAPI.ApiError apiError) {
                showSnackBar(apiError.getErrorString());
            }
        });
    }
    public void showSnackBar(String content){
        Snackbar snackbar = Snackbar.make(
                findViewById(R.id.container),
                content,
                Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(ContextCompat.getColor(PLApplication.mContext, R.color.colorAccentDark));
        snackbar.show();
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
