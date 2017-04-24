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
import android.widget.ListView;

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

    private void showResults(String query) {

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
    public Cursor searchCustomer(String inputText) throws SQLException {
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