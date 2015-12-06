package ml.puredark.personallibrary.helpers;

import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import ml.puredark.personallibrary.PLApplication;
import ml.puredark.personallibrary.User;
import ml.puredark.personallibrary.activities.LoginActivity;
import ml.puredark.personallibrary.beans.BookListItem;
import ml.puredark.personallibrary.beans.Tag;
import ml.puredark.personallibrary.beans.UserInfo;
import ml.puredark.personallibrary.utils.SharedPreferencesUtil;


public class PLServerAPI {

    public static void login(final String cellphone, final String password, final onResponseListener callBack) {
        RequestParams params = new RequestParams();
        params.put("module", "user");
        params.put("action", "login");
        params.put("cellphone", cellphone);
        params.put("password", password);

        PLServerAPIClient.post(params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JsonParser parser = new JsonParser();
                JsonObject result = parser.parse(response.toString()).getAsJsonObject();
                if(result.get("status").getAsBoolean()) {
                    JsonObject userData = result.get("data").getAsJsonObject();
                    int uid = userData.get("uid").getAsInt();
                    int sex = userData.get("sex").getAsInt();
                    String nickname = (userData.get("nickname")==null)?"":userData.get("nickname").getAsString();
                    String signature = (userData.get("signature")==null)?"":userData.get("signature").getAsString();
                    String birthday = (userData.get("birthday")==null)?"":userData.get("birthday").getAsString();
                    String sessionid = (userData.get("sessionid")==null)?null:userData.get("sessionid").getAsString();
                    User.login(uid, sex, nickname, signature, birthday, sessionid);
                    User.setAutoLogin(cellphone, password);
                    SharedPreferencesUtil.saveData(PLApplication.mContext,"User",new Gson().toJson(new User()));
                    callBack.onSuccess(userData);
                }else{
                    int errorCode = result.get("errorCode").getAsInt();
                    if(errorCode==1002)
                        checkLogin(callBack);
                    else
                        callBack.onFailure(new ApiError(result.get("errorCode").getAsInt()));
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    callBack.onFailure(new ApiError(1009));
            }
        });
    }

    public static void logout(final onResponseListener callBack) {
        RequestParams params = new RequestParams();
        params.put("module", "user");
        params.put("action", "logout");

        PLServerAPIClient.post(params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JsonParser parser = new JsonParser();
                JsonObject result = parser.parse(response.toString()).getAsJsonObject();
                if(result.get("status").getAsBoolean())
                    callBack.onSuccess(null);
                else{
                    int errorCode = result.get("errorCode").getAsInt();
                    if(errorCode==1002)
                        checkLogin(callBack);
                    else
                        callBack.onFailure(new ApiError(result.get("errorCode").getAsInt()));
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callBack.onFailure(new ApiError(1009));
            }
        });
    }

    public static void verifyCellphoneUnused(String cellphone, final onResponseListener callBack) {
        RequestParams params = new RequestParams();
        params.put("module", "user");
        params.put("action", "verifyCellphoneUnused");
        params.put("cellphone", cellphone);

        PLServerAPIClient.post(params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JsonParser parser = new JsonParser();
                JsonObject result = parser.parse(response.toString()).getAsJsonObject();
                if (result.get("status").getAsBoolean())
                    callBack.onSuccess(null);
                else{
                    int errorCode = result.get("errorCode").getAsInt();
                    if(errorCode==1002)
                        checkLogin(callBack);
                    else
                        callBack.onFailure(new ApiError(result.get("errorCode").getAsInt()));
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callBack.onFailure(new ApiError(1009));
            }
        });
    }

    public static void sendCaptcha(String cellphone, final onResponseListener callBack) {
        RequestParams params = new RequestParams();
        params.put("module", "user");
        params.put("action", "sendCaptcha");
        params.put("cellphone", cellphone);

        PLServerAPIClient.post( params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JsonParser parser = new JsonParser();
                JsonObject result = parser.parse(response.toString()).getAsJsonObject();
                if(result.get("status").getAsBoolean()) {
                    JsonObject userData = result.get("data").getAsJsonObject();
                    String sessionid = userData.get("sessionid").getAsString();
                    User.setSessionid(sessionid);
                    callBack.onSuccess(userData);
                }else{
                    int errorCode = result.get("errorCode").getAsInt();
                    if(errorCode==1002)
                        checkLogin(callBack);
                    else
                        callBack.onFailure(new ApiError(result.get("errorCode").getAsInt()));
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if(callBack!=null)
                callBack.onFailure(new ApiError(1009));
            }
        });
    }

    public static void register(String cellphone, String password, String captcha, final onResponseListener callBack) {
        RequestParams params = new RequestParams();
        params.put("module", "user");
        params.put("action", "register");
        params.put("cellphone", cellphone);
        params.put("password", password);
        params.put("captcha", captcha);
        params.put("sessionid", User.getSessionid());

        PLServerAPIClient.post( params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JsonParser parser = new JsonParser();
                JsonObject result = parser.parse(response.toString()).getAsJsonObject();
                if(result.get("status").getAsBoolean())
                    callBack.onSuccess(null);
                else{
                    int errorCode = result.get("errorCode").getAsInt();
                    if(errorCode==1002)
                        checkLogin(callBack);
                    else
                        callBack.onFailure(new ApiError(result.get("errorCode").getAsInt()));
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callBack.onFailure(new ApiError(1009));
            }
        });
    }

    public static void changePassword(String oldpass, String newpass, final onResponseListener callBack) {
        RequestParams params = new RequestParams();
        params.put("module", "user");
        params.put("action", "changePassword");
        params.put("oldpass", oldpass);
        params.put("newpass", newpass);
        params.put("sessionid", User.getSessionid());

        PLServerAPIClient.post( params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JsonParser parser = new JsonParser();
                JsonObject result = parser.parse(response.toString()).getAsJsonObject();
                if(result.get("status").getAsBoolean())
                    callBack.onSuccess(null);
                else{
                    int errorCode = result.get("errorCode").getAsInt();
                    if(errorCode==1002)
                        checkLogin(callBack);
                    else
                        callBack.onFailure(new ApiError(result.get("errorCode").getAsInt()));
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callBack.onFailure(new ApiError(1009));
            }
        });
    }

    public static void resetPassword(String cellphone, String password, String captcha, final onResponseListener callBack) {
        RequestParams params = new RequestParams();
        params.put("module", "user");
        params.put("action", "resetPassword");
        params.put("cellphone", cellphone);
        params.put("password", password);
        params.put("captcha", captcha);
        params.put("sessionid", User.getSessionid());

        PLServerAPIClient.post(params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JsonParser parser = new JsonParser();
                JsonObject result = parser.parse(response.toString()).getAsJsonObject();
                if (result.get("status").getAsBoolean())
                    callBack.onSuccess(null);
                else{
                    int errorCode = result.get("errorCode").getAsInt();
                    if(errorCode==1002)
                        checkLogin(callBack);
                    else
                        callBack.onFailure(new ApiError(result.get("errorCode").getAsInt()));
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callBack.onFailure(new ApiError(1009));
            }
        });
    }

    public static void modifyUserInfo(String avatarPath, String nickname, int sex, String signature, String birthday,  final onResponseListener callBack) {
        RequestParams params = new RequestParams();
        params.put("module", "user");
        params.put("action", "modifyUserInfo");
        if (avatarPath != null) {
            try {
                File avatar = new File(avatarPath);
                params.put("avatar", avatar);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        params.put("nickname", nickname);
        params.put("sex", sex);
        params.put("signature", signature);
        params.put("birthday", birthday);
        params.put("sessionid", User.getSessionid());

        PLServerAPIClient.post( params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JsonParser parser = new JsonParser();
                JsonObject result = parser.parse(response.toString()).getAsJsonObject();
                if (result.get("status").getAsBoolean())
                    callBack.onSuccess(null);
                else{
                    int errorCode = result.get("errorCode").getAsInt();
                    if(errorCode==1002)
                        checkLogin(callBack);
                    else
                        callBack.onFailure(new ApiError(result.get("errorCode").getAsInt()));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callBack.onFailure(new ApiError(1009));
            }
        });
    }

    public static void getUserInfo(final onResponseListener callBack) {
        RequestParams params = new RequestParams();
        params.put("module", "user");
        params.put("action", "getUserInfo");
        params.put("sessionid", User.getSessionid());

        PLServerAPIClient.post( params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JsonParser parser = new JsonParser();
                JsonObject result = parser.parse(response.toString()).getAsJsonObject();
                if(result.get("status").getAsBoolean()) {
                    UserInfo userInfo = new Gson().fromJson(result.get("data"),UserInfo.class);
                    callBack.onSuccess(userInfo);
                }else{
                    int errorCode = result.get("errorCode").getAsInt();
                    if(errorCode==1002)
                        checkLogin(callBack);
                    else
                        callBack.onFailure(new ApiError(result.get("errorCode").getAsInt()));
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callBack.onFailure(new ApiError(1009));
            }
        });
    }

    public static void getUidByCellphone(String cellphone, final onResponseListener callBack) {
        RequestParams params = new RequestParams();
        params.put("module", "user");
        params.put("action", "getUidByCellphone");
        params.put("cellphone", cellphone);

        PLServerAPIClient.post( params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JsonParser parser = new JsonParser();
                JsonObject result = parser.parse(response.toString()).getAsJsonObject();
                if(result.get("status").getAsBoolean()) {
                    int uid = result.get("data").getAsJsonObject().get("uid").getAsInt();
                    callBack.onSuccess(uid);
                }else{
                    int errorCode = result.get("errorCode").getAsInt();
                    if(errorCode==1002)
                        checkLogin(callBack);
                    else
                        callBack.onFailure(new ApiError(result.get("errorCode").getAsInt()));
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callBack.onFailure(new ApiError(1009));
            }
        });
    }

    public static void addBook(final String isbn13, final String cover, final String title, final String author,final String description, final onResponseListener callBack) {
        RequestParams params = new RequestParams();
        params.put("module", "library");
        params.put("action", "addBook");
        params.put("isbn13", isbn13);
        params.put("cover", cover);
        params.put("title", title);
        params.put("author", author);
        params.put("description", description);
        params.put("sessionid", User.getSessionid());

        PLServerAPIClient.post( params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JsonParser parser = new JsonParser();
                JsonObject result = parser.parse(response.toString()).getAsJsonObject();
                if(result.get("status").getAsBoolean()){
                    JsonObject data = result.get("data").getAsJsonObject();
                    int bid = data.get("bid").getAsInt();
                    BookListItem book = new BookListItem(bid, isbn13, cover, title, author, description);
                    callBack.onSuccess(book);
                }else{
                    int errorCode = result.get("errorCode").getAsInt();
                    if(errorCode==1002)
                        checkLogin(callBack);
                    else
                        callBack.onFailure(new ApiError(result.get("errorCode").getAsInt()));
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callBack.onFailure(new ApiError(1009));
            }
        });
    }

    public static void deleteBook(int bid, final onResponseListener callBack) {
        RequestParams params = new RequestParams();
        params.put("module", "library");
        params.put("action", "deleteBook");
        params.put("bid", bid);
        params.put("sessionid", User.getSessionid());

        PLServerAPIClient.post( params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JsonParser parser = new JsonParser();
                JsonObject result = parser.parse(response.toString()).getAsJsonObject();
                if(result.get("status").getAsBoolean())
                    callBack.onSuccess(null);
                else{
                    int errorCode = result.get("errorCode").getAsInt();
                    if(errorCode==1002)
                        checkLogin(callBack);
                    else
                        callBack.onFailure(new ApiError(result.get("errorCode").getAsInt()));
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callBack.onFailure(new ApiError(1009));
            }
        });
    }

    public static void addTag(int bid, String tag, final onResponseListener callBack) {
        RequestParams params = new RequestParams();
        params.put("module", "library");
        params.put("action", "addTag");
        params.put("bid", bid);
        params.put("tag", tag);
        params.put("sessionid", User.getSessionid());

        PLServerAPIClient.post( params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JsonParser parser = new JsonParser();
                JsonObject result = parser.parse(response.toString()).getAsJsonObject();
                if(result.get("status").getAsBoolean())
                    callBack.onSuccess(null);
                else{
                    int errorCode = result.get("errorCode").getAsInt();
                    if(errorCode==1002)
                        checkLogin(callBack);
                    else
                        callBack.onFailure(new ApiError(result.get("errorCode").getAsInt()));
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callBack.onFailure(new ApiError(1009));
            }
        });
    }

    public static void deleteTag(int bid, int tid, final onResponseListener callBack) {
        RequestParams params = new RequestParams();
        params.put("module", "library");
        params.put("action", "deleteTag");
        params.put("bid", bid);
        params.put("tid", tid);
        params.put("sessionid", User.getSessionid());

        PLServerAPIClient.post( params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JsonParser parser = new JsonParser();
                JsonObject result = parser.parse(response.toString()).getAsJsonObject();
                if(result.get("status").getAsBoolean())
                    callBack.onSuccess(null);
                else{
                    int errorCode = result.get("errorCode").getAsInt();
                    if(errorCode==1002)
                        checkLogin(callBack);
                    else
                        callBack.onFailure(new ApiError(result.get("errorCode").getAsInt()));
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callBack.onFailure(new ApiError(1009));
            }
        });
    }

    public static void getTagList(final onResponseListener callBack) {
        RequestParams params = new RequestParams();
        params.put("module", "library");
        params.put("action", "getTagList");
        params.put("sessionid", User.getSessionid());

        PLServerAPIClient.post( params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JsonParser parser = new JsonParser();
                JsonObject result = parser.parse(response.toString()).getAsJsonObject();
                if(result.get("status").getAsBoolean()) {
                    JsonElement data = result.get("data");
                    List<Tag> tags = new Gson().fromJson(data, new TypeToken<List<Tag>>(){}.getType());
                    callBack.onSuccess(tags);
                }else{
                    int errorCode = result.get("errorCode").getAsInt();
                    if(errorCode==1002)
                        checkLogin(callBack);
                    else
                        callBack.onFailure(new ApiError(result.get("errorCode").getAsInt()));
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callBack.onFailure(new ApiError(1009));
            }
        });
    }

    public static void getBookList(int[] tids, final onResponseListener callBack) {
        RequestParams params = new RequestParams();
        params.put("module", "library");
        params.put("action", "getBookList");
        if(tids!=null)
            params.put("tids", new Gson().toJson(tids));
        params.put("sessionid", User.getSessionid());

        PLServerAPIClient.post( params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JsonParser parser = new JsonParser();
                JsonObject result = parser.parse(response.toString()).getAsJsonObject();
                if(result.get("status").getAsBoolean()) {
                    JsonElement data = result.get("data");
                    List<BookListItem> tags = new Gson().fromJson(data, new TypeToken<List<BookListItem>>(){}.getType());
                    callBack.onSuccess(tags);
                }else{
                    int errorCode = result.get("errorCode").getAsInt();
                    if(errorCode==1002)
                        checkLogin(callBack);
                    else
                        callBack.onFailure(new ApiError(result.get("errorCode").getAsInt()));
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callBack.onFailure(new ApiError(1009));
            }
        });
    }







    public static void checkLogin(final onResponseListener callBack) {
        // 如果记住过手机和密码
        if(User.isRemembered()){
            PLServerAPI.login(User.getCellphone(), User.getPassword(), new PLServerAPI.onResponseListener() {
                @Override
                public void onSuccess(Object data) {
                    // 打开主界面
                    callBack.onFailure(new ApiError(1009));
                }
                @Override
                public void onFailure(PLServerAPI.ApiError apiError) {
                    // 打开登陆界面
                    Intent intent = new Intent(PLApplication.mContext, LoginActivity.class);
                    PLApplication.mContext.startActivity(intent);
                }
            });
        }else{
            // 打开登陆界面
            Intent intent = new Intent(PLApplication.mContext, LoginActivity.class);
            PLApplication.mContext.startActivity(intent);
        }
    }

    public static class PLServerAPIClient {
        private static final String BASE_URL = PLApplication.serverHost+"/PersonalLibrary/servlet/manager";
        private static AsyncHttpClient client = new AsyncHttpClient();

        public static void get(RequestParams params, AsyncHttpResponseHandler responseHandler) {
            client.get(BASE_URL, params, responseHandler);
        }

        public static void post(RequestParams params, AsyncHttpResponseHandler responseHandler) {
            client.post(BASE_URL, params, responseHandler);
        }
    }

    public interface onResponseListener{
        void onSuccess(Object data);
        void onFailure(ApiError apiError);
    }


    //对错误码的预定义
    public static class ApiError{
        private int errorCode;
        private String errorString = "";

        public ApiError(int errorCode){
            this.errorCode = errorCode;
            switch(errorCode){
                case 1000:errorString="未知错误";break;
                case 1001:errorString="参数不全";break;
                case 1002:errorString="尚未登录";break;
                case 1003:errorString="模块不存在";break;
                case 1004:errorString="没有权限";break;
                case 1005:errorString="json解析错误";break;
                case 1006:errorString="无此接口";break;
                case 1009:errorString="网络错误，请重试";break;
                case 1010:errorString="数据库错误";break;
                case 1011:errorString="密码错误";break;
                case 1012:errorString="用户不存在";break;
                case 1021:errorString="用户无此书";break;
                case 1023:errorString="Tag不存在";break;
                case 1032:errorString="用户信息不存在";break;
                default:errorString="未定义的错误码";break;
            }
        }
        public int getErrorCode(){
            return this.errorCode;
        }
        public String getErrorString(){
            return this.errorString;
        }
        @Override
        public String toString(){
            return errorCode + " : " + errorString;
        }
    }
}
