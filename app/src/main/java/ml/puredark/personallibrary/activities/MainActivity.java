package ml.puredark.personallibrary.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.telly.mrvector.MrVector;
import com.wnafee.vector.compat.ResourcesCompat;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import io.codetail.animation.arcanimator.ArcAnimator;
import io.codetail.animation.arcanimator.Side;

import io.codetail.widget.RevealFrameLayout;
import ml.puredark.personallibrary.PLApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.User;
import ml.puredark.personallibrary.beans.Book;
import ml.puredark.personallibrary.beans.BookListItem;
import ml.puredark.personallibrary.beans.Friend;
import ml.puredark.personallibrary.customs.MyCoordinatorLayout;
import ml.puredark.personallibrary.customs.MyEditText;
import ml.puredark.personallibrary.customs.MyFloatingActionButton;
import ml.puredark.personallibrary.fragments.FriendFragment;
import ml.puredark.personallibrary.fragments.IndexFragment;
import ml.puredark.personallibrary.fragments.NewsFragment;
import ml.puredark.personallibrary.helpers.ActivityTransitionHelper;
import ml.puredark.personallibrary.helpers.ActivityTransitionHelper.CustomAnimator;
import ml.puredark.personallibrary.helpers.ActivityTransitionHelper.CustomAnimatorListener;
import ml.puredark.personallibrary.helpers.DoubanRestAPI;
import ml.puredark.personallibrary.helpers.PLServerAPI;
import ml.puredark.personallibrary.utils.BitmapUtils;
import ml.puredark.personallibrary.utils.FileUtils;
import ml.puredark.personallibrary.utils.SharedPreferencesUtil;
import ml.puredark.personallibrary.utils.ViewUtils;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    //Fragment编号
    public final static int FRAGMENT_INDEX = 1;
    public final static int FRAGMENT_FRIEND = 3;
    public final static int FRAGMENT_NEWS = 4;

    //ActivityResult编号
    public final static int RESULT_SCANBOOK = 1;
    public final static int RESULT_BOOKDETAIL = 2;
    public final static int RESULT_AVATAR = 3;

    //记录当前加载的是哪个Fragment
    private int currFragment = FRAGMENT_INDEX;

    //主要元素
    private MyCoordinatorLayout mCoordinatorLayout;
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mCollapsingToolbar;
    private NavigationView navigationView;

    //搜索栏是否展开
    private boolean expanded = false;
    private MyEditText inputSearch;

    //动画相关元素
    private MyFloatingActionButton fabAdd;
    private RevealFrameLayout revealLayout;
    private View revealView, extendBar, blank;
    private ProgressBarCircularIndeterminate loading;
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
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, IndexFragment.getInstance(), IndexFragment.getInstance().getClass().getName())
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
        loading = (ProgressBarCircularIndeterminate) findViewById(R.id.loading);
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
        navigationView.setNavigationItemSelectedListener(this);
        listSwitch = (MaterialAnimatedSwitch) findViewById(R.id.list_switch);
        listSwitch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final boolean right) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        if(right)
                            IndexFragment.getInstance().setRecyclerViewToGrid();
                        else
                            IndexFragment.getInstance().setRecyclerViewToList();
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                                drawer.closeDrawer(GravityCompat.START);
                            }
                        }, 200);
                    }
                }, 300);
            }
        });
        //初始化侧边栏里的个人信息
        new Handler().postDelayed(new Runnable() {
            public void run() {
                ImageView avatar = (ImageView) findViewById(R.id.avatar);
                DisplayImageOptions options = new DisplayImageOptions.Builder()
                        .cacheInMemory(false)//设置下载的图片是否缓存在内存中
                        .cacheOnDisc(false)//设置下载的图片是否缓存在SD卡中
                        .build();//构建完成
                ImageLoader.getInstance()
                        .displayImage(PLApplication.serverHost + "/images/users/avatars/" + User.getUid() + ".png", avatar, options);

                avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent, RESULT_AVATAR);
                    }
                });
                final EditText nickname = (EditText) findViewById(R.id.nickname);
                final EditText signature = (EditText) findViewById(R.id.signature);
                nickname.setText(User.getNickname());
                signature.setText(User.getSignature());
                nickname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus)
                            PLServerAPI.modifyUserInfo(nickname.getText().toString(), 0, null, null, new PLServerAPI.onResponseListener() {
                                @Override
                                public void onSuccess(Object data) {
                                    User.setNickname(nickname.getText().toString());
                                }

                                @Override
                                public void onFailure(PLServerAPI.ApiError apiError) {
                                    Toast.makeText(MainActivity.this, apiError.getErrorString(), Toast.LENGTH_LONG);
                                    nickname.setText(User.getNickname());
                                }
                            });
                    }
                });
                signature.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if(!hasFocus)
                            PLServerAPI.modifyUserInfo(null, 0, signature.getText().toString(), null, new PLServerAPI.onResponseListener() {
                                @Override
                                public void onSuccess(Object data) {
                                    User.setSignature(signature.getText().toString());
                                }
                                @Override
                                public void onFailure(PLServerAPI.ApiError apiError) {
                                    Toast.makeText(MainActivity.this, apiError.getErrorString(), Toast.LENGTH_LONG);
                                    signature.setText(User.getSignature());
                                }
                            });
                    }
                });
            }
        }, 200);

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

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String keyword = inputSearch.getText().toString();
                IndexFragment.getInstance().getBookList(keyword);
            }
        });

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

    public void setCurrFragment(int curr){
        currFragment = curr;
    }

    public void replaceFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.container, fragment, fragment.getClass().getName())
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
        } else if(currFragment==FRAGMENT_INDEX){
            finish();
        } else if(currFragment!=FRAGMENT_INDEX){
            listSwitch.setVisibility(View.VISIBLE);
            replaceFragment(IndexFragment.getInstance());
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_index) {
            listSwitch.setVisibility(View.VISIBLE);
            replaceFragment(IndexFragment.getInstance());
        } else if (id == R.id.nav_borrow) {
            listSwitch.setVisibility(View.INVISIBLE);
        } else if (id == R.id.nav_friend) {
            listSwitch.setVisibility(View.INVISIBLE);
            replaceFragment(FriendFragment.getInstance());
        } else if (id == R.id.nav_whatshot) {
            listSwitch.setVisibility(View.INVISIBLE);
            replaceFragment(NewsFragment.getInstance());
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
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result != null&&result.getContents()!=null) {
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
        }else if(requestCode==RESULT_SCANBOOK){
            if(resultCode==RESULT_OK&& PLApplication.temp instanceof Book) {
                Book book = (Book) PLApplication.temp;
                String author = (book.author.length>0)?book.author[0]:"";
                BookListItem item = new BookListItem(book.id, book.isbn13, book.images.get("large"), book.title, author, book.summary);
                IndexFragment.getInstance().addNewBook(0, item);
            }
            loading.setVisibility(View.INVISIBLE);
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
        }else if(requestCode==RESULT_BOOKDETAIL){

        }else if(requestCode==RESULT_AVATAR){
            if (resultCode == RESULT_OK) {
                final Uri uri = intent.getData();

                // Get the File path from the Uri
                String path = FileUtils.getPath(this, uri);

                File cacheDir = getCacheDir();
                final String cachePath = cacheDir.getPath();    //缓存路径

                final String filePath = cachePath+"/"+User.getUid()+".png";

                // Alternatively, use FileUtils.getFile(Context, Uri)
                if (path != null && FileUtils.isLocal(path)) {
                    BitmapFactory.Options options =new BitmapFactory.Options();
                    options.inJustDecodeBounds =true;
                    // 获取这个图片的宽和高
                    Bitmap bitmap = BitmapFactory.decodeFile(path, options); //此时返回bm为空
                    options.inJustDecodeBounds =false;
                    //计算缩放比
                    int be = (int)(options.outHeight/ (float)256);
                    if (be <= 0)
                        be = 1;
                    options.inSampleSize = be;
                    //重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false
                    bitmap=BitmapFactory.decodeFile(path,options);
                    bitmap = BitmapUtils.cropToSquare(bitmap);
                    ImageView iv=new ImageView(this);
                    iv.setImageBitmap(bitmap);
                    File file=new File(filePath);
                    try {
                        FileOutputStream out=new FileOutputStream(file);
                        if(bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)){
                            out.flush();
                            out.close();
                        }
                    } catch (FileNotFoundException e){
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(filePath);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int count = 0;
                    try {
                        while((count = fis.read(buffer)) >= 0){
                            baos.write(buffer, 0, count);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final ImageView avatar = (ImageView) findViewById(R.id.avatar);
                    PLServerAPI.uploadAvatar(file, new PLServerAPI.onResponseListener() {
                        @Override
                        public void onSuccess(Object data){
                            avatar.setImageDrawable(Drawable.createFromPath(filePath));
                        }

                        @Override
                        public void onFailure(PLServerAPI.ApiError apiError) {
                            Toast.makeText(MainActivity.this, apiError.getErrorString(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }else{
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    new AnimationFabtoCamera().reverse();
                }
            }, 500);
        }
    }
    public void startBookDetailActivity(final Book book){
        startBookDetailActivity(book, true, null);
    }
    public void startBookDetailActivity(final Book book, View view){
        startBookDetailActivity(book, false, view);
    }
    public void startBookDetailActivity(final Book book, final boolean scaned, final View cover){
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
                                    startActivityForResult(intent, RESULT_SCANBOOK);
                                    MainActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                }
                            });
                        } else {
                            ActivityTransitionHelper.with(MainActivity.this)
                                    .fromView(fromView).startActivityForResult(intent, RESULT_BOOKDETAIL);
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

    public void setMainTitle(String title){
        mCollapsingToolbar.setTitle(title);
    }
    public void setNavigationItemSelected(int itemId){
        navigationView.setCheckedItem(itemId);
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
                    .setDuration(CustomAnimator.ANIM_DURATION_MEDIUM);
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
            blank.setVisibility(View.INVISIBLE);
            float finalRadius = Math.max(revealView.getWidth(), revealView.getHeight()) * 1.5f;
            SupportAnimator animator = ViewAnimationUtils.createCircularReveal(revealView,
                    (int)ViewUtils.centerX(fabAdd), (int)ViewUtils.centerY(fabAdd),
                    finalRadius, fabAdd.getWidth() / 2f);
            animator.setDuration(CustomAnimator.ANIM_DURATION_LONG);
            animator.addListener(new SimpleListener() {
                @Override
                public void onAnimationEnd() {
                    revealView.setVisibility(View.INVISIBLE);
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
                    .setDuration(CustomAnimator.ANIM_DURATION_MEDIUM);
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

    public abstract static class CallBack{
        public abstract void action(Object data);
    }

}
