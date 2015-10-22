package ml.puredark.personallibrary.activities;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import ml.puredark.personallibrary.PersonalLibraryApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.beans.Book;
import ml.puredark.personallibrary.customs.MyCoordinatorLayout;
import ml.puredark.personallibrary.customs.MyFloatingActionButton;
import ml.puredark.personallibrary.helpers.ActivityTransitionExitHelper;
import ml.puredark.personallibrary.helpers.FastBlur;
import ml.puredark.personallibrary.utils.SharedPreferencesUtil;

public class BookDetailActivity extends AppCompatActivity {
    private TextView bookTitle, bookSummary;
    private ImageView bookCover, backdrop;
    private View hover;
    private LinearLayout titleBar;
    private MyCoordinatorLayout mCoordinatorLayout;
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout toolbarLayout;
    private NestedScrollView summaryLayout;
    private MyFloatingActionButton fabAction;
    private ActivityTransitionExitHelper transitionExitHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mCoordinatorLayout = (MyCoordinatorLayout) findViewById(R.id.content);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        Object data = PersonalLibraryApplication.temp;
        if(data==null||!(data instanceof Book)){
            setResult(RESULT_CANCELED);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return;
        }
        final Book book = (Book)PersonalLibraryApplication.temp;
        Bitmap cover = PersonalLibraryApplication.bitmap;
        titleBar = (LinearLayout) findViewById(R.id.title_bar);
        bookTitle = (TextView) findViewById(R.id.book_title);
        bookSummary = (TextView) findViewById(R.id.book_summary);
        bookCover = (ImageView) findViewById(R.id.book_cover);
        backdrop = (ImageView) findViewById(R.id.backdrop);
        hover = findViewById(R.id.hover);
        summaryLayout = (NestedScrollView) findViewById(R.id.summary_layout);
        fabAction = (MyFloatingActionButton) findViewById(R.id.fab_action);
        fabAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferencesUtil.saveData(getBaseContext(), "isbn13_"+book.isbn13, new Gson().toJson(book));
                setResult(RESULT_OK);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

//        transitionExitHelper = ActivityTransitionExitHelper.
//                with(getIntent()).toView(bookCover).background(mCoordinatorLayout).start(savedInstanceState);

        bookTitle.setText(book.title);
        bookSummary.setText(book.summary);
        bookCover.setImageBitmap(cover);
        backdrop.setImageBitmap(cover);

        Drawable fabIcon = MaterialDrawableBuilder.with(this)
                .setIcon(MaterialDrawableBuilder.IconValue.STAR)
                .setSizeDp(24)
                .setColor(Color.WHITE)
                .build();
        fabAction.setImageDrawable(fabIcon);

        /* 让背景的封面大图上下来回缓慢移动 */
        Animation translateAnimation = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, 0f,
        TranslateAnimation.RELATIVE_TO_SELF, 0f,
        TranslateAnimation.RELATIVE_TO_SELF, 0f,
        TranslateAnimation.RELATIVE_TO_SELF, -0.4f);
        translateAnimation.setDuration(30000);
        translateAnimation.setRepeatCount(-1);
        translateAnimation.setRepeatMode(Animation.REVERSE);
        translateAnimation.setInterpolator(new LinearInterpolator());
        backdrop.startAnimation(translateAnimation);

        Bundle bundle = getIntent().getExtras();


        /* 修改UI颜色 */
        titleBar.setBackgroundColor(bundle.getInt("titleBarColor"));
        bookTitle.setTextColor(bundle.getInt("titleTextColor"));
        toolbarLayout.setContentScrimColor(bundle.getInt("topColor"));
        hover.setBackgroundColor(bundle.getInt("topColor"));
        summaryLayout.setBackgroundColor(bundle.getInt("bottomColor"));
        bookSummary.setTextColor(bundle.getInt("bottomTextColor"));
        int fabColor = bundle.getInt("fabColor");
        float[] hsv = new float[3];
        Color.colorToHSV(fabColor, hsv);
        hsv[2] *= 0.8f;
        int fabColorPressed = Color.HSVToColor(hsv);
        setFloatingActionButtonColors(fabAction, fabColor, fabColorPressed);
        /* 给背景封面加上高斯模糊 */
        Bitmap overlay = PersonalLibraryApplication.bitmap.copy(Bitmap.Config.ARGB_8888, true);
        overlay = FastBlur.doBlur(overlay, 2, true);
        backdrop.setImageBitmap(overlay);

    }

//    @Override
//    public void onBackPressed() {
//        transitionExitHelper.runExitAnimation(new Runnable() {
//            @Override
//            public void run() {
//                finish();
//            }
//        });
//    }
//    @Override
//    public void finish() {
//        super.finish();
//        // override transitions to skip the standard window animations
//        overridePendingTransition(0, 0);
//    }

    private void setFloatingActionButtonColors(MyFloatingActionButton fab, int primaryColor, int rippleColor) {
        int[][] states = {
                {android.R.attr.state_enabled},
                {android.R.attr.state_pressed},
        };
        int[] colors = {
                primaryColor,
                rippleColor,
        };
        ColorStateList colorStateList = new ColorStateList(states, colors);
        fab.setBackgroundTintList(colorStateList);
    }


    public void setToolbarCollapsible(){
        mAppBarLayout.setExpanded(true, true);
        mCoordinatorLayout.setAllowForScrool(true);
    }
    public void setToolbarUncollapsible(){
        mAppBarLayout.setExpanded(false, true);
        mCoordinatorLayout.setAllowForScrool(false);
    }
}
