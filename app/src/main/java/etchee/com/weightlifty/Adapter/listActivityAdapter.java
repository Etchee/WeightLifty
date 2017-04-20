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
import android.widget.Toast;

import etchee.com.weightlifty.R;
import etchee.com.weightlifty.data.DataContract;
import etchee.com.weightlifty.data.EventNameQueryHelper;
import etchee.com.weightlifty.data.QueryResponceHandler;

/**
 * Created by rikutoechigoya on 2017/03/27.
 */

public class listActivityAdapter extends CursorAdapter implements QueryResponceHandler{

    private TextView field_workout_name, field_repCount, field_setCount;
    private String eventString;
    private EventNameQueryHelper eventNameQueryHelper;

    public listActivityAdapter(Context context, Cursor c, int flags) {
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

        int index;
        int repCount_columnIndex = cursor.getColumnIndex(DataContract.EventEntry.COLUMN_REP_COUNT);
        int setCount_columnIndex = cursor.getColumnIndex(DataContract.EventEntry.COLUMN_SET_COUNT);
        int eventID = cursor.getInt(cursor.getColumnIndex(DataContract.EventEntry.COLUMN_EVENT_ID));

        String rep_count = cursor.getString(repCount_columnIndex);
        String set_count = cursor.getString(setCount_columnIndex);

        String projection[] = new String[]{
                DataContract.EventTypeEntry._ID,
                DataContract.EventTypeEntry.COLUMN_EVENT_NAME
        };

        String selection = DataContract.EventTypeEntry._ID + "=?";
        String selectionArgs[] = new String[]{ String.valueOf(eventID) };

        Cursor eventStringCursor = context.getContentResolver().query(
                DataContract.EventTypeEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );


        try {
            if (eventStringCursor.moveToFirst()) {
                index = eventStringCursor.getColumnIndex(DataContract.EventTypeEntry.COLUMN_EVENT_NAME);
                Log.v("CURSOR", DatabaseUtils.dumpCursorToString(eventStringCursor));
                eventString = eventStringCursor.getString(index);
            } else Toast.makeText(context, "EventString query failed", Toast.LENGTH_SHORT).show();
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
