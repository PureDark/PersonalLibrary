package ml.puredark.personallibrary.adapters;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.gc.materialdesign.views.ButtonFlat;
import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.List;

import ml.puredark.personallibrary.PLApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.beans.Friend;
import ml.puredark.personallibrary.dataprovider.AbstractDataProvider;

public class FriendListAdapter
        extends RecyclerView.Adapter<FriendListAdapter.FriendViewHolder> {
    private AbstractDataProvider mProvider;
    private MyItemClickListener mItemClickListener;




    public class FriendViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public MaterialRippleLayout rippleLayout;
        public LinearLayout container;
        public RoundedImageView avatar;
        public TextView nickname,description;
        public TextView character;
        public ButtonFlat btnAdd;
        public TextView status;
        private MyItemClickListener mListener;
        public FriendViewHolder(View view, MyItemClickListener onClickListener) {
            super(view);
            container = (LinearLayout)view.findViewById(R.id.container);
            avatar = (RoundedImageView)view.findViewById(R.id.avatar);
            rippleLayout = (MaterialRippleLayout) view.findViewById(R.id.rippleLayout);
            nickname = (TextView)view.findViewById(R.id.name);
            description = (TextView)view.findViewById(R.id.description);
            character = (TextView)view.findViewById(R.id.character);
            btnAdd = (ButtonFlat)view.findViewById(R.id.btnAdd);
            status = (TextView)view.findViewById(R.id.status);
            mListener = onClickListener;
            btnAdd.setOnClickListener(this);
            rippleLayout.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            if(mListener != null){
                mListener.onItemClick(view, getPosition());
            }
        }
    }

    public void setDataProvider(AbstractDataProvider mProvider){
        this.mProvider = mProvider;
    }

    public FriendListAdapter(AbstractDataProvider mProvider) {
        this.mProvider = mProvider;
        List<Friend> fs = (List<Friend>) mProvider.getItems();
        for(Friend f : fs){
            Log.i("Kevin",f.nickname+":"+f.getId());
        }
        setHasStableIds(true);
    }

    @Override
    public FriendListAdapter.FriendViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        // 在这里对View的参数进行设置
        FriendViewHolder vh = new FriendViewHolder(v, mItemClickListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(final FriendViewHolder holder, int position) {
        final Friend friend = (Friend) mProvider.getItem(position);
        String avatar =  PLApplication.serverHost + "/images/users/avatars/" + friend.uid + ".png";
        if(holder.avatar.getTag() != avatar) {
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)//设置下载的图片是否缓存在内存中
                    .cacheOnDisc(false)//设置下载的图片是否缓存在SD卡中
                    .displayer(new FadeInBitmapDisplayer(300))//是否图片加载好后渐入的动画时间
                    .build();//构建完成
            ImageLoader.getInstance().displayImage("drawable://"+R.drawable.avatar, holder.avatar,options);
            ImageLoader.getInstance().loadImage(avatar, options, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {}
                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {}
                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    holder.avatar.setImageBitmap(bitmap);
                }
                @Override
                public void onLoadingCancelled(String s, View view) {}
            });
            holder.avatar.setTag(avatar);
        }
        holder.nickname.setText(friend.nickname);
        holder.description.setText(friend.signature);
        if (friend.character==null){
            friend.updateCharacter();
        }
        holder.character.setText(friend.character);
        //判断与之前的好友前缀是否相同，相同则不重复显示
        if(position>0){
            Friend preFriend = (Friend) mProvider.getItem(position-1);
            if(preFriend.character.equals(friend.character)){
                holder.character.setText(" ");
            }
        }

        if(friend.isFriend){
            holder.btnAdd.setVisibility(View.GONE);
            holder.status.setVisibility(View.GONE);
        }else{
            holder.btnAdd.setVisibility(View.VISIBLE);
            holder.status.setVisibility(View.VISIBLE);
        }
        if(friend.requestSent)
            holder.status.setText("等待验证");
        else
            holder.status.setText("");
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
        void onItemClick(View view, int postion);
    }

}