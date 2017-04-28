package etchee.com.weightlifty.Activity;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.Calendar;

import etchee.com.weightlifty.Adapter.listActivityAdapter;
import etchee.com.weightlifty.R;
import etchee.com.weightlifty.data.DBviewer;
import etchee.com.weightlifty.data.DataContract;
import etchee.com.weightlifty.data.DataContract.EventEntry;
import etchee.com.weightlifty.DataMethods.subIDfixHelper;

/**
 * Created by rikutoechigoya on 2017/03/30.
 */

public class ListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private ListView listview;
    private listActivityAdapter mAdapter;
    private FloatingActionButton fab;
    private MaterialSearchView searchView;

    //To make sure that there is only one instance because OpenHelper will serialize requests anyways
    private ContentResolver contentResolver;
    private int eventID;
    private final int CREATE_LOADER_ID = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);

        contentResolver = getContentResolver();

        searchView = (MaterialSearchView) findViewById(R.id.search_view);

        //fab setup
        fab = (FloatingActionButton) findViewById(R.id.list_fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                event_insertDummyValues();
            }
        });

        //for the empty view
        View emptyView = findViewById(R.id.view_empty);
        listview = (ListView)findViewById(R.id.listview_workout);
        listview.setEmptyView(emptyView);
        Cursor cursor = createCursor();
        if (cursor == null) throw new IllegalArgumentException("Cursor creation failed: " +
                "check projection, it might not be matching with the table.");

        mAdapter = new listActivityAdapter(getApplicationContext(), cursor, 0);
        listview.setAdapter(mAdapter);

        //Init the loader

        //only display today's data for now
        Bundle bundle = new Bundle();
        bundle.putInt(DataContract.GlobalConstants.PASS_CREATE_LOADER_DATE, getDateAsInt());
        getLoaderManager().initLoader(CREATE_LOADER_ID, bundle, this);

        /**
         *  When an item on the listView is clicked, get the (INT) item id, put on the bundle and deliver
         *      to EditActivity.
         */
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                launchEditActivityWithEventID(position);
            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Do some magic
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
                searchView.setSuggestions(getResources().getStringArray(R.array.query_suggestions));
                searchView.setCursorDrawable(R.drawable.ic_searchview_cursor);
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });
    }


    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        new subIDfixHelper(getApplicationContext()).execute(getDateAsInt());
    }

    private int getEventID() {
        return eventID;
    }

    private void setEventID(int eventID) {
        this.eventID = eventID;
    }

    private void event_insertDummyValues() {

        ContentValues values = new ContentValues();

        int rep_count = 7;
        int set_count = 5;
        int date = getDateAsInt();
        int weight_count = 70;
        int sub_ID = getNextSub_id();
        int eventID = 5;

        values.put(EventEntry.COLUMN_SUB_ID, sub_ID);
        values.put(EventEntry.COLUMN_DATE, date);
        values.put(EventEntry.COLUMN_EVENT_ID, eventID);
        values.put(EventEntry.COLUMN_REP_COUNT, rep_count);
        values.put(EventEntry.COLUMN_SET_COUNT, set_count);
        values.put(EventEntry.COLUMN_WEIGHT_COUNT, weight_count);

        Uri uri = getContentResolver().insert(EventEntry.CONTENT_URI, values);

        if (uri == null) throw new IllegalArgumentException("Calendar table (insert dummy)" +
                "failed to insert data. check the MainActivity method and the table.");

    }

    private int getNextSub_id() {
        int sub_id;
        int date = getDateAsInt();

        String projection[] = new String[]{EventEntry.COLUMN_DATE, EventEntry.COLUMN_SUB_ID};
        String selection = EventEntry.COLUMN_DATE + "=?";
        String selectionArgs[] = new String[]{String.valueOf(date)};
        Cursor cursor = null;

        try {
            cursor = getContentResolver().query(
                    EventEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
            );

            //if cursor comes back, there is already some rows
            if (cursor.moveToLast()) {
                int index = cursor.getColumnIndex(EventEntry.COLUMN_SUB_ID);
                sub_id = cursor.getInt(index) + 1;

            } else {
                sub_id = 0;
            }
        } finally {
            cursor.close();
        }


        return sub_id;
    }

    /**
     * @param position equals to that of the sub_id in the event table.
     *                 This method find the specific row with the sub_id, then get the event_id
     *                 back in that row.
     */
    private void launchEditActivityWithEventID(final int position) {

        int date_today = getDateAsInt();

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

                        }

                        else if (!cursor.moveToFirst()) throw new CursorIndexOutOfBoundsException("EventID query: Cursor might be empty");

                    } else throw new IllegalArgumentException("Invalid token received at Event ID query.");
                } catch (CursorIndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }finally {
                    cursor.close();
                }

                setEventID(eventID);

                Intent intent = new Intent(getApplicationContext(), EditEventActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(DataContract.GlobalConstants.PASS_EVENT_ID, getEventID());
                bundle.putInt(DataContract.GlobalConstants.PASS_SUB_ID, position);
                bundle.putInt(DataContract.GlobalConstants.PASS_SELECTED_DATE, getDateAsInt());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        };

        //projection
        String projection[] = new String[]{
                EventEntry.COLUMN_DATE,
                EventEntry.COLUMN_SUB_ID,
                EventEntry.COLUMN_EVENT_ID
        };

        //selection
        String selection = EventEntry.COLUMN_DATE + "=?" + " AND " + EventEntry.COLUMN_SUB_ID + "=?";

        //selectionArgs
        String selectionArgs[] = new String[]{
                String.valueOf(date_today),
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



    private Cursor createCursor() {
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
                null );
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

    @Override
    protected void onResume() {
        super.onResume();
    }


    /**
     *
     * @param id Just select any desired ID. Here I define in the global constant
     * @param bundle  pass any object in bundle, no need to pass anything in here.
     * @return  defined cursor
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        /**
         *  ListView will contain specified date's events.
         *  Required components
         *
         *  1. date to specify date
         *  2. event ID to get the workout name
         *  3. set_count
         *  4. rep_count
         *
         */

        int date;

        date = bundle.getInt(DataContract.GlobalConstants.PASS_CREATE_LOADER_DATE);

        String projection[] = new String[]{
                EventEntry._ID,
                EventEntry.COLUMN_DATE,
                EventEntry.COLUMN_EVENT_ID,
                EventEntry.COLUMN_WEIGHT_COUNT,
                EventEntry.COLUMN_SET_COUNT,
                EventEntry.COLUMN_REP_COUNT
        };

        String selection = EventEntry.COLUMN_DATE + "=?";
        String selectionArgs[] = new String[]{String.valueOf(date)};

        return new CursorLoader(
                this,
                EventEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        //set delete menu text to red color
        MenuItem delete_all_events = menu.findItem(R.id.menu_delete_all_events);
        SpannableString string = new SpannableString(delete_all_events.getTitle());
        string.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(ListActivity.this, R.color.colorPrimary)),
                0,
                string.length(),
                Spanned.SPAN_PRIORITY);

        delete_all_events.setTitle(string);

        return super.onPrepareOptionsMenu(menu);
    }

    //if searchView is expanded, then collapse
    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_delete_all_events:
                int numOfDeletedRows = deleteEventTable();
                Toast.makeText(ListActivity.this, String.valueOf(numOfDeletedRows) + " deleted.",
                        Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_insert_event:
                event_insertDummyValues();
                break;

            case R.id.menu_view_tables:
                Intent intent = new Intent(getApplicationContext(), DBviewer.class);
                startActivity(intent);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private int deleteEventTable() {
        int numberOfDeletedRows = getContentResolver().delete(
                EventEntry.CONTENT_URI,
                null,
                null
        );
        return numberOfDeletedRows;
    }

    private void eventType_insertDummyValues() {

        ContentValues dummyValues = new ContentValues();

        dummyValues.put(DataContract.EventTypeEntry.COLUMN_EVENT_NAME, "Test Event");

        Uri uri = getContentResolver().insert(DataContract.EventTypeEntry.CONTENT_URI, dummyValues);

        if (uri == null) throw new IllegalArgumentException("Calendar table (inser dummy)" +
                "failed to insert data. check the MainActivity method and the table.");


        Toast.makeText(this, "EventType inserted", Toast.LENGTH_SHORT).show();
    }
}

