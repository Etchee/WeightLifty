package etchee.com.weightlifty.data;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

import etchee.com.weightlifty.R;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 *  This class is called in the MainActivity's option menu
 *
 *  Function 1:
 */

public class TextResDecoder extends AsyncTask {

    private Context context;
    private final String TAG = getClass().getSimpleName();
    private Activity activity;

    public TextResDecoder(Context context, Activity activity) throws FileNotFoundException {
        this.context = context;
        this.activity = activity;
    }

    public void main() {
        if (!checkIfEventTypeIsOk()) {
            convertBufferIntoArrayList(getStringBufferFromRawFile());
        } else Toast.makeText(activity, "EventType data optimized!", Toast.LENGTH_SHORT).show();

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

    private ArrayList<String> convertBufferIntoArrayList(StringBuffer buffer) {
        ArrayList<String> list = new ArrayList<>();
        String str = buffer.toString();
        Log.v(TAG, str);
        String[] array = str.split(",");
        for (int i = 0; i<array.length; i++){
            System.out.println(array[i]);
        }
        return list;
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

    @Override
    protected Object doInBackground(Object[] params) {
        return null;
    }
}
