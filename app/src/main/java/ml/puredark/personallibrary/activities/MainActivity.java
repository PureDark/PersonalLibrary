package ml.puredark.personallibrary.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.kogitune.activity_transition.ActivityTransition;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.telly.mrvector.MrVector;
import com.wnafee.vector.compat.ResourcesCompat;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import io.codetail.animation.arcanimator.ArcAnimator;
import io.codetail.animation.arcanimator.Side;

import io.codetail.widget.RevealFrameLayout;
import ml.puredark.personallibrary.PersonalLibraryApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.beans.Book;
import ml.puredark.personallibrary.customs.MyCoordinatorLayout;
import ml.puredark.personallibrary.customs.MyEditText;
import ml.puredark.personallibrary.customs.MyFloatingActionButton;
import ml.puredark.personallibrary.fragments.IndexFragment;
import ml.puredark.personallibrary.fragments.OnFragmentInteractionListener;
import ml.puredark.personallibrary.helpers.DoubanRestAPI;
import ml.puredark.personallibrary.utils.DensityUtils;
import ml.puredark.personallibrary.utils.ViewUtils;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnFragmentInteractionListener {
    private View rootLayout;
    // Fragment的标签
    private static final String FRAGMENT_INDEX = "index";

    //下拉刷新
    private MyCoordinatorLayout mCoordinatorLayout;
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mCollapsingToolbar;

    //搜索栏是否展开
    private boolean expanded = false;
    private MyEditText inputSearch;


    //动画相关元素
    private MyFloatingActionButton fab_add;
    private RevealFrameLayout revealLayout;
    private View revealView, extendBar, blank;
    //revealView是否展开
    private boolean revealed = false;
    //是否动画中
    private boolean animating = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MrVector.wrap(newBase));
    }
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootLayout = findViewById(R.id.drawer_layout);
        //ActivityTransition.with(getIntent()).to(findViewById(R.id.avatar)).start(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, IndexFragment.getInstance(), FRAGMENT_INDEX)
                    .commit();
        }
        mCoordinatorLayout = (MyCoordinatorLayout) findViewById(R.id.content);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        setSupportActionBar(toolbar);

        revealLayout = (RevealFrameLayout) findViewById(R.id.reveal_layout);
        revealView = findViewById(R.id.reveal_view);
        extendBar = findViewById(R.id.animator_view);
        blank = findViewById(R.id.blank);
        revealLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_DOWN&&revealed)
                    return true;
                return false;
            }
        });

        //异步加载背景图
        ImageLoader.getInstance().displayImage("drawable://" + R.drawable.index_header, (ImageView) findViewById(R.id.backdrop));
        //ImageLoader.getInstance().displayImage("drawable://" + R.drawable.polygon_14, new BgViewAware(navigationView.findViewById(R.id.navHeaderView)));

        //初始化侧边栏的图标
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        //初始化侧边栏
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_index);

        //为FAB加载图标
        Animatable crossStartIcon = (Animatable) ResourcesCompat.getDrawable(this, R.drawable.vector_animated_cross_0_to_45);
        Animatable crossEndIcon = (Animatable) ResourcesCompat.getDrawable(this, R.drawable.vector_animated_cross_45_to_0);
        fab_add = (MyFloatingActionButton) findViewById(R.id.fab_add);
        fab_add.setStartIcon(crossStartIcon);
        fab_add.setEndIcon(crossEndIcon);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!revealed&&!animating) {
                    new AnimationFabtoCamera(new MyAnimatorListener() {
                        @Override
                        public void onAnimationEnd() {
                            IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                            integrator.setCaptureActivity(MyCaptureActivity.class);
                            integrator.setOrientationLocked(true);
                            integrator.setPrompt("请扫描书籍条形码");
                            integrator.addExtra("SCAN_WIDTH", 640);
                            integrator.addExtra("SCAN_HEIGHT", 240);
                            integrator.initiateScan(IntentIntegrator.PRODUCT_CODE_TYPES);
                            MainActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }
                    }).start();
                }
            }
        });

        //为搜索栏加载图标
        Animatable searchStartIcon = (Animatable) ResourcesCompat.getDrawable(this, R.drawable.verctor_animated_bar_to_search);
        Animatable searchEndIcon = (Animatable) ResourcesCompat.getDrawable(this, R.drawable.verctor_animated_search_to_bar);
        inputSearch = (MyEditText)findViewById(R.id.search_input);
        inputSearch.setStartIcon(searchStartIcon);
        inputSearch.setEndIcon(searchEndIcon);
        searchStartIcon.start();

        //为搜索按钮绑定事件
        findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击搜索按钮展开搜索栏
                if (!expanded)
                    extendSearchBar();
                else
                    collapseSearchBar();
            }
        });

        //输入内容为空则在搜索栏失去焦点时变回搜索按钮
        inputSearch.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus&&inputSearch.getText().toString().trim().equals(""))
                    collapseSearchBar();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(revealed&&!animating){
            new AnimationFabtoCamera().reverse();
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result != null) {
            String contents = result.getContents();
            if (contents != null) {
//                final Snackbar snackbar = Snackbar.make(rootLayout, contents, Snackbar.LENGTH_LONG);
//                snackbar.setAction("隐藏", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        snackbar.dismiss();
//                    }
//                }).show();
                //TODO: 获取书籍信息时显示载入动画
                DoubanRestAPI.getBookById(contents, new CallBack() {
                    @Override
                    public void action(Object obj) {
                        if(obj instanceof Book){
                            final Book book = (Book)obj;
                            new AnimationShowBookDetail(new MyAnimatorListener() {
                                @Override
                                public void onAnimationEnd() {
                                    String url = book.images.get("large");
                                    if(url==null) url = book.image;
                                    ImageLoader.getInstance().loadImage(url, new SimpleImageLoadingListener() {
                                        @Override
                                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                            Intent intent = new Intent(MainActivity.this, BookDetailActivity.class);
                                            PersonalLibraryApplication.temp = book;
                                            PersonalLibraryApplication.bitmap = loadedImage;
                                            startActivityForResult(intent, 1);
                                            MainActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                        }
                                    });
                                }
                            }).start();
                        }else
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    new AnimationFabtoCamera().reverse();
                                }
                            }, 500);
                    }
                });
                return;
            }
        }else if(requestCode==1){
            new AnimationShowBookDetail().reverse();
            return;
        }
        new Handler().postDelayed(new Runnable() {
            public void run() {
                new AnimationFabtoCamera().reverse();
            }
        }, 500);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(animating)return false;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (expanded&&inputSearch.isFocused()) {
                Rect outRect = new Rect();
                inputSearch.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    inputSearch.clearFocus();
                    InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(inputSearch.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_index) {

        } else if (id == R.id.nav_borrow) {

        } else if (id == R.id.nav_friend) {

        } else if (id == R.id.nav_whatshot) {

        } else if (id == R.id.nav_logout) {

        } else if (id == R.id.nav_exit) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onFragmentInteraction(int action) {

    }

    public void extendSearchBar(){
        inputSearch.reverse();
        inputSearch.setEnabled(true);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                inputSearch.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(inputSearch, InputMethodManager.SHOW_FORCED);
                expanded = true;
            }
        }, 500);
    }

    public void collapseSearchBar(){
        inputSearch.start();
        inputSearch.setText("");
        inputSearch.setEnabled(false);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                expanded = false;
            }
        }, 500);
    }

    public void setToolbarCollapsible(){
        mAppBarLayout.setExpanded(true, true);
        mCoordinatorLayout.setAllowForScrool(true);
    }
    public void setToolbarUncollapsible(){
        mAppBarLayout.setExpanded(false, true);
        mCoordinatorLayout.setAllowForScrool(false);
    }

    static int startFabX;
    static int startFabY;
    private class AnimationFabtoCamera{
        final AccelerateInterpolator ACCELERATE = new AccelerateInterpolator();
        final AccelerateDecelerateInterpolator ACCELERATE_DECELERATE = new AccelerateDecelerateInterpolator();
        final DecelerateInterpolator DECELERATE = new DecelerateInterpolator();
        private MyAnimatorListener callBack;

        public AnimationFabtoCamera(){}

        public AnimationFabtoCamera(MyAnimatorListener callBack){
            this.callBack = callBack;
        }

        void start(){
            animating = true;
            startFabX = (int) ViewUtils.centerX(fab_add);
            startFabY = (int)ViewUtils.centerY(fab_add);
            fab_add.start();
            fabToCenter();
        }

        void reverse(){
            animating = true;
            shrinkCameraLayout();
        }

        void fabToCenter(){
            fab_add.setVisibility(View.VISIBLE);
            int endFabX = revealView.getWidth()/2;
            int endFabY = (int) (revealView.getHeight()*0.5f);
            ArcAnimator arcAnimator = ArcAnimator.createArcAnimator(fab_add,
                    endFabX, endFabY, 90, Side.LEFT)
                    .setDuration(300);
            arcAnimator.setInterpolator(ACCELERATE_DECELERATE);
            arcAnimator.addListener(new SimpleListener(){
                @Override
                public void onAnimationEnd(Animator animation) {
                    fab_add.setVisibility(View.INVISIBLE);
                    revealCameraLayout();
                }
            });
            arcAnimator.start();
        }

        void revealCameraLayout(){
            revealView.setVisibility(View.VISIBLE);
            float finalRadius = Math.max(revealView.getWidth(), revealView.getHeight()) * 1.5f;
            SupportAnimator animator = ViewAnimationUtils.createCircularReveal(revealView,
                    (int) ViewUtils.centerX(fab_add), (int) ViewUtils.centerY(fab_add),
                    fab_add.getWidth() / 2f, finalRadius);
            animator.setDuration(400);
            animator.setInterpolator(ACCELERATE);
            animator.addListener(new SimpleListener() {
                @Override
                public void onAnimationEnd() {
                    revealed = true;
                    animating = false;
                    if(callBack!=null)
                        callBack.onAnimationEnd();
                }
            });
            animator.start();
        }

        void shrinkCameraLayout(){
            revealView.setVisibility(View.VISIBLE);
            float finalRadius = Math.max(revealView.getWidth(), revealView.getHeight()) * 1.5f;
            SupportAnimator animator = ViewAnimationUtils.createCircularReveal(revealView,
                    (int)ViewUtils.centerX(fab_add), (int)ViewUtils.centerY(fab_add),
                    finalRadius, fab_add.getWidth() / 2f);
            animator.setDuration(400);
            animator.addListener(new SimpleListener() {
                @Override
                public void onAnimationEnd() {
                    revealView.setVisibility(View.INVISIBLE);
                    fab_add.reverse();
                    fabToOrigin();
                }
            });
            animator.setInterpolator(DECELERATE);
            animator.start();
        }

        void fabToOrigin() {
            fab_add.setVisibility(View.VISIBLE);
            ArcAnimator arcAnimator = ArcAnimator.createArcAnimator(fab_add,
                    startFabX, startFabY, 90, Side.RIGHT)
                    .setDuration(300);
            arcAnimator.setInterpolator(ACCELERATE_DECELERATE);
            arcAnimator.addListener(new SimpleListener(){
                @Override
                public void onAnimationEnd(Animator animation) {
                    revealed = false;
                    animating = false;
                    if(callBack!=null)
                        callBack.onAnimationEnd();
                }
            });
            arcAnimator.start();
        }
    }

    private class AnimationShowBookDetail{
        final AccelerateInterpolator ACCELERATE = new AccelerateInterpolator();
        final AccelerateDecelerateInterpolator ACCELERATE_DECELERATE = new AccelerateDecelerateInterpolator();
        final DecelerateInterpolator DECELERATE = new DecelerateInterpolator();
        private MyAnimatorListener callBack;

        public AnimationShowBookDetail(){}

        public AnimationShowBookDetail(MyAnimatorListener callBack){
            this.callBack = callBack;
        }

        void start(){
            blank.setVisibility(View.VISIBLE);
            animating = true;
            raiseHeader();
        }

        void reverse(){
            blank.setVisibility(View.VISIBLE);
            animating = true;
            collapseBar();
        }

        void raiseHeader(){
            revealView.setVisibility(View.VISIBLE);
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(revealView, "bottom",
                    revealView.getBottom(), revealView.getTop() + DensityUtils.dp2px(MainActivity.this,264));
            objectAnimator.setDuration(600);
            objectAnimator.addListener(new SimpleListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    extendBar();
                }
            });
            objectAnimator.setInterpolator(ACCELERATE_DECELERATE);
            objectAnimator.start();
        }

        void extendBar(){
            extendBar.setVisibility(View.VISIBLE);
            float finalRadius = extendBar.getWidth()*2.5f;
            SupportAnimator animator = ViewAnimationUtils.createCircularReveal(extendBar,
                    extendBar.getRight(), (int) ViewUtils.centerY(extendBar),
                    0, finalRadius);
            animator.setDuration(400);
            animator.setInterpolator(ACCELERATE);
            animator.addListener(new SimpleListener() {
                @Override
                public void onAnimationEnd() {
                    animating = false;
                    if(callBack!=null)
                        callBack.onAnimationEnd();
                }
            });
            animator.start();
        }

        void collapseBar(){
            extendBar.setVisibility(View.VISIBLE);
            float finalRadius = extendBar.getWidth()*2.5f;
            SupportAnimator animator = ViewAnimationUtils.createCircularReveal(extendBar,
                    extendBar.getRight(), (int) ViewUtils.centerY(extendBar),
                    finalRadius, 0);
            animator.setDuration(400);
            animator.addListener(new SimpleListener() {
                @Override
                public void onAnimationEnd() {
                    extendBar.setVisibility(View.INVISIBLE);
                    dropHeader();
                }
            });
            animator.setInterpolator(DECELERATE);
            animator.start();
        }

        void dropHeader() {
            revealView.setVisibility(View.VISIBLE);
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(revealView, "bottom",
                    revealView.getBottom(), revealLayout.getBottom());
            objectAnimator.addListener(new SimpleListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    blank.setVisibility(View.INVISIBLE);
                    new AnimationFabtoCamera(callBack).reverse();
                }
            });
            objectAnimator.setDuration(600);
            objectAnimator.setInterpolator(ACCELERATE_DECELERATE);
            objectAnimator.start();
        }
    }

    private static class SimpleListener implements SupportAnimator.AnimatorListener, ObjectAnimator.AnimatorListener{
        @Override
        public void onAnimationStart() {}
        @Override
        public void onAnimationEnd() {}
        @Override
        public void onAnimationCancel() {}
        @Override
        public void onAnimationRepeat() {}
        @Override
        public void onAnimationStart(Animator animation) {}
        @Override
        public void onAnimationEnd(Animator animation) {}
        @Override
        public void onAnimationCancel(Animator animation) {}
        @Override
        public void onAnimationRepeat(Animator animation) {}
    }
    public interface MyAnimatorListener{
        void onAnimationEnd();
    }

    public abstract class CallBack{
        public abstract void action(Object data);
    }

}
