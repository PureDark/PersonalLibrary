package ml.puredark.personallibrary.fragments;

import android.app.Activity;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.wnafee.vector.compat.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import io.codetail.animation.arcanimator.ArcAnimator;
import io.codetail.animation.arcanimator.Side;
import io.codetail.widget.RevealFrameLayout;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.activities.MainActivity;
import ml.puredark.personallibrary.adapters.BookListAdapter;
import ml.puredark.personallibrary.beans.BookListItem;
import ml.puredark.personallibrary.dataprovider.BookListDataProvider;
import ml.puredark.personallibrary.utils.ViewUtils;

public class IndexFragment extends Fragment {
    private static IndexFragment mInstance;
    private View rootView;

    private OnFragmentInteractionListener mListener;

    //首页书籍列表
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mWrappedAdapter;
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
        String cover1 =  "http://img4.douban.com/lpic/s28041509.jpg";
        String title1 = "夜莺与玫瑰";
        String author1 = "[英] 奥斯卡·王尔德";
        String description1 = "《夜莺与玫瑰》是英国唯美主义作家王尔德的童话作品之一，收录于《快乐王子及其他故事》。故事以夜莺受到大学生的爱情感动，培育玫瑰为主线。赞扬了爱情的可贵，鞭挞了世间的拜金主义。在一个寒冷的冬夜";

        String cover2 =  "http://img6.douban.com/lpic/s24173364.jpg";
        String title2 = "金枝";
        String author2 = "[英] J. G. 弗雷泽";
        String description2 = "金枝-巫术与宗教之研究-(上.下册)，ISBN：9787100088961，作者：弗雷泽";

        String cover3 =  "http://img6.douban.com/lpic/s27716905.jpg";
        String title3 = "S.";
        String author3 = "J. J. 亞伯拉罕(J. J Abrams) / 道格·道斯";
        String description3 = "這不只是一本小說，更是一場挑戰紙本書可能性的敘事冒險，一部獻給文字的動人情書。書中收錄實體解謎線索，遭美國圖書館抗議，本書只能收藏，無法借閱！這不只是一本書，這是一個穿越時空而來的物件，是解謎的鑰匙…兩個素未謀面的陌生讀者，因為對同一本書的癡迷，對文字放不下的執著，攜手踏上了一場詭譎的追尋之路……";
        myBooks.add(new BookListItem(myBooks.size(),0,cover1,title1,author1,description1));
        myBooks.add(new BookListItem(myBooks.size(),0,cover2,title2,author2,description2));
        myBooks.add(new BookListItem(myBooks.size(),0,cover3,title3,author3,description3));
        myBooks.add(new BookListItem(myBooks.size(),0,cover1,title1,author1,description1));
        myBooks.add(new BookListItem(myBooks.size(),0,cover2,title2,author2,description2));
        myBooks.add(new BookListItem(myBooks.size(),0,cover3,title3,author3,description3));
        myBooks.add(new BookListItem(myBooks.size(),0,cover1,title1,author1,description1));
        myBooks.add(new BookListItem(myBooks.size(),0,cover2,title2,author2,description2));
        myBooks.add(new BookListItem(myBooks.size(),0,cover3,title3,author3,description3));

        // drag & drop manager
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();

        // 拖拽时的阴影
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(
                (NinePatchDrawable) ContextCompat.getDrawable(this.getContext(), R.drawable.material_shadow_z3));

        // 长按开启拖拽
        mRecyclerViewDragDropManager.setInitiateOnLongPress(true);
        mRecyclerViewDragDropManager.setInitiateOnMove(false);

        BookListDataProvider mBookListDataProvider = new BookListDataProvider(myBooks);

        // 将Adapter封装成可以拖动的
        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(new BookListAdapter(mBookListDataProvider));

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


}
