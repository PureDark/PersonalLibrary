package ml.puredark.personallibrary.beans;

import android.util.Log;

import java.util.Map;

import ml.puredark.personallibrary.dataprovider.AbstractDataProvider;
import  net.sourceforge.pinyin4j.PinyinHelper;

public final class Friend extends AbstractDataProvider.Data{
    public int uid;
    public int sex;
    public String nickname,signature;
    public String birthday;
    public String character;
    public boolean isFriend = false;
    public Friend(int uid, int sex, String nickname, String signature, String birthday, boolean isFriend){
        this.uid = uid;
        this.sex = sex;
        this.nickname = nickname;
        this.signature = signature;
        this.birthday = birthday;
        this.isFriend = isFriend;
        updateCharacter();
    }
    public void updateCharacter(){
        String[] temp = PinyinHelper.toHanyuPinyinStringArray(nickname.toCharArray()[0]);
        if(temp!=null){ //上API如果内容非汉字，则返回空
            character = new String(temp[0]);
            character = character.substring(0,1);
        }else{
            character = String.valueOf(nickname.toCharArray()[0]);
        }
        character = character.toUpperCase();    //字符大写
    }
    public int getId(){
        return uid;
    }
}