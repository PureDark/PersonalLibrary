package ml.puredark.personallibrary.helpers;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import ml.puredark.personallibrary.activities.MainActivity;
import ml.puredark.personallibrary.beans.Book;


public class DoubanRestAPI {
    public static final String API_GET_BOOK_BY_ID = "/v2/book/";
    public static final String API_GET_BOOK_BY_ISBN = "/v2/book/isbn/";

    public static void getBookByID(String id, final MainActivity.CallBack callBack) {
        DoubanRestAPIClient.get(API_GET_BOOK_BY_ID+id, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Gson gson = new Gson();
                Book book = gson.fromJson(response.toString(), Book.class);
                callBack.action(book);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                callBack.action(null);
            }
        });
    }

    public static void getBookByISBN(String isbn13, final MainActivity.CallBack callBack) {
        DoubanRestAPIClient.get(API_GET_BOOK_BY_ISBN+isbn13, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Gson gson = new Gson();
                Book book = gson.fromJson(response.toString(), Book.class);
                callBack.action(book);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                callBack.action(null);
            }
        });
    }

    public static class DoubanRestAPIClient {
        private static final String BASE_URL = "https://api.douban.com";
        private static AsyncHttpClient client = new AsyncHttpClient();

        public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
            client.get(getAbsoluteUrl(url), params, responseHandler);
        }

        public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
            client.post(getAbsoluteUrl(url), params, responseHandler);
        }

        private static String getAbsoluteUrl(String relativeUrl) {
            return BASE_URL + relativeUrl;
        }
    }
}
