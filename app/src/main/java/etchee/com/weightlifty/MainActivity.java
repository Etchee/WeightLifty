package etchee.com.weightlifty;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Calendar;

import etchee.com.weightlifty.data.DataContract.CalendarEntry;
import etchee.com.weightlifty.data.DataContract.EventEntry;
import etchee.com.weightlifty.data.DBviewer;
import etchee.com.weightlifty.data.DataContract;
import etchee.com.weightlifty.data.DataDBhelper;

public class MainActivity extends AppCompatActivity {

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        //Button to view SQLite tables
        Button viewTableButton = (Button)findViewById(R.id.view_tables_button);
        viewTableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DBviewer.class);
                startActivity(intent);
            }
        });

        //"Begin Workout" button to launch listActivity
        Button begin_workout = (Button)findViewById(R.id.begin_workout_button);
        begin_workout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ListActivity.class);
                startActivity(intent);
            }
        });

        Button plan_workout = (Button)findViewById(R.id.plan_workout);
        plan_workout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ListActivity.class);
                startActivity(intent);
            }
        });

        //delete button for all data in calednar table
        Button delete_dummy_button = (Button)findViewById(R.id.button_calendar_delete_dummy);
        delete_dummy_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int numberOfDeletedRows = deleteAllTableData();
            }
        });

        //Button to insert dummy eventType data
        final Button insertDummy_eventType = (Button)findViewById(R.id.button_insert_eventtype);
        insertDummy_eventType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventType_insertDummyValues();
            }
        });

        final Button insert_dummy_event = (Button)findViewById(R.id.button_insert_event);
        insert_dummy_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                event_insertDummyValues();
            }
        });

        Button insert_today_data = (Button)findViewById(R.id.insert_calendar_today);
        insert_today_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar_insertTodaysRow();
            }
        });

        final Button delete_event_data = (Button)findViewById(R.id.button_remove_events);
        delete_event_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteEventTable();
            }
        });
    }
/*

    private void test_toastLastDateInCalendar() {
        int rowsInserted;
        Cursor cursor;

        SQLiteDatabase readDb = new DataDBhelper(context).getReadableDatabase();

        //for now, I'm looking at the date column only.
        String projection[] = new String[]{CalendarEntry.COLUMN_DATE};

        cursor = getContentResolver().query(
                CalendarEntry.CONTENT_URI,
                projection,
                null,
                null,
                null,
                null
                );

        // get the data from the last row
        if (cursor.moveToLast()) {
            int lastdate = cursor.getInt(cursor.getColumnIndex(CalendarEntry.COLUMN_DATE));

            if (lastdate == 2017430) {
                Toast.makeText(context, "Valid calendar data!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Invalid today calendar date", Toast.LENGTH_SHORT).show();
            }
            
        }

        //error handling
        if (cursor == null) {
            throw new IllegalArgumentException("test_toastLastDateInCalendar() function has " +
                    "failed. Check the method or the calendar table.");
        }
    }
*/

    private int getDateAsInt() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;   //month starts from zero
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String concatenated = String.valueOf(year) + String.valueOf(month) + String.valueOf(day);
        Log.v("Concatenated", concatenated);

        return Integer.parseInt(concatenated);
    }

    /**
     *  This method does the following:
     *  1. Checks if the last row in the calendar entry is today
     *  2. If yes, do nothing
     *      If not, insert today's row
     */
    private void calendar_insertTodaysRow() {

        // query the calendar table to check the last row
        Cursor cursor;
        int dateFromLastRow = 0;
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

        //check if the date matches to that of today
        int dateOfToday = getDateAsInt();

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
            else Toast.makeText(context, "Row inserted. Date: " + String.valueOf(dateOfToday), Toast.LENGTH_SHORT).show();
        } else Toast.makeText(context, "Calendar: today's record is there!", Toast.LENGTH_SHORT).show();

    }
