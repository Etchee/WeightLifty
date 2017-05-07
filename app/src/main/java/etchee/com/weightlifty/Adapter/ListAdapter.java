package etchee.com.weightlifty.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import etchee.com.weightlifty.R;
import etchee.com.weightlifty.data.DataContract;
import etchee.com.weightlifty.DataMethods.EventNameQueryHelper;
import etchee.com.weightlifty.DataMethods.QueryResponceHandler;

/**
 *  This adapter is set on the listView when SearchView is being opened.
 *
 *  Function 1: takes the query in the searchView
 *  Function 2: performs search in the FTS_EventType table.
 *  Function 3: Search returns a cursor
 *  Function 4: cursor contents are converted into a single listView
 */

public class ListAdapter extends CursorAdapter implements QueryResponceHandler{

    private TextView field_workout_name, field_repCount, field_setCount;
    private String eventString;
    private EventNameQueryHelper eventNameQueryHelper;
    private static final String TAG = "ListActivityAdapter";

    public ListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        eventNameQueryHelper = new EventNameQueryHelper(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {

        return LayoutInflater.from(context).inflate(R.layout.item_single_listview, viewGroup, false);
    }



    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        field_workout_name = (TextView)view.findViewById(R.id.name_workout);
        field_repCount = (TextView)view.findViewById(R.id.item_count_rep);
        field_setCount = (TextView)view.findViewById(R.id.item_count_set);

        //Get cursor indexes
        int index;
        int repCount_columnIndex = cursor.getColumnIndex(DataContract.EventEntry.COLUMN_REP_COUNT);
        int setCount_columnIndex = cursor.getColumnIndex(DataContract.EventEntry.COLUMN_SET_COUNT);
        int eventID = cursor.getInt(cursor.getColumnIndex(DataContract.EventEntry.COLUMN_EVENT_ID));
        //get resource from the cursor
        String rep_count = cursor.getString(repCount_columnIndex);
        String set_count = cursor.getString(setCount_columnIndex);

        // Query the appropriate event name
        String projection[] = new String[]{
                DataContract.EventType_FTSEntry.COLUMN_ROW_ID,
                DataContract.EventType_FTSEntry.COLUMN_EVENT_TYPE,
                DataContract.EventType_FTSEntry.COLUMN_EVENT_NAME
        };

        String selection = DataContract.EventType_FTSEntry.COLUMN_ROW_ID + "=?";
        String selectionArgs[] = new String[]{ String.valueOf(eventID) };

        Cursor eventStringCursor = context.getContentResolver().query(
                DataContract.EventType_FTSEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );


        try {
            if (eventStringCursor.moveToFirst()) {
                index = eventStringCursor.getColumnIndex(DataContract.EventType_FTSEntry.COLUMN_EVENT_NAME);
                eventString = eventStringCursor.getString(index);
            } else {
                Log.e(TAG, "Event String query failed");
            }
        } finally {
                eventStringCursor.close();
        }


        field_workout_name.setText(eventString);
        field_repCount.setText(rep_count);
        field_setCount.setText(set_count);
    }

    /**
     *  This method gets the string from the AsyncTask, through interface class
     * @param eventString from onPostExecute method of the AsyncTask
     */
    @Override
    public void EventNameHolder(String eventString) {
        this.eventString = eventString;
    }
}
