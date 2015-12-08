package ml.puredark.personallibrary.beans;

import ml.puredark.personallibrary.dataprovider.AbstractDataProvider;

/**
 * Created by PureDark on 2015/12/9.
 */
public class Request extends AbstractDataProvider.Data {
    public int rid,uid,status;
    public String nickname;
    public Request(int rid, int uid, String nickname, int status) {
        this.rid = rid;
        this.uid = uid;
        this.nickname = nickname;
        this.status = status;
    }
    @Override
    public int getId() {
        return rid;
    }
}
