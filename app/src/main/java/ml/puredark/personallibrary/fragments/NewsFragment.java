package ml.puredark.personallibrary.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.personallibrary.PLApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.User;
import ml.puredark.personallibrary.activities.MainActivity;
import ml.puredark.personallibrary.activities.BookMarkActivity;
import ml.puredark.personallibrary.adapters.BookMarkAdapter;
import ml.puredark.personallibrary.beans.Book;
import ml.puredark.personallibrary.beans.BookMark;
import ml.puredark.personallibrary.dataprovider.BookMarkDataProvider;
import ml.puredark.personallibrary.helpers.ActivityTransitionHelper;
import ml.puredark.personallibrary.helpers.DoubanRestAPI;
import ml.puredark.personallibrary.helpers.PLServerAPI;
import ml.puredark.personallibrary.utils.SharedPreferencesUtil;

;

public class NewsFragment extends Fragment {
    private static NewsFragment mInstance;
    private View rootView;

    private MainActivity mActivity;

    //首页书籍列表
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private BookMarkAdapter mBookMarkAdapter;

    //item已点击(避免多次点击同时打开多个Activity)
    private boolean newsItemClicked = false;

    public static NewsFragment newInstance() {
        mInstance = new NewsFragment();
        return mInstance;
    }
    public static NewsFragment getInstance() {
        if(mInstance!=null)
            return mInstance;
        else
            return newInstance();
    }

    public NewsFragment() {
    }

    //简化查询方法
    private View findViewById(int resId){
        return rootView.findViewById(resId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_news, container, false);

        mActivity.setToolbarUncollapsible();
        mActivity.setCurrFragment(MainActivity.FRAGMENT_NEWS);
        mActivity.setMainTitle(getResources().getString(R.string.title_fragment_news));
        mActivity.setNavigationItemSelected(R.id.nav_whatshot);

        //初始化书籍列表相关变量
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        //指定为线性列表
        mLayoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);

        List<BookMark> bookMarks = new ArrayList<>();
        String data = (String) SharedPreferencesUtil.getData(this.getContext(), "news", "");
        if(data!=null&&!data.equals(""))
            bookMarks = new Gson().fromJson(data, new TypeToken<List<BookMark>>(){}.getType());

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
                        if(view.getId()==R.id.book)
                            mActivity.startBookDetailActivity(book, view);
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
                                            if(view.getId()==R.id.book)
                                                mActivity.startBookDetailActivity(book, view);
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

        return rootView;
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
                        final Intent intent = new Intent(mActivity, BookMarkActivity.class);
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

    public void showSnackBar(String content){
        Snackbar snackbar = Snackbar.make(
                findViewById(R.id.container),
                content,
                Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(ContextCompat.getColor(PLApplication.mContext, R.color.colorAccentDark));
        snackbar.show();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mActivity = (MainActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onResume(){
        super.onResume();
        newsItemClicked = false;
        //从服务器获取最新的动态
        getRecentBookMarks(User.getUid());
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        SharedPreferencesUtil.saveData(this.getContext(), "news", new Gson().toJson(mBookMarkAdapter.getDataProvider().getItems()));
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}