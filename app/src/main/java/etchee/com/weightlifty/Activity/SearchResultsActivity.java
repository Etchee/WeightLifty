package etchee.com.weightlifty.Activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import etchee.com.weightlifty.Adapter.SearchResultsAdapter;
import etchee.com.weightlifty.R;
import etchee.com.weightlifty.data.DataContract;
import etchee.com.weightlifty.data.InstantEventTypeQuery;

import static android.content.ContentValues.TAG;
import static etchee.com.weightlifty.data.DataContract.FTS_Table.DATABASE_NAME;
import static etchee.com.weightlifty.data.DataContract.FTS_Table.DATABASE_VERSION;
import static etchee.com.weightlifty.data.DataContract.FTS_Table.FTS_VIRTUAL_TABLE;

/**
 * Created by rikutoechigoya on 2017/04/21.
 */

public class SearchResultsActivity extends Activity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private String query;
    private InstantEventTypeQuery db;
    private Context context;
    private ListView listview;
    private SearchResultsAdapter adapter;
    private SearchView searchView;
    private FTSDatabaseOpenHelper mDbHelper;
    private SQLiteDatabase mDb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //component setting
        context = SearchResultsActivity.this;
        searchView = (SearchView) findViewById(R.id.search);
        listview = (ListView)findViewById(R.id.view_search_list);

        //ListView setting
        adapter = new SearchResultsAdapter(context, handleIntent() , 0);
        listview.setAdapter(adapter);

        db = new InstantEventTypeQuery(context);

        /** Query string is bundled in the intent **/
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
        }
    }

    private Cursor handleIntent(Intent intent) {

        Cursor cursor = null;

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            cursor = db.getWordMatches(query, null);
            //process Cursor and display results
        }

        return cursor;
    }

    /**
     *  This method takes the resulting cursor from the "search method" and then inflate the
     *  result in listView.a
     * @param query
     */
    private void showResults(String query) {
        Cursor cursor = searchCustomer((query != null ? query.toString() : "@@@@"));

        if (cursor == null) {
            //
        } else {
            // Specify the columns we want to display in the result
            String[] from = new String[]{
                    KEY_CUSTOMER,
                    KEY_NAME,
                    KEY_ADDRESS,
                    KEY_CITY,
                    KEY_STATE,
                    KEY_ZIP};

            // Specify the Corresponding layout elements where we want the columns to go
            int[] to = new int[]{R.id.scustomer,
                    R.id.sname,
                    R.id.saddress,
                    R.id.scity,
                    R.id.sstate,
                    R.id.szipCode};

            // Create a simple cursor adapter for the definitions and apply them to the ListView
            android.widget.SimpleCursorAdapter customers = new android.widget.SimpleCursorAdapter(this, R.layout.customerresult, cursor, from, to);
            mListView.setAdapter(customers);

            // Define the on-click listener for the list items
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Get the cursor, positioned to the corresponding row in the result set
                    Cursor cursor = (Cursor) mListView.getItemAtPosition(position);

                    // Get the state's capital from this row in the database.
                    String customer = cursor.getString(cursor.getColumnIndexOrThrow("customer"));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    String city = cursor.getString(cursor.getColumnIndexOrThrow("city"));
                    String state = cursor.getString(cursor.getColumnIndexOrThrow("state"));
                    String zipCode = cursor.getString(cursor.getColumnIndexOrThrow("zipCode"));

                    //Check if the Layout already exists
                    LinearLayout customerLayout = (LinearLayout) findViewById(R.id.customerLayout);
                    if (customerLayout == null) {
                        //Inflate the Customer Information View
                        LinearLayout leftLayout = (LinearLayout) findViewById(R.id.rightLayout);
                        View customerInfo = getLayoutInflater().inflate(R.layout.selected_customer_detail, leftLayout, false);
                        leftLayout.addView(customerInfo);
                    }

                    //Get References to the TextViews
                    customerText = (TextView) findViewById(R.id.customer);
                    nameText = (TextView) findViewById(R.id.name);
                    addressText = (TextView) findViewById(R.id.address);
                    cityText = (TextView) findViewById(R.id.city);
                    stateText = (TextView) findViewById(R.id.state);
                    zipCodeText = (TextView) findViewById(R.id.zipCode);

                    // Update the parent class's TextView
                    customerText.setText(customer);
                    nameText.setText(name);
                    addressText.setText(address);
                    cityText.setText(city);
                    stateText.setText(state);
                    zipCodeText.setText(zipCode);

                    searchView.setQuery("", true);
                }
            });
        }
    }

    public SearchResultsActivity openDbInstance() throws SQLException {
        mDbHelper = new FTSDatabaseOpenHelper(this);
        mDb = mDbHelper.getReadableDatabase();
        return this;
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


    @Override
    public boolean onClose() {

        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }



    /**
     *  DbHelper class specific for FTS virtual copy of the EventType table.
     */
    private static class FTSDatabaseOpenHelper extends SQLiteOpenHelper {

        private final Context mHelperContext;
        private SQLiteDatabase db;

        private static final String FTS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                        " USING fts3 (" +
                        DataContract.EventTypeEntry._ID + ", " +
                        DataContract.EventTypeEntry.COLUMN_EVENT_NAME + ")";

        FTSDatabaseOpenHelper(Context context) {
            super(context, DataContract.EventTypeEntry.TABLE_NAME, null, DATABASE_VERSION);
            mHelperContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            this.db = db;
            this.db.execSQL(FTS_TABLE_CREATE);
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