package ml.puredark.personallibrary.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.nostra13.universalimageloader.core.ImageLoader;

import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.beans.FriendListItem;
import ml.puredark.personallibrary.beans.NewsListItem;
import ml.puredark.personallibrary.dataprovider.AbstractDataProvider;

public class NewsListAdapter
        extends RecyclerView.Adapter<NewsListAdapter.NewsViewHolder> {
    private AbstractDataProvider mProvider;
    private MyItemClickListener mItemClickListener;

    public class NewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public MaterialRippleLayout rippleLayout;
        public LinearLayout book;
        public ImageView avatar, bookCover;
        public TextView nickname,datetime,content;
        private MyItemClickListener mListener;
        public TextView bookTitle;

        public NewsViewHolder(View view, MyItemClickListener onClickListener) {
            super(view);
            rippleLayout = (MaterialRippleLayout) view.findViewById(R.id.rippleLayout);
            avatar = (ImageView)view.findViewById(R.id.avatar);
            nickname = (TextView)view.findViewById(R.id.nickname);
            datetime = (TextView)view.findViewById(R.id.datetime);
            content = (TextView)view.findViewById(R.id.content);
            book = (LinearLayout)view.findViewById(R.id.book);
            bookCover = (ImageView)view.findViewById(R.id.book_cover);
            bookTitle = (TextView)view.findViewById(R.id.book_title);
            mListener = onClickListener;
            book.setOnClickListener(this);
            rippleLayout.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            if(mListener != null){
                mListener.onItemClick(view, getPosition());
            }
        }
    }

    public NewsListAdapter(AbstractDataProvider mProvider) {
        this.mProvider = mProvider;
        setHasStableIds(true);
    }

    @Override
    public NewsListAdapter.NewsViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        // 在这里对View的参数进行设置
        NewsViewHolder vh = new NewsViewHolder(v, mItemClickListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(NewsViewHolder holder, int position) {
        final NewsListItem news = (NewsListItem) mProvider.getItem(position);

        if(holder.avatar.getTag()!=news.avatar) {
            ImageLoader.getInstance().displayImage(null, holder.avatar);
            ImageLoader.getInstance().displayImage(news.avatar, holder.avatar);
            holder.avatar.setTag(news.avatar);
        }
        if(holder.bookCover.getTag()!=news.book.cover) {
            ImageLoader.getInstance().displayImage(null, holder.bookCover);
            ImageLoader.getInstance().displayImage(news.book.cover, holder.bookCover);
            holder.avatar.setTag(news.book.cover);
        }
        if (news.content.length() > 100)
            holder.content.setText(Html.fromHtml(news.content.substring(0, 100) + "..."));
        else
            holder.content.setText(news.content);

        holder.nickname.setText(news.nickname);
        holder.datetime.setText(news.datetime);
        holder.bookTitle.setText(news.book.title);
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