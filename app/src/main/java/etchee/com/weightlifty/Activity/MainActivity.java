package etchee.com.weightlifty.Activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Calendar;

import etchee.com.weightlifty.R;
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


    @Override
    protected void onResume() {
        super.onResume();
        calendar_insertTodaysRow();
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

        ContentValues values = new ContentValues();

        int rep_count = 7;
        int set_count = 5;
        int date = getDateAsInt();
        int weight_count = 70;
        int sub_ID = getNextSub_id();
        int eventID = 2;

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

    private void eventType_insertDummyValues() {

        ContentValues dummyValues = new ContentValues();

        dummyValues.put(DataContract.EventTypeEntry.COLUMN_EVENT_NAME, "Test Event");

        Uri uri = getContentResolver().insert(DataContract.EventTypeEntry.CONTENT_URI, dummyValues);

        if (uri == null) throw new IllegalArgumentException("Calendar table (inser dummy)" +
                "failed to insert data. check the MainActivity method and the table.");


        Toast.makeText(this, "EventType Data inserted: " + uri.toString(), Toast.LENGTH_SHORT).show();
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
}


