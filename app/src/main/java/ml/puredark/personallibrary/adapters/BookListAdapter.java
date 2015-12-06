package ml.puredark.personallibrary.adapters;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.LegacySwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.android.percent.support.PercentRelativeLayout;

import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.beans.BookListItem;
import ml.puredark.personallibrary.dataprovider.AbstractDataProvider;
import ml.puredark.personallibrary.utils.ViewUtils;

public class BookListAdapter
        extends RecyclerView.Adapter<BookListAdapter.BookViewHolder>
        implements DraggableItemAdapter<BookListAdapter.BookViewHolder>,
        LegacySwipeableItemAdapter<BookListAdapter.BookViewHolder> {
    private AbstractDataProvider mProvider;
    private EventListener mEventListener;
    private MyItemClickListener mItemClickListener;
    private boolean isGrid = false;

    public interface EventListener {
        void onItemRemoved(int position, BookListItem book);
    }

    // NOTE: 短名引用
    private interface Draggable extends DraggableItemConstants {
    }
    private interface Swipeable extends SwipeableItemConstants {
    }

    public class BookViewHolder extends AbstractDraggableSwipeableItemViewHolder implements View.OnClickListener {
        public MaterialRippleLayout rippleLayout;
        public View container;
        public ImageView cover;
        public TextView title,author,description;
        private MyItemClickListener mListener;

        public BookViewHolder(View view, MyItemClickListener onClickListener) {
            super(view);
            container = view.findViewById(R.id.container);
            cover = (ImageView)view.findViewById(R.id.cover);
            rippleLayout = (MaterialRippleLayout) view.findViewById(R.id.rippleLayout);
            title = (TextView)view.findViewById(R.id.title);
            author = (TextView)view.findViewById(R.id.author);
            description = (TextView)view.findViewById(R.id.description);
            mListener = onClickListener;
            if(cover!=null)
                cover.setOnClickListener(this);
            if(rippleLayout!=null)
                rippleLayout.setOnClickListener(this);
        }

        @Override
        public View getSwipeableContainerView() {
            return container;
        }

        @Override
        public void onClick(View view) {
            if(mListener != null){
                mListener.onItemClick(cover, getPosition());
            }
        }
    }

    public BookListAdapter(AbstractDataProvider mProvider) {
        this.mProvider = mProvider;
        setHasStableIds(true);
    }

    public void setDataProvider(AbstractDataProvider mProvider){
        this.mProvider = mProvider;
    }

    @Override
    public BookListAdapter.BookViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        int layout = (isGrid)?R.layout.item_book_grid:R.layout.item_book_list;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);

        // 在这里对View的参数进行设置
        BookViewHolder vh = new BookViewHolder(v, mItemClickListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(BookViewHolder holder, int position) {
        final BookListItem book = (BookListItem) mProvider.getItem(position);

        if(holder.cover.getTag()!=book.cover) {
            ImageLoader.getInstance().displayImage(null, holder.cover);
            ImageLoader.getInstance().displayImage(book.cover, holder.cover);
            holder.cover.setTag(book.cover);
        }
        if(!isGrid) {
            holder.title.setText(book.title);
            holder.author.setText(book.author);
            holder.description.setText(book.description);
        }

        // set swiping properties
        holder.setSwipeItemHorizontalSlideAmount(0);
    }

    @Override
    public int getItemCount() {
        int count = 1;
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

    @Override
    public int onGetSwipeReactionType(BookViewHolder holder, int position, int x, int y) {
        if (onCheckCanStartDrag(holder, position, x, y)) {
            return Swipeable.REACTION_CAN_NOT_SWIPE_BOTH_H;
        } else {
            return Swipeable.REACTION_CAN_SWIPE_LEFT | Swipeable.REACTION_MASK_START_SWIPE_LEFT |
                    Swipeable.REACTION_CAN_SWIPE_RIGHT | Swipeable.REACTION_MASK_START_SWIPE_RIGHT |
                    Swipeable.REACTION_START_SWIPE_ON_LONG_PRESS;
        }
    }

    @Override
    public void onSetSwipeBackground(BookViewHolder holder, int position, int type) {
        int rootViewBgRes = 0;
        int containerBgRes = 0;
        switch (type) {
            case Swipeable.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND:
                rootViewBgRes = R.drawable.carview_radius_bg;
                containerBgRes = R.color.transparent;
                break;
            case Swipeable.DRAWABLE_SWIPE_LEFT_BACKGROUND:
                rootViewBgRes = R.drawable.bg_swipe_item_right;
                containerBgRes = R.drawable.carview_radius_bg;
                break;
            case Swipeable.DRAWABLE_SWIPE_RIGHT_BACKGROUND:
                rootViewBgRes = R.drawable.bg_swipe_item_left;
                containerBgRes = R.drawable.carview_radius_bg;
                break;
        }
            holder.itemView.setBackgroundResource(rootViewBgRes);
            holder.container.setBackgroundResource(containerBgRes);
    }

    @Override
    public int onSwipeItem(BookViewHolder holder, int position, int result) {
        switch (result) {
            // swipe right
            case Swipeable.RESULT_SWIPED_RIGHT:
                return Swipeable.AFTER_SWIPE_REACTION_REMOVE_ITEM;
            // swipe left
            case Swipeable.RESULT_SWIPED_LEFT:
                return Swipeable.AFTER_SWIPE_REACTION_REMOVE_ITEM;
            // other
            case Swipeable.RESULT_CANCELED:
            default:
                return Swipeable.AFTER_SWIPE_REACTION_DEFAULT;
        }
    }

    @Override
    public void onPerformAfterSwipeReaction(BookViewHolder holder, int position, int result, int reaction) {
        if (reaction == Swipeable.AFTER_SWIPE_REACTION_REMOVE_ITEM) {
            BookListItem book = (BookListItem) getDataProvider().getItem(position);
            mProvider.removeItem(position);
            notifyItemRemoved(position);
            if (mEventListener != null) {
                mEventListener.onItemRemoved(position, book);
            }
        }
    }

    public void setIsGrid(boolean isGrid){
        this.isGrid = isGrid;
    }

    public EventListener getEventListener() {
        return mEventListener;
    }

    public void setEventListener(EventListener eventListener) {
        mEventListener = eventListener;
    }

    public void setOnItemClickListener(MyItemClickListener listener){
        this.mItemClickListener = listener;
    }

    public AbstractDataProvider getDataProvider(){
        return mProvider;
    }
    public interface MyItemClickListener {
        void onItemClick(View view,int postion);
    }

}