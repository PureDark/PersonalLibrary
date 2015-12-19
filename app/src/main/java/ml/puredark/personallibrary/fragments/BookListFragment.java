package ml.puredark.personallibrary.fragments;

import android.app.Activity;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.personallibrary.PLApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.User;
import ml.puredark.personallibrary.activities.MainActivity;
import ml.puredark.personallibrary.activities.MyActivity;
import ml.puredark.personallibrary.adapters.BookListAdapter;
import ml.puredark.personallibrary.beans.Book;
import ml.puredark.personallibrary.beans.BookListItem;
import ml.puredark.personallibrary.customs.EmptyRecyclerView;
import ml.puredark.personallibrary.dataprovider.BookListDataProvider;
import ml.puredark.personallibrary.helpers.DoubanRestAPI;
import ml.puredark.personallibrary.helpers.PLServerAPI;
import ml.puredark.personallibrary.utils.SharedPreferencesUtil;

;

public class BookListFragment extends Fragment {
    private static BookListFragment mInstance;
    private View rootView;

    // 要获取书籍的用户编号
    private int uid;

    //首页书籍列表
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLinearLayoutManager,mGridLayoutManager;
    private BookListAdapter mBookAdapter;

    //书籍已点击(避免多次点击同时打开多个Activity)
    private boolean bookItemClicked = false;

    public static BookListFragment newInstance() {
        mInstance = new BookListFragment();
        return mInstance;
    }
    public static BookListFragment getInstance() {
        if(mInstance!=null)
            return mInstance;
        else
            return newInstance();
    }

    public BookListFragment() {
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
        rootView = inflater.inflate(R.layout.list_friend_book, container, false);

        ((MyActivity)getActivity()).setCurrFragment(MyActivity.FRAGMENT_BOOK_LIST);

        uid = getArguments().getInt("uid");

        //初始化书籍列表相关变量
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        List<BookListItem> myBooks = new ArrayList<>();

        BookListDataProvider mBookListDataProvider = new BookListDataProvider(myBooks);
        mBookAdapter = new BookListAdapter(mBookListDataProvider);
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
                        book.uid = uid;
                        MainActivity.getInstance().startBookDetailActivity(book, view);
                    } else {
                        DoubanRestAPI.getBookByISBN(item.isbn13, new MainActivity.CallBack() {
                            @Override
                            public void action(final Object obj) {
                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        if (obj instanceof Book) {
                                            Book book = (Book) obj;
                                            book.id = item.getId();
                                            book.uid = uid;
                                            MainActivity.getInstance().startBookDetailActivity(book, view);
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

        //指定为线性列表
        mLinearLayoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        mGridLayoutManager = new GridLayoutManager(this.getContext(), 3);

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mBookAdapter);

        mRecyclerView.addItemDecoration(new ItemShadowDecorator(
                (NinePatchDrawable) ContextCompat.getDrawable(this.getContext(), R.drawable.material_shadow_z1)));


        //从服务器获取最新的书籍列表
        getBookList(uid, null);
        return rootView;
    }


    public void addNewBook(final int position, BookListItem book) {
        PLServerAPI.addBook(book.isbn13, book.cover, book.title, book.author, book.summary,
                new PLServerAPI.onResponseListener() {
                    @Override
                    public void onSuccess(Object data) {
                        BookListItem book = (BookListItem) data;
                        mBookAdapter.getDataProvider().addItem(position, book);
                        mBookAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(PLServerAPI.ApiError apiError) {
                        showSnackBar(apiError.getErrorString());
                    }
                });
    }

    public void getBookList(int uid, String keyword){
        PLServerAPI.getBookList(uid, null, keyword, new PLServerAPI.onResponseListener() {
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
        View container = findViewById(R.id.container);
        if(container==null)return;
        Snackbar snackbar = Snackbar.make(
                container,
                content,
                Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(ContextCompat.getColor(PLApplication.mContext, R.color.colorAccentDark));
        snackbar.show();
    }

    public void setRecyclerViewToList() {
        mBookAdapter.setIsGrid(false);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mBookAdapter);
    }
    public void setRecyclerViewToGrid() {
        mBookAdapter.setIsGrid(true);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setAdapter(mBookAdapter);
    }

    @Override
    public void onResume(){
        super.onResume();
        bookItemClicked = false;
    }

}