package etchee.com.weightlifty;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
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

    //component declaration
    private NumberPicker numberPicker_set;
    private NumberPicker numberPicker_rep;
    private TextView name_workout;
    private TextView weight_sequence;
    private Button add_event;

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

        //other components assignment
        name_workout = (TextView) findViewById(R.id.name_workout);
        weight_sequence = (TextView)findViewById(R.id.input_weight_number);
        add_event = (Button)findViewById(R.id.add_event);

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
        // use inner class to just get the title of the workout, then display.
        if (bundle.get(DataContract.GlobalConstants.CONTENT_VALUES)!= null) {
            Toast.makeText(this, "Create new event mode", Toast.LENGTH_SHORT).show();

            ContentValues values = (ContentValues) bundle.get(DataContract.GlobalConstants.CONTENT_VALUES);
            int sampleValue = values.getAsInteger(EventEntry.COLUMN_EVENT_ID);

            //TODO fix onQueryComplete nullpointer
            String eventName = getEventTypeAsString(sampleValue);

            name_workout.setText(eventName);
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


        if (id == LOADER_CREATE_NEW_EVENT_MODE) {
            //handled in onCreate... don't have to do anything here

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

    /**
     *
     * @param loader    takes the loader (with the set ID) from onCreateLoader
     * @param cursor    takes the cursor created from onCreateLoader
     */
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
            int eventID = cursor.getColumnIndex(EventEntry.COLUMN_EVENT_ID);

            int setCount = cursor.getInt(setCountIndex);
            String repSequence = cursor.getString(repSequenceIndex);
            String weightSequence = cursor.getString(weightSequenceIndex);

            numberPicker_set.setValue(setCount);

            getEventTypeAsString(eventID);

            //TODO: take first item in rep sequence, assign to the textView

//            name_workout.setText();
        }
        /*
        *   components that need to be updated
        *   private NumberPicker numberPicker_set;
            private NumberPicker numberPicker_rep;
            private TextView name_workout;
            private TextView weight_sequence;
            private Button add_event;
        * */

    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {

    }

    public String getEventTypeAsString(int eventID) {

        String result = "";

        //projection takes on the two columns, because I query the ID and get the string column back
        String projection[] = new String[]{
                DataContract.EventTypeEntry._ID,
                DataContract.EventTypeEntry.COLUMN_EVENT_NAME
        };

        String selection = DataContract.EventTypeEntry._ID + "=?";

        // in the table, return row where the COLUMN_ID = eventID
        String selectionArgs[] = new String[] { String.valueOf(eventID) };

        //Async querying of the eventType table

        AsyncQueryHandler eventTypeQuery = new AsyncQueryHandler(getContentResolver()) {

            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                if (cursor == null) {
                    throw new IllegalArgumentException("Background Query returned null");
                }

                int eventNameColumnIndex =
                        cursor.getColumnIndex(DataContract.EventTypeEntry.COLUMN_EVENT_NAME);

                String testString = cursor.getString(eventNameColumnIndex);
                Toast.makeText(getApplicationContext(),
                        "Event name queried: " + testString,
                        Toast.LENGTH_SHORT).show();
            }
        };

        eventTypeQuery.startQuery(
                DataContract.GlobalConstants.QUERY_EVENT_NAME,
                null,
                DataContract.EventTypeEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );

        return result;
    }
}