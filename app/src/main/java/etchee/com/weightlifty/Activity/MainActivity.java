package etchee.com.weightlifty.Activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import etchee.com.weightlifty.Fragment.MainActivityFragment;
import etchee.com.weightlifty.Fragment.SettingsFragment;
import etchee.com.weightlifty.R;
import etchee.com.weightlifty.data.DBviewer;
import etchee.com.weightlifty.data.DataContract;
import etchee.com.weightlifty.data.DataContract.CalendarEntry;
import etchee.com.weightlifty.data.DataContract.EventEntry;
import etchee.com.weightlifty.data.DataDbHelper;
import etchee.com.weightlifty.data.TextResDecoder;

public class MainActivity extends AppCompatActivity {

    Context context;
    private SQLiteOpenHelper dbHelper;
    private Toolbar toolbar;
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getFragmentManager()
                .beginTransaction()
                .add(R.id.container_fragment_main, new MainActivityFragment()).commit();
        context = getApplicationContext();
        dbHelper = new DataDbHelper(context);

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

    }


    @Override
    protected void onResume() {
        super.onResume();
        calendar_insertTodaysRow();
    }

    private void testEventNameQuery() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String projection[] = new String[]{
                DataContract.EventType_FTSEntry.COLUMN_EVENT_NAME,
                DataContract.EventType_FTSEntry.COLUMN_EVENT_TYPE,
                DataContract.EventType_FTSEntry.COLUMN_ROW_ID
        };
        String selection = DataContract.EventType_FTSEntry.COLUMN_ROW_ID + "=?";
        String []selectionArgs = new String[]{String.valueOf(3)};
        db.query(
                DataContract.EventType_FTSEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null, null, null
        );
    }


    public String getFormattedDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);

        int month = calendar.get(Calendar.MONTH) + 1;   //month starts from zero
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String concatenated = String.valueOf(year) + "/" + String.valueOf(month) + "/" + String.valueOf(day);

        return concatenated;
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

    private String getDateString() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        String date = simpleDateFormat.format(now);
        Log.v(TAG, date);
        return date;
    }

    /**
     *  This method does the following:
     *  1. Checks if the last row in the calendar entry is today
     *  2. If yes, do nothing
     *      If not, insert today's row
     */
    private void calendar_insertTodaysRow() {
        Cursor cursor = null;
        // query the calendar table to check the last row
        int dateFromLastRow;
        try {
            dateFromLastRow = 0;
            String projection[] = new String[]{CalendarEntry.COLUMN_DATE};

            cursor = getContentResolver().query(
                    CalendarEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null
            );
            if (cursor.moveToLast()) {
                int dateColumnIndex = cursor.getColumnIndex(CalendarEntry.COLUMN_DATE);
                dateFromLastRow = cursor.getInt(dateColumnIndex);
            }
        } finally {
            cursor.close();
        }

        //check if the date matches to that of today
        int dateOfToday = getDateAsInt();
        String formattedDate = getFormattedDate();

        //if today's date doesn't match to that of the last row in calendar table, insert.
        if (dateFromLastRow != dateOfToday) {
            ContentValues dummyValues = new ContentValues();
            Uri uri;

            dummyValues.put(CalendarEntry.COLUMN_DATE, dateOfToday);
            dummyValues.put(CalendarEntry.COLUMN_EVENT_IDs, "");
            dummyValues.put(CalendarEntry.COLUMN_DAY_TAG, "");

            uri = getContentResolver().insert(DataContract.CalendarEntry.CONTENT_URI, dummyValues);

            //error handling block
            if (uri == null) throw new IllegalArgumentException("Calendar table (inser dummy)" +
                    "failed to insert data. check the MainActivity method and the table.");
        }

    }

    private void event_insertDummyValues() {

        for (int i  = 0; i < new Random().nextInt(10); i++) {
            ContentValues values = new ContentValues();

            int rep_count = new Random().nextInt(10);
            int set_count = new Random().nextInt(20);
            int weight_count = 70;
            int sub_ID = getNextSub_id();
            int eventID = new Random().nextInt(900);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;   //month starts from zero
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            String formattedDate = String.valueOf(year) + "/" + String.valueOf(month) + "/" + String.valueOf(day);

            values.put(EventEntry.COLUMN_SUB_ID, sub_ID);
            values.put(EventEntry.COLUMN_EVENT_ID, eventID);
            values.put(EventEntry.COLUMN_REP_COUNT, rep_count);
            values.put(EventEntry.COLUMN_SET_COUNT, set_count);
            values.put(EventEntry.COLUMN_WEIGHT_COUNT, weight_count);
            values.put(EventEntry.COLUMN_FORMATTED_DATE, formattedDate);

            Uri uri = getContentResolver().insert(EventEntry.CONTENT_URI, values);
            if (uri == null) throw new IllegalArgumentException("Calendar table (insert dummy)" +
                    "failed to insert data. check the MainActivity method and the table.");
        }

    }

    private void event_insertDummyValues2() {

        ContentValues values = new ContentValues();

        int rep_count = new Random().nextInt(10);
        int set_count = new Random().nextInt(20);
        int date = getDateAsInt() - 2;
        int weight_count = 70;
        int sub_ID = getNextSub_id();
        int eventID = new Random().nextInt(900);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.DATE, -2);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;   //month starts from zero
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String formattedDate = String.valueOf(year) + "/" + String.valueOf(month) + "/" + String.valueOf(day);

        values.put(EventEntry.COLUMN_SUB_ID, sub_ID);
        values.put(EventEntry.COLUMN_EVENT_ID, eventID);
        values.put(EventEntry.COLUMN_REP_COUNT, rep_count);
        values.put(EventEntry.COLUMN_SET_COUNT, set_count);
        values.put(EventEntry.COLUMN_WEIGHT_COUNT, weight_count);
        values.put(EventEntry.COLUMN_FORMATTED_DATE, formattedDate);

        Uri uri = getContentResolver().insert(EventEntry.CONTENT_URI, values);

        if (uri == null) throw new IllegalArgumentException("Calendar table (insert dummy)" +
                "failed to insert data. check the MainActivity method and the table.");

    }


    private void event_insertDummyValues3() {

        ContentValues values = new ContentValues();

        int rep_count = new Random().nextInt(10);
        int set_count = new Random().nextInt(20);
        int date = getDateAsInt();
        int weight_count = 70;
        int sub_ID = getNextSub_id();
        int eventID = new Random().nextInt(900);
        String formattedDate = getFormattedDate();

        values.put(EventEntry.COLUMN_SUB_ID, sub_ID);
        values.put(EventEntry.COLUMN_EVENT_ID, eventID);
        values.put(EventEntry.COLUMN_REP_COUNT, rep_count);
        values.put(EventEntry.COLUMN_SET_COUNT, set_count);
        values.put(EventEntry.COLUMN_WEIGHT_COUNT, weight_count);
        values.put(EventEntry.COLUMN_FORMATTED_DATE, formattedDate);

        Uri uri = getContentResolver().insert(EventEntry.CONTENT_URI, values);

        if (uri == null) throw new IllegalArgumentException("Calendar table (insert dummy)" +
                "failed to insert data. check the MainActivity method and the table.");


    }

    private int getNextSub_id() {
        int sub_id;

        String projection[] = new String[]{EventEntry.COLUMN_FORMATTED_DATE, EventEntry.COLUMN_SUB_ID};
        String selection = EventEntry.COLUMN_FORMATTED_DATE + "=?";
        String selectionArgs[] = new String[]{getFormattedDate()};
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

    private void eventType_insertDummyValues() {

        ContentValues values = new ContentValues();

        values.put(DataContract.EventType_FTSEntry.COLUMN_EVENT_NAME, "Test Event");
        values.put(DataContract.EventType_FTSEntry.COLUMN_EVENT_TYPE, "Test Category");
        Uri uri = getContentResolver().insert(DataContract.EventType_FTSEntry.CONTENT_URI, values);
        if (uri == null) throw new IllegalArgumentException("Calendar table (inser dummy)" +
                "failed to insert data. check the MainActivity method and the table.");


        Log.v(TAG, "EventType inserted at: " + uri.toString());
    }

    private int deleteEventTable() {
        int numberOfDeletedRows = getContentResolver().delete(
                EventEntry.CONTENT_URI,
                null,
                null
        );
        Toast.makeText(context, String.valueOf(numberOfDeletedRows) + " rows deleted.", Toast.LENGTH_SHORT).show();
        return numberOfDeletedRows;
    }

    private int deleteEventTypeTable() {
        int numberOfDeletedRows = getContentResolver().delete(
                DataContract.EventType_FTSEntry.CONTENT_URI,
                null,
                null
        );
        Toast.makeText(context, String.valueOf(numberOfDeletedRows) + " rows deleted.", Toast.LENGTH_SHORT).show();
        return numberOfDeletedRows;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        //set delete menu text to red color
        MenuItem delete_all_events = menu.findItem(R.id.menu_delete_all_events);
        SpannableString string = new SpannableString(delete_all_events.getTitle());
        string.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary)),
                0,
                string.length(),
                Spanned.SPAN_PRIORITY);

        delete_all_events.setTitle(string);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_delete_all_events:
                int numOfDeletedRows = deleteEventTable();
                Toast.makeText(context, String.valueOf(numOfDeletedRows) + " deleted.",
                        Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_insert_event:
                event_insertDummyValues();
                break;
            case R.id.menu_insert_event_type:
                eventType_insertDummyValues();
                /**
                 * V/MainActivity: Columns in FTS are: [table_eventType_event_name, table_eventType_event_type]
                 */
                break;

            case R.id.menu_view_tables:
                Intent intent = new Intent(getApplicationContext(), DBviewer.class);
                startActivity(intent);
                break;

            case R.id.decode_workout_res:
                try {
                    TextResDecoder decoder = new TextResDecoder(context, this);
                    decoder.main();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.reset_FTS_table:
                deleteEventTypeTable();
                break;

            case R.id.test_eventType_db:
//                Cursor cursor = getContentResolver().query(
//                        DataContract.EventType_FTSEntry.CONTENT_URI,
//                        new String[]{DataContract.EventType_FTSEntry.COLUMN_EVENT_NAME,
//                                DataContract.EventType_FTSEntry.COLUMN_ROW_ID},
//                        DataContract.EventType_FTSEntry.COLUMN_ROW_ID + "=?",
//                        new String[]{String.valueOf(872)},
//                        null
//                );
                Cursor cursor = getContentResolver().query(
                        DataContract.EventType_FTSEntry.CONTENT_URI,
                        null, null, null, null
                );
                int index = cursor.getColumnIndex(DataContract.EventType_FTSEntry.COLUMN_EVENT_NAME);
                String str = null;
                if (cursor.moveToLast()) {
                    cursor.moveToPrevious();
                    str = cursor.getString(index);
                }
                Toast.makeText(context, "Debug(Event name): " + str,
                        Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_settings:
                getFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.animator.slide_in_from_left,
                                R.animator.slide_out_to_right,
                                R.animator.slide_in_from_right,
                                R.animator.slide_out_to_left
                        )
                        .replace(R.id.container_fragment_main, new SettingsFragment())
                        .addToBackStack(null)
                        .commit();
                break;

        }

        return super.onOptionsItemSelected(item);
    }
}


