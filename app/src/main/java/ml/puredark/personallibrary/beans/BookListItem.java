package ml.puredark.personallibrary.beans;

import ml.puredark.personallibrary.dataprovider.AbstractDataProvider;

public final class BookListItem extends AbstractDataProvider.Data {
    public final int id;
    private final int mViewType;
    private boolean mPinned;
    public final String cover, title, author, description;

    public BookListItem(int id, int viewType, String cover, String title, String author, String description){
        this.id = id;
        this.mViewType = viewType;
        this.cover = cover;
        this.title = title;
        this.author = author;
        this.description = description;
    }

    @Override
    public int getViewType() {
        return mViewType;
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