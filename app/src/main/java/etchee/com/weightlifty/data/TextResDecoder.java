package etchee.com.weightlifty.data;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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
            Log.v(TAG, "Optimizing db process initiated.");
            //fire AsyncTask here
            new AsyncEventTypleInsertProcess(context, activity).execute();
    }

}

class AsyncEventTypleInsertProcess extends AsyncTask<Void, Integer, Integer> {

    private String TAG = getClass().getSimpleName();
    private Context context;
    private Activity activity;

    private TextView number_textView, hint_text, hint_percent;
    private Button begin_workout;

    public AsyncEventTypleInsertProcess(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        number_textView = (TextView) activity.findViewById(R.id.hint_number_update);
        hint_text = (TextView)activity.findViewById(R.id.hint_update_text);
        hint_percent = (TextView)activity.findViewById(R.id.hint_text_update_percent);
        begin_workout = (Button)activity.findViewById(R.id.begin_workout_button);
    }

    @Override
    protected Integer doInBackground(Void... params) {
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
        int numOfUpdatedRow = 0;
        Uri uri;
        ContentValues values = new ContentValues();
        int counter = 0;
        while(counter < (jsonArray != null ? jsonArray.length() : 0)){
            //if even, then Event Name
            //if odd, then Event Type
            try {
                values.put(EventType_FTSEntry.COLUMN_EVENT_NAME, jsonArray.get(counter).toString());
                values.put(EventType_FTSEntry.COLUMN_EVENT_TYPE, jsonArray.get(counter + 1).toString());
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
            numOfUpdatedRow++;
            counter += 2;
            if (numOfUpdatedRow % 9 == 0) publishProgress(numOfUpdatedRow / 9);
        }

        if (errorFlag == 1) {
            throw new SQLException(TAG + ": Data insertion has failed. Check the Async method.");
        }

        return numOfUpdatedRow;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        number_textView.setText(String.valueOf(values[0]));
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        Toast.makeText(context, "Workout database ready with 900 workouts!",
                Toast.LENGTH_SHORT).show();

        //Hint texts can all go away now
        number_textView.setVisibility(View.GONE);
        hint_text.setVisibility(View.GONE);
        hint_percent.setVisibility(View.GONE);

        //and start workout button now should be enabled
        begin_workout.setEnabled(true);
    }

    private String queryFTSforDebug() {
        Cursor cursor = null;
        String str = null;
        try {

            String projection[] = new String[] {
                    EventType_FTSEntry.COLUMN_ROW_ID,
                    EventType_FTSEntry.COLUMN_EVENT_NAME
            };

            String selection = EventType_FTSEntry.COLUMN_ROW_ID + "=?";
            String selectionArgs[] = new String[]{
                String.valueOf(259)
            };
              cursor = context.getContentResolver().query(
                    EventType_FTSEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
            );
            int index = cursor.getColumnIndex(EventType_FTSEntry.COLUMN_EVENT_NAME);
            if (cursor.moveToFirst()) str = cursor.getString(index);
        } finally {
            cursor.close();
        }

        return str;
    }

    private StringBuffer getStringBufferFromRawFile() {
        String str;
        StringBuffer buffer = new StringBuffer();
        InputStream is = context.getResources().openRawResource(R.raw.raw_list_workout);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            if (is != null) {
                while ((str = reader.readLine()) != null) {
                      buffer.append(str);
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
        str = str.replaceAll("\\\\", ",");

        String[] array = str.split(",");
        for (int k = 0; k<array.length; k++) {
            System.out.println(array[k]);
        }
        for (int i = 0; i<array.length; i++){
            //in index i, put the processed version of the array
            jsonArray.put(i, array[i].trim());
        }

        return jsonArray;
    }
}