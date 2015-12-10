package ml.puredark.personallibrary.activities;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by PureDark on 2015/12/10.
 */
public abstract class MyActivity extends AppCompatActivity {

    //Fragment编号
    public final static int FRAGMENT_INDEX = 1;
    public final static int FRAGMENT_BORROW = 2;
    public final static int FRAGMENT_FRIEND = 3;
    public final static int FRAGMENT_NEWS = 4;

    public final static int FRAGMENT_VIEW_BOOK_MARK = 5;
    public final static int FRAGMENT_BOOK_MARK_LIST = 6;

    public abstract void setCurrFragment(int curr);
    public abstract void replaceFragment(Fragment fragment);
}
