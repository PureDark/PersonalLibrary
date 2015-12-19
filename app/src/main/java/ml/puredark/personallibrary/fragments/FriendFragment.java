package ml.puredark.personallibrary.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
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
import ml.puredark.personallibrary.activities.MainActivity;
import ml.puredark.personallibrary.adapters.FriendListAdapter;
import ml.puredark.personallibrary.adapters.RequestAdapter;
import ml.puredark.personallibrary.adapters.ViewPagerAdapter;
import ml.puredark.personallibrary.beans.Friend;
import ml.puredark.personallibrary.beans.Request;
import ml.puredark.personallibrary.dataprovider.FriendListDataProvider;
import ml.puredark.personallibrary.dataprovider.RequestDataProvider;
import ml.puredark.personallibrary.helpers.PLServerAPI;
import ml.puredark.personallibrary.utils.SharedPreferencesUtil;

;

public class FriendFragment extends MyFragment {
    private static FriendFragment mInstance;
    private View rootView;

    private MainActivity mActivity;

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private View viewFriendList,viewRequestList;

    //好友列表
    private SwipeRefreshLayout mFriendSwipeRefreshLayout, mRequestSwipeRefreshLayout;
    private RecyclerView mFriendRecyclerView, mRequestRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private FriendListAdapter mFriendAdapter;
    private RequestAdapter mRequestAdapter;

    //已点击(避免多次点击同时打开多个Activity)
    private boolean itemClicked = false;

    public static FriendFragment newInstance() {
        mInstance = new FriendFragment();
        return mInstance;
    }
    public static FriendFragment getInstance() {
        if(mInstance!=null)
            return mInstance;
        else
            return newInstance();
    }

