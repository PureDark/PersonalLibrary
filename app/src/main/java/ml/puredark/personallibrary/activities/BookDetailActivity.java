package ml.puredark.personallibrary.activities;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.wasabeef.blurry.Blurry;
import ml.puredark.personallibrary.PersonalLibraryApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.beans.Book;
import ml.puredark.personallibrary.customs.MyFloatingActionButton;
import ml.puredark.personallibrary.helpers.FastBlur;

public class BookDetailActivity extends AppCompatActivity {
    private TextView bookTitle, bookSummary;
    private ImageView bookCover, backdrop;
    private View hover;
    private LinearLayout titleBar;
    private Animation translateAnimation;
    private CollapsingToolbarLayout toolbarLayout;
    private NestedScrollView summaryLayout;
    private MyFloatingActionButton fab_action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        fab_action = (MyFloatingActionButton) findViewById(R.id.fab_action);

        Object data = PersonalLibraryApplication.temp;
        if(data==null||!(data instanceof Book)){
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        Book book = (Book)PersonalLibraryApplication.temp;
        Bitmap cover = PersonalLibraryApplication.bitmap;
        titleBar = (LinearLayout) findViewById(R.id.title_bar);
        bookTitle = (TextView) findViewById(R.id.book_title);
        bookSummary = (TextView) findViewById(R.id.book_summary);
        bookCover = (ImageView) findViewById(R.id.book_cover);
        backdrop = (ImageView) findViewById(R.id.backdrop);
        hover = findViewById(R.id.hover);
        summaryLayout = (NestedScrollView) findViewById(R.id.summary_layout);

        bookTitle.setText(book.title);
        bookSummary.setText(book.summary);
        bookCover.setImageBitmap(cover);
        backdrop.setImageBitmap(cover);


        translateAnimation = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, 0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0f,
                TranslateAnimation.RELATIVE_TO_SELF, -0.4f);
        translateAnimation.setDuration(30000);
        translateAnimation.setRepeatCount(-1);
        translateAnimation.setRepeatMode(Animation.REVERSE);
        translateAnimation.setInterpolator(new LinearInterpolator());
        backdrop.startAnimation(translateAnimation);

        /* 从封面提取颜色 */
        Palette.generateAsync(cover, new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                Palette.Swatch vibrant = palette.getVibrantSwatch();
                Palette.Swatch darkVibrant = palette.getDarkVibrantSwatch();
                Palette.Swatch darkmuted = palette.getDarkMutedSwatch();
                Palette.Swatch top = darkmuted;
                if(darkmuted!=null&&darkVibrant!=null)
                    top = (darkmuted.getPopulation() >= darkVibrant.getPopulation()) ? darkmuted : darkVibrant;
                Palette.Swatch muted = palette.getMutedSwatch();
                Palette.Swatch lightmuted = palette.getLightMutedSwatch();
                Palette.Swatch bottom = (lightmuted != null) ? lightmuted : muted;
                Palette.Swatch fabcolor = muted;
                /* 修改UI颜色 */
                titleBar.setBackgroundColor(vibrant.getRgb());
                bookTitle.setTextColor(vibrant.getTitleTextColor());
                toolbarLayout.setContentScrimColor(top.getRgb());
                fab_action.setBackgroundColor(vibrant.getPopulation());
                hover.setBackgroundColor(top.getRgb());
                summaryLayout.setBackgroundColor(bottom.getRgb());
                bookSummary.setTextColor(bottom.getBodyTextColor());
                fab_action.setBackgroundTintList(new ColorStateList (
                        new int [] [] {
                                new int [] {android.R.attr.state_pressed},
                                new int [] {android.R.attr.state_focused},
                                new int [] {}
                        },
                        new int [] {
                                top.getRgb(),
                                top.getRgb(),
                                fabcolor.getRgb()
                        }
                ));
                Bitmap overlay = PersonalLibraryApplication.bitmap.copy(Bitmap.Config.ARGB_8888, true);
                overlay = FastBlur.doBlur(overlay, 2, true);
                backdrop.setImageBitmap(overlay);
            }
        });

    }

}
