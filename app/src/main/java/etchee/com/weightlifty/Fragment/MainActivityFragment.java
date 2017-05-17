package etchee.com.weightlifty.Fragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.Random;

import etchee.com.weightlifty.Activity.WorkoutListActivity;
import etchee.com.weightlifty.R;
import etchee.com.weightlifty.data.DataContract;
import etchee.com.weightlifty.data.TextResDecoder;
import etchee.com.weightlifty.data.DataContract.EventEntry;

/**
 * The welcome activity fragment
 * Created by rikutoechigoya on 2017/05/11.
 */

public class MainActivityFragment extends android.app.Fragment {

    private Button begin_workout;
    private SubsamplingScaleImageView imageView;
    private Context context;
    private TextView hint_update_text, hint_number, hint_percent_text;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        hint_update_text = (TextView) view.findViewById(R.id.hint_update_text);
        hint_percent_text = (TextView)view.findViewById(R.id.hint_text_update_percent);
        hint_number = (TextView)view.findViewById(R.id.hint_number_update);

        //"Begin Workout" button to launch listActivity
        begin_workout = (Button) view.findViewById(R.id.begin_workout_button);
        begin_workout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), WorkoutListActivity.class);
                startActivity(intent);
            }
        });

        if (!checkEventData()) {
            //make all the elements visible
            hint_number.setVisibility(View.VISIBLE);
            hint_percent_text.setVisibility(View.VISIBLE);
            hint_update_text.setVisibility(View.VISIBLE);
            //and start workout button disabled
            begin_workout.setEnabled(false);
            // parse database
            try {
                TextResDecoder decoder = new TextResDecoder(context, getActivity());
                decoder.main();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!checkEventData()) {
            //make all the elements visible
            hint_number.setVisibility(View.VISIBLE);
            hint_percent_text.setVisibility(View.VISIBLE);
            hint_update_text.setVisibility(View.VISIBLE);
            event_insertDummyValues();
            // parse database
            try {
                TextResDecoder decoder = new TextResDecoder(context, getActivity());
                decoder.main();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    private void event_insertDummyValues() {

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

            Uri uri = context.getContentResolver().insert(EventEntry.CONTENT_URI, values);
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
            cursor = context.getContentResolver().query(
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

    public String getFormattedDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);

        int month = calendar.get(Calendar.MONTH) + 1;   //month starts from zero
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String concatenated = String.valueOf(year) + "/" + String.valueOf(month) + "/" + String.valueOf(day);

        return concatenated;
    }

    private Boolean checkEventData() {
        String tester = "";
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    DataContract.EventType_FTSEntry.CONTENT_URI,
                    new String[]{DataContract.EventType_FTSEntry.COLUMN_EVENT_NAME},
                    DataContract.EventType_FTSEntry.COLUMN_EVENT_NAME + "=?",
                    new String[]{"zercher squats"},
                    null
            );
            if (cursor.moveToFirst()) {
                tester = cursor.getString(cursor.getColumnIndex(DataContract.EventType_FTSEntry.COLUMN_EVENT_NAME));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }

        if (tester.equals("zercher squats")) {
            return true;
        } else {
            return false;
        }
    }
}
