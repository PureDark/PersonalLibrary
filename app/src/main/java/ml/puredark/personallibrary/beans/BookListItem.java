package ml.puredark.personallibrary.beans;

import ml.puredark.personallibrary.dataprovider.AbstractDataProvider;

public final class BookListItem extends AbstractDataProvider.Data {
    public final int id;
    private boolean mPinned;
    public final String cover, title, author, description, isbn13;

    public BookListItem(int id, String isbn13, String cover, String title, String author, String description){
        this.id = id;
        this.isbn13 = isbn13;
        this.cover = cover;
        this.title = title;
        this.author = author;
        this.description = description;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public boolean isPinned() {
        return mPinned;
    }

    @Override
    public void setPinned(boolean pinned) {
        mPinned = pinned;
    }
}