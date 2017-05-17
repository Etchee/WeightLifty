package etchee.com.weightlifty.Activity;

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import etchee.com.weightlifty.DataMethods.DeleteActionHelper;
import etchee.com.weightlifty.DataMethods.ModifyEventHelper;
import etchee.com.weightlifty.DataMethods.QueryEventIDFromName;
import etchee.com.weightlifty.DataMethods.QueryResponceHandler;
import etchee.com.weightlifty.R;
import etchee.com.weightlifty.data.DataContract;
import etchee.com.weightlifty.data.DataContract.EventEntry;

import static etchee.com.weightlifty.data.DataContract.GlobalConstants.LAUNCH_EDIT_CODE;
import static etchee.com.weightlifty.data.DataContract.GlobalConstants.LAUNCH_EDIT_EXISTING;
import static etchee.com.weightlifty.data.DataContract.GlobalConstants.LAUNCH_EDIT_NEW;
import static etchee.com.weightlifty.data.DataContract.GlobalConstants.PASS_EVENT_ID;
import static etchee.com.weightlifty.data.DataContract.GlobalConstants.PASS_EVENT_STRING;
import static etchee.com.weightlifty.data.DataContract.GlobalConstants.PASS_SELECTED_DATE;
import static etchee.com.weightlifty.data.DataContract.GlobalConstants.QUERY_EVENT_TYPE;
import static etchee.com.weightlifty.data.DataContract.GlobalConstants.QUERY_REPS_COUNT;
import static etchee.com.weightlifty.data.DataContract.GlobalConstants.QUERY_SETS_NUMBER;
import static etchee.com.weightlifty.data.DataContract.GlobalConstants.QUERY_WEIGHT_COUNT;
import static etchee.com.weightlifty.data.DataContract.GlobalConstants.UNIT_IMPEREIAL;
import static etchee.com.weightlifty.data.DataContract.GlobalConstants.UNIT_METRIC;
import static java.lang.Integer.parseInt;

/**
 * Created by rikutoechigoya on 2017/04/07.
 *
 *  From WorkoutListInterface, when user taps on one of the events, this class is called to load the
 *  specific event.
 */

public class EditEventActivity extends FragmentActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, NumberPicker.OnValueChangeListener, QueryResponceHandler {

    //component declaration
    private NumberPicker numberPicker_set;
    private NumberPicker numberPicker_rep;
    private TextView name_workout;
    private TextView weight_count;
    private TextView hint_unit_text;
    private Button button_add_event;
    private Button button_delete_event;
    private final String NAME_PREF_FILE = "preferences";

    private static final int SET_MAXVALUE = 30;
    private static final int SET_MINVALUE = 1;
    private static final int REP_MAXVALUE = 500;
    private static final int REP_MINVALUE = 1;
    private final String TAG = getClass().getSimpleName();

    private static final String ORDER_DECENDING = " DESC";
    private static final String ORDER_ASCENDING = " ASC";

    private static final int LOADER_CREATE_NEW_EVENT_MODE = 0;
    private static final int LOADER_MODIFY_EVENT_MODE = 1;

    private int LAUNCH_MODE;

    private String eventType;
    private String formattedDate;

    private int receivedEventID = -1;

    private AsyncQueryHandler queryHandler; // for querying data

    private int sub_ID;

    private String unit_pref;

    private String eventString;
    private DeleteActionHelper deleteHelper;

