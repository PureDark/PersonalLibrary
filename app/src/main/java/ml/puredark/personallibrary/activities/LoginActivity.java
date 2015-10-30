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
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.dd.CircularProgressButton;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import carbon.widget.Button;
import ml.puredark.personallibrary.adapters.ViewPagerAdapter;
import ml.puredark.personallibrary.customs.FixedSpeedScroller;
import ml.puredark.personallibrary.helpers.BgViewAware;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.customs.NoScrollViewPager;
/**
 * 通过 手机号/密码 登录的登录界面
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * 将登录的AsyncTask保存下来以便需要时能取消
     */
    private UserLoginTask mAuthTask = null;
    private GetUserAvatarTask mAvatarTask = null;
    //注册的AsyncTask
    //private UserRegisterTask mRegisterTask = null;
    // UI相关引用
    private AutoCompleteTextView mCellphoneView;
    private EditText mPasswordView;
    private ImageView mAvatarView;
    private CircularProgressButton btnLogin;
    private Button btnGoRegister;
    private Button btnGoForgot;

    private boolean isDefaultAvatar = true;
    private static Drawable defaultAvatar;

    private AutoCompleteTextView mRegisterCellphone;
    private EditText mRegisterPassword;
    private EditText mConfirePassword;
    private CircularProgressButton btnRegister;

    private EditText mForgotCellphone;
    private EditText mVerifyNumber;
    private Button btnSendVerifyNum;
    private CircularProgressButton btnVerify;

    private ViewPager mViewPager;
    private List<View> views = new ArrayList<View>();
    private ViewPagerAdapter adpter;
    private int currentItem;
    private Animation animation;
    private int offSet;
    private int bmWidth;
    private View viewForgetPassword,viewLogin,viewRegister;
    private boolean mSended = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        defaultAvatar = getResources().getDrawable(R.drawable.avatar);
        //异步加载背景图
        ImageLoader.getInstance().displayImage("drawable://" + R.drawable.bg_login, new BgViewAware(findViewById(R.id.loginBackground)));
        viewForgetPassword = getLayoutInflater().inflate(R.layout.view_forget_password, null);
        viewLogin = getLayoutInflater().inflate(R.layout.view_login, null);
        viewRegister = getLayoutInflater().inflate(R.layout.view_register, null);
        views.add(viewForgetPassword);
        views.add(viewLogin);
        views.add(viewRegister);
        adpter = new ViewPagerAdapter(views);
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
        mForgotCellphone = (EditText)viewForgetPassword.findViewById(R.id.fogot_cellphone);
        mVerifyNumber = (EditText)viewForgetPassword.findViewById(R.id.verify_number);
        btnSendVerifyNum = (Button)viewForgetPassword.findViewById(R.id.send_verify);
        btnVerify = (CircularProgressButton)viewForgetPassword.findViewById(R.id.commit);

        btnSendVerifyNum.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVerifyNumber();
            }
        });
        btnVerify.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                verify();
            }
        });

        //忘记密码界面跳转
        btnGoForgot = (Button)viewLogin.findViewById(R.id.btnGoForgot);
        btnGoForgot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem(0);
            }
        });
        //注册界面跳转
        btnGoRegister = (Button)viewLogin.findViewById(R.id.btnGoRegister);
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
            super.onBackPressed();
        }else {
            mViewPager.setCurrentItem(1);
        }
    }

    /* 根据输入的手机号来获取对应用户头像
     */
    private void attemptGetAvatar() {
        if (mAvatarTask != null) {
            mAvatarTask.cancel(true);
        }

        String cellphone = mCellphoneView.getText().toString();

        // 检查手机号输入是否有效
        if (TextUtils.isEmpty(cellphone)||!isCellphoneValid(cellphone)) {
            if(isDefaultAvatar)
                return;
        }

        // 加载头像
        mAvatarTask = new GetUserAvatarTask(cellphone);
        mAvatarTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    /*
    *输入手机号，发送验证码
     */
    private void sendVerifyNumber(){
        //初始化错误信息
        mForgotCellphone.setError(null);
        //获取输入框中信息
        String cellphone = mForgotCellphone.getText().toString();
        boolean cancel = false;
        View focusView = null;
        //判断手机号格式是否正确
        if(TextUtils.isEmpty(cellphone)){
            mForgotCellphone.setError(getString(R.string.error_field_required));
            focusView = mForgotCellphone;
            cancel = true;
        }else if(!isCellphoneValid(cellphone)){
            mForgotCellphone.setError(getString(R.string.error_invalid_cellphone));
            focusView = mForgotCellphone;
            cancel=true;
        }
        //若格式正确则发送验证码
        if (cancel){
            focusView.requestFocus();
        }else{
            //发送验证码
            mSended = true;
        }
    }

    private void verify(){
        //对验证码进行确认，发送重置后的密码
        mVerifyNumber.setError(null);
        boolean cancle =false;
        String verifyNum = mVerifyNumber.getText().toString();
        if (TextUtils.isEmpty(verifyNum)){
            mVerifyNumber.setError(getString(R.string.error_field_required));
            cancle = true;
        }else if (!mSended){
            mVerifyNumber.setError(getString(R.string.error_send_requested));
            cancle = true;
        }
        if (cancle){
            mVerifyNumber.requestFocus();
        }else{
            //真正的验证
        }
    }
    /*
    *尝试用用户手机号喝密码进行注册
    * 若存在错误（手机号格式不正确，密码太短，两次密码不一致等）
    * 则产生提示信息，不会真的进行注册尝试
    */
    private void attemptRegister(){
        //重置错误
        mRegisterCellphone.setError(null);
        mRegisterPassword.setError(null);
        //存储尝试注册时的手机号和密码
        String cellphone = mRegisterCellphone.getText().toString();
        String password1 = mRegisterPassword.getText().toString();
        String password2 = mConfirePassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        //检查手机号喝密码输入是否有效
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
            // 进行登录调用
            btnRegister.setProgress(50);
            //mAuthTask = new UserLoginTask(cellphone, password);
            //mAuthTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }
    /**
     * 尝试用用户的手机号和密码进行登录
     * 如果存在错误（手机号格式不正确、密码太短等）
     * 则产生提示信息，不会真的进行登录尝试
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

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
            mAuthTask = new UserLoginTask(cellphone, password);
            mAuthTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
    }

    /**
     * 异步尝试获取用户头像
     */
    public class GetUserAvatarTask extends AsyncTask<Void, Void, Drawable> {

        private final String mCellphone;

        GetUserAvatarTask(String cellphone) {
            mCellphone = cellphone;
        }

        @Override
        protected Drawable doInBackground(Void... params) {
            if(isCancelled())
                return null;
            // TODO: 调用检查用户是否存在的接口

            try {
                // Simulate network access.
                Thread.sleep(0);
            } catch (InterruptedException e) {
                return null;
            }
            Drawable avatarDrawable;
            if(mCellphone.equals("13200000000")){
                Bitmap myavatar =  BitmapFactory.decodeResource(getResources(), R.drawable.myavatar);
                avatarDrawable = new BitmapDrawable(myavatar);
                isDefaultAvatar = false;
            }else{
                avatarDrawable = defaultAvatar;
                isDefaultAvatar = true;
            }
            return avatarDrawable;
        }

        @Override
        protected void onPostExecute(final Drawable avatarDrawable) {
            mAvatarTask = null;
            mAvatarView.setImageDrawable(avatarDrawable);
        }

        @Override
        protected void onCancelled() {
            mAvatarTask = null;
        }
    }

    /**
     * 异步发起调用登陆接口的请求
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mCellphone;
        private final String mPassword;

        UserLoginTask(String cellphone, String password) {
            mCellphone = cellphone;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: 调用登陆接口

            try {
                // Simulate network access.
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                return false;
            }

            return mPassword.equals("123456");
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            // 启用输入框
            toggleInput(true);

            if (success) {
                btnLogin.setProgress(100);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        //ActivityTransitionLauncher.with(LoginActivity.this).from(mAvatarView).launch(intent);
                        startActivity(intent);
                        finish();
                    }
                }, 500);
            } else {
                btnLogin.setProgress(-1);
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        btnLogin.setProgress(0);
                    }
                }, 1500);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            btnLogin.setProgress(0);
            // 启用输入框
            toggleInput(true);
        }
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

