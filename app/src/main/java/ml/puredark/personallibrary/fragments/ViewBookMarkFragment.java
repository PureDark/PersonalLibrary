package ml.puredark.personallibrary.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import ml.puredark.personallibrary.PLApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.activities.BookMarkActivity;
import ml.puredark.personallibrary.activities.MyActivity;
import ml.puredark.personallibrary.beans.BookMark;
import ml.puredark.personallibrary.helpers.PLServerAPI;

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

        ((MyActivity)getActivity()).setCurrFragment(MyActivity.FRAGMENT_VIEW_BOOK_MARK);

        bookMark = new Gson().fromJson(getArguments().getString("bookMark"), BookMark.class);
        ((BookMarkActivity)getActivity()).setBookMark(bookMark);

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
                ((BookMarkActivity)getActivity()).setBookMark(bookMark);
            }

            @Override
            public void onFailure(PLServerAPI.ApiError apiError) {
                showSnackBar(apiError.getErrorString());
            }
        });

        return rootView;
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
        ((MyActivity)getActivity()).setCurrFragment(MyActivity.FRAGMENT_VIEW_BOOK_MARK);
    }

}