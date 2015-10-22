package ml.puredark.personallibrary.adapters;

import android.graphics.drawable.NinePatchDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;


import ml.puredark.personallibrary.PersonalLibraryApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.beans.BookListItem;
import ml.puredark.personallibrary.dataprovider.AbstractDataProvider;
import ml.puredark.personallibrary.dataprovider.BookListDataProvider;
import ml.puredark.personallibrary.utils.DensityUtils;
import ml.puredark.personallibrary.utils.ViewUtils;

public class BookListAdapter
        extends RecyclerView.Adapter<BookListAdapter.BookViewHolder>
        implements DraggableItemAdapter<BookListAdapter.BookViewHolder> {
    private AbstractDataProvider mProvider;
    private MyItemClickListener mItemClickListener;

    // NOTE: 短名引用
    private interface Draggable extends DraggableItemConstants {
    }

    public class BookViewHolder extends AbstractDraggableItemViewHolder implements View.OnClickListener {
        public View card;
        public LinearLayout container;
        public ImageView cover;
        public TextView title,author,description;
        private MyItemClickListener mListener;

        public BookViewHolder(View view, MyItemClickListener onClickListener) {
            super(view);
            card = view.findViewById(R.id.card);
            container = (LinearLayout)view.findViewById(R.id.container);
            cover = (ImageView)view.findViewById(R.id.cover);
            title = (TextView)view.findViewById(R.id.title);
            author = (TextView)view.findViewById(R.id.author);
            description = (TextView)view.findViewById(R.id.description);
            mListener = onClickListener;
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(mListener != null){
                System.out.println("ViewHolder.cover="+(cover==null));
                mListener.onItemClick(cover,getPosition());
            }
        }
    }

    public BookListAdapter(AbstractDataProvider mProvider) {
        this.mProvider = mProvider;
        setHasStableIds(false);
    }

    @Override
    public BookListAdapter.BookViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        // 在这里对View的参数进行设置
        BookViewHolder vh = new BookViewHolder(v, mItemClickListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(BookViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        BookListItem book = (BookListItem) mProvider.getItem(position);
        ImageLoader.getInstance().displayImage(null, holder.cover);
        ImageLoader.getInstance().displayImage(book.cover, holder.cover);
        holder.title.setText(book.title);
        holder.author.setText(book.author);
        holder.description.setText(book.description);
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


    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        mProvider.moveItem(fromPosition, toPosition);

        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public boolean onCheckCanStartDrag(BookViewHolder holder, int position, int x, int y) {
        // x, y --- relative from the itemView's top-left
        final View containerView = holder.container;
        final View dragHandleView = holder.cover;

        final int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
        final int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);

        return ViewUtils.hitTest(dragHandleView, x - offsetX, y - offsetY);
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(BookViewHolder holder, int position) {
        // no drag-sortable range specified
        return null;
    }

    public void setOnItemClickListener(MyItemClickListener listener){
        this.mItemClickListener = listener;
    }

    public AbstractDataProvider getDataProvider(){
        return mProvider;
    }
    public interface MyItemClickListener {
        public void onItemClick(View view,int postion);
    }

}