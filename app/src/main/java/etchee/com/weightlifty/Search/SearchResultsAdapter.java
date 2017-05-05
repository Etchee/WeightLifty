package etchee.com.weightlifty.Search;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import etchee.com.weightlifty.R;
import etchee.com.weightlifty.data.DataContract;

/**
 * Created by rikutoechigoya on 2017/04/22.
 */

public class SearchResultsAdapter extends CursorAdapter {

    private TextView workout_name, hint_text;

    public SearchResultsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_single_searchview, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String eventString = null;
        workout_name = (TextView) view.findViewById(R.id.searchview_event_name);
        hint_text = (TextView)view.findViewById(R.id.searchview_hint_text);

        try {
            int index = cursor.getColumnIndex(DataContract.EventType_FTSEntry.COLUMN_EVENT_NAME);
            if (cursor.moveToFirst()){
                eventString = cursor.getString(index);
            } else if (!cursor.moveToFirst()){
                eventString = null;
                Log.e("Search", "Event name query failed at cursor size of: " +
                        String.valueOf(cursor.getCount()));
            }
        } finally {
            cursor.close();
        }
        workout_name.setText(eventString);
    }
}