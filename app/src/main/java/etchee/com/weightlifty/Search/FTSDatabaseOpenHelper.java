package etchee.com.weightlifty.Search;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import etchee.com.weightlifty.data.DataContract;

import static android.content.ContentValues.TAG;
import static etchee.com.weightlifty.data.DataContract.EventType_FTSEntry.DATABASE_VERSION;
import static etchee.com.weightlifty.data.DataContract.EventType_FTSEntry.FTS_DATABASE_NAME;
import static etchee.com.weightlifty.data.DataContract.EventType_FTSEntry.FTS_TABLE_CREATE;

/**
 * Created by rikutoechigoya on 2017/04/27.
 */

public class FTSDatabaseOpenHelper extends SQLiteOpenHelper {
    private Context context;

    public FTSDatabaseOpenHelper(Context context) {
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
        db.execSQL("DROP TABLE IF EXISTS " + FTS_DATABASE_NAME); //db name is imported from the Contract
        onCreate(db);
    }


    //TODO Create a method here to copy eventType table into FTS3
    private void copyDataFromEventTypeTable() {

    }
}