/*

    //insert fake values to all the tables to test if the tables are properly working
    private void calendar_insertDummyValues() {

        //get the date data from the last row in the calendar table
        Cursor cursor;

        //for now, I'm looking at the date column only.
        String projection[] = new String[]{CalendarEntry.COLUMN_DATE};

        cursor = getContentResolver().query(
                CalendarEntry.CONTENT_URI,
                projection,
                null,
                null,
                null,
                null
        );

        // get the data from the last row
        if (cursor.moveToLast()) {
            cursor.getInt(cursor.getColumnIndex(CalendarEntry.COLUMN_DATE));
        }

        //error handling
        if (cursor == null) {
            throw new IllegalArgumentException("test_toastLastDateInCalendar() function has " +
                    "failed. Check the method or the calendar table.");
        }

        int eventIDs[] = new int[]{2,5,3,14,2};
        String eventIDsString = Arrays.toString(eventIDs);

        int date;
        ContentValues dummyValues = new ContentValues();
        Uri uri = null;

        for (int i = 1; i < 31; i++) {
            String dateAsStr = "20174" + String.valueOf(i);
            date = Integer.parseInt(dateAsStr);

            dummyValues.put(CalendarEntry.COLUMN_DATE, date);
            dummyValues.put(CalendarEntry.COLUMN_EVENT_IDs, eventIDsString);
            dummyValues.put(CalendarEntry.COLUMN_DAY_TAG, "");

            uri = getContentResolver().insert(DataContract.CalendarEntry.CONTENT_URI, dummyValues);

            dummyValues.clear();
            dateAsStr = "";
        }

        if (uri == null) throw new IllegalArgumentException("Calendar table (inser dummy)" +
                "failed to insert data. check the MainActivity method and the table.");

        Log.v("DUMMYDATA", "Data inserted in: " + uri);
        Toast.makeText(this, eventIDsString, Toast.LENGTH_SHORT).show();
    }
*/


    private void event_insertDummyValues() {

        /*
        *   From the helper class
        *    final String CREATE_EVENT_TABLE =
                "CREATE TABLE IF NOT EXISTS " + EventEntry.TABLE_NAME + " ("
                + EventEntry._ID + " INTEGER NOT NULL, "
                + EventEntry.COLUMN_SUB_ID + " INTEGER NOT NULL, "
                + EventEntry.COLUMN_SET_COUNT + " INTEGER NOT NULL, "
                + EventEntry.COLUMN_REP_SEQUENCE + " INTEGER NOT NULL, "
                + EventEntry.COLUMN_WEIGHT_SEQUENCE + " TEXT);";
        * */

        ContentValues dummyValues = new ContentValues();

        int repSequence[] = new int[] {7,7,7,7,7};
        int set_count = 5;
        int weightSequence[] = new int[]{70,70,70,70,70};
        int sub_ID = 0;
        int id = 0;
        int eventID = 2;


        dummyValues.put(EventEntry._ID, id);
        dummyValues.put(EventEntry.COLUMN_SUB_ID, sub_ID);
        dummyValues.put(EventEntry.COLUMN_EVENT_ID, eventID);
        dummyValues.put(EventEntry.COLUMN_REP_SEQUENCE, Arrays.toString(repSequence));
        dummyValues.put(EventEntry.COLUMN_SET_COUNT, set_count);
        dummyValues.put(EventEntry.COLUMN_WEIGHT_SEQUENCE, Arrays.toString(weightSequence));

        Uri uri = getContentResolver().insert(EventEntry.CONTENT_URI, dummyValues);

        if (uri == null) throw new IllegalArgumentException("Calendar table (inser dummy)" +
                "failed to insert data. check the MainActivity method and the table.");

        Log.v("DUMMYDATA", "Data inserted in: " + uri);
        Toast.makeText(this, "Event Data inserted: " + uri.toString(), Toast.LENGTH_SHORT).show();
    }

    private void eventType_insertDummyValues() {

        ContentValues dummyValues = new ContentValues();

        dummyValues.put(DataContract.EventTypeEntry.COLUMN_EVENT_NAME, "Test Event");

        Uri uri = getContentResolver().insert(DataContract.EventTypeEntry.CONTENT_URI, dummyValues);

        if (uri == null) throw new IllegalArgumentException("Calendar table (inser dummy)" +
                "failed to insert data. check the MainActivity method and the table.");

        Log.v("DUMMYDATA", "Data inserted in: " + uri);
        Toast.makeText(this, "EventType Data inserted: " + uri.toString(), Toast.LENGTH_SHORT).show();
    }

    private int deleteEventTable() {
        int numberofDeletedRows = getContentResolver().delete(
                EventEntry.CONTENT_URI,
                null,
                null
        );
        Toast.makeText(context, String.valueOf(numberofDeletedRows) + " rows deleted.", Toast.LENGTH_SHORT).show();
        return numberofDeletedRows;
    }

    private int deleteAllTableData() {
        int numberOfDeletedRows = getContentResolver().delete(
                CalendarEntry.CONTENT_URI,
                null,
                null);

        //if succeeded
        if (numberOfDeletedRows > 0) {
            Toast.makeText(this,
                    "Deleted: " + String.valueOf(numberOfDeletedRows) + " rows",
                    Toast.LENGTH_SHORT).show();
        }

        //if failed
        if (numberOfDeletedRows <= 0) {
            Toast.makeText(this, "reset failed", Toast.LENGTH_SHORT).show();
        }



        return numberOfDeletedRows;
    }

    @Override
    protected void onResume() {
        super.onResume();
        calendar_insertTodaysRow();
    }


}