    /**
     * Two modes for this activity:
     * 1. From WorkoutListInterface, tapping on already existing item.
     * → Modify event, bundle w/ selection. Select that in the event table, query, display.
     * <p>
     * 2. From ChooseEventActivity, selecting which workout.
     * → Create event, bundle w/ contentValues.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);

        //View Initialization
        numberPicker_set = (NumberPicker) findViewById(R.id.set_numberPicker);
        numberPicker_set.setMaxValue(SET_MAXVALUE);
        numberPicker_set.setMinValue(SET_MINVALUE);
        numberPicker_rep = (NumberPicker) findViewById(R.id.rep_numberPicker);
        numberPicker_rep.setMaxValue(REP_MAXVALUE);
        numberPicker_rep.setMinValue(REP_MINVALUE);
        name_workout = (TextView) findViewById(R.id.edit_workout_name);
        weight_count = (EditText) findViewById(R.id.input_weight_number);
        button_add_event = (Button) findViewById(R.id.add_event);
        button_delete_event = (Button) findViewById(R.id.delete_workout);
        hint_unit_text = (TextView)findViewById(R.id.hint_text_unit);



        //get user pref for weight unit_pref
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        unit_pref = sharedPreferences.getString(getResources().getString(R.string.pref_unit), DataContract.GlobalConstants.UNIT_METRIC);

        //default is metric. if imperial, replace the hint text with lbs unit_pref sign, then when saving,
        // do some conversion.

        switch (unit_pref) {
            case UNIT_IMPEREIAL:
                hint_unit_text.setText(getResources().getText(R.string.lb));
                break;

            case UNIT_METRIC:
                //do nothing
                break;

            default: Log.e(TAG, "Unit info could not be retrieved.");
        }

        defineQueryHandler();

        Bundle bundle = getIntent().getExtras();

        int launchMode = bundle.getInt(LAUNCH_EDIT_CODE, -1);

        switch (launchMode) {
            case LAUNCH_EDIT_NEW:   //launching a new event
                Toast.makeText(this, "Create new event mode", Toast.LENGTH_SHORT).show();
                LAUNCH_MODE = LAUNCH_EDIT_NEW;
                eventString = bundle.getString(DataContract.GlobalConstants.PASS_EVENT_STRING);
                //first thing fire the asyncTask to get the id
                QueryEventIDFromName queryEventIDFromName =
                        new QueryEventIDFromName(getApplicationContext(), this);
                queryEventIDFromName.queryResponceHandler = this;
                queryEventIDFromName.execute(eventString);

                //not "delete", but "cancel"
                button_delete_event.setText(R.string.cancel);

                //add button behavior
                button_add_event.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //make correct contentValues here

                        if (weightIsValid()) {
                            Uri uri = addNewEvent(getUserInputsAsContentValues(getFormattedDate()));
                            Toast.makeText(EditEventActivity.this, eventString + " added." + uri.toString(),
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(EditEventActivity.this, "Please input integer for weight.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                String event = getIntent().getStringExtra(PASS_EVENT_STRING);
                name_workout.setText(event);


                button_delete_event.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditEventActivity.super.onBackPressed();
                    }
                });
                break;

            case LAUNCH_EDIT_EXISTING:
                Toast.makeText(this, "Edit mode", Toast.LENGTH_SHORT).show();   //Editing an existing event
                LAUNCH_MODE = LAUNCH_EDIT_EXISTING;
                formattedDate = bundle.getString(PASS_SELECTED_DATE);

                //change button texts so that they make sense
                button_add_event.setText(R.string.update_event);
                button_add_event.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (weightIsValid()) {
                            new ModifyEventHelper(
                                    getApplicationContext(),
                                    EditEventActivity.this,
                                    formattedDate,
                                    sub_ID
                            ).execute(getUserInputsAsContentValues(formattedDate));
                            finish();
                        } else {
                            Toast.makeText(EditEventActivity.this, "Please input integer for weight.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                receivedEventID = bundle.getInt(PASS_EVENT_ID, -1);
                if (receivedEventID < 0 ) {
                    Toast.makeText(this, "Invalid event ID.", Toast.LENGTH_SHORT).show();
                    Log.e("TAG", "Edit mode, but bundle did not have any event ID info");
                }
                sub_ID = bundle.getInt(DataContract.GlobalConstants.PASS_SUB_ID, -1);
                if (sub_ID < 0 ) {
                    Toast.makeText(this, "Invalid SUB_ID.", Toast.LENGTH_SHORT).show();
                    Log.e("TAG", "Edit mode, but bundle did not have any SUB_ID info");
                }

                //init the loader
                getSupportLoaderManager().initLoader(LOADER_MODIFY_EVENT_MODE, bundle, this);

                //then show the event String
                queryEventType(receivedEventID);

                //set the number of sets
                queryNumberOfSets(sub_ID);

                //set the number of reps
                queryNumberOfReps(sub_ID);

                //set the weight figure
                queryWeightCount(sub_ID);

                button_delete_event.setOnClickListener(new View.OnClickListener() {
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

                break;

            case -1:
                Toast.makeText(this, "Error loading data.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Did not receive a bundle of launch mode.");
                break;

            default: throw new IllegalArgumentException(TAG + ": EditEventActivity could not recognize" +
                    "the launch mode.");
        }

        weight_count.setText("0");
        weight_count.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                weight_count.setText("");
                return false;
            }
        });

    }

    private Boolean weightIsValid() {

        CharSequence input = weight_count.getText();
        try {
            int weight = Integer.parseInt(String.valueOf(input));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private int convertKiloToPound(int kilo) {
        double temp;
        temp = kilo*2.20462;
        int pound = Integer.parseInt(String.valueOf(Math.round(temp)));
        return pound;
    }

    private int convertPoundToKilo(int pound) {
        double temp;
        temp = (1/2.20462) * pound;
        int kilo = Integer.parseInt(String.valueOf(Math.round(temp)));
        return kilo;
    }

    private void defineQueryHandler() {

        queryHandler = new AsyncQueryHandler(getContentResolver()) {

            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {

                switch (token) {

                    case QUERY_EVENT_TYPE:
                        if (cursor.moveToFirst()) {
                            int index = cursor.getColumnIndex(DataContract.EventType_FTSEntry.COLUMN_EVENT_NAME);
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
                            //if imperial, do conversion and then show
                            if (unit_pref.equals(UNIT_IMPEREIAL)) {
                                weight_count = convertKiloToPound(weight_count);
                            }
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

    private ContentValues getUserInputsAsContentValues(String date) {
        ContentValues values = new ContentValues();
        /*
             From DataContract:
              "CREATE TABLE IF NOT EXISTS " + EventEntry.TABLE_NAME + " ("
                + EventEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + EventEntry.COLUMN_DATE + " INTEGER NOT NULL, "
                + EventEntry.COLUMN_SUB_ID + " INTEGER NOT NULL, "
                + EventEntry.COLUMN_EVENT_ID + " INTEGER NOT NULL, "
                + EventEntry.COLUMN_SET_COUNT + " INTEGER, "
                + EventEntry.COLUMN_REP_COUNT + " INTEGER, "
                + EventEntry.COLUMN_WEIGHT_COUNT + " INTEGER);";

                Not null: date, sub_id, event_id
         */

