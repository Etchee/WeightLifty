package etchee.com.weightlifty;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.Calendar;

import etchee.com.weightlifty.data.DataContract;
import etchee.com.weightlifty.data.DataContract.EventEntry;

import static etchee.com.weightlifty.data.DataContract.EventTypeEntry.COLUMN_EVENT_NAME;

/**
 * Created by rikutoechigoya on 2017/03/30.
 */

public class ListActivity extends AppCompatActivity {

    private ListView listview;
    private listActivityAdapter mAdapter;

    //To make sure that there is only one instance because OpenHelper will serialize threads anyways
    private ContentResolver contentResolver;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);

        //create the fabs
        onCreateFabCreator();

        contentResolver = getContentResolver();

        //for the empty view
        View emptyView = findViewById(R.id.view_empty);
        listview = (ListView)findViewById(R.id.listview_workout);
        listview.setEmptyView(emptyView);
        Cursor cursor = createCursor();
        if (cursor == null) throw new IllegalArgumentException("Cursor creation failed: " +
                "check projection, it might not be matching with the table.");

        mAdapter = new listActivityAdapter(getApplicationContext(), cursor, 0);
        listview.setAdapter(mAdapter);

        /**
         *  When an item on the listView is clicked, get the (INT) item id, put on the bundle and deliver
         *      to EditActivity.
         */
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Toast.makeText(ListActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
                queryEventID(position);
            }
        });

        FloatingActionsMenu floatingActionsMenu = (FloatingActionsMenu) findViewById(R.id.button_chest);
        floatingActionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ListActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton fab_leg = (FloatingActionButton) findViewById(R.id.fab_leg);
        fab_leg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ChooseEventActivity.class);
                startActivity(intent);
            }
        });
    }

    private void queryEventID(int position) {

        int date_today = getDateAsInt();

        //Define async query handler
        AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {

            @Override
            public void startQuery(int token, Object cookie, Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy) {
                super.startQuery(token, cookie, uri, projection, selection, selectionArgs, orderBy);
            }


            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {

                //Token will be passed to prevent confusion on multi querying
                if (token == DataContract.GlobalConstants.QUERY_EVENT_ID) {

                    if (cursor != null) {
                        int index = cursor.getColumnIndex(EventEntry.COLUMN_EVENT_ID);
                        int eventID[] = new int[1];
                        int count = cursor.getCount();
                        eventID[0] = cursor.getInt(index);
                        Toast.makeText(ListActivity.this, "event ID is: " +
                                String.valueOf(eventID[0]), Toast.LENGTH_SHORT).show();
                    } if (cursor == null) throw new IllegalArgumentException("Cursor is null");
                } else throw new IllegalArgumentException("Invalid token received at Event ID query.");
                Log.v("Event ID query", "Status: Completed");
            }
        };

        //projection
        String projection[] = new String[]{
                EventEntry.COLUMN_DATE,
                EventEntry.COLUMN_SUB_ID,
                EventEntry.COLUMN_EVENT_ID
        };

        //selection
        String selection = EventEntry.COLUMN_DATE + "=?" + " AND " + EventEntry.COLUMN_SUB_ID + "=?";

        //selectionArgs
        String selectionArgs[] = new String[]{
                String.valueOf(date_today),
                String.valueOf(position)
        };

        queryHandler.startQuery( //Get event ID based on position first
                DataContract.GlobalConstants.QUERY_EVENT_ID,
                null,
                EventEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );
    }



    /**
     *
     * @param ID: index of the item in ListView. This is equivalent to the item number in the
     *                sub_id column in Event Table.
     *
     *        This method takes the sub_id, and traces to the event name String in the event table.
     *
     */
    private void queryEventName(int ID) {
        final int[] eventID = new int[1];
        final String[] eventString = new String[1];


        //Event Table query
        String projection_eventTable[] = new String[]{
                EventEntry.COLUMN_DATE,
                EventEntry.COLUMN_SUB_ID,
                EventEntry.COLUMN_EVENT_ID
        };
        //Specify rows by date (getDateAsInt()) and subID(position)
        String selection_eventTable = EventEntry.COLUMN_DATE + "=?" + " AND " + EventEntry.COLUMN_SUB_ID + "=?";

        //Date is today, SUB_ID is position.
        int date_today = getDateAsInt();

        String selectionArgs_eventTable[] = new String[]{
                String.valueOf(date_today),
                String.valueOf(ID)
        };
        /**
         *  when eventTable finishes querying, the result (eventID) will be stored in eventID[0].
         */

        //EventType query
        String projection_eventTypeTable[] = new String[]{
                DataContract.EventTypeEntry._ID,
                DataContract.EventTypeEntry.COLUMN_EVENT_NAME };
        //specifying by _ID
        String selection_eventTypeTAble = DataContract.EventTypeEntry._ID + "=?";
        //_ID is what I get in the another query
//        String selectionArgs_eventTypeTable[] = new String[]{String.valueOf(eventID[0])};
        String selectionArgs_eventTypeTable[] = new String[]{String.valueOf(1)};

        //Async query for EVENT ID
        AsyncQueryHandler queryEventID = new AsyncQueryHandler(getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor EventIDcursor) {
                //Event table　→　put Event ID in eventID[0]
                if (token == DataContract.GlobalConstants.QUERY_EVENT_ID) {
                    int index = EventIDcursor.getColumnIndex(EventEntry.COLUMN_EVENT_ID);
                    if (EventIDcursor.moveToFirst()) {
                        eventID[0] = EventIDcursor.getInt(index);
                    } else Log.e("queryEventID", "Cursor returned null");
                }



            }
        };

        //Async query for EVENT TYPE str
        AsyncQueryHandler queryEventType = new AsyncQueryHandler(getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor EventTypeCursor) {
                //EventType table → put event name str in eventString[0]
                if (token == DataContract.GlobalConstants.QUERY_EVENT_TYPE) {
                    if (EventTypeCursor.moveToFirst()) {
                        int index1 = EventTypeCursor.getColumnIndex(DataContract.EventTypeEntry.COLUMN_EVENT_NAME);
                        eventString[0] = EventTypeCursor.getString(index1);
                    } else Log.e("queryEventType", "Cursor returned null");
                }
            }
        };

        queryEventID.startQuery( //Get event ID based on position first
                DataContract.GlobalConstants.QUERY_EVENT_ID,
                null,
                EventEntry.CONTENT_URI,
                projection_eventTable,
                selection_eventTable,
                selectionArgs_eventTable,
                null
        );

