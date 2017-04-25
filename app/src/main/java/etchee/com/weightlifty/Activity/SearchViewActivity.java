package etchee.com.weightlifty.Activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import etchee.com.weightlifty.Adapter.SearchResultsAdapter;
import etchee.com.weightlifty.R;
import etchee.com.weightlifty.data.DataContract;

import static android.content.ContentValues.TAG;
import static etchee.com.weightlifty.data.DataContract.FTS_Table.DATABASE_VERSION;
import static etchee.com.weightlifty.data.DataContract.FTS_Table.FTS_TABLE_CREATE;
import static etchee.com.weightlifty.data.DataContract.FTS_Table.FTS_VIRTUAL_TABLE;

/**
 * Created by rikutoechigoya on 2017/04/21.
 */

public class SearchViewActivity extends Activity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private String query;
    private Context context;
    private ListView listview;
    private SearchResultsAdapter adapter;
    private SearchView searchView;
    private FTSDatabaseOpenHelper mDbHelper;
    private SQLiteDatabase mDb;
    private TextView search_textView_workoutName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //component setting
        listview = (ListView) findViewById(R.id.view_search_list);
        context = SearchViewActivity.this;

        //search view setup
        searchView = (SearchView) findViewById(R.id.search);
        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
        search_textView_workoutName = (TextView) findViewById(R.id.searchview_event_name);

        openDbInstance();

    }

    /**
     *  This method takes the resulting cursor from the "search method" and then inflate the
     *  result in listView.a
     * @param query
     */
    private void showResults(String query) {
        Cursor cursor = search((query != null ? query.toString() : "@@@@"));

        if (cursor == null) {
            Log.e(TAG, "InstaSearch cursor returned null");
        } else {
            // Specify the columns we want to display in the result
            String[] projection = new String[]{
                    DataContract.EventTypeEntry.COLUMN_EVENT_NAME
            };

            // Specify the Corresponding layout elements where we want the columns to go
            int[] searchView_element = new int[]{
                    R.id.searchview_event_name  //the array will match because this method is about
                    //what the cursor will return.
            };

            // Create a simple cursor adapter for the definitions and apply them to the ListView
            android.widget.SimpleCursorAdapter adapter = new android.widget.SimpleCursorAdapter(
                    this, R.layout.item_single_searchview, cursor, projection, searchView_element, 0);
            listview.setAdapter(adapter);

            // Define the on-click listener for the list items
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Get the cursor, positioned to the corresponding row in the result set
                    Cursor cursor = (Cursor) listview.getItemAtPosition(position);

                    // Get the state's capital from this row in the database.
                    String workoutName = cursor.getString(cursor.getColumnIndexOrThrow(
                            DataContract.EventTypeEntry.COLUMN_EVENT_NAME
                    ));


                    // Update the parent class's TextView
                    search_textView_workoutName.setText(workoutName);
                    searchView.setQuery("", true);
                }
            });
        }
    }

    public SearchViewActivity openDbInstance() throws SQLException {
        mDbHelper = new FTSDatabaseOpenHelper(this);
        mDb = mDbHelper.getReadableDatabase();
        return this;
    }

    /**
     * Method to put customer values into the SQL
     */
    public long insertCustomer(String workoutName) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(DataContract.EventTypeEntry.COLUMN_EVENT_NAME, workoutName);

        return mDb.insert(FTS_VIRTUAL_TABLE, null, initialValues);
    }

    /**
     * Takes userInput, search thru the db and then returns cursor to iterate in the adapter.
     *
     * @param inputText User input text sent from the UI
     * @return cursor to iterate thru to show results in ListView
     * @throws SQLException if search fails.
     */
    public Cursor search(String inputText) throws SQLException {
        Log.w(TAG, inputText);
        String query = "SELECT docid as _id," +
                DataContract.EventTypeEntry.COLUMN_EVENT_NAME + "," +
                " from " + FTS_VIRTUAL_TABLE +
                " where " + DataContract.EventTypeEntry.COLUMN_EVENT_NAME + " MATCH '" + inputText + "';";

        Cursor mCursor = mDb.rawQuery(query, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public boolean deleteAllEntries() {

        int numberOfRowsDeleted;
        numberOfRowsDeleted = mDb.delete(FTS_VIRTUAL_TABLE, null, null);
        Log.w(TAG, Integer.toString(numberOfRowsDeleted));
        return numberOfRowsDeleted > 0;

    }

    public void close() {
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }


    @Override
    public boolean onClose() {
        showResults("");
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        showResults(query + "*");
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        showResults(newText + "*");
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        close();
    }


    /**
     *  DbHelper class specific for FTS virtual copy of the EventType table.
     */
    private static class FTSDatabaseOpenHelper extends SQLiteOpenHelper {

        private Context context;

        FTSDatabaseOpenHelper(Context context) {
            super(context, DataContract.EventTypeEntry.TABLE_NAME, null, DATABASE_VERSION);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(FTS_TABLE_CREATE);
            copyDataFromEventTypeTable();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            onCreate(db);
        }


        //TODO Create a method here to copy eventType table into FTS3
        private void copyDataFromEventTypeTable() {

        }
    }

}