        int set_count = numberPicker_set.getValue();
        int rep_count = numberPicker_rep.getValue();

        //base on the unit_pref pref for weight
        int weight_count = 0;
        switch (unit_pref) {
            case UNIT_IMPEREIAL:
                //do the conversion
                int weight_imperial = Integer.parseInt(this.weight_count.getText().toString());
                int weight_metric = convertPoundToKilo(weight_imperial);
                weight_count = weight_metric;
                break;

            case UNIT_METRIC:
                //normal
                weight_count = Integer.parseInt(this.weight_count.getText().toString());
                break;

            default: Log.e(TAG, "CONVERSION HAS FAILED.");
        }

        if (weight_count == 0) Log.e(TAG, "There is something wrong with unit conversion when" +
                "saving. OOOOOOOOPS");

        values.put(EventEntry.COLUMN_SET_COUNT, set_count);
        values.put(EventEntry.COLUMN_REP_COUNT, rep_count);
        values.put(EventEntry.COLUMN_WEIGHT_COUNT, weight_count);
        values.put(EventEntry.COLUMN_EVENT_ID, receivedEventID);
        values.put(EventEntry.COLUMN_FORMATTED_DATE, date);
        //subID doesn't matter because this will be organized onResume for ListActivity anyways
        values.put(EventEntry.COLUMN_SUB_ID, 0);

        return values;
    }



    /**
     * If modifying data, fire up loader for background thread data loading.
     *
     * @param id     not needed. Set to 1 in onCreate()
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

    private Uri addNewEvent (ContentValues values) {

        Uri uri = getContentResolver().insert(
                EventEntry.CONTENT_URI,
                values
        );

        return uri;
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
                DataContract.EventType_FTSEntry.COLUMN_ROW_ID,
                DataContract.EventType_FTSEntry.COLUMN_EVENT_TYPE,
                DataContract.EventType_FTSEntry.COLUMN_EVENT_NAME
        };

        //select row by eventID
        String selection = DataContract.EventType_FTSEntry.COLUMN_ROW_ID + "=?";

        //eventID will be thrown in the parameter
        String selectionArgs[] = new String[]{String.valueOf(eventID)};


        //Finally, fire the query!
        queryHandler.startQuery(
                QUERY_EVENT_TYPE,
                null,
                DataContract.EventType_FTSEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );
    }


    private void queryNumberOfSets(int subID) {
        //projection
        String projection[] = new String[]{
                EventEntry.COLUMN_FORMATTED_DATE,
                EventEntry.COLUMN_SUB_ID,
                EventEntry.COLUMN_SET_COUNT
        };

        //select row by eventID
        String selection = EventEntry.COLUMN_FORMATTED_DATE + "=?" + " AND " + EventEntry.COLUMN_SUB_ID + "=?";

        //eventID will be thrown in the parameter
        String selectionArgs[] = new String[]{
                formattedDate,
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

    private void queryNumberOfReps(int subID) {
        //projection
        String projection[] = new String[]{
                EventEntry.COLUMN_FORMATTED_DATE,
                EventEntry.COLUMN_SUB_ID,
                EventEntry.COLUMN_REP_COUNT
        };

        //selection
        String selection = EventEntry.COLUMN_FORMATTED_DATE + "=?" + " AND " + EventEntry.COLUMN_SUB_ID + "=?";

        //selectionArgs
        String selectionArgs[] = new String[]{
                formattedDate,
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

    private void queryWeightCount(int subID) {
        //projection
        String projection[] = new String[]{
                EventEntry.COLUMN_FORMATTED_DATE,
                EventEntry.COLUMN_SUB_ID,
                EventEntry.COLUMN_WEIGHT_COUNT
        };

        //selection
        String selection = EventEntry.COLUMN_FORMATTED_DATE + "=?" + " AND " + EventEntry.COLUMN_SUB_ID + "=?";

        //selectionArgs
        String selectionArgs[] = new String[]{
                formattedDate,
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

    private Cursor initFTSCursor() {
        Cursor cursor = getContentResolver().query(
                DataContract.EventType_FTSEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        return cursor;
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

    @Override
    public void EventNameHolder(String EventName) {
        // not needed as of now
    }

    @Override
    public void EventIDHolder(String id) {
        receivedEventID = Integer.parseInt(id);
    }

    private String getFormattedDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);

        int month = calendar.get(Calendar.MONTH) + 1;   //month starts from zero
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String concatenated = String.valueOf(year) + "/" + String.valueOf(month) + "/" + String.valueOf(day);

        return concatenated;
    }

}