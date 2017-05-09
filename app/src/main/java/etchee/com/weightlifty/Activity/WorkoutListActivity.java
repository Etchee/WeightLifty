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
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;

import java.util.Calendar;
import java.util.Random;

import etchee.com.weightlifty.Adapter.ListAdapter;
import etchee.com.weightlifty.Adapter.SearchAdapter;
import etchee.com.weightlifty.R;
import etchee.com.weightlifty.data.DBviewer;
import etchee.com.weightlifty.data.DataContract;
import etchee.com.weightlifty.data.DataContract.EventEntry;
import etchee.com.weightlifty.DataMethods.subIDfixHelper;
import etchee.com.weightlifty.data.DataContract.EventType_FTSEntry;
import etchee.com.weightlifty.data.DataDbHelper;

import static android.R.attr.data;

/**
 *  From MainActivity -> Loads today's data, display as a list.
 *  When SearchView is initiated -> Performs a search, then pass the resulting cursor to SearchAdapter
 */

public class WorkoutListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private ListView listview;
    private FloatingActionButton fab;

    //To make sure that there is only one instance because OpenHelper will serialize requests anyways
    private ContentResolver contentResolver;
    private int eventID;
    private final int CREATE_LOADER_ID = 1;
    private final String TAG = getClass().getSimpleName();
    private SearchManager searchManager;
    private Toolbar toolbar;
    private SearchView searchView;
    private Context context;
    private ListAdapter listAdapter;
    private SearchAdapter searchAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);

        context = getApplicationContext();

        toolbar = (Toolbar) findViewById(R.id.list_toolbar);
        setSupportActionBar(toolbar);

        contentResolver = getContentResolver();

        //fab setup
        fab = (FloatingActionButton) findViewById(R.id.list_fab);

        /**
         *  on FAB click, enter chooseEventMode
         */
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterChooseEventMode();
            }
        });

        //for the empty view
        View emptyView = findViewById(R.id.view_empty);
        listview = (ListView)findViewById(R.id.listview_workout);
        listview.setEmptyView(emptyView);

        //cursor init
        Cursor listCursor = initListCursor();
        Cursor eventTypeCursor = initEventTypeCursor();

        //adapter initialization
        searchAdapter = new SearchAdapter(context, eventTypeCursor);
        listAdapter = new ListAdapter(getApplicationContext(), listCursor, 0);
        listview.setAdapter(listAdapter);

        //Init the loader

        //only display today's data for now
        Bundle bundle = new Bundle();
        bundle.putInt(DataContract.GlobalConstants.PASS_CREATE_LOADER_DATE, getDateAsInt());
        getLoaderManager().initLoader(CREATE_LOADER_ID, bundle, this);

        //listView setup
        listview.setOnItemClickListener(listViewOnItemClickSetup());
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        deleteOptionRed(menu);
        return super.onPrepareOptionsMenu(menu);
    }


    /**
     *  When creating the option, define the searchView. (Pretty sure this is done right)
     * @param menu menu layout
     * @return  true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);

        searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        final MenuItem item = menu.findItem(R.id.action_search_button);
        MenuItemCompat.expandActionView(item);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listview.setAdapter(null);
            }
        });
        searchView.setQueryHint(getString(R.string.hint_search_events));
        searchView.setOnQueryTextListener(this);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_search_button:
                onSearchRequested();
                break;

            case R.id.menu_delete_all_events:
                int numOfDeletedRows = deleteEventTableDatabase();
                Toast.makeText(WorkoutListActivity.this, String.valueOf(numOfDeletedRows) + " deleted.",
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

    /**
     *  OnClickListener of the listView. Behaves differently depending on which of the two adapters
     *  are set atm.
     *
     *  case 1: listAdapter →　gets the SUB_ID, moves on to EditEventActivity
     *
     *  case 2: SearchAdapter → gets the ROW_ID (FTS Table), moves on to EditEventActivity
     * @return
     */
    private AdapterView.OnItemClickListener listViewOnItemClickSetup() {
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //check which adapters are set
                if (listview.getAdapter() == searchAdapter) {
                    //I have to get either ROW_ID or just item name here
                    String event = (String) searchAdapter.getItem(position);

                    //for debug, query the FTS table to see if the item name is correct
                    String projection[] = new String[]{
                            EventType_FTSEntry.COLUMN_EVENT_NAME
                    };

                    String selection = EventType_FTSEntry.COLUMN_EVENT_NAME + "=?";
                    String selectionArgs[] = new String[]{String.valueOf(event)};

                    String eventName = null;
                    Cursor cursor = null;
                    try {
                        cursor = getContentResolver().query(
                                DataContract.EventType_FTSEntry.CONTENT_URI,
                                projection,
                                selection,
                                selectionArgs,
                                null
                        );
                        if (cursor.moveToFirst()) {
                            eventName = cursor.getString(cursor.getColumnIndex(
                                    EventType_FTSEntry.COLUMN_EVENT_NAME));
                        }

                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                    launchEditEventActivityWithNewEvent(eventName);

                } else if (listview.getAdapter() == listAdapter) {
                    launchEditActivityWithEventID(position);
                    Log.v(TAG, "SUB_ID received as: " + String.valueOf(position));
                } else {
                    //adapter null. Do nothing
                }
            }
        };

        return listener;
    }



    /**
     * When user clicks FAB, this method is called.
     * User will direcly start typing their desired events.
     *
     *  1. searchView must open
     *  2. Wipe the contents in the listView.
     *  3. Hook up a new adapter.
     */
    private void enterChooseEventMode() {
        searchView.setIconified(false);
        listview.setAdapter(null);
    }

    private int deleteEventTableDatabase() {
        int numberOfDeletedRows = getContentResolver().delete(
                EventEntry.CONTENT_URI,
                null,
                null
        );
        return numberOfDeletedRows;
    }

    private int getEventID() {
        return eventID;
    }

    private void setEventID(int eventID) {
        this.eventID = eventID;
    }

    private void event_insertDummyValues() {

        ContentValues values = new ContentValues();

        int rep_count = new Random().nextInt(10);
        int set_count = new Random().nextInt(20);
        int date = getDateAsInt();
        int weight_count = 70;
        int sub_ID = getNextSub_id();
        int eventID = new Random().nextInt(900);

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

    //Send contentValues
    private void launchEditEventActivityWithNewEvent(String eventName) {
        Intent intent = new Intent(this, EditEventActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(DataContract.GlobalConstants.PASS_EVENT_STRING, eventName);
        intent.putExtras(bundle);
        startActivity(intent);
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
                null );
        return cursor;
    }

    private Cursor initEventTypeCursor() {

        //Cursor cursor;
//        String projection[] = {
//                EventType_FTSEntry.COLUMN_ROW_ID,
//                EventType_FTSEntry.COLUMN_EVENT_TYPE,
//                EventType_FTSEntry.COLUMN_EVENT_NAME
//        };
//
//        String selection = EventType_FTSEntry.COLUMN_ROW_ID + "=?";
//        String selectionArgs[] = new String[]{ String.valueOf(1) };
//
//        cursor = contentResolver.query(
//                EventType_FTSEntry.CONTENT_URI,
//                projection,
//                selection,
//                selectionArgs,
//                null
//        );

        //do a rawQuery because I want to specify the hidden docid column
        Cursor cursor;
        SQLiteDatabase db = new DataDbHelper(context).getReadableDatabase();
        String query = "SELECT " + " * " + " FROM "
                + DataContract.EventType_FTSEntry.TABLE_NAME;
        cursor = db.rawQuery(query, null);
        Log.v(TAG, "Init cursor gave a count of " + String.valueOf(cursor.getCount()));

        if (cursor != null) return cursor;
        else throw new NullPointerException(TAG + ": FTS table cursor initialization has failed");
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
        new subIDfixHelper(getApplicationContext()).execute(getDateAsInt());
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
        listAdapter.swapCursor(cursor);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        listAdapter.swapCursor(null);
    }


    //if searchView is expanded, then collapse
    @Override
    public void onBackPressed() {
       if (!searchView.isIconified()) {
           searchView.onActionViewCollapsed();
           listview.setAdapter(listAdapter);
       } else {
           super.onBackPressed();
       }
    }

    private void deleteOptionRed(Menu menu) {
        //set delete menu text to red color
        MenuItem delete_all_events = menu.findItem(R.id.menu_delete_all_events);
        SpannableString string = new SpannableString(delete_all_events.getTitle());
        string.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(WorkoutListActivity.this, R.color.colorPrimary)),
                0,
                string.length(),
                Spanned.SPAN_PRIORITY);

        delete_all_events.setTitle(string);
    }

    private void eventType_insertDummyValues() {

        ContentValues dummyValues = new ContentValues();

        dummyValues.put(DataContract.EventTypeEntry.COLUMN_EVENT_NAME, "Test Event");

        Uri uri = getContentResolver().insert(DataContract.EventTypeEntry.CONTENT_URI, dummyValues);

        if (uri == null) throw new IllegalArgumentException("Calendar table (inser dummy)" +
                "failed to insert data. check the MainActivity method and the table.");


        Toast.makeText(this, "EventType inserted", Toast.LENGTH_SHORT).show();
    }

    /**
     *      When SearchView is closed, the listView should have the list of today's workouts
     * @return false
     */
    @Override
    public boolean onClose() {

        listview.setAdapter(listAdapter);

        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.v(TAG, "Text submitted: " + query);
        query = query + "*";
        Cursor cursor = queryWorkout(query);
        listview.setAdapter(searchAdapter);
        searchAdapter.swapCursor(cursor);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        //INITIATE SEARCH
        Log.v(TAG, "Text submitted: " + newText);
        newText = newText + "*";
        Cursor cursor = queryWorkout(newText);
        listview.setAdapter(searchAdapter);
        searchAdapter.swapCursor(cursor);
        return false;
    }



    /**
     *  Search method that returns a cursor.
     *
     * @param input User input text sent from the UI
     * @return cursor that contains the results of the search
     * @throws SQLException if search fails.
     */
    public Cursor queryWorkout(String input) {
        Log.w(TAG, input);

        /*
                Original sample code:
                String query = "SELECT docid as _id," +
                KEY_CUSTOMER + "," +
                KEY_NAME + "," +
                "(" + KEY_ADDRESS1 + "||" +
                "(case when " + KEY_ADDRESS2 + "> '' then '\n' || "
                + KEY_ADDRESS2 + " else '' end)) as " + KEY_ADDRESS + "," +
                KEY_ADDRESS1 + "," +
                KEY_ADDRESS2 + "," +
                KEY_CITY + "," +
                KEY_STATE + "," +
                KEY_ZIP +
                " from " + FTS_VIRTUAL_TABLE +
                " where " + KEY_SEARCH + " MATCH '" + inputText + "';";
         */

        /*
                Original sample code output:
                SELECT docid as _id,customer,name,(address1||(case when address2> '' then '
               ' || address2 else '' end)) as address,address1,address2,city,state,zipCode from
               CustomerInfo where searchData MATCH 'Piz*';

               My output:
               SELECT docid as _id,name_event,type_event FROM table_eventType_FTS WHERE name_event MATCH 'dumb';

               TODO I should make another column that joints all other columns (key_search) to search EVERYTHING
         */

        String query = "SELECT docid as _id," +
                EventType_FTSEntry.COLUMN_EVENT_NAME + "," +
                EventType_FTSEntry.COLUMN_EVENT_TYPE +
                " FROM " + EventType_FTSEntry.TABLE_NAME +
                " WHERE " + EventType_FTSEntry.COLUMN_EVENT_NAME + " MATCH '" + input + "';";


        Cursor cursor = new DataDbHelper(context).getReadableDatabase().rawQuery(query, null);

        Log.v(TAG, DatabaseUtils.dumpCursorToString(cursor));

        return cursor;
    }

}

