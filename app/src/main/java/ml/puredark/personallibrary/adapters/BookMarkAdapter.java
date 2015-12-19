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

import ml.puredark.personallibrary.PLApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.beans.BookMark;
import ml.puredark.personallibrary.dataprovider.AbstractDataProvider;

public class BookMarkAdapter
        extends RecyclerView.Adapter<BookMarkAdapter.BookMarkViewHolder> {
    private AbstractDataProvider mProvider;
    private MyItemClickListener mItemClickListener;

    public class BookMarkViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public MaterialRippleLayout rippleLayout;
        public LinearLayout markLayout,book;
        public ImageView avatar, bookCover;
        public TextView nickname,datetime,content;
        private MyItemClickListener mListener;
        public TextView bookTitle;

        public BookMarkViewHolder(View view, MyItemClickListener onClickListener) {
            super(view);
            rippleLayout = (MaterialRippleLayout) view.findViewById(R.id.rippleLayout);
            avatar = (ImageView)view.findViewById(R.id.avatar);
            nickname = (TextView)view.findViewById(R.id.nickname);
            datetime = (TextView)view.findViewById(R.id.datetime);
            content = (TextView)view.findViewById(R.id.content);
            markLayout = (LinearLayout)view.findViewById(R.id.markLayout);
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

    public BookMarkAdapter(AbstractDataProvider mProvider) {
        this.mProvider = mProvider;
        setHasStableIds(true);
    }

    public void setDataProvider(AbstractDataProvider mProvider){
        this.mProvider = mProvider;
    }

    @Override
    public BookMarkAdapter.BookMarkViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book_mark, parent, false);
        // 在这里对View的参数进行设置
        BookMarkViewHolder vh = new BookMarkViewHolder(v, mItemClickListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(BookMarkViewHolder holder, int position) {
        final BookMark bookMark = (BookMark) mProvider.getItem(position);

        if(holder.avatar.getTag()!=bookMark.bid) {
            ImageLoader.getInstance().displayImage(null, holder.avatar);
            ImageLoader.getInstance().displayImage(PLApplication.serverHost + "/images/users/avatars/" + bookMark.uid + ".png", holder.avatar);
            holder.avatar.setTag(bookMark.bid);
        }
        if(holder.bookCover.getTag()!=bookMark.book_cover) {
            ImageLoader.getInstance().displayImage(null, holder.bookCover);
            ImageLoader.getInstance().displayImage(bookMark.book_cover, holder.bookCover);
            holder.avatar.setTag(bookMark.book_cover);
        }
        if (bookMark.summary.length() > 100)
            holder.content.setText(Html.fromHtml(bookMark.summary.substring(0, 100) + "..."));
        else
            holder.content.setText(bookMark.summary);

        holder.nickname.setText(bookMark.nickname);
        holder.datetime.setText(bookMark.time);
        holder.bookTitle.setText(bookMark.book_title);
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