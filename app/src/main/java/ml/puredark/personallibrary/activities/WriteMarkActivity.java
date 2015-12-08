package ml.puredark.personallibrary.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.personallibrary.PLApplication;
import ml.puredark.personallibrary.R;
import ml.puredark.personallibrary.adapters.ViewPagerAdapter;
import ml.puredark.personallibrary.helpers.PLServerAPI;

public class WriteMarkActivity extends AppCompatActivity {
    // 书评所关联的书籍的ID
    private int bid = 0;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    // 正在提交标志
    private boolean posting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("写书评");
        setContentView(R.layout.activity_write_mark);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        if(bundle==null)finish();
        bid = bundle.getInt("bid");
        if(bid==0)finish();

        findViewById(R.id.appbar).setBackgroundColor(bundle.getInt("topColor"));
        findViewById(R.id.toolbar).setBackgroundColor(bundle.getInt("topColor"));

        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        mTabLayout.setBackgroundColor(bundle.getInt("topColor"));
        mTabLayout.setTabTextColors(bundle.getInt("topTextColor"),getResources().getColor(R.color.white));

        List<View> views = new ArrayList<>();
        final View viewWriteTitle = getLayoutInflater().inflate(R.layout.view_write_title, null);
        final View viewWriteMarks = getLayoutInflater().inflate(R.layout.view_write_marks, null);
        views.add(viewWriteTitle);
        views.add(viewWriteMarks);
        List<String> titles = new ArrayList<String>();
        titles.add("标题");
        titles.add("书评");
        ViewPagerAdapter mAdapter = new ViewPagerAdapter(views, titles);
        mTabLayout.setTabsFromPagerAdapter(mAdapter);
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        // 初始化FAB
        Drawable yourDrawable = MaterialDrawableBuilder.with(this)
                .setIcon(MaterialDrawableBuilder.IconValue.CHECK)
                .setColor(Color.WHITE)
                .setToActionbarSize()
                .build();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageDrawable(yourDrawable);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(posting) return;
                String title = ((EditText)viewWriteTitle.findViewById(R.id.inputTitle)).getText().toString();
                String content = ((EditText)viewWriteMarks.findViewById(R.id.inputMarks)).getText().toString();
                posting = true;
                PLServerAPI.addBookMark(bid, title, content, new PLServerAPI.onResponseListener() {
                    @Override
                    public void onSuccess(Object data) {
                        finish();
                        Toast.makeText(WriteMarkActivity.this, "发表成功！", Toast.LENGTH_LONG);
                        posting = false;
                    }
                    @Override
                    public void onFailure(PLServerAPI.ApiError apiError) {
                        showSnackBar(getString(R.string.network_error));
                        posting = false;
                    }
                });
            }
        });
        fab.setBackgroundTintList(ColorStateList.valueOf(bundle.getInt("fabColor")));
        fab.setRippleColor(bundle.getInt("fabColorPressed"));

    }

    public void showSnackBar(String content){
        Snackbar snackbar = Snackbar.make(
                findViewById(R.id.container),
                content,
                Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(ContextCompat.getColor(PLApplication.mContext, R.color.colorAccentDark));
        snackbar.show();
    }


}
