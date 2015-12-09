package ml.puredark.personallibrary.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.personallibrary.PLApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.User;
import ml.puredark.personallibrary.adapters.BookMarkAdapter;
import ml.puredark.personallibrary.adapters.ViewPagerAdapter;
import ml.puredark.personallibrary.beans.Book;
import ml.puredark.personallibrary.beans.BookMark;
import ml.puredark.personallibrary.customs.MyCoordinatorLayout;
import ml.puredark.personallibrary.dataprovider.BookMarkDataProvider;
import ml.puredark.personallibrary.helpers.DoubanRestAPI;
import ml.puredark.personallibrary.helpers.PLServerAPI;
import ml.puredark.personallibrary.utils.DensityUtils;
import ml.puredark.personallibrary.utils.SharedPreferencesUtil;

public class BookMarkListActivity extends AppCompatActivity {
    // 书评所关联的书籍的ID
    private BookMark bookMark;
    private Book book;

    private MyCoordinatorLayout mCoordinatorLayout;
    private AppBarLayout mAppBarLayout;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private View viewBookMarkList,viewBookDetails;

    //书籍列表View相关引用
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private BookMarkAdapter mBookMarkAdapter;

    //item已点击(避免多次点击同时打开多个Activity)
    private boolean newsItemClicked = false;

    //书籍详情View相关引用
    private ImageView mBookCover;
    private TextView tvAuthor,tvPages,tvPrice,tvPubdate,tvIsbn;
    private TextView mBookTitle,mBookSummary;

