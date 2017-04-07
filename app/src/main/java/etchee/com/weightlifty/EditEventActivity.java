package etchee.com.weightlifty;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.NumberPicker;

import etchee.com.weightlifty.data.DataContract;

/**
 * Created by rikutoechigoya on 2017/04/07.
 *
 *  From ListActivity, when user taps on one of the events, this class is called to load the
 *  specific event.
 */

public class EditEventActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor>, NumberPicker.OnValueChangeListener {

    private NumberPicker numberPicker_set;
    private NumberPicker numberPicker_rep;
    private static final int SET_MAXVALUE = 30;
    private static final int SET_MINVALUE = 1;
    private static final int REP_MAXVALUE = 500;
    private static final int REP_MINVALUE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);

        //number picker for set
        numberPicker_set = (NumberPicker)findViewById(R.id.set_numberPicker);
        numberPicker_set.setMaxValue(SET_MAXVALUE);
        numberPicker_set.setMinValue(SET_MINVALUE);

        numberPicker_rep = (NumberPicker)findViewById(R.id.rep_numberPicker);
        numberPicker_rep.setMaxValue(REP_MAXVALUE);
        numberPicker_rep.setMinValue(REP_MINVALUE);

        //Get selection(column) in which the loader will be loading the data from
        Bundle bundleForLoader = getIntent().getExtras();

        //init the loader
        getSupportLoaderManager().initLoader(1, bundleForLoader, this);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        String ORDER_DECENDING = " DESC";
        String ORDER_ASCENDING = " ASC";
        String selection = args.getString("selection");
        String projection[] = new String[]{DataContract.CalendarEntry.COLUMN_EVENT_IDs};
        //sort order is "column_name ASC" or "column_name DESC"
        String sortorder = DataContract.CalendarEntry.COLUMN_EVENT_IDs + ORDER_DECENDING;


        CursorLoader cursorLoader = new CursorLoader(
                getApplicationContext(),
                DataContract.CalendarEntry.CONTENT_URI,
                projection,
                selection,
                null,
                null
                );

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {

    }
}
