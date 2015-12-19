package ml.puredark.personallibrary.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import android.content.Intent;

import android.os.AsyncTask;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.dd.CircularProgressButton;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.gc.materialdesign.views.ButtonFlat;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import ml.puredark.personallibrary.PLApplication;
import ml.puredark.personallibrary.adapters.ViewPagerAdapter;
import ml.puredark.personallibrary.customs.FixedSpeedScroller;
import ml.puredark.personallibrary.helpers.BgViewAware;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.helpers.PLServerAPI;
import ml.puredark.personallibrary.utils.SharedPreferencesUtil;

/**
 * 通过 手机号/密码 登录的登录界面
 */
public class LoginActivity extends AppCompatActivity {

    //注册的AsyncTask
    //private UserRegisterTask mRegisterTask = null;
    // 登录界面相关引用
    private AutoCompleteTextView mCellphoneView;
    private EditText mPasswordView;
    private ImageView mAvatarView;
    private CircularProgressButton btnLogin;
    private ButtonFlat btnGoRegister;
    private ButtonFlat btnGoForgot;

    private boolean isDefaultAvatar = true;

    // 注册界面相关引用
    private AutoCompleteTextView mRegisterCellphone;
    private EditText mRegisterPassword;
    private EditText mConfirePassword;
    private CircularProgressButton btnRegister;

    // 忘记密码界面相关引用
    private EditText mForgetCellphone;
    private EditText mForgetPassword;
    private EditText mForgetCaptcha;
    private ButtonFlat btnSendCaptchaForget;
    private CircularProgressButton btnForgetPass;

    private ViewPager mViewPager;
    private List<View> views = new ArrayList<View>();
    private ViewPagerAdapter adpter;
    private View viewForgetPassword,viewLogin,viewRegister;
    private boolean mSended = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //异步加载背景图
        ImageLoader.getInstance().displayImage("drawable://" + R.drawable.bg_login, new BgViewAware(findViewById(R.id.loginBackground)));
        viewForgetPassword = getLayoutInflater().inflate(R.layout.view_forget_password, null);
        viewLogin = getLayoutInflater().inflate(R.layout.view_login, null);
        viewRegister = getLayoutInflater().inflate(R.layout.view_register, null);
        views.add(viewForgetPassword);
        views.add(viewLogin);
        views.add(viewRegister);
        List<String> titles = new ArrayList<String>();
        titles.add("忘记密码");
        titles.add("登录");
        titles.add("注册");
        adpter = new ViewPagerAdapter(views, titles);
        mViewPager = ( ViewPager) findViewById(R.id.viewPager);
        setViewPagerScrollSpeed();
        mViewPager.setAdapter(adpter);
        mViewPager.setCurrentItem(1);



