package ml.puredark.personallibrary.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.personallibrary.PLApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.User;
import ml.puredark.personallibrary.activities.BookMarkActivity;
import ml.puredark.personallibrary.activities.MainActivity;
import ml.puredark.personallibrary.adapters.BookMarkAdapter;
import ml.puredark.personallibrary.beans.Book;
import ml.puredark.personallibrary.beans.BookMark;
import ml.puredark.personallibrary.dataprovider.BookMarkDataProvider;
import ml.puredark.personallibrary.helpers.DoubanRestAPI;
import ml.puredark.personallibrary.helpers.PLServerAPI;
import ml.puredark.personallibrary.utils.SharedPreferencesUtil;

;

public class ViewBookMarkFragment extends Fragment {
    private View rootView;

    private BookMark bookMark;

    //书评View相关引用
    private ImageView mAvatarView;
    private TextView tvNickname,tvSignature;
    private TextView tvMarkTitle,tvMarkContent;

    public ViewBookMarkFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_view_book_mark, container, false);

        ((BookMarkActivity)getActivity()).setCurrFragment(BookMarkActivity.FRAGMENT_VIEW_BOOK_MARK);

        bookMark = new Gson().fromJson(getArguments().getString("bookMark"), BookMark.class);

        mAvatarView = (ImageView)findViewById(R.id.avatar);
        tvNickname = (TextView)findViewById(R.id.nickname);
        tvSignature = (TextView)findViewById(R.id.signature);
        tvMarkTitle = (TextView)findViewById(R.id.mark_title);
        tvMarkContent = (TextView)findViewById(R.id.mark_content);

        tvNickname.setText(bookMark.nickname);
        tvSignature.setText(bookMark.signature);
        tvMarkTitle.setText(bookMark.title);
        //tvMarkContent.setText(bookMark.content);

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(false)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();//构建完成
        ImageLoader.getInstance().displayImage(PLApplication.serverHost + "/images/users/avatars/" + bookMark.uid + ".png", mAvatarView, options);

        PLServerAPI.getBookMarkDetails(bookMark.mid, new PLServerAPI.onResponseListener() {
            @Override
            public void onSuccess(Object data) {
                bookMark = (BookMark) data;
                tvNickname.setText(bookMark.nickname);
                tvSignature.setText(bookMark.signature);
                tvMarkTitle.setText(bookMark.title);
                tvMarkContent.setText(bookMark.content);
            }

            @Override
            public void onFailure(PLServerAPI.ApiError apiError) {
                ((BookMarkActivity)getActivity()).showSnackBar(apiError.getErrorString());
            }
        });

        return rootView;
    }

}