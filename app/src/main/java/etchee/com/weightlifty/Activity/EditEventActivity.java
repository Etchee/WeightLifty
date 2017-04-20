package etchee.com.weightlifty.Activity;

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import etchee.com.weightlifty.R;
import etchee.com.weightlifty.data.DataContract;
import etchee.com.weightlifty.data.DataContract.EventEntry;
import etchee.com.weightlifty.data.DeleteActionHelper;
import etchee.com.weightlifty.data.ModifyEventHelper;

import static etchee.com.weightlifty.data.DataContract.GlobalConstants.QUERY_EVENT_TYPE;
import static etchee.com.weightlifty.data.DataContract.GlobalConstants.QUERY_REPS_COUNT;
import static etchee.com.weightlifty.data.DataContract.GlobalConstants.QUERY_SETS_NUMBER;
import static etchee.com.weightlifty.data.DataContract.GlobalConstants.QUERY_WEIGHT_COUNT;
import static java.lang.Integer.parseInt;

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
    private TextView weight_count;
    private Button event_button;
    private Button delete_event;

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

    private AsyncQueryHandler queryHandler; // for querying data

    private int sub_ID;

    private String eventString;

    private DeleteActionHelper deleteHelper;

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
        weight_count = (TextView) findViewById(R.id.input_weight_number);
        event_button = (Button) findViewById(R.id.add_event);
        delete_event = (Button) findViewById(R.id.delete_workout);

        defineQueryHandler();


        Bundle bundle = getIntent().getExtras();

        sub_ID = bundle.getInt(DataContract.GlobalConstants.PASS_SUB_ID);

        delete_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteHelper = new DeleteActionHelper(
                        getApplicationContext(),
                        EditEventActivity.this
                );

                String array[] = new String[]{
                        String.valueOf(getDateAsInt()),
                        String.valueOf(sub_ID)
                };
                ArrayList<String> list = new ArrayList<>(2);
                list.add(0, String.valueOf(getDateAsInt()));
                list.add(1, String.valueOf(sub_ID));

                deleteHelper.execute(list);
                finish();
            }
        });

        // Case 1: creating a new event → bundle with contentValues.
        // use inner class to just get the title of the workout, then display.
        if (bundle.get(DataContract.GlobalConstants.CONTENT_VALUES) != null) {
            Toast.makeText(this, "Create new event mode", Toast.LENGTH_SHORT).show();

            ContentValues values = (ContentValues) bundle.get(DataContract.GlobalConstants.CONTENT_VALUES);
            int sampleValue = values.getAsInteger(DataContract.GlobalConstants.SUB_ID);
        }

        // Case 2: modifying an already existing event → bundle with selection.
        if (bundle.get(DataContract.GlobalConstants.PASS_EVENT_ID) != null) {

            //currently in ListActivity, this date is just set as today's date. Just pass another date
            //in future updates.
            final int selectedDate = bundle.getInt(DataContract.GlobalConstants.PASS_SELECTED_DATE);

            //Not create event but modify event
            event_button.setText(R.string.modify_event);
            event_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new ModifyEventHelper(
                            getApplicationContext(),
                            EditEventActivity.this,
                            selectedDate,
                            sub_ID
                            ).execute(getUserInputs());
                }
            });

            setReceivedEventID(bundle.getInt(DataContract.GlobalConstants.PASS_EVENT_ID));
            if (receivedEventID < 0 ) {
                throw new IllegalArgumentException("Null contentValues");
            }

            //init the loader
            getSupportLoaderManager().initLoader(LOADER_MODIFY_EVENT_MODE, bundle, this);

            //then show the event String
            queryEventType(getReceivedEventID());

            //set the number of sets
            queryNumberOfSets(getDateAsInt(), sub_ID);

            //set the number of reps
            queryNumberOfReps(getDateAsInt(), sub_ID);

            //set the weight figure
            queryWeightCount(getDateAsInt(), sub_ID);


        } else {
            throw new IllegalArgumentException("EditEventActivity did not receive bundle of" +
                    "selection columns nor contentValues to make a new event");
        }

    }

    private void defineQueryHandler() {

        queryHandler = new AsyncQueryHandler(getContentResolver()) {

            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {

                switch (token) {

                    case QUERY_EVENT_TYPE:
                        if (cursor.moveToFirst()) {
                            int index = cursor.getColumnIndex(DataContract.EventTypeEntry.COLUMN_EVENT_NAME);
                            setEventString(cursor.getString(index));
                            cursor.close();
                        } else throw new CursorIndexOutOfBoundsException("Event string query: " +
                                "cursor returned null.");

                        name_workout.setText(getEventString());

                        break;

                    case QUERY_SETS_NUMBER:
                        int count = cursor.getCount();
                        if (cursor.moveToFirst()) {
                            int index = cursor.getColumnIndex(EventEntry.COLUMN_SET_COUNT);
                            int set_count = cursor.getInt(index);
                            numberPicker_set.setValue(set_count);
                        } else throw new CursorIndexOutOfBoundsException("Set Number query: " +
                                "cursor returned null");
                        cursor.close();
                        break;

                    case QUERY_REPS_COUNT:
                        count = cursor.getCount();
                        if (cursor.moveToFirst()) {
                            int index = cursor.getColumnIndex(EventEntry.COLUMN_REP_COUNT);
                            int rep_number = cursor.getInt(index);
                            numberPicker_rep.setValue(rep_number);
                        }
                        cursor.close();
                        break;

                    case QUERY_WEIGHT_COUNT:
                        count = cursor.getCount();
                        if (cursor.moveToFirst()) {
                            int index = cursor.getColumnIndex(EventEntry.COLUMN_WEIGHT_COUNT);
                            int weight_count = cursor.getInt(index);
                            EditEventActivity.this.weight_count.setText(String.valueOf(weight_count));
                        }

                        cursor.close();
                        break;

                    default:
                        throw new IllegalArgumentException("QueryHandler received wrong " +
                                    "token to process. Debug to check token.");
                }
            }

        };
    }

    private ContentValues getUserInputs() {
        ContentValues values = new ContentValues();

        int set_count = numberPicker_set.getValue();
        int rep_count = numberPicker_rep.getValue();
        int weight_count = Integer.parseInt(this.weight_count.getText().toString());

        values.put(EventEntry.COLUMN_SET_COUNT, set_count);
        values.put(EventEntry.COLUMN_REP_COUNT, rep_count);
        values.put(EventEntry.COLUMN_WEIGHT_COUNT, weight_count);
        values.put(EventEntry.COLUMN_EVENT_ID, receivedEventID);

        return values;
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

        //WHEN MODIFYING A EVENT
        if (id == LOADER_MODIFY_EVENT_MODE) {

            //this would be the selection number
            int rowID = bundle.getInt(DataContract.GlobalConstants.SUB_ID);
            String projection[] = new String[]{
                    EventEntry.COLUMN_EVENT_ID,
                    EventEntry.COLUMN_SET_COUNT,
                    EventEntry.COLUMN_REP_COUNT,
                    EventEntry.COLUMN_WEIGHT_COUNT
            };
            //sort order is "column_name ASC" or "column_name DESC"
            String sortorder = DataContract.CalendarEntry.COLUMN_EVENT_IDs + ORDER_DECENDING;

            return new CursorLoader(
                    getApplicationContext(),
                    DataContract.EventEntry.CONTENT_URI,
                    projection,
                    String.valueOf(rowID),
                    null,
                    null
            );
        }

        else{
            throw new IllegalArgumentException("Loader creation received invalid token(id)." +
                    "Check onCreateLoader method");
        }


    }

    /**
     * @param loader takes the loader (with the set ID) from onCreateLoader
     * @param cursor takes the cursor created from onCreateLoader
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor.moveToFirst()) {
            int setCountIndex = cursor.getColumnIndex(EventEntry.COLUMN_SET_COUNT);
            int repSequenceIndex = cursor.getColumnIndex(EventEntry.COLUMN_REP_COUNT);
            int weightSequenceIndex = cursor.getColumnIndex(EventEntry.COLUMN_WEIGHT_COUNT);

            int setCount = cursor.getInt(setCountIndex);
            String repSequence = cursor.getString(repSequenceIndex);
            String weightSequence = cursor.getString(weightSequenceIndex);

            numberPicker_set.setValue(setCount);

            queryEventType(getReceivedEventID());
        }

        getLoaderManager().destroyLoader(LOADER_MODIFY_EVENT_MODE);

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
        queryHandler.startQuery(
                QUERY_EVENT_TYPE,
                null,
                DataContract.EventTypeEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );
    }


    private void queryNumberOfSets(int date, int subID) {
        //projection
        String projection[] = new String[]{
                EventEntry.COLUMN_DATE,
                EventEntry.COLUMN_SUB_ID,
                EventEntry.COLUMN_SET_COUNT
        };

        //select row by eventID
        String selection = EventEntry.COLUMN_DATE + "=?" + " AND " + EventEntry.COLUMN_SUB_ID + "=?";

        //eventID will be thrown in the parameter
        String selectionArgs[] = new String[]{
                String.valueOf(date),
                String.valueOf(subID)
        };


        //Finally, fire the query!
        queryHandler.startQuery(
                QUERY_SETS_NUMBER,
                null,
                EventEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );
    }

    private void queryNumberOfReps(int date, int subID) {
        //projection
        String projection[] = new String[]{
                EventEntry.COLUMN_DATE,
                EventEntry.COLUMN_SUB_ID,
                EventEntry.COLUMN_REP_COUNT
        };

        //selection
        String selection = EventEntry.COLUMN_DATE + "=?" + " AND " + EventEntry.COLUMN_SUB_ID + "=?";

        //selectionArgs
        String selectionArgs[] = new String[]{
                String.valueOf(date),
                String.valueOf(subID)
        };

        queryHandler.startQuery(
                QUERY_REPS_COUNT,
                null,
                EventEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );
    }

    private void queryWeightCount(int date, int subID) {
        //projection
        String projection[] = new String[]{
                EventEntry.COLUMN_DATE,
                EventEntry.COLUMN_SUB_ID,
                EventEntry.COLUMN_WEIGHT_COUNT
        };

        //selection
        String selection = EventEntry.COLUMN_DATE + "=?" + " AND " + EventEntry.COLUMN_SUB_ID + "=?";

        //selectionArgs
        String selectionArgs[] = new String[]{
                String.valueOf(date),
                String.valueOf(subID)
        };

        queryHandler.startQuery(
                QUERY_WEIGHT_COUNT,
                null,
                EventEntry.CONTENT_URI,
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

    private int getDateAsInt() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;   //month starts from zero
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String concatenated = String.valueOf(year) + String.valueOf(month) + String.valueOf(day);

        return parseInt(concatenated);
    }
}

