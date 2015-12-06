package ml.puredark.personallibrary.beans;

public class UserInfo {
    public int uid, sex;
    public String nickname,signature,birthday;
    public String sessionid;
    public UserInfo(int uid, int sex, String nickname, String signature, String birthday, String sessionid){
        this.uid = uid;
        this.sex = sex;
        this.nickname = nickname;
        this.signature = signature;
        this.birthday = birthday;
        this.sessionid = sessionid;
    }
}
