package etchee.com.weightlifty.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import etchee.com.weightlifty.R;
import etchee.com.weightlifty.data.DataContract.EventType_FTSEntry;

/** This adapter takes the query from searchView, performs search and then display the result as
 * a listView.
 */

public class SearchAdapter extends CursorAdapter {

    private TextView field_workout_name;
    private Context context;
    private final String TAG = getClass().getSimpleName();

    public SearchAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        this.context = context;
        Log.v(TAG, "Adapter cursor: " + DatabaseUtils.dumpCursorToString(cursor));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context)
                .inflate(R.layout.item_single_workout_events, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        field_workout_name = (TextView)view.findViewById(R.id.name_workout_item_single_workout_events);


        int workoutName_columnIndex = cursor.getColumnIndex(EventType_FTSEntry.COLUMN_EVENT_NAME);

        String workout = cursor.getString(workoutName_columnIndex);

        field_workout_name.setText(workout);
    }
}