        // 配置登录框
        mCellphoneView = (AutoCompleteTextView) viewLogin.findViewById(R.id.cellphone);
        mCellphoneView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                attemptGetAvatar();
            }
        });
        mCellphoneView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_NEXT) {
                    mPasswordView.requestFocus();
                    return true;
                }
                return false;
            }
        });

        mPasswordView = (EditText) viewLogin.findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        btnLogin = (CircularProgressButton) viewLogin.findViewById(R.id.btnLogin);
        btnLogin.setIndeterminateProgressMode(true);
        btnLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mAvatarView = (ImageView)viewLogin.findViewById(R.id.avatar);
        //配置注册界面
        mRegisterCellphone = (AutoCompleteTextView)viewRegister.findViewById(R.id.register_cellphone);
        mRegisterPassword = (EditText)viewRegister.findViewById(R.id.register_password);
        mConfirePassword = (EditText)viewRegister.findViewById(R.id.confirm_password);

        mRegisterCellphone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_NEXT) {
                    mRegisterPassword.requestFocus();
                    return true;
                }
                return false;
            }
        });
        mRegisterPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_NEXT) {
                    mConfirePassword.requestFocus();
                    return true;
                }
                return false;
            }
        });
        mConfirePassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });
        btnRegister = (CircularProgressButton)viewRegister.findViewById(R.id.btnRegister);
        btnRegister.setIndeterminateProgressMode(true);
        btnRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        //配置忘记密码界面
        mForgetCellphone = (EditText)viewForgetPassword.findViewById(R.id.cellphone);
        mForgetPassword = (EditText)viewForgetPassword.findViewById(R.id.password);
        mForgetCaptcha = (EditText)viewForgetPassword.findViewById(R.id.captcha_forget);
        btnSendCaptchaForget = (ButtonFlat)viewForgetPassword.findViewById(R.id.send_captcha_forget);
        btnForgetPass = (CircularProgressButton)viewForgetPassword.findViewById(R.id.commit);

        btnSendCaptchaForget.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sendForgetCaptcha();
            }
        });
        btnForgetPass.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptForgetPassword();
            }
        });

        //忘记密码界面跳转
        btnGoForgot = (ButtonFlat)viewLogin.findViewById(R.id.btnForgot);
        btnGoForgot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem(0);
            }
        });
        //注册界面跳转
        btnGoRegister = (ButtonFlat)viewLogin.findViewById(R.id.btnRegister);
        btnGoRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem(2);
            }
        });
    }
    //重写返回事件
    @Override
    public void onBackPressed(){
        int page = mViewPager.getCurrentItem();
        if(page == 1){
            //super.onBackPressed();
            //在登录界面按返回则完全退出
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }else {
            mViewPager.setCurrentItem(1);
        }
    }

    /* 根据输入的手机号来获取对应用户头像
     */
    private void attemptGetAvatar() {
        String cellphone = mCellphoneView.getText().toString();
        // 检查手机号输入是否有效
        if (TextUtils.isEmpty(cellphone)||!isCellphoneValid(cellphone)) {
            if(!isDefaultAvatar){
                ImageLoader.getInstance().displayImage("Drawable://"+R.drawable.avatar, mAvatarView);
                isDefaultAvatar = true;
                System.out.println("setDefaultAvatar");
            }
            return;
        }

        System.out.println("loadAvatar");
        // 加载头像
        PLServerAPI.getUidByCellphone(cellphone, new PLServerAPI.onResponseListener() {
            @Override
            public void onSuccess(Object data) {
                int uid = (int)data;

                DisplayImageOptions options = new DisplayImageOptions.Builder()
                        .cacheInMemory(false)//设置下载的图片是否缓存在内存中
                        .cacheOnDisc(false)//设置下载的图片是否缓存在SD卡中
                        .displayer(new FadeInBitmapDisplayer(300))//图片加载好后渐入的动画时间
                        .build();//构建完成
                ImageLoader.getInstance().displayImage(PLApplication.serverHost + "/images/users/avatars/" + uid + ".png", mAvatarView, options);
                isDefaultAvatar = false;
            }

            @Override
            public void onFailure(PLServerAPI.ApiError apiError) {
                ImageLoader.getInstance().displayImage("Drawable://"+R.drawable.avatar, mAvatarView);
                isDefaultAvatar = true;
            }
        });
    }
    /*
    *输入手机号，发送验证码
     */
    private void sendForgetCaptcha(){
        //初始化错误信息
        mForgetCellphone.setError(null);
        //获取输入框中信息
        String cellphone = mForgetCellphone.getText().toString();
        boolean cancel = false;
        View focusView = null;
        //判断手机号格式是否正确
        if(TextUtils.isEmpty(cellphone)){
            mForgetCellphone.setError(getString(R.string.error_field_required));
            focusView = mForgetCellphone;
            cancel = true;
        }else if(!isCellphoneValid(cellphone)){
            mForgetCellphone.setError(getString(R.string.error_invalid_cellphone));
            focusView = mForgetCellphone;
            cancel=true;
        }
        //若格式正确则发送验证码
        if (cancel){
            focusView.requestFocus();
        }else{
            //发送验证码
            PLServerAPI.sendCaptcha(cellphone, new PLServerAPI.onResponseListener() {
                @Override
                public void onSuccess(Object data) {
                }
                @Override
                public void onFailure(PLServerAPI.ApiError apiError) {
                }
            });
            mSended = true;
        }
    }

    private void attemptForgetPassword(){
        //对验证码进行确认，发送重置后的密码
        mForgetCellphone.setError(null);
        mForgetPassword.setError(null);
        mForgetCaptcha.setError(null);
        boolean cancel =false;
        View focusView = null;
        //存储忘记密码的手机号和密码
        final String cellphone = mForgetCellphone.getText().toString();
        final String password = mForgetPassword.getText().toString();
        final String captcha = mForgetCaptcha.getText().toString();
        if (TextUtils.isEmpty(captcha)){
            mForgetCaptcha.setError(getString(R.string.error_field_required));
            cancel = true;
        }else if (!mSended){
            mForgetCaptcha.setError(getString(R.string.error_send_requested));
            cancel = true;
        }else if(TextUtils.isEmpty(cellphone)){
            mRegisterCellphone.setError(getString(R.string.error_field_required));
            focusView = mRegisterCellphone;
            cancel = true;
        }else if(!isCellphoneValid(cellphone)){
            mRegisterCellphone.setError(getString(R.string.error_invalid_cellphone));
            focusView = mRegisterCellphone;
            cancel=true;
        }else if (TextUtils.isEmpty(password)) {
            mRegisterPassword.setError(getString(R.string.error_field_required));
            focusView = mRegisterPassword;
            cancel = true;
        }else if (!isPasswordValid(password)) {
            mRegisterPassword.setError(getString(R.string.error_invalid_password));
            focusView = mRegisterPassword;
            cancel = true;
        }
        if (cancel){
            focusView.requestFocus();
        }else{
            toggleInput(false);
            PLServerAPI.resetPassword(cellphone, password, captcha, new PLServerAPI.onResponseListener() {
                @Override
                public void onSuccess(Object data) {
                    toggleInput(true);
                    btnLogin.setProgress(100);
                    mCellphoneView.setText(cellphone);
                    mPasswordView.setText(password);
                    mViewPager.setCurrentItem(1);
                    attemptLogin();
                }

                @Override
                public void onFailure(PLServerAPI.ApiError apiError) {
                    if(apiError.getErrorCode()==1034) { //验证码
                        mForgetCaptcha.setError(apiError.getErrorString());
                        mForgetCaptcha.requestFocus();
                    }else if(apiError.getErrorCode()==1012){ //用户不存在
                        mForgetCellphone.setError(apiError.getErrorString());
                        mForgetCellphone.requestFocus();
                    }else{
                        btnForgetPass.setError(apiError.getErrorString());
                        btnForgetPass.requestFocus();
                    }
                    // 启用输入框
                    toggleInput(true);
                    btnForgetPass.setProgress(-1);
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            btnForgetPass.setProgress(0);
                        }
                    }, 1500);
                }
            });
        }
    }
    /*
    * 尝试用用户手机号和密码进行注册
    * 若存在错误（手机号格式不正确，密码太短，两次密码不一致等）
    * 则产生提示信息，不会真的进行注册尝试
    */
    private void attemptRegister(){
        //重置错误
        mRegisterCellphone.setError(null);
        mRegisterPassword.setError(null);
        //存储尝试注册时的手机号和密码
        final String cellphone = mRegisterCellphone.getText().toString();
        final String password1 = mRegisterPassword.getText().toString();
        final String password2 = mConfirePassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        //检查手机号和密码输入是否有效
        if(TextUtils.isEmpty(cellphone)){
            mRegisterCellphone.setError(getString(R.string.error_field_required));
            focusView = mRegisterCellphone;
            cancel = true;
        }else if(!isCellphoneValid(cellphone)){
            mRegisterCellphone.setError(getString(R.string.error_invalid_cellphone));
            focusView = mRegisterCellphone;
            cancel=true;
        }else if (TextUtils.isEmpty(password1)) {
            mRegisterPassword.setError(getString(R.string.error_field_required));
            focusView = mRegisterPassword;
            cancel = true;
        }else if (TextUtils.isEmpty(password2)) {
            mConfirePassword.setError(getString(R.string.error_field_required));
            focusView = mConfirePassword;
            cancel = true;
        }else if (!isPasswordValid(password1)) {
            mRegisterPassword.setError(getString(R.string.error_invalid_password));
            focusView = mRegisterPassword;
            cancel = true;
        }else if (!password1.equals(password2)){
            mConfirePassword.setError(getString(R.string.error_confire_failed));
            focusView = mConfirePassword;
            cancel = true;
        }
        if (cancel) {
            // 如果有错误，则让有错输入框获取焦点，不进行登录调用
            focusView.requestFocus();
        } else {
            // 禁用输入框
            toggleInput(false);
            // 进行注册调用
            btnRegister.setProgress(50);
            PLServerAPI.register(cellphone, password1, "", new PLServerAPI.onResponseListener() {
                @Override
                public void onSuccess(Object data) {
                    toggleInput(true);
                    btnRegister.setProgress(100);
                    mCellphoneView.setText(cellphone);
                    mPasswordView.setText(password1);
                    mViewPager.setCurrentItem(1);
                    btnLogin.setProgress(50);
                    attemptLogin();
                }

                @Override
                public void onFailure(PLServerAPI.ApiError apiError) {
                    if(apiError.getErrorCode()==1034) { //验证码
                        //mRegisterCaptcha.setError(apiError.getErrorString());
                        //mRegisterCaptcha.requestFocus();
                    }else if(apiError.getErrorCode()==1012){ //用户不存在
                        mRegisterCellphone.setError(apiError.getErrorString());
                        mRegisterCellphone.requestFocus();
                    }else if(apiError.getErrorCode()==1041){ //用户已存在
                        mRegisterCellphone.setError(apiError.getErrorString());
                        mRegisterCellphone.requestFocus();
                    }else{
                        mRegisterCellphone.setError(apiError.getErrorString());
                        mRegisterCellphone.requestFocus();
                    }
                    // 启用输入框
                    toggleInput(true);
                    btnRegister.setProgress(-1);
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            btnRegister.setProgress(0);
                        }
                    }, 1500);
                }
            });
        }

    }
    /**
     * 尝试用用户的手机号和密码进行登录
     * 如果存在错误（手机号格式不正确、密码太短等）
     * 则产生提示信息，不会真的进行登录尝试
     */
    private void attemptLogin() {

        // 重置错误
        mCellphoneView.setError(null);
        mPasswordView.setError(null);

        // 存储尝试登录时的手机号和密码
        String cellphone = mCellphoneView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // 检查手机号和密码输入是否有效
        if (TextUtils.isEmpty(cellphone)) {
            mCellphoneView.setError(getString(R.string.error_field_required));
            focusView = mCellphoneView;
            cancel = true;
        }else if (!isCellphoneValid(cellphone)) {
            mCellphoneView.setError(getString(R.string.error_invalid_cellphone));
            focusView = mCellphoneView;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }  else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // 如果有错误，则让有错输入框获取焦点，不进行登录调用
            focusView.requestFocus();
        } else {
            // 禁用输入框
            toggleInput(false);
            // 进行登录调用
            btnLogin.setProgress(50);
            PLServerAPI.login(cellphone, password, new PLServerAPI.onResponseListener() {
                @Override
                public void onSuccess(Object data) {
                    // 启用输入框
                    toggleInput(true);
                    btnLogin.setProgress(100);
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 800);
                }

                @Override
                public void onFailure(PLServerAPI.ApiError apiError) {
                    if(apiError.getErrorCode()==1011) { //密码错误
                        mPasswordView.setError(apiError.getErrorString());
                        mPasswordView.requestFocus();
                    }else if(apiError.getErrorCode()==1012){ //用户不存在
                        mCellphoneView.setError(apiError.getErrorString());
                        mCellphoneView.requestFocus();
                    }else{
                        mCellphoneView.setError(apiError.getErrorString());
                        mCellphoneView.requestFocus();
                    }
                    // 启用输入框
                    toggleInput(true);
                    btnLogin.setProgress(-1);
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            btnLogin.setProgress(0);
                        }
                    }, 1500);
                }
            });
        }

    }

    private boolean isCellphoneValid(String cellphone) {
        return cellphone.length()==11;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * 启用/禁用 输入框
     */
    private void toggleInput(final boolean enable) {
        mCellphoneView.setEnabled(enable);
        mPasswordView.setEnabled(enable);
        mForgetCellphone.setEnabled(enable);
        mForgetPassword.setEnabled(enable);
        mForgetCaptcha.setEnabled(enable);
        mRegisterCellphone.setEnabled(enable);
        mRegisterPassword.setEnabled(enable);
        mConfirePassword.setEnabled(enable);
    }

    private void setViewPagerScrollSpeed( ){
        try {
            Field mScroller = null;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller( mViewPager.getContext( ) );
            mScroller.set( mViewPager, scroller);
        }catch(NoSuchFieldException e){
            Log.e("login",e.toString());
        }catch (IllegalArgumentException e){
            Log.e("login",e.toString());
        }catch (IllegalAccessException e){
            Log.e("login",e.toString());
        }
    }
}