    // 正在提交标志
    private boolean posting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_mark_list);
        mCoordinatorLayout = (MyCoordinatorLayout) findViewById(R.id.coordinator_layout);
        mAppBarLayout = (AppBarLayout)findViewById(R.id.appbar);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        book = (Book) PLApplication.temp;
        Bundle bundle = getIntent().getExtras();
        if(bundle==null)finish();

        //设置标题为书名
        setTitle(book.title);

        //设置appbar颜色为封面提取色
        mAppBarLayout.setBackgroundColor(bundle.getInt("topColor"));
        toolbar.setBackgroundColor(bundle.getInt("topColor"));

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float alpha = Math.max((24f + DensityUtils.px2dp(PLApplication.mContext, verticalOffset)) / 24f, 0);
                toolbar.setAlpha(alpha);
            }
        });

        // 设置Tab和对应的ViewPager
        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        mTabLayout.setBackgroundColor(bundle.getInt("topColor"));
        mTabLayout.setTabTextColors(bundle.getInt("topTextColor"),getResources().getColor(R.color.white));

        List<View> views = new ArrayList<>();
        viewBookMarkList = getLayoutInflater().inflate(R.layout.list_news, null);
        viewBookDetails = getLayoutInflater().inflate(R.layout.view_book_details, null);
        views.add(viewBookMarkList);
        views.add(viewBookDetails);
        List<String> titles = new ArrayList<String>();
        titles.add("书评列表");
        titles.add("书籍详情");
        ViewPagerAdapter mAdapter = new ViewPagerAdapter(views, titles);
        mTabLayout.setTabsFromPagerAdapter(mAdapter);
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                if(position==1)
                    setToolbarUncollapsible();
                else
                    setToolbarCollapsible();
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


        //初始化书籍列表相关变量
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        //指定为线性列表
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        List<BookMark> bookMarks = new ArrayList<>();

        //从服务器获取最新的动态
        getRecentBookMarks(User.getUid());

        BookMarkDataProvider mBookMarkDataProvider = new BookMarkDataProvider(bookMarks);
        mBookMarkAdapter = new BookMarkAdapter(mBookMarkDataProvider);
        mBookMarkAdapter.setOnItemClickListener(new BookMarkAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(final View view, int postion) {
                if (newsItemClicked == false) {
                    newsItemClicked = true;
                    final BookMark bookMark = (BookMark) mBookMarkAdapter.getDataProvider().getItem(postion);
                    String bookString = (String) SharedPreferencesUtil.getData(PLApplication.mContext, "isbn13_"+bookMark.isbn13, "");
                    if(!bookString.equals("")){
                        Book book = new Gson().fromJson(bookString, Book.class);
                        book.id = bookMark.bid;
                        book.uid = bookMark.uid;
                        if(view.getId()==R.id.book)
                            MainActivity.getInstance().startBookDetailActivity(book, view);
                        else
                            startViewBookMarkActivity(bookMark, book);
                    }else{
                        DoubanRestAPI.getBookByISBN(bookMark.isbn13, new MainActivity.CallBack() {
                            @Override
                            public void action(final Object obj) {
                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        if (obj instanceof Book) {
                                            Book book = (Book) obj;
                                            book.id = bookMark.bid;
                                            book.uid = bookMark.uid;
                                            if (view.getId() == R.id.book)
                                                MainActivity.getInstance().startBookDetailActivity(book, view);
                                            else
                                                startViewBookMarkActivity(bookMark, book);
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

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mBookMarkAdapter);




        //书籍详情View相关内容
        mBookCover = (ImageView)viewBookDetails.findViewById(R.id.book_cover);
        tvAuthor = (TextView)viewBookDetails.findViewById(R.id.author);
        tvPages = (TextView)viewBookDetails.findViewById(R.id.pages);
        tvPrice = (TextView)viewBookDetails.findViewById(R.id.price);
        tvPubdate = (TextView)viewBookDetails.findViewById(R.id.pubdate);
        tvIsbn = (TextView)viewBookDetails.findViewById(R.id.isbn);
        mBookTitle = (TextView)viewBookDetails.findViewById(R.id.book_title);
        mBookSummary = (TextView)viewBookDetails.findViewById(R.id.book_summary);

        Bitmap cover = PLApplication.bitmap;
        /* 修改界面文字 */
        mBookTitle.setText(book.title);
        mBookSummary.setText(book.summary);
        mBookCover.setImageBitmap(cover);
        String author = (book.author.length>0)?book.author[0]:(book.translator.length>0)?book.translator[0]+"[译]":"";
        tvAuthor.setText(author);
        tvPages.setText(book.pages + "页");
        tvPrice.setText(book.price);
        tvPubdate.setText(book.pubdate + "出版");
        tvIsbn.setText(book.isbn13);
        /* 修改UI颜色 */
        viewBookDetails.findViewById(R.id.title_bar).setBackgroundColor(bundle.getInt("titleBarColor"));
        mBookTitle.setTextColor(bundle.getInt("titleTextColor"));
        viewBookDetails.findViewById(R.id.toolbar_layout).setBackgroundColor(bundle.getInt("topColor"));
        mViewPager.setBackgroundColor(bundle.getInt("bottomColor"));
        mBookSummary.setTextColor(bundle.getInt("bottomTextColor"));
        setInfoIconColor(bundle.getInt("topTextColor"));
        setInfoTextColor(bundle.getInt("topTextColor"));

    }

    public void startViewBookMarkActivity(final BookMark bookMark, final Book book){
        final String url = (book.images.get("large")==null)?book.image:book.images.get("large");
        ImageLoader.getInstance().loadImage(url, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                PLApplication.temp = book;
                PLApplication.bitmap = loadedImage;
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
                        final Intent intent = new Intent(BookMarkListActivity.this, BookMarkActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("bookMark", new Gson().toJson(bookMark));
                        bundle.putInt("topColor", top.getRgb());
                        bundle.putInt("topTextColor", top.getTitleTextColor());
                        bundle.putInt("bottomColor", bottom.getRgb());
                        bundle.putInt("bottomTextColor", bottom.getBodyTextColor());
                        bundle.putInt("titleBarColor", vibrant.getRgb());
                        bundle.putInt("titleTextColor", vibrant.getTitleTextColor());
                        bundle.putInt("fabColor", fabColor.getRgb());
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });
            }
        });
    }

    public void getRecentBookMarks(int uid){
        PLServerAPI.getRecentBookMarks(uid, new PLServerAPI.onResponseListener() {
            @Override
            public void onSuccess(Object data) {
                List<BookMark> bookMarks = (List<BookMark>) data;
                mBookMarkAdapter.setDataProvider(new BookMarkDataProvider(bookMarks));
                mBookMarkAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(PLServerAPI.ApiError apiError) {
                showSnackBar(apiError.getErrorString());
            }
        });
    }

    private void setInfoIconColor(int color){
        LinearLayout book_info_layout = (LinearLayout) viewBookDetails.findViewById(R.id.book_info_layout);
        int count = book_info_layout.getChildCount();
        LinearLayout childAt;
        for(int i=0;i<count;i++) {
            childAt = (LinearLayout) book_info_layout.getChildAt(i);
            ((MaterialIconView)childAt.getChildAt(0)).setColor(color);
        }
    }
    private void setInfoTextColor(int color){
        LinearLayout book_info_layout = (LinearLayout) viewBookDetails.findViewById(R.id.book_info_layout);
        int count = book_info_layout.getChildCount();
        LinearLayout childAt;
        for(int i=0;i<count;i++) {
            childAt = (LinearLayout) book_info_layout.getChildAt(i);
            ((TextView)childAt.getChildAt(1)).setTextColor(color);
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

    public void showSnackBar(String content){
        Snackbar snackbar = Snackbar.make(
                findViewById(R.id.container),
                content,
                Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(ContextCompat.getColor(PLApplication.mContext, R.color.colorAccentDark));
        snackbar.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 如果书评是当前用户发布的
        if(bookMark.uid == User.getUid())
            getMenuInflater().inflate(R.menu.book_mark, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete&&posting==false) {
            posting = true;
            PLServerAPI.deleteBookMark(bookMark.mid, new PLServerAPI.onResponseListener() {
                @Override
                public void onSuccess(Object data) {
                    posting = false;
                    Toast.makeText(BookMarkListActivity.this, "书评删除成功！", Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onFailure(PLServerAPI.ApiError apiError) {
                    posting = false;
                    showSnackBar(apiError.getErrorString());
                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
