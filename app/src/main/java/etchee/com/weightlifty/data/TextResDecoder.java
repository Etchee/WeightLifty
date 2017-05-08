package etchee.com.weightlifty.data;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import etchee.com.weightlifty.data.DataContract.EventType_FTSEntry;
import etchee.com.weightlifty.R;

/**
 *  This class is called in the MainActivity's option menu
 *
 *  Function 1:
 */

public class TextResDecoder {

    private Context context;
    private final String TAG = getClass().getSimpleName();
    private Activity activity;

    public TextResDecoder(Context context, Activity activity) throws FileNotFoundException {
        this.context = context;
        this.activity = activity;
    }

    public void main() {
        if (!checkIfEventTypeIsOk()) {
            Log.v(TAG, "Optimizing db process starts:");
            //fire AsyncTask here
            new AsyncEventTypleInsertProcess(context).execute();
        } else Toast.makeText(activity, "EventType data optimized!", Toast.LENGTH_SHORT).show();
    }

    /**
     *  This method checks if the raw workout file has already been input in the FTS table.
     *
     *  Checking method: get the 40th item (randomly selected... and if the name exists, then it's a pass.)
     *
     * @return if the database is okay, true. If not okay, false.
     */
    private boolean checkIfEventTypeIsOk () {
        Boolean TF = null;
        Cursor cursor = null;
        try {
            String eventName;

            String projection[] = new String[]{
                    DataContract.EventType_FTSEntry.COLUMN_ROW_ID,
                    DataContract.EventType_FTSEntry.COLUMN_EVENT_NAME
            };
            String selection = DataContract.EventType_FTSEntry.COLUMN_ROW_ID + "=?";
            String selectionArgs[] = new String[]{ String.valueOf(40) };

            cursor = context.getContentResolver().query(
                    DataContract.EventType_FTSEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
            );
            int index = cursor.getColumnIndex(DataContract.EventType_FTSEntry.COLUMN_EVENT_NAME);

            if (cursor.moveToFirst()){
                eventName = cursor.getString(index);
                if (!eventName.equals("")) TF = true;
                else TF = false;
            } else{
                TF = false;
                Log.e(TAG, "Check method cursor has returned null");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }

        return TF;
    }
}

class AsyncEventTypleInsertProcess extends AsyncTask<Void, Void, Void> {

    private String TAG = getClass().getSimpleName();
    private Context context;

    public AsyncEventTypleInsertProcess(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        //init JSON object
        JSONObject jsonObject = null;
        JSONArray jsonArray = null;
        try {
            jsonArray = convertBufferIntoJSONArray(getStringBufferFromRawFile());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //now insert the contents in jsonArray into the FTS table
        int errorFlag = 0;
        Uri uri;
        ContentValues values = new ContentValues();
        for (int i = 0; i<jsonArray.length(); i+=2) {
            //if even, then Event Name
            //if odd, then Event Type
            try {
                values.put(EventType_FTSEntry.COLUMN_EVENT_NAME, jsonArray.get(i).toString());
                values.put(EventType_FTSEntry.COLUMN_EVENT_TYPE, jsonArray.get(i + 1).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            uri = context.getContentResolver().insert(
                    EventType_FTSEntry.CONTENT_URI,
                    values
            );
            if (uri == null){
                errorFlag = 1;
                break;
            }
            values.clear();
        }

        if (errorFlag == 1) {
            throw new SQLException(TAG + ": Data insertion has failed. Check the Async method.");
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    private StringBuffer getStringBufferFromRawFile() {
        String str;
        StringBuffer buffer = new StringBuffer();
        InputStream is = context.getResources().openRawResource(R.raw.list_short_workout);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            if (is != null) {
                while ((str = reader.readLine()) != null) {
                    buffer.append(str + "\n" );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { is.close(); } catch (Throwable ignore) {}
        }
        return buffer;
    }

    private JSONArray convertBufferIntoJSONArray(StringBuffer buffer) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        String str = buffer.toString();

        String[] array = str.split(",");
        for (int i = 0; i<array.length; i++){
            //in index i, put the processed version of the array
            jsonArray.put(i, array[i].replace("\\", "").trim());
            //successful up to here!
        }

        return jsonArray;
    }

    /**
     *  This method converts the array thrown in → JSON object
     * @param array
     * @return
     */
    private JSONObject convertArrayToJSON(JSONArray array) {
        JSONObject jsonObject = new JSONObject();

        //if even number starting from 0 → workout name
        //if odd number starting from 1 → category.

        return jsonObject;
    }

}