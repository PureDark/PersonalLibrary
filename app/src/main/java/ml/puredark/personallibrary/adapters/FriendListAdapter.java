package ml.puredark.personallibrary.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.nostra13.universalimageloader.core.ImageLoader;

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
        public ImageView avatar;
        public TextView nickname,description;
        private MyItemClickListener mListener;
        public TextView character;
        public FriendViewHolder(View view, MyItemClickListener onClickListener) {
            super(view);
            container = (LinearLayout)view.findViewById(R.id.container);
            avatar = (ImageView)view.findViewById(R.id.avatar);
            rippleLayout = (MaterialRippleLayout) view.findViewById(R.id.rippleLayout);
            nickname = (TextView)view.findViewById(R.id.name);
            description = (TextView)view.findViewById(R.id.description);
            character = (TextView)view.findViewById(R.id.character);
            mListener = onClickListener;
            avatar.setOnClickListener(this);
            rippleLayout.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            if(mListener != null){
                mListener.onItemClick(view, getPosition());
            }
        }
    }

    public FriendListAdapter(AbstractDataProvider mProvider) {
        this.mProvider = mProvider;
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
    public void onBindViewHolder(FriendViewHolder holder, int position) {
        final Friend friend = (Friend) mProvider.getItem(position);

        if(holder.avatar.getTag()!=friend.uid) {
            ImageLoader.getInstance().displayImage(null, holder.avatar);
            //ImageLoader.getInstance().displayImage(friend.avatar, holder.avatar);
            holder.avatar.setTag(friend.uid);
        }
        holder.nickname.setText(friend.nickname);
        holder.description.setText(friend.signature);
        holder.character.setText(friend.character);
        //判断与之前的好友前缀是否相同，相同则不重复显示
        if(position>0){
            Friend preFriend = (Friend) mProvider.getItem(position-1);
            if(preFriend.character.equals(friend.character)){
                holder.character.setText(" ");
            }
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
        void onItemClick(View view, int postion);
    }

}