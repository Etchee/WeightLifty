package etchee.com.weightlifty.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import etchee.com.weightlifty.R;
import etchee.com.weightlifty.data.DataContract.EventType_FTSEntry;

/**
 * Created by rikutoechigoya on 2017/05/06.
 */

public class SearchWorkoutAdapter extends CursorAdapter {

    private TextView field_workout_name;

    public SearchWorkoutAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context)
                .inflate(R.layout.item_single_workout_events, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        field_workout_name = (TextView)view.findViewById(R.id.name_workout_item_single_workout_events);

        int rowId_columnIndex = cursor.getColumnIndex(EventType_FTSEntry.COLUMN_ROW_ID);
        int workoutName_columnIndex = cursor.getColumnIndex(EventType_FTSEntry.COLUMN_EVENT_NAME);
        int workoutType_columnIndex = cursor.getColumnIndex(EventType_FTSEntry.COLUMN_EVENT_TYPE);

        String workout = cursor.getString(workoutName_columnIndex);
        String type = cursor.getString(workoutType_columnIndex);
        int rowId = cursor.getInt(rowId_columnIndex);
    }
}