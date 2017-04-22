package etchee.com.weightlifty.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

/**
 * Created by rikutoechigoya on 2017/04/21.
 */

public class InstantEventTypeQuery {

    private DataDbHelper dbHelper;

    public static final String TAG = DataContract.EventTypeEntry.TABLE_NAME;
    public static final String event_id = "ID";
    public static final String event_string = "eventString";

    private static final String DATABASE_NAME = "DICTIONARY";
    private static final String FTS_VIRTUAL_TABLE = "FTS";
    private static final int DATABASE_VERSION = 1;


    public InstantEventTypeQuery(Context context) {
//        dbHelper = new DataDbHelper(context);
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
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
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


        //TODO Create a method here to copy
        private void copyDataFromEventTypeTable() {

        }
    }




    public Cursor getWordMatches(String query, String[] columns) {
        String selection = DataContract.EventTypeEntry.COLUMN_EVENT_NAME + " MATCH ?";
        String [] selectionArgs = new String[] { query + "*" };

        return query(selection, selectionArgs, columns);
    }

    private Cursor query(String selection, String[] selectionArgs, String[] projection) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(DataContract.EventTypeEntry.TABLE_NAME);

        Cursor cursor = null    ;
        try {
            cursor = builder.query(
                    dbHelper.getReadableDatabase(),
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor == null) return null;
        } finally {
            cursor.close();
        }

        return cursor;
    }

}