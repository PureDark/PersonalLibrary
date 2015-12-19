package ml.puredark.personallibrary.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by PureDark on 2015/12/11.
 */
public class FragmentViewPagerAdapter extends FragmentPagerAdapter {
    List<Fragment> fragments;
    List<String> titles;

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

    public FragmentViewPagerAdapter(FragmentManager manager, List<Fragment> fragments, List<String> titles) {
        super(manager);
        this.fragments = fragments;
        this.titles = titles;
    }
    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }


}
