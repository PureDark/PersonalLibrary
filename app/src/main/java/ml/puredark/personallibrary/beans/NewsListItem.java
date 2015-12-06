package ml.puredark.personallibrary.beans;

import ml.puredark.personallibrary.dataprovider.AbstractDataProvider;

public final class NewsListItem extends AbstractDataProvider.Data{
    public int id;
    public String avatar, nickname, datetime, content;
    public BookListItem book;
    public NewsListItem(int id, String avatar, String nickname, String datetime, String content, BookListItem book){
        this.id = id;
        this.avatar = avatar;
        this.nickname = nickname;
        this.datetime = datetime;
        this.content = content;
        this.book = book;
    }
    public int getId(){
        return id;
    }
}