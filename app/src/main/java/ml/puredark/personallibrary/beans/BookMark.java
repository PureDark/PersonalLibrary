package ml.puredark.personallibrary.beans;

import ml.puredark.personallibrary.dataprovider.AbstractDataProvider;

/**
 * Created by PureDark on 2015/12/8.
 */
public class BookMark extends AbstractDataProvider.Data {
    public int mid, bid, uid;
    public String title, summary, content, time, isbn13, book_title, book_cover, nickname;
    public BookMark(int mid, String title, String summary, String content, String time, int bid, String isbn13, String book_title, String book_cover, int uid, String nickname) {
        this.mid = mid;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.time = time;
        this.bid = bid;
        this.isbn13 = isbn13;
        this.book_title = book_title;
        this.book_cover = book_cover;
        this.uid = uid;
        this.nickname = nickname;
    }

    @Override
    public int getId() {
        return mid;
    }
}
