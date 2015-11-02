package ml.puredark.personallibrary.fragments;

import android.app.Activity;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ml.puredark.personallibrary.PLApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.activities.MainActivity;
import ml.puredark.personallibrary.adapters.BookListAdapter;
import ml.puredark.personallibrary.beans.BookListItem;
import ml.puredark.personallibrary.dataprovider.AbstractDataProvider;
import ml.puredark.personallibrary.dataprovider.BookListDataProvider;
import ml.puredark.personallibrary.utils.SharedPreferencesUtil;

public class IndexFragment extends Fragment {
    private static IndexFragment mInstance;
    private View rootView;

    private OnFragmentInteractionListener mListener;

    //首页书籍列表
    private RecyclerView mRecyclerView;
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
        ((MainActivity)getActivity()).setToolbarCollapsible();
        rootView = inflater.inflate(R.layout.fragment_index, container, false);

        mListener.onFragmentInteraction(MainActivity.FRAGMENT_ACTION_SET_TITLE, getResources().getString(R.string.title_fragment_index), null);
        mListener.onFragmentInteraction(MainActivity.FRAGMENT_ACTION_SET_NAVIGATION_ITEM, R.id.nav_index, null);

        //初始化书籍列表相关变量
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        List<BookListItem> myBooks = new ArrayList<>();
        String data = (String) SharedPreferencesUtil.getData(this.getContext(), "books", "");
        if(data!=null&&!data.equals(""))
            myBooks = new Gson().fromJson(data, new TypeToken<List<BookListItem>>(){}.getType());

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
            public void onItemClick(View view, int postion) {
                if (bookItemClicked == false) {
                    bookItemClicked = true;
                    mListener.onFragmentInteraction(MainActivity.FRAGMENT_ACTION_START_BOOK_DETAIL_ACTIVITY, mBookAdapter.getDataProvider().getItem(postion), view);
                }
            }
        });
        mBookAdapter.setEventListener(new BookListAdapter.EventListener() {
            @Override
            public void onItemRemoved(int position) {
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

    public void addNewBook(BookListItem book){
        mBookAdapter.getDataProvider().addItem(book);
        mBookAdapter.notifyDataSetChanged();
    }
    public void addNewBook(int position, BookListItem book) {
        mBookAdapter.getDataProvider().addItem(position, book);
        mBookAdapter.notifyDataSetChanged();
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
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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