//        Get Event String based on the sub ID acquired
        queryEventType.startQuery(
                DataContract.GlobalConstants.QUERY_EVENT_TYPE,
                null,
                DataContract.EventTypeEntry.CONTENT_URI,
                projection_eventTypeTable,
                selection_eventTypeTAble,
                selectionArgs_eventTypeTable,
                null
        );


        Intent intent = new Intent(getApplicationContext(), EditEventActivity.class);
        Bundle bundle = new Bundle();
        if (eventString[0] != null) {
            //put event name in the bundle
            bundle.putString(DataContract.GlobalConstants.PASS_EVENT_STRING, eventString[0]);
            intent.putExtras(bundle);
            startActivity(intent);
        } else throw new IllegalArgumentException("Event name query has failed.");
    }

    private Cursor createCursor() {
        Cursor cursor;

        String projection[] = {
                EventEntry._ID,
                EventEntry.COLUMN_WEIGHT_SEQUENCE,
                EventEntry.COLUMN_REP_SEQUENCE,
                EventEntry.COLUMN_SUB_ID,
                EventEntry.COLUMN_SET_COUNT,
        };

        cursor = getContentResolver().query(
                EventEntry.CONTENT_URI,
                projection,
                null,
                null,
                null );
        return cursor;
    }

    private void onCreateFabCreator() {

        //Action button A  (Top)
        final FloatingActionButton actionA = (FloatingActionButton) findViewById(R.id.fab_leg);
        actionA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //Action button B (
        // iddle)
        final View actionB = findViewById(R.id.action_b);
        actionB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //Action button C (Bottom)
        FloatingActionButton actionC = new FloatingActionButton(getBaseContext());
        actionC.setTitle("Chest");
        actionC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        final FloatingActionsMenu menuMultipleActions =
                (FloatingActionsMenu)findViewById(R.id.button_chest);
        menuMultipleActions.addButton(actionC);

        ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
        drawable.getPaint().setColor(ContextCompat.getColor(getApplicationContext(), R.color.white));

    }

    private int getDateAsInt() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;   //month starts from zero
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String concatenated = String.valueOf(year) + String.valueOf(month) + String.valueOf(day);
        Log.v("Concatenated", concatenated);

        return Integer.parseInt(concatenated);
    }
}
