package etchee.com.weightlifty.Activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;

import java.util.Calendar;
import java.util.Random;

import etchee.com.weightlifty.R;
import etchee.com.weightlifty.data.DBviewer;
import etchee.com.weightlifty.data.DataContract.EventEntry;
import etchee.com.weightlifty.DataMethods.subIDfixHelper;
import etchee.com.weightlifty.data.DataContract.EventType_FTSEntry;
import etchee.com.weightlifty.data.DataDbHelper;

/**
 *  From MainActivity -> Loads today's data, display as a list.
 *  When SearchView is initiated -> Performs a search, then pass the resulting cursor to SearchAdapter
 */

public class WorkoutListActivity extends AppCompatActivity implements
        SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    SearchInitiationListener searchListener;
    private FloatingActionButton fab;

    //To make sure that there is only one instance because OpenHelper will serialize requests anyways
    private ContentResolver contentResolver;
    private int eventID;
    private final int CREATE_LOADER_ID = 1;
    private final String TAG = getClass().getSimpleName();
    private Toolbar toolbar;
    private SearchView searchView;
    private Context context;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        context = getApplicationContext();
        toolbar = (Toolbar) findViewById(R.id.list_toolbar);
        setSupportActionBar(toolbar);
        contentResolver = getContentResolver();

        //fab setup
        fab = (FloatingActionButton) findViewById(R.id.listactivity_fab);

        /*
         *  on FAB click, enter chooseEventMode
         */
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterChooseEventMode();
            }
        });

        getFragmentManager().beginTransaction().add(
                R.id.container_fragment_listActivity,
                new CurrentListFragment()
        ).commit();
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
        final MenuItem item = menu.findItem(R.id.action_search_button);
        MenuItemCompat.expandActionView(item);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setQueryHint(getString(R.string.hint_search_events));
        searchView.setOnQueryTextListener(this);
        searchView.setOnSearchClickListener(searchViewOnClickSetup());

        return true;
    }

    private View.OnClickListener searchViewOnClickSetup(){
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.animator.slide_in_from_left,
                                R.animator.slide_out_to_right,
                                R.animator.slide_in_from_right,
                                R.animator.slide_out_to_left)
                        .replace(R.id.container_fragment_listActivity, new SearchFragment())
                        .commit();
            }
        };

        return listener;
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
     * When user clicks FAB, this method is called.
     * User will direcly start typing their desired events.
     *
     *  1. searchView must open
     *  2. Wipe the contents in the listView.
     *  3. Hook up a new adapter.
     */
    private void enterChooseEventMode() {
        searchView.setIconified(false);
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
    //if searchView is expanded, then collapse
    @Override
    public void onBackPressed() {
       if (!searchView.isIconified()) {
           searchView.onActionViewCollapsed();
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

    /**
     *      When SearchView is closed, the listView should have the list of today's workouts
     * @return false
     */
    @Override
    public boolean onClose() {
        //call current fragment?
        return false;
    }

    public interface SearchInitiationListener {
        void onSearchCursorReceived(Cursor cursor);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.v(TAG, "Text submitted: " + query);
        query = query + "*";
        Cursor cursor = queryWorkout(query);
        //send off cursor to search fragment
        searchListener.onSearchCursorReceived(cursor);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        //INITIATE SEARCH
        Log.v(TAG, "Text submitted: " + newText);
        //so that the search is for every part of the texts
        newText = newText + "*";
        Cursor cursor = queryWorkout(newText);
//        listview.setAdapter(searchAdapter);
//        searchAdapter.swapCursor(cursor);
        return false;
    }



    /**
     * Search method that returns a cursor.
     *
     * @param input User input text sent from the UI
     * @return cursor that contains the results of the search
     * @throws SQLException if search fails.
     */
    public Cursor queryWorkout(String input) {
        Log.w(TAG, input);
        /*
                Original sample code output:
                SELECT docid as _id,customer,name,(address1||(case when address2> '' then '
               ' || address2 else '' end)) as address,address1,address2,city,state,zipCode from
               CustomerInfo where searchData MATCH 'Piz*';

               My output in here (success):
               SELECT docid as _id,name_event,type_event FROM table_eventType_FTS WHERE name_event MATCH 'dumb';

                From queryEventIDFromName class:
               SELECT docid as _id,* FROM table_eventType_FTS WHERE name_event MATCH 'press sit-up';

               TODO I should make another column that joints all other columns (key_search) to search EVERYTHING
         */
        String query = "SELECT docid as _id," +
                EventType_FTSEntry.COLUMN_EVENT_NAME + "," +
                EventType_FTSEntry.COLUMN_EVENT_TYPE +
                " FROM " + EventType_FTSEntry.TABLE_NAME +
                " WHERE " + EventType_FTSEntry.COLUMN_EVENT_NAME + " MATCH '" + input + "';";

        Cursor cursor = new DataDbHelper(context).getReadableDatabase().rawQuery(query, null);
        return cursor;
    }

}

