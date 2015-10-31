package ml.puredark.personallibrary.fragments;

import android.app.Activity;
import android.graphics.drawable.NinePatchDrawable;
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
import ml.puredark.personallibrary.adapters.FriendListAdapter;
import ml.puredark.personallibrary.beans.BookListItem;
import ml.puredark.personallibrary.beans.FriendListItem;
import ml.puredark.personallibrary.dataprovider.BookListDataProvider;
import ml.puredark.personallibrary.dataprovider.FriendListDataProvider;
import ml.puredark.personallibrary.utils.SharedPreferencesUtil;

;

public class FriendFragment extends Fragment {
    private static FriendFragment mInstance;
    private View rootView;

    private OnFragmentInteractionListener mListener;

    //首页书籍列表
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mWrappedAdapter;
    private FriendListAdapter mFriendAdapter;
    //书籍已点击(避免多次点击同时打开多个Activity)
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

        //初始化书籍列表相关变量
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        //指定为线性列表
        mLayoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);

        List<FriendListItem> myFriends = new ArrayList<>();
        String data = (String) SharedPreferencesUtil.getData(this.getContext(), "friends", "");
        if(data!=null&&!data.equals(""))
            myFriends = new Gson().fromJson(data, new TypeToken<List<FriendListItem>>(){}.getType());


        FriendListDataProvider mFriendListDataProvider = new FriendListDataProvider(myFriends);
        mFriendAdapter = new FriendListAdapter(mFriendListDataProvider);
        mFriendAdapter.setOnItemClickListener(new FriendListAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                if (friendItemClicked == false) {
                    friendItemClicked = true;
                    mListener.onFragmentInteraction(1, mFriendAdapter.getDataProvider().getItem(postion), view);
                }
            }
        });



        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mFriendAdapter);  // 设置的是处理过的mWrappedAdapter



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
    public void onDestroy() {
        super.onDestroy();
        SharedPreferencesUtil.saveData(this.getContext(), "books", new Gson().toJson(mFriendAdapter.getDataProvider().getItems()));
    }

}