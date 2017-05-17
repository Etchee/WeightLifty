package etchee.com.weightlifty.DataMethods;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.ArrayList;

import etchee.com.weightlifty.data.DataContract;

/**
 *  Inner class to handle heavy row delete action.
 *  ArrayList will contain two Integers: date, sub_id.
 */
public class DeleteActionHelper extends AsyncTask<ArrayList, Void, Integer> {

    private Context context;
    private Activity activity;

    public DeleteActionHelper(Context context, Activity activity) {
        super();
        this.context = context;
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        activity.finish();
    }

    @Override
    protected Integer doInBackground(ArrayList... arrayList) {
        Object date = arrayList[0].get(0);
        Object sub_id = arrayList[0].get(1);

        String formattedate = String.valueOf(date);
        int SubIdAsInt = Integer.parseInt(sub_id.toString());

        String selection = DataContract.EventEntry.COLUMN_FORMATTED_DATE + "=?" + " AND "
                + DataContract.EventEntry.COLUMN_EVENT_ID + "=?";
        String selectionArgs[] = new String[]{
                formattedate,
                String.valueOf(SubIdAsInt)
        };

        int numberOfDeletedRows = context.getContentResolver().delete(
                DataContract.EventEntry.CONTENT_URI,
                selection,
                selectionArgs
        );

        return numberOfDeletedRows;
    }

    @Override
    protected void onPostExecute(Integer numOfDeletedRows) {
        Toast.makeText(context, String.valueOf(numOfDeletedRows) + " deleted.", Toast.LENGTH_SHORT).show();
    }
}
