package etchee.com.weightlifty.data;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import etchee.com.weightlifty.data.DataContract;

/**
 *  Class to handle heavy row update action when modifying an event.
 */
public class ModifyEventHelper extends AsyncTask<ContentValues, Void, Integer> {

    private Context context;
    private Activity activity;

    private int set_count, rep_count, weight_count;
    private String workout_name;

    private int date, sub_id;

    public ModifyEventHelper(Context context, Activity activity, int date, int sub_id) {
        super();
        this.context = context;
        this.activity = activity;
        this.date = date;
        this.sub_id = sub_id;
    }

    @Override
    protected Integer doInBackground(ContentValues... values) {
        int numberOfRowsUpdated;

        String selection = DataContract.EventEntry.COLUMN_DATE + "=?" + " AND " + DataContract.EventEntry.COLUMN_SUB_ID + "=?";
        String selectionArgs[] = new String[] {
                String.valueOf(date),
                String.valueOf(sub_id)
        };

        numberOfRowsUpdated = context.getContentResolver().update(
                DataContract.EventEntry.CONTENT_URI,
                values[0],
                selection,
                selectionArgs
        );

        return numberOfRowsUpdated;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (integer > 0) Toast.makeText(context, "Event Updated!", Toast.LENGTH_SHORT).show();
        else Toast.makeText(context, "Event update failed.", Toast.LENGTH_SHORT).show();
        activity.finish();
    }
}