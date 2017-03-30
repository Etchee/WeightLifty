package etchee.com.weightlifty;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import etchee.com.weightlifty.data.DBviewer;

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
    }
}
