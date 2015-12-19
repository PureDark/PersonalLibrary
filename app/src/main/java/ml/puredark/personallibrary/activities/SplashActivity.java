package ml.puredark.personallibrary.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import ml.puredark.personallibrary.PLApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.User;
import ml.puredark.personallibrary.beans.UserInfo;
import ml.puredark.personallibrary.helpers.PLServerAPI;
import ml.puredark.personallibrary.utils.SharedPreferencesUtil;

public class SplashActivity extends AppCompatActivity {
    private String version = "1.0.0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //用代码生成欢迎界面的渐变背景图以减小APK体积
        ImageView bg = (ImageView)findViewById(R.id.background);
        GradientDrawable bg_drawable = (GradientDrawable)bg.getDrawable();
        //获取窗体高度
        WindowManager winManager=(WindowManager)getSystemService(Context.WINDOW_SERVICE);
        bg_drawable.setGradientRadius((int)(winManager.getDefaultDisplay().getHeight()*0.6));
        //获取当前版本号并展示在欢迎界面右下角
        try {
            version = PLApplication.getVersionName();
            TextView versionNumber = (TextView) findViewById(R.id.versionNumber);
            versionNumber.setText("Version " + version);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String userdata = (String)SharedPreferencesUtil.getData(this, "User", "{}");
        new Gson().fromJson(userdata, User.class);
        // 如果记住过手机和密码
        if(User.isRemembered()){
            PLServerAPI.login(User.getCellphone(), User.getPassword(), new PLServerAPI.onResponseListener() {
                @Override
                public void onSuccess(Object data) {
                    // 打开主界面
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    SplashActivity.this.startActivity(intent);
                    SplashActivity.this.finish();
                }

                @Override
                public void onFailure(PLServerAPI.ApiError apiError) {
                    // 打开登陆界面
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    SplashActivity.this.startActivity(intent);
                    SplashActivity.this.finish();
                }
            });
        }else{
            // 打开登陆界面
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            SplashActivity.this.startActivity(intent);
            SplashActivity.this.finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //在欢迎界面屏蔽BACK键
        if(keyCode==KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return false;
    }

}
