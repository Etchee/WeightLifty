package etchee.com.weightlifty.Activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;

import java.util.Calendar;
import java.util.Random;

import etchee.com.weightlifty.DataMethods.subIDfixHelper;
import etchee.com.weightlifty.Fragment.CurrentListFragment;
import etchee.com.weightlifty.Fragment.SearchFragment;
import etchee.com.weightlifty.R;
import etchee.com.weightlifty.data.DBviewer;
import etchee.com.weightlifty.data.DataContract.EventEntry;

/**
 *  From MainActivity -> Loads today's data, display as a list.
 *  When SearchView is initiated -> Performs a search, then pass the resulting cursor to SearchAdapter
 */

public class WorkoutListActivity extends AppCompatActivity {

    private SearchFragment fragmentActivity;
    private Activity activity;
    private FloatingActionButton fab;

    //To make sure that there is only one instance because OpenHelper will serialize requests anyways
    private ContentResolver contentResolver;
    private int eventID;
    private final int CREATE_LOADER_ID = 1;
    private final String TAG = getClass().getSimpleName();
    private Toolbar toolbar;
    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        context = getApplicationContext();
        toolbar = (Toolbar) findViewById(R.id.list_toolbar);
        setSupportActionBar(toolbar);
        contentResolver = getContentResolver();
        activity = this;

        //fab setup
        fab = (FloatingActionButton) findViewById(R.id.listactivity_fab);

        /*
         *  on FAB click, enter chooseEventMode
         */
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterSearchMode();
            }
        });

        getSupportFragmentManager().beginTransaction().add(
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
        return true;
    }

    private void enterSearchMode() {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_from_left,
                        R.anim.slide_out_to_right,
                        R.anim.slide_in_from_right,
                        R.anim.slide_out_to_left)
                .addToBackStack(null)
                .replace(R.id.container_fragment_listActivity, new SearchFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_search_button:

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

    private int deleteEventTableDatabase() {
        int numberOfDeletedRows = getContentResolver().delete(
                EventEntry.CONTENT_URI,
                null,
                null
        );
        return numberOfDeletedRows;
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
}

