package etchee.com.weightlifty;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Bundle;
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

import etchee.com.weightlifty.data.DataContract;
import etchee.com.weightlifty.data.DataContract.EventEntry;

/**
 * Created by rikutoechigoya on 2017/03/30.
 */

public class ListActivity extends AppCompatActivity {

    private ListView listview;
    private listActivityAdapter mAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);

        //create the fabs
        onCreateFabCreator();

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
                Toast.makeText(ListActivity.this, "Item" + String.valueOf(position) + " clicked!",
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), EditEventActivity.class);
                Bundle bundle = new Bundle();
                //put ID in the bundle
                bundle.putInt(DataContract.GlobalConstants.ITEM_ID, position);
                intent.putExtras(bundle);
                startActivity(intent);
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


    /*
    * TODO: this method should add the number of sub IDs that matches to the number of EventEntry
    * */
    private void event_insertDummyValues() {

        /*
        *   From the helper class
        *    final String CREATE_EVENT_TABLE =
                "CREATE TABLE IF NOT EXISTS " + EventEntry.TABLE_NAME + " ("
                + EventEntry._ID + " INTEGER NOT NULL, "
                + EventEntry.COLUMN_SUB_ID + " INTEGER NOT NULL, "
                + EventEntry.COLUMN_SET_COUNT + " INTEGER NOT NULL, "
                + EventEntry.COLUMN_REP_SEQUENCE + " INTEGER NOT NULL, "
                + EventEntry.COLUMN_WEIGHT_SEQUENCE + " TEXT);";
        * */

        ContentValues dummyValues = new ContentValues();
        int sub_ID = 0;
        int id = 0;
        int eventID = 2;


        dummyValues.put(EventEntry._ID, id);
        dummyValues.put(EventEntry.COLUMN_SUB_ID, sub_ID);
        dummyValues.put(EventEntry.COLUMN_EVENT_ID, eventID);
        dummyValues.put(EventEntry.COLUMN_REP_SEQUENCE, "");
        dummyValues.put(EventEntry.COLUMN_SET_COUNT, 0);
        dummyValues.put(EventEntry.COLUMN_WEIGHT_SEQUENCE, "");

        Uri uri = getContentResolver().insert(EventEntry.CONTENT_URI, dummyValues);

        if (uri == null) throw new IllegalArgumentException("Calendar table (inser dummy)" +
                "failed to insert data. check the MainActivity method and the table.");

        Log.v("DUMMYDATA", "Data inserted in: " + uri);
        Toast.makeText(this, "Event Data inserted in: " + uri.toString(), Toast.LENGTH_SHORT).show();
    }
}
