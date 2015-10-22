package ml.puredark.personallibrary.fragments;

import android.app.Activity;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.activities.MainActivity;
import ml.puredark.personallibrary.adapters.BookListAdapter;
import ml.puredark.personallibrary.beans.BookListItem;
import ml.puredark.personallibrary.dataprovider.BookListDataProvider;
import ml.puredark.personallibrary.utils.SharedPreferencesUtil;

public class IndexFragment extends Fragment {
    private static IndexFragment mInstance;
    private View rootView;

    private OnFragmentInteractionListener mListener;

    //首页书籍列表
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mWrappedAdapter;
    private BookListAdapter mBookAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;

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

        //初始化书籍列表相关变量
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        //指定为线性列表
        mLayoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);

        List<BookListItem> myBooks = new ArrayList<>();
        String data = (String) SharedPreferencesUtil.getData(this.getContext(), "books", "");
        if(data!=null&&!data.equals(""))
            myBooks = new Gson().fromJson(data, new TypeToken<List<BookListItem>>(){}.getType());


        // drag & drop manager
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();

        // 拖拽时的阴影
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(
                (NinePatchDrawable) ContextCompat.getDrawable(this.getContext(), R.drawable.material_shadow_z3));

        // 长按开启拖拽
        mRecyclerViewDragDropManager.setInitiateOnLongPress(true);
        mRecyclerViewDragDropManager.setInitiateOnMove(false);

        BookListDataProvider mBookListDataProvider = new BookListDataProvider(myBooks);
        mBookAdapter = new BookListAdapter(mBookListDataProvider);
        mBookAdapter.setOnItemClickListener(new BookListAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {

                mListener.onFragmentInteraction(1, mBookAdapter.getDataProvider().getItem(postion), view);
            }
        });

        // 将Adapter封装成可以拖动的
        mWrappedAdapter =  mRecyclerViewDragDropManager.createWrappedAdapter(mBookAdapter);

        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);  // 设置的是处理过的mWrappedAdapter
        mRecyclerView.setItemAnimator(animator);

        mRecyclerView.addItemDecoration(new ItemShadowDecorator(
                (NinePatchDrawable) ContextCompat.getDrawable(this.getContext(), R.drawable.material_shadow_z1)));

        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);

//        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.main_swipe);
//        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
//        //设置刷新时动画的颜色，可以设置4个
//        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_red_light,
//                android.R.color.holo_orange_light,
//                android.R.color.holo_green_light,
//                android.R.color.holo_blue_light);
//        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                mSwipeRefreshLayout.setEnabled(false);
//                new Handler().postDelayed(new Runnable() {
//                    public void run() {
//                        mSwipeRefreshLayout.setRefreshing(false);
//                        mSwipeRefreshLayout.setEnabled(true);
//                    }
//                }, 4000);
//            }
//        });

        return rootView;
    }

    public void addNewBook(BookListItem book){
        mBookAdapter.getDataProvider().addItem(book);
        mBookAdapter.notifyDataSetChanged();
        mWrappedAdapter.notifyDataSetChanged();
    }
    public void addNewBook(int position, BookListItem book){
        mBookAdapter.getDataProvider().addItem(position, book);
        mBookAdapter.notifyDataSetChanged();
        mWrappedAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity)activity).setToolbarCollapsible();
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
    public void onDestroy() {
        super.onDestroy();
        SharedPreferencesUtil.saveData(this.getContext(), "books", new Gson().toJson(mBookAdapter.getDataProvider().getItems()));
    }

}
