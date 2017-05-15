package etchee.com.weightlifty.Fragment;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.github.clans.fab.FloatingActionButton;

import java.util.Calendar;

import etchee.com.weightlifty.Activity.EditEventActivity;
import etchee.com.weightlifty.Adapter.ListAdapter;
import etchee.com.weightlifty.R;
import etchee.com.weightlifty.data.DataContract;
import etchee.com.weightlifty.data.DataContract.EventEntry;
import etchee.com.weightlifty.data.DataContract.GlobalConstants;

import static etchee.com.weightlifty.data.DataContract.GlobalConstants.LAUNCH_EDIT_CODE;
import static etchee.com.weightlifty.data.DataContract.GlobalConstants.LAUNCH_EDIT_EXISTING;
import static etchee.com.weightlifty.data.DataContract.GlobalConstants.PASS_EVENT_ID;
import static etchee.com.weightlifty.data.DataContract.GlobalConstants.PASS_SELECTED_DATE;
import static etchee.com.weightlifty.data.DataContract.GlobalConstants.PASS_SUB_ID;

/**
 * Displays the date's fragment.
 * Created by rikutoechigoya on 2017/05/12.
 */

public class CurrentListFragment extends Fragment implements
        android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>{

    private ListView listview;
    private FloatingActionButton fab;

    //To make sure that there is only one instance because OpenHelper will serialize requests anyways
    private int eventID;
    private final int CREATE_LOADER_ID = 1;
    private final String TAG = getClass().getSimpleName();
    private Context context;
    private ListAdapter listAdapter;
    private ContentResolver contentResolver;
    private String displayDate;
    public CurrentListFragment() {
    }

    //do non-graphical assignments
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        //get the position in the viewPager
        if (getArguments() != null) {
            displayDate = getArguments().getString(DataContract.GlobalConstants.VIEWPAGER_POSITION_AS_DATE);
        } else Log.e(TAG, "Argument not received");

    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //for the empty view
        View emptyView = view.findViewById(R.id.view_empty);
        listview = (ListView) view.findViewById(R.id.listview_fragment_current);
        listview.setEmptyView(emptyView);
        listAdapter = new ListAdapter(context, null, 0);    //cursor will be swapped later
        listview.setAdapter(listAdapter);
        //listView setup
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                launchEditActivityWithEventID(position);
            }
        });
    }


    /**
     *  This fragment is called from ListViewPagerAdapter.
     *  getArgument() gets the bundle which contains the position of this fragment in the viewPager.
     * @param inflater inflater
     * @param container container of this fragment
     * @param savedInstanceState savedInstanceState
     * @return  View of the fragment to onViewCreated
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentResolver = context.getContentResolver();

        Bundle bundle = new Bundle();
        bundle.putString(DataContract.GlobalConstants.PASS_CREATE_LOADER_DATE, displayDate);
        getLoaderManager().initLoader(CREATE_LOADER_ID, bundle, this);

        return inflater.inflate(R.layout.fragment_current_list_layout, container, false);
    }

    private void setEventID(int eventID) {
        this.eventID = eventID;
    }

    /**
     * @param position = SUB_ID in the Event Table.
     *                 What row? = specify by the formatted date shown in the viewPager title.
     *                 How to get what row? = use field variable "displayDate".
     */
    private void launchEditActivityWithEventID(final int position) {

        //Define async query handler
        AsyncQueryHandler queryHandler = new AsyncQueryHandler(contentResolver) {

            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                int eventID = -1;

                try {
                    //Token will be passed to prevent confusion on multi querying
                    if (token == DataContract.GlobalConstants.QUERY_EVENT_ID) {
                        if (cursor.moveToFirst()) {
                            int index = cursor.getColumnIndex(EventEntry.COLUMN_EVENT_ID);
                            eventID = Integer.parseInt(cursor.getString(index));

                        } else if (!cursor.moveToFirst())
                            throw new CursorIndexOutOfBoundsException("EventID query: Cursor might be empty");

                    } else
                        throw new IllegalArgumentException("Invalid token received at Event ID query.");
                } catch (CursorIndexOutOfBoundsException e) {
                    e.printStackTrace();
                } finally {
                    cursor.close();
                }

                Intent intent = new Intent(context, EditEventActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(PASS_EVENT_ID, eventID);
                bundle.putInt(PASS_SUB_ID, position);
                bundle.putString(PASS_SELECTED_DATE, displayDate);
                bundle.putInt(LAUNCH_EDIT_CODE, LAUNCH_EDIT_EXISTING);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        };

        //projection
        String projection[] = new String[]{
                EventEntry.COLUMN_FORMATTED_DATE,
                EventEntry.COLUMN_SUB_ID,
                EventEntry.COLUMN_EVENT_ID
        };

        //selection
        String selection = EventEntry.COLUMN_FORMATTED_DATE + "=?" + " AND " + EventEntry.COLUMN_SUB_ID + "=?";

        //selectionArgs
        String selectionArgs[] = new String[]{
                displayDate,
                String.valueOf(position)
        };

        //Finally, fire the query!
        queryHandler.startQuery(
                DataContract.GlobalConstants.QUERY_EVENT_ID,
                null,
                EventEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );
    }


    private Cursor initListCursor() {
        Cursor cursor;

        String projection[] = {
                EventEntry._ID,
                EventEntry.COLUMN_WEIGHT_COUNT,
                EventEntry.COLUMN_REP_COUNT,
                EventEntry.COLUMN_SUB_ID,
                EventEntry.COLUMN_SET_COUNT,
                EventEntry.COLUMN_EVENT_ID
        };

        cursor = contentResolver.query(
                EventEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
        return cursor;
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

    private String getFormattedDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);

        int month = calendar.get(Calendar.MONTH) + 1;   //month starts from zero
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String concatenated = String.valueOf(year) + "/" + String.valueOf(month) + "/" + String.valueOf(day);

        return concatenated;
    }

    /**
     *  The bundle contains the position of the fragment in the viewPager.
     *  0 = today, 1 = yesterday, 2 = the day before yesterday, etc.
     * @param id     Just select any desired ID. Here I define in the global constant
     * @param bundle pass any object in bundle, no need to pass anything in here.
     * @return defined cursor
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        //method to convert position into date

        String date  = bundle.getString(DataContract.GlobalConstants.PASS_CREATE_LOADER_DATE);

        String projection[] = new String[]{
                EventEntry._ID,
                EventEntry.COLUMN_DATE,
                EventEntry.COLUMN_FORMATTED_DATE,
                EventEntry.COLUMN_EVENT_ID,
                EventEntry.COLUMN_WEIGHT_COUNT,
                EventEntry.COLUMN_SET_COUNT,
                EventEntry.COLUMN_REP_COUNT
        };

        String selection = EventEntry.COLUMN_FORMATTED_DATE + "=?";
        String selectionArgs[] = new String[]{date};

        return new CursorLoader(
                context,
                EventEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        listAdapter.swapCursor(cursor);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        listAdapter.swapCursor(null);
    }
}
