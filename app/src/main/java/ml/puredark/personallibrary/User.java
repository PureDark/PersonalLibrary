package ml.puredark.personallibrary;

public class User{
    static int uid, sex;
    static String nickname,signature,birthday;
    static String sessionid;
    static String cellphone, password;
    public static void login(int uid, int sex, String nickname, String signature, String birthday, String sessionid){
        User.uid = uid;
        User.sex = sex;
        User.nickname = nickname;
        User.signature = signature;
        User.birthday = birthday;
        User.sessionid = sessionid;
    }

    public static void setAutoLogin(String cellphone, String password){
        User.cellphone = cellphone;
        User.password = password;
    }

    public static boolean isRemembered(){
        return (User.getCellphone()!=null&&User.getPassword()!=null);
    }

    public static String getCellphone(){
        return User.cellphone;
    }
    public static String getPassword(){
        return User.password;
    }

    public static int getUid(){
        return User.uid;
    }

    public static int getSex(){
        return User.sex;
    }
    public static void setSex(int sex){
        User.sex = sex;
    }

    public static String getNickname(){
        return User.nickname;
    }
    public static void setNickname(String nickname){
        User.nickname = nickname;
    }

    public static String getSignature(){
        return User.signature;
    }
    public static void setSignature(String signature){
        User.signature = signature;
    }

    public static String getBirthday(){
        return User.birthday;
    }
    public static void setBirthday(String birthday){
        User.birthday = birthday;
    }

    public static String getSessionid(){
        return User.sessionid;
    }
    public static void setSessionid(String sessionid){
        User.sessionid = sessionid;
    }
}
