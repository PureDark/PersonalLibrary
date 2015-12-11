package ml.puredark.personallibrary.fragments;

import android.content.Intent;
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
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.personallibrary.PLApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.activities.FriendDetailActivity;
import ml.puredark.personallibrary.activities.MainActivity;
import ml.puredark.personallibrary.activities.MyActivity;
import ml.puredark.personallibrary.adapters.FriendListAdapter;
import ml.puredark.personallibrary.beans.Friend;
import ml.puredark.personallibrary.dataprovider.FriendListDataProvider;
import ml.puredark.personallibrary.helpers.PLServerAPI;
import ml.puredark.personallibrary.utils.SharedPreferencesUtil;

;

public class FriendListFragment extends Fragment {
    private static FriendListFragment mInstance;
    private View rootView;

    //好友列表
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mWrappedAdapter;
    private FriendListAdapter mFriendAdapter;

    //好友已点击(避免多次点击同时打开多个Activity)
    private boolean friendItemClicked = false;

    public static FriendListFragment newInstance() {
        mInstance = new FriendListFragment();
        return mInstance;
    }
    public static FriendListFragment getInstance() {
        if(mInstance!=null)
            return mInstance;
        else
            return newInstance();
    }

    public FriendListFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_friend_list, container, false);

        ((MyActivity)getActivity()).setCurrFragment(MyActivity.FRAGMENT_FRIEND_LIST);

        //初始化好友列表相关变量
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        //指定为线性列表
        mLayoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        List<Friend> myFriends = new ArrayList<Friend>();

        FriendListDataProvider mFriendListDataProvider = new FriendListDataProvider(myFriends);
        mFriendAdapter = new FriendListAdapter(mFriendListDataProvider);
        mFriendAdapter.setOnItemClickListener(new FriendListAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                if (friendItemClicked) return;
                friendItemClicked = true;
                final Friend item = (Friend) mFriendAdapter.getDataProvider().getItem(postion);
                if (view.getId() == R.id.btnAdd) {
                    PLServerAPI.addRequest(item.uid, new PLServerAPI.onResponseListener() {
                        @Override
                        public void onSuccess(Object data) {
                            item.requestSent = true;
                            mFriendAdapter.notifyDataSetChanged();
                            friendItemClicked = false;
                        }

                        @Override
                        public void onFailure(PLServerAPI.ApiError apiError) {
                            showSnackBar(apiError.getErrorString());
                            friendItemClicked = false;
                        }
                    });
                } else {
                    PLApplication.temp = item;
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), FriendDetailActivity.class);
                    intent.putExtra("uid", item.uid);
                    startActivity(intent);
                    friendItemClicked = false;
                }
            }
        });

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mFriendAdapter);  // 设置的是处理过的mWrappedAdapter

        getFriendList(1);
        return rootView;
    }
    public void getFriendList(int page){
        PLServerAPI.getFriendList(page, new PLServerAPI.onResponseListener() {
            @Override
            public void onSuccess(Object data) {
                List<Friend> myFriends = (List<Friend>) data;
                mFriendAdapter.setDataProvider(new FriendListDataProvider(myFriends));
                mFriendAdapter.notifyDataSetChanged();
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
        friendItemClicked = false;
    }

}