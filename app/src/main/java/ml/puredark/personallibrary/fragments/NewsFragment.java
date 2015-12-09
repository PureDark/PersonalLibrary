package ml.puredark.personallibrary.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.personallibrary.PLApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.User;
import ml.puredark.personallibrary.activities.MainActivity;
import ml.puredark.personallibrary.adapters.BookMarkAdapter;
import ml.puredark.personallibrary.beans.Book;
import ml.puredark.personallibrary.beans.BookListItem;
import ml.puredark.personallibrary.beans.BookMark;
import ml.puredark.personallibrary.dataprovider.BookListDataProvider;
import ml.puredark.personallibrary.dataprovider.BookMarkDataProvider;
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
                //if (newsItemClicked == false) {
                    //newsItemClicked = true;
                    System.out.println("view.getId()="+view.getId()+" R.id.book="+R.id.book);
                    if(view.getId()==R.id.book) {
                        final BookMark item = (BookMark) mBookMarkAdapter.getDataProvider().getItem(postion);
                        System.out.println("book_title="+item.book_title+" isbn13="+item.isbn13);
                        String bookString = (String) SharedPreferencesUtil.getData(PLApplication.mContext, "isbn13_"+item.isbn13, "");
                        if(!bookString.equals("")){
                            Book book = new Gson().fromJson(bookString, Book.class);
                            book.id = item.bid;
                            System.out.println("book.price="+book.price);
                            mActivity.startBookDetailActivity(book, view);
                        }else{
                            System.out.println("getDouban");
                            DoubanRestAPI.getBookByISBN(item.isbn13, new MainActivity.CallBack() {
                                @Override
                                public void action(final Object obj) {
                                    new Handler().postDelayed(new Runnable() {
                                        public void run() {
                                            if (obj instanceof Book) {
                                                System.out.println("Book");
                                                Book book = (Book) obj;
                                                book.id = item.bid;
                                                mActivity.startBookDetailActivity(book, view);
                                                SharedPreferencesUtil.saveData(PLApplication.mContext, "isbn13_" + book.isbn13, new Gson().toJson(book));
                                            }
                                        }
                                    }, 500);
                                }
                            });
                        }
                    }else{
                        System.out.println("fuck up!!!!!!!!");
                        //TODO: 打开书评详情页面
                    }
                }
            //}
        });

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mBookMarkAdapter);

        return rootView;
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