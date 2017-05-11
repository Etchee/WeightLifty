package etchee.com.weightlifty.DataMethods;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import etchee.com.weightlifty.data.DataContract;
import etchee.com.weightlifty.data.DataDbHelper;
import etchee.com.weightlifty.data.DataContract.EventType_FTSEntry;

import static android.R.id.input;
import static android.R.id.inputExtractEditText;

/** When launching ModifyEventActivity with a new event, event ID query fails sometimes.
 * Might be due to querying on the main thread right after a new Activity launches...?
 * So just put the full-text query in here.
 * Created by rikutoechigoya on 2017/05/10.
 */

public class QueryEventIDFromName extends AsyncTask<String, Void, Integer> {

    private String inputString;
    private Context context;
    private Activity activity;
    private final String TAG = getClass().getSimpleName();
    public QueryResponceHandler queryResponceHandler = null;
    private SQLiteDatabase db;

    public QueryEventIDFromName(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    @Override
    protected Integer doInBackground(String... eventName) {
        //event name gets thrown in
        inputString = eventName[0];
        //do rawQuery because I need to specify rowid column
        int id = -1;
        Cursor cursor = null;
        try {
            db = new DataDbHelper(context).getWritableDatabase();
            String query = "SELECT docid as _id" + "," +
                    EventType_FTSEntry.COLUMN_EVENT_NAME + "," +
                    EventType_FTSEntry.COLUMN_EVENT_TYPE +
                    " FROM " + EventType_FTSEntry.TABLE_NAME +
                    " WHERE " +  EventType_FTSEntry.COLUMN_EVENT_NAME + " MATCH '" + inputString + "';";
            Log.v(TAG, query);
            cursor = db.rawQuery(query, null);
            Log.v(TAG, DatabaseUtils.dumpCursorToString(cursor));
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex("_id");
                id = cursor.getInt(index);
                Log.v(TAG , DatabaseUtils.dumpCursorToString(cursor));
            } else throw new CursorIndexOutOfBoundsException("query Event ID failed.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }

        return id;
    }

    @Override
    protected void onPostExecute(Integer id) {
        queryResponceHandler.EventIDHolder(String.valueOf(id));
    }
}
