package ml.puredark.personallibrary.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.activities.MainActivity;
import ml.puredark.personallibrary.adapters.FriendListAdapter;
import ml.puredark.personallibrary.adapters.NewsListAdapter;
import ml.puredark.personallibrary.beans.BookListItem;
import ml.puredark.personallibrary.beans.FriendListItem;
import ml.puredark.personallibrary.beans.NewsListItem;
import ml.puredark.personallibrary.dataprovider.FriendListDataProvider;
import ml.puredark.personallibrary.dataprovider.NewsListDataProvider;
import ml.puredark.personallibrary.utils.SharedPreferencesUtil;

;

public class NewsFragment extends Fragment {
    private static NewsFragment mInstance;
    private View rootView;

    private OnFragmentInteractionListener mListener;

    //首页书籍列表
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private NewsListAdapter mNewsAdapter;
    //书籍已点击(避免多次点击同时打开多个Activity)
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

        ((MainActivity)getActivity()).setToolbarUncollapsible();
        ((MainActivity)getActivity()).setCurrFragment(MainActivity.FRAGMENT_NEWS);
        mListener.onFragmentInteraction(MainActivity.FRAGMENT_ACTION_SET_TITLE, getResources().getString(R.string.title_fragment_news), null);
        mListener.onFragmentInteraction(MainActivity.FRAGMENT_ACTION_SET_NAVIGATION_ITEM, R.id.nav_whatshot, null);

        //初始化书籍列表相关变量
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        //指定为线性列表
        mLayoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);

        List<NewsListItem> myNews = new ArrayList<>();
//        String data = (String) SharedPreferencesUtil.getData(this.getContext(), "news", "");
//        if(data!=null&&!data.equals(""))
//            myFriends = new Gson().fromJson(data, new TypeToken<List<FriendListItem>>(){}.getType());
        BookListItem book = new BookListItem(1, "9787536693968", "https://img1.doubanio.com/lpic/s3078482.jpg","三体Ⅱ","[刘慈欣]","");
        myNews.add(new NewsListItem(1, "drawable://"+R.drawable.avatar, "PureDark", "2015-11-09 13:18:50", getResources().getString(R.string.testText), book));
        myNews.add(new NewsListItem(2, "drawable://"+R.drawable.avatar, "PureDark", "2015-11-09 13:18:50", getResources().getString(R.string.testText), book));
        myNews.add(new NewsListItem(3, "drawable://"+R.drawable.avatar, "PureDark", "2015-11-09 13:18:50", getResources().getString(R.string.testText), book));
        myNews.add(new NewsListItem(4, "drawable://"+R.drawable.avatar, "PureDark", "2015-11-09 13:18:50", getResources().getString(R.string.testText), book));

        NewsListDataProvider mNewsListDataProvider = new NewsListDataProvider(myNews);
        mNewsAdapter = new NewsListAdapter(mNewsListDataProvider);
        mNewsAdapter.setOnItemClickListener(new NewsListAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                /*if (newsItemClicked == false) {
                    newsItemClicked = true;
                    mListener.onFragmentInteraction(1, mNewsAdapter.getDataProvider().getItem(postion), view);
                }*/
            }
        });

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mNewsAdapter);

        return rootView;
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
        newsItemClicked = false;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        SharedPreferencesUtil.saveData(this.getContext(), "news", new Gson().toJson(mNewsAdapter.getDataProvider().getItems()));
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}