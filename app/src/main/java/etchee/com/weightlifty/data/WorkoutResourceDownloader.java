package etchee.com.weightlifty.data;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import etchee.com.weightlifty.R;

/**
 * Created by rikutoechigoya on 2017/05/07.
 */

public class WorkoutResourceDownloader extends AsyncTask {

    private Context context;
    private final String TAG = getClass().getSimpleName();

    public WorkoutResourceDownloader(Context context, Activity activity) throws FileNotFoundException {
        this.context = context;


    }

    private StringBuffer getStringBufferFromRawFile() {
        String str = "";
        StringBuffer buffer = new StringBuffer();
        InputStream is = context.getResources().openRawResource(R.raw.raw_list_workout);
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

    private JSONObject convertBufferIntoJSON(StringBuffer buffer) {
        
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
            String eventName = cursor.getString(index);

            if (eventName != null) TF = true;
            else TF = false;

        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException(TAG + ": EventType checking has failed. Check the method.");
        } finally {
            cursor.close();
        }

        return TF;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        return null;
    }
}
