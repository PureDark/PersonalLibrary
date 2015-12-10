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
import com.gc.materialdesign.views.ButtonFlat;
import com.nostra13.universalimageloader.core.ImageLoader;

import ml.puredark.personallibrary.PLApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.beans.BookMark;
import ml.puredark.personallibrary.beans.BorrowRecord;
import ml.puredark.personallibrary.dataprovider.AbstractDataProvider;

public class BorrowRecordAdapter
        extends RecyclerView.Adapter<BorrowRecordAdapter.BorrowRecordViewHolder> {
    private AbstractDataProvider mProvider;
    private MyItemClickListener mItemClickListener;
    private boolean isLoaned = false;

    public class BorrowRecordViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout book;
        public ImageView avatar;
        public TextView nickname,datetime,content,status;
        private ButtonFlat btnAccept,btnDecline, btnReturn;
        private MyItemClickListener mListener;

        public BorrowRecordViewHolder(View view, MyItemClickListener onClickListener) {
            super(view);
            avatar = (ImageView)view.findViewById(R.id.avatar);
            nickname = (TextView)view.findViewById(R.id.nickname);
            datetime = (TextView)view.findViewById(R.id.datetime);
            content = (TextView)view.findViewById(R.id.content);
            status = (TextView)view.findViewById(R.id.status);
            btnAccept = (ButtonFlat)view.findViewById(R.id.btnAccept);
            btnDecline = (ButtonFlat)view.findViewById(R.id.btnDecline);
            btnReturn = (ButtonFlat)view.findViewById(R.id.btnReturn);
            mListener = onClickListener;
            if(btnAccept!=null)
                btnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mListener != null){
                            mListener.onItemClick(view, getPosition(), MyItemClickListener.ACCEPT);
                        }
                    }
                });
            if(btnDecline!=null)
                btnDecline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mListener != null){
                            mListener.onItemClick(view, getPosition(), MyItemClickListener.DECLINE);
                        }
                    }
                });
            if(btnReturn!=null)
                btnReturn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mListener != null){
                            mListener.onItemClick(view, getPosition(), MyItemClickListener.RETURN);
                        }
                    }
                });
        }
    }

    public BorrowRecordAdapter(AbstractDataProvider mProvider) {
        this.mProvider = mProvider;
        setHasStableIds(true);
    }

    public void setDataProvider(AbstractDataProvider mProvider){
        this.mProvider = mProvider;
    }

    @Override
    public BorrowRecordAdapter.BorrowRecordViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_borrow_record, parent, false);
        // 在这里对View的参数进行设置
        BorrowRecordViewHolder vh = new BorrowRecordViewHolder(v, mItemClickListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(BorrowRecordViewHolder holder, int position) {
        final BorrowRecord borrowRecord = (BorrowRecord) mProvider.getItem(position);
        int uid = (isLoaned)?borrowRecord.borrow_uid:borrowRecord.loan_uid;
        if(holder.avatar.getTag()!=uid) {
            ImageLoader.getInstance().displayImage(null, holder.avatar);
            ImageLoader.getInstance().displayImage(PLApplication.serverHost + "/images/users/avatars/" + uid + ".png", holder.avatar);
            holder.avatar.setTag(uid);
        }

        holder.nickname.setText(borrowRecord.nickname);
        String time = (borrowRecord.status==4)?borrowRecord.return_time:borrowRecord.borrow_time;
        holder.datetime.setText(time);

        if(isLoaned){
            holder.content.setText("请求借阅《" + borrowRecord.book_name + "》");
            if(borrowRecord.status==1){
                holder.btnAccept.setVisibility(View.VISIBLE);
                holder.btnDecline.setVisibility(View.VISIBLE);
                holder.btnReturn.setVisibility(View.GONE);
                holder.status.setVisibility(View.GONE);
            }else{
                holder.btnAccept.setVisibility(View.GONE);
                holder.btnDecline.setVisibility(View.GONE);
                if(borrowRecord.status==2){
                    holder.status.setVisibility(View.GONE);
                    holder.btnReturn.setVisibility(View.VISIBLE);
                }else{
                    holder.status.setVisibility(View.VISIBLE);
                    holder.btnReturn.setVisibility(View.GONE);
                }
            }
            String status = (borrowRecord.status==2)?"已同意":(borrowRecord.status==3)?"已拒绝":(borrowRecord.status==4)?"已归还":"";
            holder.status.setText(status);
        }else{
            holder.content.setText("向TA借了《"+borrowRecord.book_name+"》");
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnDecline.setVisibility(View.GONE);
            holder.btnReturn.setVisibility(View.GONE);
            holder.status.setVisibility(View.VISIBLE);
            String status = (borrowRecord.status==2)?"未归还":(borrowRecord.status==3)?"已拒绝":(borrowRecord.status==4)?"已归还":"待接受";
            holder.status.setText(status);
        }
    }

    public void setIsLoaned(boolean isLoaned){
        this.isLoaned = isLoaned;
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
        public static int ACCEPT = 1;
        public static int DECLINE = 2;
        public static int RETURN = 3;
        void onItemClick(View view, int postion, int action);
    }
}