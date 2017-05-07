package etchee.com.weightlifty.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import etchee.com.weightlifty.R;
import etchee.com.weightlifty.data.DataContract;

/** This adapter takes the query from searchView, performs search and then display the result as
 * a listView.
 */

public class SearchAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private Cursor cursor;
    private String workout;

    public SearchAdapter(Context context, Cursor cursor){
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.cursor = cursor;
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if (view == null) {
            holder = new ViewHolder();

            view = layoutInflater.inflate(R.layout.item_single_workout_events, null);
            holder.workout_textView = (TextView) view.findViewById(R.id.name_workout_item_single_workout_events);
            view.setTag(holder);
        } else {
            holder = (ViewHolder)view.getTag();
        }

        // get the workout name String

        int index = cursor.getColumnIndex(DataContract.EventType_FTSEntry.COLUMN_EVENT_NAME);
        if (cursor.moveToNext()) {
            workout = cursor.getString(index);
        }

        if (workout != null) {
            if (holder.workout_textView != null) {
                //set the item name on the TextView
                holder.workout_textView.setText(workout);
            }
        }
        return view;

    }


    private static class ViewHolder {

        protected TextView workout_textView;
    }
}