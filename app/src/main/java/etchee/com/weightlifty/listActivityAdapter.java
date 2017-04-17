package etchee.com.weightlifty;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import etchee.com.weightlifty.data.DataContract;

/**
 * Created by rikutoechigoya on 2017/03/27.
 */

public class listActivityAdapter extends CursorAdapter {

    private TextView workout_name, repCount, setCount;

    public listActivityAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {

        return LayoutInflater.from(context).inflate(R.layout.item_single_listview, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        workout_name = (TextView)view.findViewById(R.id.name_workout);
        repCount = (TextView)view.findViewById(R.id.item_count_rep);
        setCount = (TextView)view.findViewById(R.id.item_count_set);

        //TODO: Do the steps below to get workout name shown in the listView
        //To get the workout name --
        //first, get the event number the cursor is pointing to.
        //　→
        // 1. get the current _ID from the EventTable. (i.g. _ID = 0)
        // 2. at 0th row in the Calendar table, get the event column.
        // 3. parse the field into an array → ([0, 3, 5, 9])
        // 4. get the sub_ID from the ORIGINAL cursor, then get array[sub_ID]. This will be the index
        // for the event Type table.
        // 5. getContentResolver.query(EventType, where _ID = index). = answer String. SetText.

//        int workoutName_columnIndex = cursor.getColumnIndex(DataContract.EventEntry.COLUMN_SUB_ID);

        int repCount_columnIndex = cursor.getColumnIndex(DataContract.EventEntry.COLUMN_REP_COUNT);
        int setCount_columnIndex = cursor.getColumnIndex(DataContract.EventEntry.COLUMN_SET_COUNT);

        String repSequnce = cursor.getString(repCount_columnIndex);
        String set = cursor.getString(setCount_columnIndex);
//        String workoutName = cursor.getString(workoutName_columnIndex);

//        workout_name.setText(workoutName);
        repCount.setText(repSequnce);
        setCount.setText(set);
    }
}
