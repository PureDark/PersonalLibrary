package ml.puredark.personallibrary.beans;

import ml.puredark.personallibrary.dataprovider.AbstractDataProvider;

public final class BookListItem extends AbstractDataProvider.Data {
    public final int bid;
    public final String cover, title, author, summary, isbn13;

    public BookListItem(int bid, String isbn13, String cover, String title, String author, String summary){
        this.bid = bid;
        this.isbn13 = isbn13;
        this.cover = cover;
        this.title = title;
        this.author = author;
        this.summary = summary;
    }

    @Override
    public int getId() {
        return bid;
    }
}