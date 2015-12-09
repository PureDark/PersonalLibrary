package ml.puredark.personallibrary.fragments;

import android.app.Activity;
import android.os.Bundle;
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
import ml.puredark.personallibrary.adapters.FriendListAdapter;
import ml.puredark.personallibrary.beans.BookMark;
import ml.puredark.personallibrary.beans.Friend;
import ml.puredark.personallibrary.dataprovider.BookMarkDataProvider;
import ml.puredark.personallibrary.dataprovider.FriendListDataProvider;
import ml.puredark.personallibrary.helpers.PLServerAPI;
import ml.puredark.personallibrary.utils.SharedPreferencesUtil;

;

public class FriendFragment extends MyFragment {
    private static FriendFragment mInstance;
    private View rootView;

    private MainActivity mActivity;

    //好友列表
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mWrappedAdapter;
    private FriendListAdapter mFriendAdapter;
    //好友已点击(避免多次点击同时打开多个Activity)
    private boolean friendItemClicked = false;

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

        //初始化好友列表相关变量
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        //指定为线性列表
        mLayoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);

        List<Friend> myFriends = new ArrayList<>();



        FriendListDataProvider mFriendListDataProvider = new FriendListDataProvider(myFriends);
        mFriendAdapter = new FriendListAdapter(mFriendListDataProvider);
        mFriendAdapter.setOnItemClickListener(new FriendListAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                if (friendItemClicked == false) {
                    friendItemClicked = true;
                }
            }
        });



        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mFriendAdapter);  // 设置的是处理过的mWrappedAdapter



        return rootView;
    }

    public void searchUser(String keyword){
        PLServerAPI.searchUser(keyword, new PLServerAPI.onResponseListener() {
            @Override
            public void onSuccess(Object data) {
                List<Friend> friends = (List<Friend>) data;
            }

            @Override
            public void onFailure(PLServerAPI.ApiError apiError) {
                showSnackBar(apiError.getErrorString());
            }
        });
    }

    @Override
    public void onSearch(String keyword) {
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
        friendItemClicked = false;
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