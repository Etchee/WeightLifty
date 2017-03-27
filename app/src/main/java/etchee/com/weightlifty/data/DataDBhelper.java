package etchee.com.weightlifty.data;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import etchee.com.weightlifty.data.DataContract.CalendarEntry;
import etchee.com.weightlifty.data.DataContract.EventEntry;
import etchee.com.weightlifty.data.DataContract.EventTypeEntry;

import static android.R.attr.version;

/**
 * Created by rikutoechigoya on 2017/03/24.
 */

public class DataDBhelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = DataDBhelper.class.getSimpleName();

    private static final String DATABASE_NAME = "log.db";
    private static final int DATABASE_VERSION = 1;

    public DataDBhelper(Context context) {

        super(context, DATABASE_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create the Calendar table
        final String CREATE_CALENDAR_TABLE =
                "CREATE TABLE IF NOT EXISTS " + CalendarEntry.TABLE_NAME + " ("
                + CalendarEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CalendarEntry.COLUMN_DATE + " TEXT, " //ここDateが入るけど、フォーマット？
                + CalendarEntry.COLUMN_EVENT_IDs + " INTEGER, " //どの種目をやったのか、順番にIDでいれてく
                + CalendarEntry.COLUMN_DAY_TAG + " TEXT);";    //その日の状態のノート。オプショナル

        //This table will contain events of workout. i.e. Barbell bench press, pull up, preacher curls...
        //Each event will have its own unique key
        //because I might use Java Rx to search for desired key later on.
        final String CREATE_EVENT_TYPE_TABLE =
                "CREATE TABLE IF NOT EXISTS " + DataContract.EventTypeEntry.TABLE_NAME + " ("
                + EventTypeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + EventTypeEntry.COLUMN_EVENT_NAME + " TEXT NOT NULL);";

        final String CREATE_EVENT_TABLE =
                "CREATE TABLE IF NOT EXISTS " + EventEntry.TABLE_NAME + " ("
                + EventEntry._ID + " INTEGER NOT NULL, "
                + EventEntry.COLUMN_SUB_ID + " INTEGER NOT NULL, "
                + EventEntry.COLUMN_SET_COUNT + " INTEGER NOT NULL, "
                + EventEntry.COLUMN_REP_SEQUENCE + " INTEGER NOT NULL, "
                + EventEntry.COLUMN_WEIGHT_SEQUENCE + " TEXT);";

        db.execSQL(CREATE_CALENDAR_TABLE);
        db.execSQL(CREATE_EVENT_TYPE_TABLE);
        db.execSQL(CREATE_EVENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


    /*
    *   For the helper class to read and write database from device.
    * */
    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "mesage" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }


    }
}
