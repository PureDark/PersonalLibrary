package ml.puredark.personallibrary.adapters;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.List;

/**
 * Created by kevin on 15/10/24.
 */
public class ViewPagerAdapter extends PagerAdapter {
    List<View> viewLists;
    List<String> titles;

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

    public ViewPagerAdapter(List<View> lists, List<String> titles) {
        viewLists = lists;
        this.titles = titles;
    }
    @Override
    public int getCount() {                                                                 //获得size
        // TODO Auto-generated method stub
        return viewLists.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        // TODO Auto-generated method stub
        return arg0 == arg1;
    }

    @Override
    public void destroyItem(View view, int position, Object object){                       //销毁Item
        ((ViewPager) view).removeView(viewLists.get(position));
    }

    @Override
    public Object instantiateItem(View view, int position){                             //实例化Item
        ((ViewPager) view).addView(viewLists.get(position), 0);
        return viewLists.get(position);
    }
}
