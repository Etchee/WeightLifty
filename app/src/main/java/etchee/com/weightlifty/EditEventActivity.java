package etchee.com.weightlifty;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
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

    private String eventType;

    private int receivedEventID = -1;



    private String eventString;

    /**
     * Two modes for this activity:
     * 1. From ListActivity, tapping on already existing item.
     * → Modify event, bundle w/ selection. Select that in the event table, query, display.
     * <p>
     * 2. From ChooseEventActivity, selecting which workout.
     * → Create event, bundle w/ contentValues.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);

        numberPicker_set = (NumberPicker) findViewById(R.id.set_numberPicker);
        numberPicker_set.setMaxValue(SET_MAXVALUE);
        numberPicker_set.setMinValue(SET_MINVALUE);
        numberPicker_rep = (NumberPicker) findViewById(R.id.rep_numberPicker);
        numberPicker_rep.setMaxValue(REP_MAXVALUE);
        numberPicker_rep.setMinValue(REP_MINVALUE);
        name_workout = (TextView) findViewById(R.id.edit_workout_name);
        weight_sequence = (TextView) findViewById(R.id.input_weight_number);
        add_event = (Button) findViewById(R.id.add_event);


        Bundle bundle = getIntent().getExtras();

        // Case 1: creating a new event → bundle with contentValues.
        // use inner class to just get the title of the workout, then display.
        if (bundle.get(DataContract.GlobalConstants.CONTENT_VALUES) != null) {
            Toast.makeText(this, "Create new event mode", Toast.LENGTH_SHORT).show();

            ContentValues values = (ContentValues) bundle.get(DataContract.GlobalConstants.CONTENT_VALUES);
            int sampleValue = values.getAsInteger(DataContract.GlobalConstants.SUB_ID);
        }

        // Case 2: modifying an already existing event → bundle with selection.
        if (bundle.get(DataContract.GlobalConstants.PASS_EVENT_ID) != null) {

            setReceivedEventID(bundle.getInt(DataContract.GlobalConstants.PASS_EVENT_ID));
            if (receivedEventID < 0 ) {
                Log.e("receivedEventID", "Did not get event ID from ListActivity. Check Intent");
            }
            //init the loader
            Toast.makeText(this, "Modifying event: " + String.valueOf(receivedEventID), Toast.LENGTH_SHORT).show();
            getSupportLoaderManager().initLoader(LOADER_MODIFY_EVENT_MODE, bundle, this);

            //then show the event String
            queryEventType(getReceivedEventID());
        } else {
            throw new IllegalArgumentException("EditEventActivity did not receive bundle of" +
                    "selection columns nor contentValues to make a new event");
        }

    }

    /**
     * If modifying data, fire up loader for background thread data loading.
     *
     * @param id     onCreate sets this to 1
     * @param bundle bundle received from the UI thread. Open this magic box to get specific item
     * @return passes cursor to onLoadFinished
     */
    @Override
    public Loader onCreateLoader(int id, Bundle bundle) {

        CursorLoader cursorLoader = null;


        //WHEN CREATING A NEW EVENT
        if (id == LOADER_CREATE_NEW_EVENT_MODE) {
            //handled in onCreate... don't have to do anything here

        }

        //WHEN MODIFYING A EVENT
        if (id == LOADER_MODIFY_EVENT_MODE) {

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
     * @param loader takes the loader (with the set ID) from onCreateLoader
     * @param cursor takes the cursor created from onCreateLoader
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

            int setCount = cursor.getInt(setCountIndex);
            String repSequence = cursor.getString(repSequenceIndex);
            String weightSequence = cursor.getString(weightSequenceIndex);

            numberPicker_set.setValue(setCount);

            queryEventType(getReceivedEventID());

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

    /**
     * @param eventID: index of the item in ListView. This is equivalent to the item number in the
     *                 sub_id column in Event Table.
     *                 <p>
     *                 This method takes the sub_id, and traces to the event name String in the event table.
     */
    private void queryEventType(int eventID) {

        //define onQueryComplete
        AsyncQueryHandler queryEventType = new AsyncQueryHandler(getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {

                if (token == DataContract.GlobalConstants.QUERY_EVENT_TYPE) {

                    if (cursor.moveToFirst()) {
                        int index = cursor.getColumnIndex(DataContract.EventTypeEntry.COLUMN_EVENT_NAME);
                        setEventString(cursor.getString(index));
                    } else
                        throw new CursorIndexOutOfBoundsException("Cursor could not move to first.");
                } else
                    throw new IllegalArgumentException("Illegal token received at Event String query.");

                Toast.makeText(EditEventActivity.this, "Type Query finished", Toast.LENGTH_SHORT).show();
                name_workout.setText(getEventString());
            }
        };

        //projection
        String projection[] = new String[]{
                DataContract.EventTypeEntry._ID,
                DataContract.EventTypeEntry.COLUMN_EVENT_NAME
        };

        //select row by eventID
        String selection = DataContract.EventTypeEntry._ID + "=?";

        //eventID will be thrown in the parameter
        String selectionArgs[] = new String[]{String.valueOf(eventID)};


        //Finally, fire the query!
        queryEventType.startQuery(
                DataContract.GlobalConstants.QUERY_EVENT_TYPE,
                null,
                DataContract.EventTypeEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );
    }

    private int getReceivedEventID() {
        return receivedEventID;
    }

    private void setReceivedEventID(int receivedEventID) {
        this.receivedEventID = receivedEventID;
    }

    private String getEventString() {
        return eventString;
    }

    private void setEventString(String eventString) {
        this.eventString = eventString;
    }
}