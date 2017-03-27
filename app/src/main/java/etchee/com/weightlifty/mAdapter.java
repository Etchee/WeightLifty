package etchee.com.weightlifty;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

/**
 * Created by rikutoechigoya on 2017/03/27.
 */

public class mAdapter extends CursorAdapter {

    public mAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {

        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }
}
