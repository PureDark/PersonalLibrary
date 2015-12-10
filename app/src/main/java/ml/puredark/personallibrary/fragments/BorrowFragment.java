package ml.puredark.personallibrary.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
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
import ml.puredark.personallibrary.adapters.BorrowRecordAdapter;
import ml.puredark.personallibrary.adapters.ViewPagerAdapter;
import ml.puredark.personallibrary.beans.BorrowRecord;
import ml.puredark.personallibrary.dataprovider.BorrowRecordDataProvider;
import ml.puredark.personallibrary.helpers.PLServerAPI;
import ml.puredark.personallibrary.utils.SharedPreferencesUtil;

;

public class BorrowFragment extends MyFragment {
    private static BorrowFragment mInstance;
    private View rootView;

    private MainActivity mActivity;

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    // 借出记录列表
    private RecyclerView mLoanRecyclerView, mBorrowRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private BorrowRecordAdapter mLoanRecordAdapter, mBorrowRecordAdapter;

    //按钮已点击(避免多次点击重复提交请求)
    private boolean buttonClicked = false;

    public static BorrowFragment newInstance() {
        mInstance = new BorrowFragment();
        return mInstance;
    }
    public static BorrowFragment getInstance() {
        if(mInstance!=null)
            return mInstance;
        else
            return newInstance();
    }

    public BorrowFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_borrow, container, false);

        mActivity.setToolbarUncollapsible();
        mActivity.setCurrFragment(MainActivity.FRAGMENT_BORROW);
        mActivity.setMainTitle(getResources().getString(R.string.title_fragment_borrow));
        mActivity.setNavigationItemSelected(R.id.nav_borrow);
        mActivity.setSearchEnable(false);
        mActivity.setShadowEnable(false);

        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        List<View> views = new ArrayList<>();
        final View viewLoanList = inflater.inflate(R.layout.view_loan_records, null);
        final View viewBorrowList = inflater.inflate(R.layout.view_borrow_records, null);
        views.add(viewLoanList);
        views.add(viewBorrowList);
        List<String> titles = new ArrayList<String>();
        titles.add("借出");
        titles.add("借入");
        ViewPagerAdapter mAdapter = new ViewPagerAdapter(views, titles);
        mTabLayout.setTabsFromPagerAdapter(mAdapter);
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        //初始化借出列表相关变量
        mLoanRecyclerView = (RecyclerView) viewLoanList.findViewById(R.id.my_recycler_view);
        mLoanRecyclerView.setHasFixedSize(true);
        //指定为线性列表
        mLayoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);

        List<BorrowRecord> loanRecords = new ArrayList<>();
        String data = (String) SharedPreferencesUtil.getData(this.getContext(), "loan_records", "");
        if(data!=null&&!data.equals(""))
            loanRecords = new Gson().fromJson(data, new TypeToken<List<BorrowRecord>>(){}.getType());

        BorrowRecordDataProvider mLoanDataProvider = new BorrowRecordDataProvider(loanRecords);
        mLoanRecordAdapter = new BorrowRecordAdapter(mLoanDataProvider);
        mLoanRecordAdapter.setIsLoaned(true);
        mLoanRecordAdapter.setOnItemClickListener(new BorrowRecordAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, int action) {
                BorrowRecord record = (BorrowRecord) mLoanRecordAdapter.getDataProvider().getItem(postion);
                if (!buttonClicked) {
                    buttonClicked = true;
                    if(action != BorrowRecordAdapter.MyItemClickListener.RETURN) {
                        boolean accept = (action==BorrowRecordAdapter.MyItemClickListener.ACCEPT);
                        PLServerAPI.acceptBorrowRecord(record.brid, accept, new PLServerAPI.onResponseListener() {
                            @Override
                            public void onSuccess(Object data) {
                                getLoanedBookRecordList(0);
                            }

                            @Override
                            public void onFailure(PLServerAPI.ApiError apiError) {
                                showSnackBar(apiError.getErrorString());
                            }
                        });
                    }else{
                        PLServerAPI.setBookReturned(record.brid, new PLServerAPI.onResponseListener() {
                            @Override
                            public void onSuccess(Object data) {
                                getLoanedBookRecordList(0);
                            }

                            @Override
                            public void onFailure(PLServerAPI.ApiError apiError) {
                                showSnackBar(apiError.getErrorString());
                            }
                        });
                    }
                }
            }
        });

        mLoanRecyclerView.setLayoutManager(mLayoutManager);
        mLoanRecyclerView.setAdapter(mLoanRecordAdapter);



        //初始化借入列表相关变量
        mBorrowRecyclerView = (RecyclerView) viewBorrowList.findViewById(R.id.my_recycler_view);
        mBorrowRecyclerView.setHasFixedSize(true);
        //指定为线性列表
        mLayoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);

        List<BorrowRecord> borrowRecords = new ArrayList<>();
        data = (String) SharedPreferencesUtil.getData(this.getContext(), "borrow_records", "");
        if(data!=null&&!data.equals(""))
            borrowRecords = new Gson().fromJson(data, new TypeToken<List<BorrowRecord>>(){}.getType());

        BorrowRecordDataProvider mBorrowDataProvider = new BorrowRecordDataProvider(borrowRecords);
        mBorrowRecordAdapter = new BorrowRecordAdapter(mBorrowDataProvider);

        mBorrowRecyclerView.setLayoutManager(mLayoutManager);
        mBorrowRecyclerView.setAdapter(mBorrowRecordAdapter);

        return rootView;
    }

    public void getLoanedBookRecordList(int status){
        PLServerAPI.getLoanedBookRecordList(status, new PLServerAPI.onResponseListener() {
            @Override
            public void onSuccess(Object data) {
                List<BorrowRecord> loanRecords = (List<BorrowRecord>) data;
                mLoanRecordAdapter.setDataProvider(new BorrowRecordDataProvider(loanRecords));
                mLoanRecordAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(PLServerAPI.ApiError apiError) {
                showSnackBar(apiError.getErrorString());
            }
        });
    }

    public void getBorrowedBookRecordList(int status){
        PLServerAPI.getBorrowedBookRecordList(status, new PLServerAPI.onResponseListener() {
            @Override
            public void onSuccess(Object data) {
                List<BorrowRecord> borrowRecords = (List<BorrowRecord>) data;
                mBorrowRecordAdapter.setDataProvider(new BorrowRecordDataProvider(borrowRecords));
                mBorrowRecordAdapter.notifyDataSetChanged();
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
        buttonClicked = false;
        //从服务器获取最新的动态
        getLoanedBookRecordList(0);
        getBorrowedBookRecordList(0);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        SharedPreferencesUtil.saveData(this.getContext(), "loan_records", new Gson().toJson(mLoanRecordAdapter.getDataProvider().getItems()));
        SharedPreferencesUtil.saveData(this.getContext(), "borrow_records", new Gson().toJson(mBorrowRecordAdapter.getDataProvider().getItems()));
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}