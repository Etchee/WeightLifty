package etchee.com.weightlifty;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.NumberPicker;
import android.widget.Toast;
import etchee.com.weightlifty.data.DataContract.EventEntry;

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

    private static final String ORDER_DECENDING = " DESC";
    private static final String ORDER_ASCENDING = " ASC";

    private static final int LOADER_CREATE_NEW_EVENT_MODE = 0;
    private static final int LOADER_MODIFY_EVENT_MODE = 1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);

        //Number pickers settings
        numberPicker_set = (NumberPicker)findViewById(R.id.set_numberPicker);
        numberPicker_set.setMaxValue(SET_MAXVALUE);
        numberPicker_set.setMinValue(SET_MINVALUE);

        numberPicker_rep = (NumberPicker)findViewById(R.id.rep_numberPicker);
        numberPicker_rep.setMaxValue(REP_MAXVALUE);
        numberPicker_rep.setMinValue(REP_MINVALUE);

        /**
         *  When this activity is opened, two modes:
         *  1. From ListActivity, tapping on already existing item.
         *      → Modify event, bundle w/ selection. Select that in the event table, query, display.
         *
         *  2. From ChooseEventActivity, selecting which workout.
         *      → Create event, bundle w/ contentValues.
         */
        Bundle bundle = getIntent().getExtras();

        // Case 1: creating a new event → bundle with contentValues.
        if (bundle.get(DataContract.GlobalConstants.CONTENT_VALUES)!= null) {

            getSupportLoaderManager().initLoader(LOADER_CREATE_NEW_EVENT_MODE, bundle, this);

            Toast.makeText(this, "Create new event mode", Toast.LENGTH_SHORT).show();
        }
        // Case 2: modifying an already existing event → bundle with selection.
        else if (bundle.get(DataContract.GlobalConstants.SUB_ID) != null) {
            //init the loader
            Toast.makeText(this, "Modify event mode", Toast.LENGTH_SHORT).show();
            getSupportLoaderManager().initLoader(LOADER_MODIFY_EVENT_MODE, bundle, this);
        } else {
            throw new IllegalArgumentException("EditEventActivity did not receive bundle of" +
                    "selection columns nor contentValues to make a new event");
        }

    }

    /**
     *
     *     If modifying data, fire up loader for background thread data loading.
     *
     * @param id    onCreate sets this to 1
     * @param bundle  bundle received from the UI thread. Open this magic box to get specific item
     * @return      passes cursor to onLoadFinished
     */
    @Override
    public Loader onCreateLoader(int id, Bundle bundle) {

        CursorLoader cursorLoader = null;

         /*
        *   Quoted contentValue making statement from the ChooseEventActivity.class
        *
        int set_count = 5;
        int sub_ID = 0;
        int id = 0;
        int eventID = 2;

        squatValues.put(EventEntry._ID, id);
        squatValues.put(EventEntry.COLUMN_SUB_ID, sub_ID);
        squatValues.put(EventEntry.COLUMN_EVENT_ID, eventID);
        squatValues.put(EventEntry.COLUMN_SET_COUNT, set_count);
        * */

        //when creating a new event,
        /*
        * ContentValues values = (ContentValues) bundle.get("values");
            //how many sets
            int set_count = values.getAsInteger(DataContract.EventEntry.COLUMN_SET_COUNT);
            //同じ日付IDで、何番目のアイテム？
            int sub_id = values.getAsInteger(DataContract.EventEntry.COLUMN_SUB_ID);
            //イベントのID
            int event_id = values.getAsInteger(DataContract.EventEntry.COLUMN_EVENT_ID);
            */
        if (id == LOADER_CREATE_NEW_EVENT_MODE) {
            //get all the variables from the contentValues, then display on the fields

        }

        else if (id == LOADER_MODIFY_EVENT_MODE) {

            /*  Example selection selectionArgs

            Cursor cursor = mDb.query(DATABASE_TABLE,
            new String [] {KEY_DATE, KEY_REPS, KEY_REPS_FEEL, KEY_WEIGHT, KEY_WEIGHT_FEEL}, "KEY_WORKOUT = ? AND KEY_EXERCISE = ?",
            new String[] { workout, exercise },
            null,
            null,
            KEY_DATE);

            * */

            //this would be the selection number
            int rowID = bundle.getInt(DataContract.GlobalConstants.SUB_ID);
            String projection[] = new String[]{
                    EventEntry.COLUMN_EVENT_ID,
                    EventEntry.COLUMN_SET_COUNT,
                    EventEntry.COLUMN_REP_SEQUENCE,
                    EventEntry.COLUMN_WEIGHT_SEQUENCE
            };
            //sort order is "column_name ASC" or "column_name DESC"
            String sortorder = DataContract.CalendarEntry.COLUMN_EVENT_IDs + ORDER_DECENDING;


            cursorLoader = new CursorLoader(
                    getApplicationContext(),
                    DataContract.EventEntry.CONTENT_URI,
                    projection,
                    String.valueOf(rowID),
                    null,
                    null
            );
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        /*
        EventEntry.COLUMN_EVENT_ID,
                    EventEntry.COLUMN_SET_COUNT,
                    EventEntry.COLUMN_REP_SEQUENCE,
                    EventEntry.COLUMN_WEIGHT_SEQUENCE
        * */

        if (cursor.moveToFirst()) {
            int setCountIndex = cursor.getColumnIndex(EventEntry.COLUMN_SET_COUNT);
            int repSequenceIndex = cursor.getColumnIndex(EventEntry.COLUMN_REP_SEQUENCE);
            int weightSequenceIndex = cursor.getColumnIndex(EventEntry.COLUMN_WEIGHT_SEQUENCE);

            int setCount = cursor.getInt(setCountIndex);
            String repSequence = cursor.getString(repSequenceIndex);
            String weightSequence = cursor.getString(weightSequenceIndex);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {

    }
}
