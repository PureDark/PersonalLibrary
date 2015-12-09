package ml.puredark.personallibrary.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.personallibrary.PLApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.User;
import ml.puredark.personallibrary.adapters.ViewPagerAdapter;
import ml.puredark.personallibrary.beans.Book;
import ml.puredark.personallibrary.beans.BookMark;
import ml.puredark.personallibrary.customs.MyCoordinatorLayout;
import ml.puredark.personallibrary.helpers.PLServerAPI;
import ml.puredark.personallibrary.utils.DensityUtils;

public class BookMarkActivity extends AppCompatActivity {
    // 书评所关联的书籍的ID
    private BookMark bookMark;
    private Book book;

    private MyCoordinatorLayout mCoordinatorLayout;
    private AppBarLayout mAppBarLayout;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private View viewBookMark,viewBookDetails;

    //书评View相关引用
    private ImageView mAvatarView;
    private TextView tvNickname,tvSignature;
    private TextView tvMarkTitle,tvMarkContent;

    //书籍详情View相关引用
    private ImageView mBookCover;
    private TextView tvAuthor,tvPages,tvPrice,tvPubdate,tvIsbn;
    private TextView mBookTitle,mBookSummary;

    // 正在提交标志
    private boolean posting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_mark);
        mCoordinatorLayout = (MyCoordinatorLayout) findViewById(R.id.coordinator_layout);
        mAppBarLayout = (AppBarLayout)findViewById(R.id.appbar);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        book = (Book) PLApplication.temp;
        Bundle bundle = getIntent().getExtras();
        if(bundle==null)finish();
        String bookMarkJson = bundle.getString("bookMark");
        if(bookMarkJson==null)finish();
        bookMark = new Gson().fromJson(bookMarkJson,BookMark.class);

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
        viewBookMark = getLayoutInflater().inflate(R.layout.view_book_mark, null);
        viewBookDetails = getLayoutInflater().inflate(R.layout.view_book_details, null);
        views.add(viewBookMark);
        views.add(viewBookDetails);
        List<String> titles = new ArrayList<String>();
        titles.add("书评");
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

        mAvatarView = (ImageView)viewBookMark.findViewById(R.id.avatar);
        tvNickname = (TextView)viewBookMark.findViewById(R.id.nickname);
        tvSignature = (TextView)viewBookMark.findViewById(R.id.signature);
        tvMarkTitle = (TextView)viewBookMark.findViewById(R.id.mark_title);
        tvMarkContent = (TextView)viewBookMark.findViewById(R.id.mark_content);

        tvNickname.setText(bookMark.nickname);
        tvSignature.setText(bookMark.signature);
        tvMarkTitle.setText(bookMark.title);
        //tvMarkContent.setText(bookMark.content);

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(false)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();//构建完成
        ImageLoader.getInstance().displayImage(PLApplication.serverHost + "/images/users/avatars/" + bookMark.uid + ".png", mAvatarView, options);

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


        PLServerAPI.getBookMarkDetails(bookMark.mid, new PLServerAPI.onResponseListener() {
            @Override
            public void onSuccess(Object data) {
                bookMark = (BookMark) data;
                tvNickname.setText(bookMark.nickname);
                tvSignature.setText(bookMark.signature);
                tvMarkTitle.setText(bookMark.title);
                tvMarkContent.setText(bookMark.content);
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
                    Toast.makeText(BookMarkActivity.this, "书评删除成功！", Toast.LENGTH_LONG).show();
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
