package ml.puredark.personallibrary.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.telly.mrvector.MrVector;
import com.wnafee.vector.compat.ResourcesCompat;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import carbon.widget.ProgressBar;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import io.codetail.animation.arcanimator.ArcAnimator;
import io.codetail.animation.arcanimator.Side;

import io.codetail.widget.RevealFrameLayout;
import ml.puredark.personallibrary.PLApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.beans.Book;
import ml.puredark.personallibrary.beans.BookListItem;
import ml.puredark.personallibrary.customs.MyCoordinatorLayout;
import ml.puredark.personallibrary.customs.MyEditText;
import ml.puredark.personallibrary.customs.MyFloatingActionButton;
import ml.puredark.personallibrary.fragments.IndexFragment;
import ml.puredark.personallibrary.fragments.OnFragmentInteractionListener;
import ml.puredark.personallibrary.helpers.ActivityTransitionHelper;
import ml.puredark.personallibrary.helpers.ActivityTransitionHelper.CustomAnimator;
import ml.puredark.personallibrary.helpers.ActivityTransitionHelper.CustomAnimatorListener;
import ml.puredark.personallibrary.helpers.DoubanRestAPI;
import ml.puredark.personallibrary.utils.SharedPreferencesUtil;
import ml.puredark.personallibrary.utils.ViewUtils;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnFragmentInteractionListener {
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
    private MyFloatingActionButton fabAdd;
    private RevealFrameLayout revealLayout;
    private View revealView, extendBar, blank;
    private ProgressBar loading;
    private MaterialAnimatedSwitch listSwitch;
    //revealView是否展开
    private boolean revealed = false;
    //是否动画中
    private boolean animating = false;
    //是否正在从网络获取数据
    private boolean getting = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MrVector.wrap(newBase));
    }
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        loading = (ProgressBar) findViewById(R.id.loading);
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
        listSwitch = (MaterialAnimatedSwitch) findViewById(R.id.list_switch);
        listSwitch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean right) {
                if(right)
                    IndexFragment.getInstance().setRecyclerViewToGrid();
                else
                    IndexFragment.getInstance().setRecyclerViewToList();
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        //为FAB加载图标
        Animatable crossStartIcon = (Animatable) ResourcesCompat.getDrawable(this, R.drawable.vector_animated_cross_0_to_45);
        Animatable crossEndIcon = (Animatable) ResourcesCompat.getDrawable(this, R.drawable.vector_animated_cross_45_to_0);
        fabAdd = (MyFloatingActionButton) findViewById(R.id.fab_add);
        fabAdd.setStartIcon(crossStartIcon);
        fabAdd.setEndIcon(crossEndIcon);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!revealed&&!animating) {
                    new AnimationFabtoCamera().start(new CustomAnimatorListener() {
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
                    });
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

    public void replaceFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, FRAGMENT_INDEX)
                .addToBackStack(getSupportFragmentManager().getBackStackEntryAt(0).getClass().getName())
                .commit();
    }

    @Override
    public void onBackPressed() {
        if(animating||getting)return;
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(revealed){
            new AnimationFabtoCamera().reverse();
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result != null&&result.getContents()!=null) {
//                final Snackbar snackbar = Snackbar.make(rootLayout, contents, Snackbar.LENGTH_LONG);
//                snackbar.setAction("隐藏", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        snackbar.dismiss();
//                    }
//                }).show();

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    loading.setVisibility(View.VISIBLE);
                }
            }, 500);
            getting = true;
            DoubanRestAPI.getBookByISBN(result.getContents(), new CallBack() {
                @Override
                public void action(final Object obj) {
                    getting = false;
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            if (obj instanceof Book) {
                                Book book = (Book) obj;
                                startBookDetailActivity(book);
                            } else {
                                loading.setVisibility(View.INVISIBLE);
                                new AnimationFabtoCamera().reverse();
                            }
                        }
                    }, 500);
                }
            });
            return;
        }else if(requestCode==1){
            if(resultCode==RESULT_OK&& PLApplication.temp instanceof Book) {
                Book book = (Book) PLApplication.temp;
                String author = (book.author.length>0)?book.author[0]:"";
                BookListItem item = new BookListItem(book.id, book.isbn13, book.images.get("large"), book.title, author, book.summary);
                IndexFragment.getInstance().addNewBook(0, item);
            }
            loading.setVisibilityImmediate(View.INVISIBLE);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    ObjectAnimator bgColorAnimator = ObjectAnimator.ofObject(revealView,
                            "backgroundColor",
                            new ArgbEvaluator(),
                            ((ColorDrawable)revealView.getBackground()).getColor(),
                            getResources().getColor(R.color.colorAccent));
                    bgColorAnimator.setDuration(700);
                    bgColorAnimator.start();
                    new AnimationShowBookDetail().reverse(new CustomAnimatorListener() {
                        @Override
                        public void onAnimationEnd() {
                            new AnimationFabtoCamera().reverse();
                        }
                    });
                }
            }, 500);
            return;
        }else if(requestCode==2){
            return;
        }
        new Handler().postDelayed(new Runnable() {
            public void run() {
                new AnimationFabtoCamera().reverse();
            }
        }, 500);
    }
    private void startBookDetailActivity(final Book book){
        startBookDetailActivity(book, true, null);
    }
    private void startBookDetailActivity(final Book book, View view){
        startBookDetailActivity(book, false, view);
    }
    private void startBookDetailActivity(final Book book, final boolean scaned, final View cover){
        final String url = (book.images.get("large")==null)?book.image:book.images.get("large");
        ImageLoader.getInstance().loadImage(url, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                PLApplication.temp = book;
                PLApplication.bitmap = loadedImage;
                final View fromView = cover;
                Palette.generateAsync(loadedImage, new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        Palette.Swatch vibrant = palette.getVibrantSwatch();
                        Palette.Swatch darkVibrant = palette.getDarkVibrantSwatch();
                        Palette.Swatch darkmuted = palette.getDarkMutedSwatch();
                        Palette.Swatch top = (darkmuted != null) ? darkmuted : darkVibrant;
                        if (darkmuted != null && darkVibrant != null)
                            top = (darkmuted.getPopulation() >= darkVibrant.getPopulation()) ? darkmuted : darkVibrant;
                        Palette.Swatch muted = palette.getMutedSwatch();
                        Palette.Swatch lightmuted = palette.getLightMutedSwatch();
                        Palette.Swatch bottom = (lightmuted != null) ? lightmuted : muted;
                        Palette.Swatch fabColor = muted;
                        final Intent intent = new Intent(MainActivity.this, BookDetailActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putInt("topColor", top.getRgb());
                        bundle.putInt("topTextColor", top.getTitleTextColor());
                        bundle.putInt("bottomColor", bottom.getRgb());
                        bundle.putInt("bottomTextColor", bottom.getBodyTextColor());
                        bundle.putInt("titleBarColor", vibrant.getRgb());
                        bundle.putInt("titleTextColor", vibrant.getTitleTextColor());
                        bundle.putInt("fabColor", fabColor.getRgb());
                        intent.putExtras(bundle);
                        intent.putExtra("scaned", scaned);
                        if (scaned) {
                            loading.setVisibility(View.INVISIBLE);
                            ObjectAnimator bgColorAnimator = ObjectAnimator.ofObject(revealView,
                                    "backgroundColor",
                                    new ArgbEvaluator(),
                                    getResources().getColor(R.color.colorAccent),
                                    top.getRgb());
                            bgColorAnimator.setDuration(700);
                            bgColorAnimator.start();
                            extendBar.setBackgroundColor(vibrant.getRgb());
                            new AnimationShowBookDetail().start(new CustomAnimatorListener() {
                                @Override
                                public void onAnimationEnd() {
                                    startActivityForResult(intent, 1);
                                    MainActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                }
                            });
                        } else {
                            ActivityTransitionHelper.with(MainActivity.this)
                                    .fromView(fromView).startActivityForResult(intent, 2);
                        }
                    }
                });
            }
        });
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_index) {
            listSwitch.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_borrow) {
            listSwitch.setVisibility(View.INVISIBLE);
        } else if (id == R.id.nav_friend) {
            listSwitch.setVisibility(View.INVISIBLE);
        } else if (id == R.id.nav_whatshot) {
            listSwitch.setVisibility(View.INVISIBLE);
        } else if (id == R.id.nav_logout) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_exit) {
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onFragmentInteraction(int action, Object data, View view) {
        if(action==1&&data instanceof BookListItem){
                BookListItem item = (BookListItem) data;
                String bookString = (String) SharedPreferencesUtil.getData(this, "isbn13_"+item.isbn13, "");
                if(!bookString.equals("")){
                    Book book = new Gson().fromJson(bookString, Book.class);
                    startBookDetailActivity(book, view);
                }
        }
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
    private class AnimationFabtoCamera extends CustomAnimator {

        public void start(){
            animating = true;
            startFabX = (int) ViewUtils.centerX(fabAdd);
            startFabY = (int)ViewUtils.centerY(fabAdd);
            fabAdd.start();
            fabToCenter();
        }

        public void reverse(){
            animating = true;
            shrinkCameraLayout();
        }

        void fabToCenter(){
            fabAdd.setVisibility(View.VISIBLE);
            int endFabX = revealView.getWidth()/2;
            int endFabY = (int) (revealView.getHeight()*0.5f);
            ArcAnimator arcAnimator = ArcAnimator.createArcAnimator(fabAdd,
                    endFabX, endFabY, 90, Side.LEFT)
                    .setDuration(300);
            arcAnimator.setInterpolator(ACCELERATE_DECELERATE);
            arcAnimator.addListener(new SimpleListener(){
                @Override
                public void onAnimationEnd(Animator animation) {
                    fabAdd.setVisibility(View.INVISIBLE);
                    revealCameraLayout();
                }
            });
            arcAnimator.start();
        }

        void revealCameraLayout(){
            revealView.setVisibility(View.VISIBLE);
            float finalRadius = Math.max(revealView.getWidth(), revealView.getHeight()) * 1.5f;
            SupportAnimator animator = ViewAnimationUtils.createCircularReveal(revealView,
                    (int) ViewUtils.centerX(fabAdd), (int) ViewUtils.centerY(fabAdd),
                    fabAdd.getWidth() / 2f, finalRadius);
            animator.setDuration(CustomAnimator.ANIM_DURATION_LONG);
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
                    (int)ViewUtils.centerX(fabAdd), (int)ViewUtils.centerY(fabAdd),
                    finalRadius, fabAdd.getWidth() / 2f);
            animator.setDuration(CustomAnimator.ANIM_DURATION_LONG);
            animator.addListener(new SimpleListener() {
                @Override
                public void onAnimationEnd() {
                    revealView.setVisibility(View.INVISIBLE);
                    blank.setVisibility(View.INVISIBLE);
                    fabAdd.reverse();
                    fabToOrigin();
                }
            });
            animator.setInterpolator(DECELERATE);
            animator.start();
        }

        void fabToOrigin() {
            fabAdd.setVisibility(View.VISIBLE);
            ArcAnimator arcAnimator = ArcAnimator.createArcAnimator(fabAdd,
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

    private class AnimationShowBookDetail extends CustomAnimator{

        public void start(){
            startAnimation(true);
        }

        public void reverse(){
            startAnimation(false);
        }

        private void startAnimation(boolean show){
            animating = true;
            final ObjectAnimator headerAnimator = getHeaderAnimator(show);
            final ObjectAnimator extendBarAnimator = getExtendBarAnimator(show);
            AnimatorSet animatorSet = new AnimatorSet();
            if(show)
                animatorSet.playSequentially(headerAnimator, extendBarAnimator);
            else
                animatorSet.playSequentially(extendBarAnimator, headerAnimator );
            animatorSet.addListener(new SimpleListener() {
                @Override
                public void onAnimationEnd(com.nineoldandroids.animation.Animator animation) {
                    animating = false;
                    if (callBack != null)
                        callBack.onAnimationEnd();
                }
            });
            animatorSet.start();
        }

        ObjectAnimator getHeaderAnimator(boolean show){
            int startY = (show)?revealLayout.getBottom():revealView.getBottom();
            int endY = (show)?revealLayout.getTop()
                                +getResources().getDimensionPixelSize(R.dimen.book_detail_app_bar_height)
                            :revealLayout.getBottom();
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(revealView, "bottom",
                    startY, endY);
            objectAnimator.setDuration(500);
            objectAnimator.setInterpolator(ACCELERATE);
            objectAnimator.addListener(new SimpleListener() {
                @Override
                public void onAnimationStart(com.nineoldandroids.animation.Animator animation) {
                    revealView.setVisibility(View.VISIBLE);
                    blank.setVisibility(View.VISIBLE);
                }
            });
            return objectAnimator;
        }

        ObjectAnimator getExtendBarAnimator(final boolean show){
            int startX = (show)?revealLayout.getRight():0;
            int endX = (show)?0:revealLayout.getRight();
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(extendBar, "left",
                    startX, endX);
            objectAnimator.setDuration(500);
            objectAnimator.setInterpolator(ACCELERATE);
            objectAnimator.addListener(new SimpleListener() {
                @Override
                public void onAnimationStart(com.nineoldandroids.animation.Animator animation) {
                    extendBar.setVisibility(View.VISIBLE);
                }
                @Override
                public void onAnimationEnd(com.nineoldandroids.animation.Animator animation) {
                    if(!show)
                        extendBar.setVisibility(View.INVISIBLE);
                }
            });
            return objectAnimator;
        }
    }

    private static class SimpleListener implements SupportAnimator.AnimatorListener, com.nineoldandroids.animation.ObjectAnimator.AnimatorListener{
        @Override
        public void onAnimationStart() {}
        @Override
        public void onAnimationEnd() {}
        @Override
        public void onAnimationCancel() {}
        @Override
        public void onAnimationRepeat() {}
        @Override
        public void onAnimationStart(com.nineoldandroids.animation.Animator animation) {}
        @Override
        public void onAnimationEnd(com.nineoldandroids.animation.Animator animation) {}
        @Override
        public void onAnimationCancel(com.nineoldandroids.animation.Animator animation) {}
        @Override
        public void onAnimationRepeat(com.nineoldandroids.animation.Animator animation) {}
    }

    public abstract class CallBack{
        public abstract void action(Object data);
    }

}
