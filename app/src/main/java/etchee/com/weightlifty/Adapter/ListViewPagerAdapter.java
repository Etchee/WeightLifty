package etchee.com.weightlifty.Adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

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
}
