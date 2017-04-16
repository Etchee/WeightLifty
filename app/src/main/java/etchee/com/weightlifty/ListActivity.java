package etchee.com.weightlifty;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.Calendar;

import etchee.com.weightlifty.data.DataContract;
import etchee.com.weightlifty.data.DataContract.EventEntry;

/**
 * Created by rikutoechigoya on 2017/03/30.
 */

public class ListActivity extends AppCompatActivity {

    private ListView listview;
    private listActivityAdapter mAdapter;

    //To make sure that there is only one instance because OpenHelper will serialize threads anyways
    private ContentResolver contentResolver;

    private int eventID;
    private String eventString;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);

        //create the fabs
        onCreateFabCreator();

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
                queryEventID(position);
                queryEventType(1);
            }
        });


    }

    private void queryEventIDHandler(int eventID) {
        this.eventID = eventID;
    }


    /**
     * @param position equals to that of the sub_id in the event table.
     *                 This method find the specific row with the sub_id, then get the event_id
     *                 back in that row.
     */
    private void queryEventID(int position) {

        int date_today = getDateAsInt();

        //Define async query handler
        AsyncQueryHandler queryHandler = new AsyncQueryHandler(contentResolver) {

            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                int eventID = -1;

                //Token will be passed to prevent confusion on multi querying
                if (token == DataContract.GlobalConstants.QUERY_EVENT_ID) {
                    if (cursor.moveToFirst()) {
                        int index = cursor.getColumnIndex(EventEntry.COLUMN_EVENT_ID);
                        eventID = Integer.parseInt(cursor.getString(index));

                    }

                    else if (!cursor.moveToFirst()) throw new CursorIndexOutOfBoundsException("EventID query: Cursor might be empty");

                } else throw new IllegalArgumentException("Invalid token received at Event ID query.");

                Log.v("Event ID query", "Status: Completed");
                queryEventIDHandler(eventID);
                Toast.makeText(ListActivity.this, "event ID is: " + String.valueOf(eventID), Toast.LENGTH_SHORT).show();
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

        //Finally, fire the query!
        queryHandler.startQuery(
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
     * @param eventID: index of the item in ListView. This is equivalent to the item number in the
     *                sub_id column in Event Table.
     *
     *        This method takes the sub_id, and traces to the event name String in the event table.
     *
     */
    private void queryEventType(int eventID) {

        //define onQueryComplete
        AsyncQueryHandler queryEventType = new AsyncQueryHandler(contentResolver) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {

                String eventString;

                //EventType table → put event name str in eventString[0]
                if (token == DataContract.GlobalConstants.QUERY_EVENT_TYPE) {

                    if (cursor.moveToFirst()) {
                        int index = cursor.getColumnIndex(DataContract.EventTypeEntry.COLUMN_EVENT_NAME);
                        eventString = cursor.getString(index);

                    } else throw new CursorIndexOutOfBoundsException("Cursor could not move to first.");
                }else throw new IllegalArgumentException("Illegal token received at Event String query.");

                Log.v("Event ID query", "Status: Completed");
                queryEventTypeHandler(ListActivity.this.eventString);
                Toast.makeText(ListActivity.this, "Event is: " + eventString, Toast.LENGTH_SHORT).show();
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

    private void queryEventTypeHandler(String eventType) {
        this.eventString = eventType;
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

        cursor = contentResolver.query(
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
