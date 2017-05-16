package etchee.com.weightlifty.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.widget.Toast;

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
    private Cursor allEventsCursor;
    private final String TAG = getClass().getSimpleName();
    private int count;

    public ListViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
        allEventsCursor = context.getContentResolver().query(
                EventEntry.CONTENT_URI,
                new String[]{EventEntry.COLUMN_SUB_ID, EventEntry.COLUMN_FORMATTED_DATE, EventEntry._ID},
                EventEntry.COLUMN_SUB_ID + "=?",
                new String[]{String.valueOf(0)},
                EventEntry._ID + DataContract.GlobalConstants.ORDER_DESCENDING
        );
        if (allEventsCursor.moveToFirst()){
            count = allEventsCursor.getCount();
            Log.v(TAG, DatabaseUtils.dumpCursorToString(allEventsCursor));
        } else count = 0;
    }

    //make a new fragment, with the variable being the position
    //And pass the cursor to inflate the view from
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new CurrentListFragment();
        Bundle args = new Bundle();

        int index;
        String date = null;
        if (allEventsCursor.moveToPosition(getCount() - 1 - position)) {
            index = allEventsCursor.getColumnIndex(EventEntry.COLUMN_FORMATTED_DATE);
            date = allEventsCursor.getString(index);
        }

        args.putString(DataContract.GlobalConstants.VIEWPAGER_POSITION_AS_DATE, date);

        fragment.setArguments(args);
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
        if (allEventsCursor.moveToPosition(getCount() - 1 - position)) {
            index = allEventsCursor.getColumnIndex(EventEntry.COLUMN_FORMATTED_DATE);
        }
        return allEventsCursor.getString(index);
    }
}
