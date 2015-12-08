package ml.puredark.personallibrary.fragments;

import android.app.Activity;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;;
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
import ml.puredark.personallibrary.activities.MainActivity;
import ml.puredark.personallibrary.adapters.BookListAdapter;
import ml.puredark.personallibrary.beans.Book;
import ml.puredark.personallibrary.beans.BookListItem;
import ml.puredark.personallibrary.customs.EmptyRecyclerView;
import ml.puredark.personallibrary.dataprovider.BookListDataProvider;
import ml.puredark.personallibrary.helpers.DoubanRestAPI;
import ml.puredark.personallibrary.helpers.PLServerAPI;
import ml.puredark.personallibrary.utils.SharedPreferencesUtil;

public class IndexFragment extends Fragment {
    private static IndexFragment mInstance;
    private View rootView;

    private MainActivity mActivity;

    //首页书籍列表
    private EmptyRecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLinearLayoutManager,mGridLayoutManager;
    private RecyclerView.Adapter mWrappedAdapter;
    private BookListAdapter mBookAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;

    //书籍已点击(避免多次点击同时打开多个Activity)
    private boolean bookItemClicked = false;

    public static IndexFragment newInstance() {
        mInstance = new IndexFragment();
        return mInstance;
    }
    public static IndexFragment getInstance() {
        if(mInstance!=null)
            return mInstance;
        else
            return newInstance();
    }

    public IndexFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_index, container, false);

        mActivity.setToolbarCollapsible();
        mActivity.setCurrFragment(MainActivity.FRAGMENT_INDEX);
        mActivity.setMainTitle(getResources().getString(R.string.title_fragment_index));
        mActivity.setNavigationItemSelected(R.id.nav_index);

        //初始化书籍列表相关变量
        mRecyclerView = (EmptyRecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setEmptyView(rootView.findViewById(R.id.empty_view));
        mRecyclerView.setHasFixedSize(true);

        //从本地载入书籍缓存
        List<BookListItem> myBooks = new ArrayList<>();
        String data = (String) SharedPreferencesUtil.getData(this.getContext(), "books", "");
        if(data!=null&&!data.equals(""))
            myBooks = new Gson().fromJson(data, new TypeToken<List<BookListItem>>(){}.getType());

        //从服务器获取最新的书籍列表
        getBookList(null);

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        // drag & drop manager
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();

        // swipe manager
        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        // 拖拽时的阴影
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(
                (NinePatchDrawable) ContextCompat.getDrawable(this.getContext(), R.drawable.material_shadow_z3));

        // 长按开启拖拽
        mRecyclerViewDragDropManager.setInitiateOnMove(false);
        mRecyclerViewDragDropManager.setInitiateOnLongPress(true);

        BookListDataProvider mBookListDataProvider = new BookListDataProvider(myBooks);
        mBookAdapter = new BookListAdapter(mBookListDataProvider);
        mBookAdapter.setOnItemClickListener(new BookListAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(final View view, int postion) {
                if (bookItemClicked == false) {
                    bookItemClicked = true;
                    final BookListItem item = (BookListItem) mBookAdapter.getDataProvider().getItem(postion);
                    String bookString = (String) SharedPreferencesUtil.getData(PLApplication.mContext, "isbn13_"+item.isbn13, "");
                    if(!bookString.equals("")){
                        Book book = new Gson().fromJson(bookString, Book.class);
                        book.id = item.getId();
                        mActivity.startBookDetailActivity(book, view);
                    }else{
                        DoubanRestAPI.getBookByISBN(item.isbn13, new MainActivity.CallBack() {
                            @Override
                            public void action(final Object obj) {
                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        if (obj instanceof Book) {
                                            Book book = (Book) obj;
                                            book.id = item.getId();
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
        mBookAdapter.setEventListener(new BookListAdapter.EventListener() {
            @Override
            public void onItemRemoved(int position, final BookListItem book) {
                PLServerAPI.deleteBook(book.getId(), new PLServerAPI.onResponseListener() {
                    @Override
                    public void onSuccess(Object data) {
                    }
                    @Override
                    public void onFailure(PLServerAPI.ApiError apiError) {
                        showSnackBar(getString(R.string.network_error));
                    }
                });
                Snackbar snackbar = Snackbar.make(
                        findViewById(R.id.container),
                        R.string.item_removed,
                        Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.snack_bar_action_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = mBookAdapter.getDataProvider().undoLastRemoval();
                        if (position >= 0) {
                            notifyItemInserted(position);
                        }
                        PLServerAPI.addBook(book.isbn13, book.cover, book.title, book.author, book.summary, new PLServerAPI.onResponseListener() {
                            @Override
                            public void onSuccess(Object data) {
                            }

                            @Override
                            public void onFailure(PLServerAPI.ApiError apiError) {
                                showSnackBar(getString(R.string.network_error));
                            }
                        });
                    }
                });
                snackbar.setActionTextColor(ContextCompat.getColor(PLApplication.mContext, R.color.colorAccentDark));
                snackbar.show();
            }
        });

        // wrap for dragging
        mWrappedAdapter =  mRecyclerViewDragDropManager.createWrappedAdapter(mBookAdapter);
        // wrap for swiping
        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(mWrappedAdapter);

        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();

        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        animator.setSupportsChangeAnimations(false);

        //指定为线性列表
        mLinearLayoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        mGridLayoutManager = new GridLayoutManager(this.getContext(), 3);

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);  // 设置的是处理过的mWrappedAdapter
        mRecyclerView.setItemAnimator(animator);

        mRecyclerView.setRecyclerListener(new RecyclerView.RecyclerListener() {
            @Override
            public void onViewRecycled(RecyclerView.ViewHolder holder) {

            }
        });

        mRecyclerView.addItemDecoration(new ItemShadowDecorator(
                (NinePatchDrawable) ContextCompat.getDrawable(this.getContext(), R.drawable.material_shadow_z1)));

        // NOTE:
        // The initialization order is very important! This order determines the priority of touch event handling.
        //
        // priority: TouchActionGuard > Swipe > DragAndDrop
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewSwipeManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);

        return rootView;
    }

    public void addNewBook(final int position, BookListItem book) {
        PLServerAPI.addBook(book.isbn13, book.cover, book.title, book.author, book.summary,
            new PLServerAPI.onResponseListener() {
                @Override
                public void onSuccess(Object data) {
                    BookListItem book = (BookListItem)data;
                    mBookAdapter.getDataProvider().addItem(position, book);
                    mBookAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(PLServerAPI.ApiError apiError) {
                    showSnackBar(apiError.getErrorString());
                }
        });
    }

    public void getBookList(String keyword){
        PLServerAPI.getBookList(null, keyword, new PLServerAPI.onResponseListener() {
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

    public void notifyItemInserted(int position) {
        mBookAdapter.notifyItemInserted(position);
        mRecyclerView.scrollToPosition(position);
    }

    public void setRecyclerViewToList() {
        mBookAdapter.setIsGrid(false);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);
    }
    public void setRecyclerViewToGrid() {
        mBookAdapter.setIsGrid(true);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mActivity =  (MainActivity)activity;
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
        bookItemClicked = false;
    }

    @Override
    public void onPause() {
        mRecyclerViewDragDropManager.cancelDrag();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        SharedPreferencesUtil.saveData(this.getContext(), "books", new Gson().toJson(mBookAdapter.getDataProvider().getItems()));
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}