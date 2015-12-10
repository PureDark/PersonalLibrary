package ml.puredark.personallibrary.beans;

import ml.puredark.personallibrary.dataprovider.AbstractDataProvider;

/**
 * Created by PureDark on 2015/12/10.
 */
public class BorrowRecord extends AbstractDataProvider.Data {
    public int brid,loan_uid,borrow_uid,book_id,status;
    public String nickname,book_name,borrow_time,return_time;
    public BorrowRecord(int brid,int loan_uid,int borrow_uid, String nickname, int book_id ,String book_name,String borrow_time,String return_time,int status){
        this.brid = brid;
        this.loan_uid = loan_uid;
        this.borrow_uid = borrow_uid;
        this.nickname = nickname;
        this.book_id = book_id;
        this.book_name = book_name;
        this.borrow_time = borrow_time;
        this.status = status;
        this.return_time = return_time;
    }

    @Override
    public int getId() {
        return brid;
    }
}
