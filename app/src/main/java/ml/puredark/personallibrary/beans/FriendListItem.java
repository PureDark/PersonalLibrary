package ml.puredark.personallibrary.beans;

import java.util.Map;

import ml.puredark.personallibrary.dataprovider.AbstractDataProvider;

public final class FriendListItem  extends AbstractDataProvider.Data{
    public int id;
    public int sex;
    public String avatar, nickName,signature;
    public String birthday;
    public FriendListItem(int id, int sex, String avatar, String nickName, String signature, String birthday){
        this.id = id;
        this.sex = sex;
        this.avatar = avatar;
        this.nickName = nickName;
        this.signature = signature;
        this.birthday = birthday;
    }
    public long getId(){
        return id;
    }
}