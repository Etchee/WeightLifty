package etchee.com.weightlifty.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.Calendar;

import etchee.com.weightlifty.Fragment.CurrentListFragment;
import etchee.com.weightlifty.data.DataContract;
import etchee.com.weightlifty.data.DataContract.EventEntry;

/**
 * Adapter for viewPager
 * Created by rikutoechigoya on 2017/05/13.
 */

public class ListViewPagerAdapter extends FragmentStatePagerAdapter {

    private Context context;
    private int todayDate;
    private Cursor allEventsCursor;
    private final String TAG = getClass().getSimpleName();
    private int count;

    public ListViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
        todayDate = getDateAsInt();
        allEventsCursor = context.getContentResolver().query(
                EventEntry.CONTENT_URI,
                new String[]{EventEntry.COLUMN_DATE, EventEntry.COLUMN_SUB_ID, EventEntry.COLUMN_FORMATTED_DATE},
                EventEntry.COLUMN_SUB_ID + "=?",
                new String[]{String.valueOf(0)},
                null
        );
        if (allEventsCursor != null)count = allEventsCursor.getCount();
        else count = 0;
    }



    //make a new fragment, with the variable being the position
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new CurrentListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        int index = 0;
        if (allEventsCursor.moveToPosition(position)) {
            index = allEventsCursor.getColumnIndex(EventEntry.COLUMN_DATE);
        }

        args.putInt(DataContract.GlobalConstants.VIEWPAGER_POSITION, allEventsCursor.getInt(index));

        return fragment;
    }

    //return the current number of item
    @Override
    public int getCount() {
        return count;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        int index = 0;
        if (allEventsCursor.moveToPosition(position)) {
            index = allEventsCursor.getColumnIndex(EventEntry.COLUMN_FORMATTED_DATE);
        }
        return allEventsCursor.getString(index);
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
