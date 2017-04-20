package etchee.com.weightlifty.data;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import static etchee.com.weightlifty.data.DBviewer.indexInfo.index;

/**
 * Created by rikutoechigoya on 2017/04/20.
 */

public class EventNameQueryHelper extends AsyncTask<Integer, Void, String> {

    private Context context;
    private String eventString;

    public QueryResponceHandler delegate = null;

    public EventNameQueryHelper(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(Integer... query_eventID) {
        int eventID = query_eventID[0];

        String projection[] = new String[]{
                DataContract.EventTypeEntry._ID,
                DataContract.EventTypeEntry.COLUMN_EVENT_NAME
        };
        Cursor cursor = null;

        String selection = DataContract.EventTypeEntry._ID + "=?";
        try {
            String selectionArgs[] = new String[]{ String.valueOf(eventID) };
            cursor = context.getContentResolver().query(
                    DataContract.EventTypeEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
            );

            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(DataContract.EventTypeEntry.COLUMN_EVENT_NAME);
                eventString = cursor.getString(index);
            } else {
                Log.e("EventNameQUery", "Query failed.");
            }
        } finally {
                cursor.close();
        }


        return eventString;
    }

    @Override
    protected void onPostExecute(String eventString) {
        super.onPostExecute(eventString);
        delegate.EventNameHolder(eventString);
    }
}
