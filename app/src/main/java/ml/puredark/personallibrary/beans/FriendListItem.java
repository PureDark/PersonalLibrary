package ml.puredark.personallibrary.beans;

import android.util.Log;

import java.util.Map;

import ml.puredark.personallibrary.dataprovider.AbstractDataProvider;
import  net.sourceforge.pinyin4j.PinyinHelper;

public final class FriendListItem  extends AbstractDataProvider.Data{
    public int id;
    public int sex;
    public String avatar, nickName,signature;
    public String birthday;
    public String character;
    public FriendListItem(int id, int sex, String avatar, String nickName, String signature, String birthday){
        this.id = id;
        this.sex = sex;
        this.avatar = avatar;
        this.nickName = nickName;
        this.signature = signature;
        this.birthday = birthday;
        String[] temp = PinyinHelper.toHanyuPinyinStringArray(nickName.toCharArray()[0]);
        character = new String(temp[0]);
        character = character.substring(0,1);
        character = character.toUpperCase();
    }
    public int getId(){
        return id;
    }
}