    public FriendFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_friend, container, false);

        mActivity.setToolbarUncollapsible();
        mActivity.setCurrFragment(MainActivity.FRAGMENT_FRIEND);
        mActivity.setMainTitle(getResources().getString(R.string.title_fragment_friend));
        mActivity.setNavigationItemSelected(R.id.nav_friend);
        mActivity.setSearchEnable(true);
        mActivity.setShadowEnable(false);

        // 设置Tab和对应的ViewPager
        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        List<View> views = new ArrayList<>();
        viewFriendList = inflater.inflate(R.layout.view_friend_list, null);
        viewRequestList = inflater.inflate(R.layout.view_request_list, null);
        views.add(viewFriendList);
        views.add(viewRequestList);
        List<String> titles = new ArrayList<String>();
        titles.add("书友列表");
        titles.add("书友请求");
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
                if (position == 1)
                    mActivity.setSearchEnable(false);
                else
                    mActivity.setSearchEnable(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //初始化下拉刷新相关变量
        mFriendSwipeRefreshLayout = (SwipeRefreshLayout)viewFriendList.findViewById(R.id.swipe_container);
        mFriendSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        mFriendSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFriendList(1);
            }
        });
        mRequestSwipeRefreshLayout = (SwipeRefreshLayout)viewRequestList.findViewById(R.id.swipe_container);
        mRequestSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        mRequestSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getRequestList(1);
            }
        });

        //初始化好友列表相关变量
        mFriendRecyclerView = (RecyclerView) viewFriendList.findViewById(R.id.my_recycler_view);
        mFriendRecyclerView.setHasFixedSize(true);
        //指定为线性列表
        mLayoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);

        List<Friend> myFriends = new ArrayList<Friend>();
        String data = (String) SharedPreferencesUtil.getData(this.getContext(), "friends", "");
        if(data!=null&&!data.equals(""))
            myFriends = new Gson().fromJson(data, new TypeToken<List<Friend>>(){}.getType());

        FriendListDataProvider mFriendListDataProvider = new FriendListDataProvider(myFriends);
        mFriendAdapter = new FriendListAdapter(mFriendListDataProvider);

        mFriendAdapter.setOnItemClickListener(new FriendListAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                if (itemClicked) return;
                itemClicked = true;
                final Friend friend = (Friend) mFriendAdapter.getDataProvider().getItem(postion);
                if (view.getId() == R.id.btnAdd) {
                    PLServerAPI.addRequest(friend.uid, new PLServerAPI.onResponseListener() {
                        @Override
                        public void onSuccess(Object data) {
                            friend.requestSent = true;
                            mFriendAdapter.notifyDataSetChanged();
                            itemClicked = false;
                        }

                        @Override
                        public void onFailure(PLServerAPI.ApiError apiError) {
                            showSnackBar(apiError.getErrorString());
                            itemClicked = false;
                        }
                    });
                } else {
                    PLApplication.temp = friend;
                    PLApplication.bitmap = view.getDrawingCache();
                    mActivity.startFriendDetailActivity(friend, view);
                    itemClicked = false;
                }
            }
        });

        mFriendRecyclerView.setLayoutManager(mLayoutManager);
        mFriendRecyclerView.setAdapter(mFriendAdapter);


        //初始化借出列表相关变量
        mRequestRecyclerView = (RecyclerView) viewRequestList.findViewById(R.id.my_recycler_view);
        mRequestRecyclerView.setHasFixedSize(true);
        //指定为线性列表
        mLayoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);

        List<Request> requests = new ArrayList<>();

        RequestDataProvider mRequestProvider = new RequestDataProvider(requests);
        mRequestAdapter = new RequestAdapter(mRequestProvider);
        mRequestAdapter.setOnItemClickListener(new RequestAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, boolean accept) {
                Request request = (Request) mRequestAdapter.getDataProvider().getItem(postion);
                if (!itemClicked) {
                    itemClicked = true;
                    PLServerAPI.responseRequest(request.rid, accept, new PLServerAPI.onResponseListener() {
                        @Override
                        public void onSuccess(Object data) {
                            getRequestList(1);
                            itemClicked = false;
                        }

                        @Override
                        public void onFailure(PLServerAPI.ApiError apiError) {
                            showSnackBar(apiError.getErrorString());
                            itemClicked = false;
                        }
                    });
                }
            }
        });

        mRequestRecyclerView.setLayoutManager(mLayoutManager);
        mRequestRecyclerView.setAdapter(mRequestAdapter);

        // 获取最新
        getFriendList(1);
        getRequestList(1);
        return rootView;
    }

    @Override
    public void onSearch(String keyword) {
        if(keyword.equals(""))
            getFriendList(1);
        else
            searchUser(keyword);
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

    public void getFriendList(int page){
        PLServerAPI.getFriendList(page, new PLServerAPI.onResponseListener() {
            @Override
            public void onSuccess(Object data) {
                List<Friend> myFriends = (List<Friend>) data;
                mFriendAdapter.setDataProvider(new FriendListDataProvider(myFriends));
                mFriendAdapter.notifyDataSetChanged();
                mFriendSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(PLServerAPI.ApiError apiError) {
                showSnackBar(apiError.getErrorString());
                mFriendSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    public void searchUser(String keyword){
        PLServerAPI.searchUser(keyword, new PLServerAPI.onResponseListener() {
            @Override
            public void onSuccess(Object data) {
                List<Friend> friends = (List<Friend>) data;
                mFriendAdapter.setDataProvider(new FriendListDataProvider(friends));
                mFriendAdapter.notifyDataSetChanged();
                mFriendSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(PLServerAPI.ApiError apiError) {
                showSnackBar(apiError.getErrorString());
                mFriendSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }


    public void getRequestList(int page){
        PLServerAPI.getRequestList(page, new PLServerAPI.onResponseListener() {
            @Override
            public void onSuccess(Object data) {
                List<Request> myRequests = (List<Request>) data;
                mRequestAdapter.setDataProvider(new RequestDataProvider(myRequests));
                mRequestAdapter.notifyDataSetChanged();
                mRequestSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(PLServerAPI.ApiError apiError) {
                showSnackBar(apiError.getErrorString());
                mRequestSwipeRefreshLayout.setRefreshing(false);
            }
        });
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
        itemClicked = false;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        SharedPreferencesUtil.saveData(this.getContext(), "friends", new Gson().toJson(mFriendAdapter.getDataProvider().getItems()));
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}