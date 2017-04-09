package etchee.com.weightlifty;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import etchee.com.weightlifty.data.DataContract.EventEntry;
import etchee.com.weightlifty.data.DataContract.CalendarEntry;

import java.util.Arrays;

/**
 * Created by rikutoechigoya on 2017/04/09.
 */

public class ChooseEventActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_event);

        /**
         *  For now, on button press, passing dummy event data to next activity
         */
        Button button = (Button)findViewById(R.id.button_add_squat);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues values = addSquatEvent();
                Intent intent = new Intent(getApplicationContext(), EditEventActivity.class);
                intent.putExtra("values", values);
                startActivity(intent);
            }
        });
    }

    private ContentValues addSquatEvent() {
        ContentValues squatValues = new ContentValues();

        int set_count = 5;
        int sub_ID = 0;
        int id = 0;
        int eventID = 2;

        squatValues.put(EventEntry._ID, id);
        squatValues.put(EventEntry.COLUMN_SUB_ID, sub_ID);
        squatValues.put(EventEntry.COLUMN_EVENT_ID, eventID);
        squatValues.put(EventEntry.COLUMN_SET_COUNT, set_count);

        return squatValues;
    }

}
