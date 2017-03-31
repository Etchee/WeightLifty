package etchee.com.weightlifty;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import etchee.com.weightlifty.data.DBviewer;
import etchee.com.weightlifty.data.DataContract;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Button to view SQLite table
        Button viewTableButton = (Button)findViewById(R.id.view_tables_button);
        viewTableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DBviewer.class);
                startActivity(intent);
            }
        });

        //Button to insert dummy data for testing purposes
        Button insert_dummy_data = (Button)findViewById(R.id.insert_dummy_data);
        insert_dummy_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO insert dummy data here
                calendar_insertDummyValues();
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
    }

    //insert fake values to all the tables to test if the tables are properly working
    private void calendar_insertDummyValues() {

        int eventIDs[] = new int[]{2,5,3,14,2};

        ContentValues dummyValues = new ContentValues();
        dummyValues.put(DataContract.CalendarEntry.COLUMN_DATE, "2017/02/14");
        dummyValues.put(DataContract.CalendarEntry.COLUMN_EVENT_IDs, eventIDs.toString());
        dummyValues.put(DataContract.CalendarEntry.COLUMN_DAY_TAG, "");

        Uri uri = getContentResolver().insert(DataContract.CalendarEntry.CONTENT_URI, dummyValues);

        if (uri == null) throw new IllegalArgumentException("Calendar table (inser dummy)" +
                "failed to insert data. check the MainActivity method and the table.");

        Log.v("DUMMYDATA", "Data inserted in: " + uri);
    }
}
