package ml.puredark.personallibrary.fragments;

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

import java.util.ArrayList;
import java.util.List;

import ml.puredark.personallibrary.PLApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.activities.MainActivity;
import ml.puredark.personallibrary.activities.MyActivity;
import ml.puredark.personallibrary.adapters.BookMarkAdapter;
import ml.puredark.personallibrary.beans.Book;
import ml.puredark.personallibrary.beans.BookMark;
import ml.puredark.personallibrary.dataprovider.BookMarkDataProvider;
import ml.puredark.personallibrary.helpers.DoubanRestAPI;
import ml.puredark.personallibrary.helpers.PLServerAPI;
import ml.puredark.personallibrary.utils.SharedPreferencesUtil;

public class BookMarkListFragment extends Fragment {
    private static BookMarkListFragment mInstance;
    private View rootView;

    private Book book;

    //首页书籍列表
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private BookMarkAdapter mBookMarkAdapter;

    //item已点击(避免多次点击同时打开多个Activity)
    private boolean markItemClicked = false;

    public static BookMarkListFragment newInstance() {
        mInstance = new BookMarkListFragment();
        return mInstance;
    }
    public static BookMarkListFragment getInstance() {
        if(mInstance!=null)
            return mInstance;
        else
            return newInstance();
    }

    public BookMarkListFragment() {
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

        ((MyActivity)getActivity()).setCurrFragment(MyActivity.FRAGMENT_BOOK_MARK_LIST);

        book = (Book) PLApplication.temp;

        //初始化书籍列表相关变量
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        //指定为线性列表
        mLayoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);

        List<BookMark> bookMarks = new ArrayList<>();

        //从服务器获取最新的动态
        getBookMarkList(book.id, book.uid);

        BookMarkDataProvider mBookMarkDataProvider = new BookMarkDataProvider(bookMarks);
        mBookMarkAdapter = new BookMarkAdapter(mBookMarkDataProvider);
        mBookMarkAdapter.setOnItemClickListener(new BookMarkAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(final View view, int postion) {
                if (markItemClicked == false) {
                    markItemClicked = true;
                    final BookMark bookMark = (BookMark) mBookMarkAdapter.getDataProvider().getItem(postion);
                    String bookString = (String) SharedPreferencesUtil.getData(PLApplication.mContext, "isbn13_"+bookMark.isbn13, "");
                    if(!bookString.equals("")){
                        Book book = new Gson().fromJson(bookString, Book.class);
                        book.id = bookMark.bid;
                        book.uid = bookMark.uid;
                        if(view.getId()==R.id.book)
                            MainActivity.getInstance().startBookDetailActivity(book, view);
                        else
                            viewBookMark(bookMark);
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
                                                viewBookMark(bookMark);
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

    public void viewBookMark(final BookMark bookMark){
        Fragment viewBookMark = new ViewBookMarkFragment();
        Bundle newbund = new Bundle();
        newbund.putString("bookMark", new Gson().toJson(bookMark));
        viewBookMark.setArguments(newbund);
        ((MyActivity)getActivity()).replaceFragment(viewBookMark);
    }

    public void getBookMarkList(int bid, int uid){
        PLServerAPI.getBookMarkList(bid, uid, new PLServerAPI.onResponseListener() {
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
        View container = findViewById(R.id.container);
        if(container==null)return;
        Snackbar snackbar = Snackbar.make(
                container,
                content,
                Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(ContextCompat.getColor(PLApplication.mContext, R.color.colorAccentDark));
        snackbar.show();
    }

    @Override
    public void onResume(){
        super.onResume();
        markItemClicked = false;
    }


}
