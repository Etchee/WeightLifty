package etchee.com.weightlifty.Adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.Calendar;

import etchee.com.weightlifty.Fragment.CurrentListFragment;
import etchee.com.weightlifty.data.DataContract;

/**
 * Adapter for viewPager
 * Created by rikutoechigoya on 2017/05/13.
 */

public class ListViewPagerAdapter extends FragmentStatePagerAdapter {

    public ListViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }



    //make a new fragment, with the variable being the position
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new CurrentListFragment();
        Bundle args = new Bundle();
        args.putInt(DataContract.GlobalConstants.VIEWPAGER_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    //return the current number of item
    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return String.valueOf(getDateAsInt() + position);
    }


    private int getDateAsInt() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;   //month starts from zero
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String concatenated = String.valueOf(year) + String.valueOf(month) + String.valueOf(day);

        return Integer.parseInt(concatenated);
    }
}
