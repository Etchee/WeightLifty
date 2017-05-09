package etchee.com.weightlifty.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import etchee.com.weightlifty.R;
import etchee.com.weightlifty.data.DataContract;
import etchee.com.weightlifty.data.DataDbHelper;

/** This adapter takes the query from searchView, performs search and then display the result as
 * a listView.
 */

public class SearchAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private Cursor cursor;
    private String workout;
    private String number_String;
    private String hint_string;
    private Context context;
    private final String TAG = getClass().getSimpleName();
    private int index_name, index_hint;

    public SearchAdapter(Context context, Cursor cursor){
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.cursor = cursor;
        this.context = context;
        // get the workout name String

        index_name = cursor.getColumnIndex(DataContract.EventType_FTSEntry.COLUMN_EVENT_NAME);
        index_hint = cursor.getColumnIndex(DataContract.EventType_FTSEntry.COLUMN_EVENT_TYPE);
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public Object getItem(int position) {
        int rowid = -1;

        //rawQuery must be used because FTS table row_id column is "hidden"

        try {
            Cursor cursor;
            SQLiteDatabase db = new DataDbHelper(context).getReadableDatabase();
            String query = "SELECT " + DataContract.EventType_FTSEntry.COLUMN_ROW_ID + ", "
                    + DataContract.EventType_FTSEntry.COLUMN_EVENT_NAME + " FROM "
                    + DataContract.EventType_FTSEntry.TABLE_NAME + " WHERE "
                    + DataContract.EventType_FTSEntry.COLUMN_ROW_ID + "=?";
            String selectionArgs[] = new String[]{String.valueOf(position)};
            cursor = db.rawQuery(query, selectionArgs);
            Log.v(TAG, DatabaseUtils.dumpCursorToString(cursor));

            int index = cursor.getColumnIndex(DataContract.EventType_FTSEntry.COLUMN_ROW_ID);
            if (cursor.moveToFirst()) {
                rowid = cursor.getInt(index);
            }
        } finally {
            if (cursor != null)  cursor.close();
        }

        return rowid ;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;

        // no view at this position so create a new one.
        if (convertView == null) {
            //get the layout information
            convertView = layoutInflater.inflate(R.layout.item_single_search, null);
            holder = new ViewHolder();

            //assign component IDs
            holder.workout_textView = (TextView) convertView.findViewById(R.id.searchview_workout_text);
            holder.hint_textView = (TextView)convertView.findViewById(R.id.searchview_hint_text);
            holder.number_textView = (TextView)convertView.findViewById(R.id.searchview_number);


            //setTag for reuse
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        //        int index_number = cursor.getColumnIndex("rowid");

        //open the passed cursor from the constructor
        if (cursor.moveToPosition(position)) {
            workout = cursor.getString(index_name);
            hint_string = cursor.getString(index_hint);
//            number_String = String.valueOf(cursor.getInt(index_number));
        }

        //set the item name on the TextView.
        //prevent NullPointer on fast scrolling?
        if (holder.workout_textView != null) {

            holder.workout_textView.setText(workout);
            holder.hint_textView.setText(hint_string);
//                holder.number_textView.setText(number_String);
        }

        return convertView;

    }

    public void swapCursor(Cursor newCursor) {
        cursor = newCursor;
        notifyDataSetChanged();
    }


    private static class ViewHolder {

        private TextView workout_textView;
        private TextView hint_textView;
        private TextView number_textView;
    }
}