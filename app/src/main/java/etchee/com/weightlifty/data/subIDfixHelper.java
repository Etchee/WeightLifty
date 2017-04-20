package etchee.com.weightlifty.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.Toast;

import etchee.com.weightlifty.data.DataContract;

/**
 *  Background operation to fix wrong sub_id order upon deleting rows.
 *
 *  Get the first and last _ID, from rows of the specified date.
 *
 *  Last-first + 1 = actual number of events.
 *
 *  for(i = 0; i < actual number of events; i++) update sub_id
 */
public class subIDfixHelper extends AsyncTask<Integer, Void, Integer> {

    private Context context;
    private int subId;
    private int startID, endID;

    public subIDfixHelper(Context context) {
        this.context = context;
    }

    @Override
    protected Integer doInBackground(Integer... dateSelection) {
        //get the date
        int date = dateSelection[0];

        //set up the query to get the number of rows. Any column is okay

        String projection[] = new String[]{
                DataContract.EventEntry._ID,
                DataContract.EventEntry.COLUMN_DATE
        };

        String selection = DataContract.EventEntry.COLUMN_DATE + "=?";
        String selectionArgs[] = new String[]{ String.valueOf(date) };
        Cursor cursor = null;
        int numOfRowsToUpdate;
        int columnIndex_ID;
        try {
            cursor = context.getContentResolver().query(
                    DataContract.EventEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
            );
            numOfRowsToUpdate = cursor.getCount();
            columnIndex_ID = cursor.getColumnIndex(DataContract.EventEntry._ID);
            if (cursor.moveToFirst()) {
                startID = cursor.getInt(columnIndex_ID);
            }

            if (cursor.moveToLast()) {
                endID = cursor.getInt(columnIndex_ID);
            }

        } finally {
            cursor.close();
        }

        //necessary items for update method
        ContentValues values = new ContentValues();
        String selection_update = DataContract.EventEntry._ID + "=?";
        //Sum of rows updated
        int iteratedRowCount = 0;
        //current row updated? Or not
        int currentRowUpdate;

        // For "the number of items" times,
        while (startID <= endID){
            values.put(DataContract.EventEntry.COLUMN_SUB_ID, iteratedRowCount);
            currentRowUpdate = context.getContentResolver().update(
                    DataContract.EventEntry.CONTENT_URI,
                    values,
                    selection_update,
                    new String[]{String.valueOf(startID)}
            );
            //current row (_ID) is there,
            if (currentRowUpdate > 0) {
                iteratedRowCount++;
            }
            //current row (_ID) ISN'T there, BUT THE SUB_ID AT NEXT _ID NEEDS TO BE iteratedRowCount++!!!
            if (currentRowUpdate == 0) {

            }
            startID++;
            values.clear();
        }
        //because while loop
        return iteratedRowCount;
    }

    @Override
    protected void onPostExecute(Integer integer) {
    }
}
