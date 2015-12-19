package ml.puredark.personallibrary.adapters;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonFlat;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import ml.puredark.personallibrary.PLApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.beans.Request;
import ml.puredark.personallibrary.dataprovider.AbstractDataProvider;

public class RequestAdapter
        extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
    private AbstractDataProvider mProvider;
    private MyItemClickListener mItemClickListener;

    public class RequestViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout book;
        public ImageView avatar;
        public TextView nickname,content,status;
        private ButtonFlat btnAccept,btnDecline;
        private MyItemClickListener mListener;

        public RequestViewHolder(View view, MyItemClickListener onClickListener) {
            super(view);
            avatar = (ImageView)view.findViewById(R.id.avatar);
            nickname = (TextView)view.findViewById(R.id.nickname);
            content = (TextView)view.findViewById(R.id.content);
            status = (TextView)view.findViewById(R.id.status);
            btnAccept = (ButtonFlat)view.findViewById(R.id.btnAccept);
            btnDecline = (ButtonFlat)view.findViewById(R.id.btnDecline);
            mListener = onClickListener;
            if(btnAccept!=null)
                btnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mListener != null){
                            mListener.onItemClick(view, getPosition(), true);
                        }
                    }
                });
            if(btnDecline!=null)
                btnDecline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mListener != null){
                            mListener.onItemClick(view, getPosition(), false);
                        }
                    }
                });
        }
    }

    public RequestAdapter(AbstractDataProvider mProvider) {
        this.mProvider = mProvider;
        setHasStableIds(true);
    }

    public void setDataProvider(AbstractDataProvider mProvider){
        this.mProvider = mProvider;
    }

    @Override
    public RequestAdapter.RequestViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request, parent, false);
        // 在这里对View的参数进行设置
        RequestViewHolder vh = new RequestViewHolder(v, mItemClickListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(final RequestViewHolder holder, int position) {
        final Request request = (Request) mProvider.getItem(position);
        if(holder.avatar.getTag()!=request.uid) {
            final DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)//设置下载的图片是否缓存在内存中
                    .cacheOnDisc(false)//设置下载的图片是否缓存在SD卡中
                    .displayer(new FadeInBitmapDisplayer(300))//是否图片加载好后渐入的动画时间
                    .build();//构建完成
            final String avatar =  PLApplication.serverHost + "/images/users/avatars/" + request.uid + ".png";
            ImageLoader.getInstance().loadImage(avatar, options, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {}
                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {
                    ImageLoader.getInstance().displayImage("drawable://"+R.drawable.avatar, holder.avatar,options);
                }
                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    holder.avatar.setImageBitmap(bitmap);
                }
                @Override
                public void onLoadingCancelled(String s, View view) {
                    ImageLoader.getInstance().displayImage(avatar, holder.avatar,options);
                }
            });
            holder.avatar.setTag(request.uid);
        }
        holder.nickname.setText(request.nickname);
        if(request.status==0){
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnDecline.setVisibility(View.VISIBLE);
            holder.status.setVisibility(View.GONE);
        }else{
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnDecline.setVisibility(View.GONE);
            holder.status.setVisibility(View.VISIBLE);
            String status = (request.status==1)?"已添加":(request.status==2)?"已拒绝":"";
            holder.status.setText(status);
        }
    }

    @Override
    public int getItemCount() {
        return (mProvider!=null)?mProvider.getCount():0;
    }

    @Override
    public long getItemId(int position) {
        return mProvider.getItem(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    public void setOnItemClickListener(MyItemClickListener listener){
        this.mItemClickListener = listener;
    }

    public AbstractDataProvider getDataProvider(){
        return mProvider;
    }

    public interface MyItemClickListener {
        int ACCEPT = 1;
        int DECLINE = 2;
        void onItemClick(View view, int postion, boolean accept);
    }
}