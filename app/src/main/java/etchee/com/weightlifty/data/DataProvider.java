package etchee.com.weightlifty.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import etchee.com.weightlifty.data.DataContract.*;

import static etchee.com.weightlifty.data.DataContract.CONTENT_AUTHORITY;
import static etchee.com.weightlifty.data.DataContract.PATH_CALENDAR;
import static etchee.com.weightlifty.data.DataContract.PATH_EVENT;
import static etchee.com.weightlifty.data.DataContract.PATH_EVENT_TYPE;

/**
 * Created by rikutoechigoya on 2017/03/24.
 */

public class DataProvider extends ContentProvider {

    private DataDBhelper dbHelper;

    //adding codes to different type of URIs that this provider can handle.
    private static final int CODE_CALENDAR = 100;
    private static final int CODE_CALENDAR_ID = 101;
    private static final int CODE_EVENT_TYPE = 200;
    private static final int CODE_EVENT_TYPE_ID = 201;
    private static final int CODE_EVENT = 300;
    private static final int CODE_EVENT_ID = 301;

    //sURIMatcher declaration. Call a static reference here
    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        //CALENDAR: querying the entire table i.e. inserting a new row
        matcher.addURI(CONTENT_AUTHORITY, PATH_CALENDAR, CODE_CALENDAR);
        //CALENDAR: querying a certain row from the table
        matcher.addURI(CONTENT_AUTHORITY, PATH_CALENDAR + "/#", CODE_CALENDAR_ID);

        //EVENT_TYPE: querying the entire table i.e. inserting a new row
        matcher.addURI(CONTENT_AUTHORITY, PATH_EVENT_TYPE, CODE_EVENT_TYPE);
        //EVENT_TYPE: querying a certain row from the table i.e. fixing typo in event name
        matcher.addURI(CONTENT_AUTHORITY, PATH_EVENT_TYPE + "/#", CODE_EVENT_TYPE_ID);

        //EVENT: querying the entire table i.e. inserting a new row
        matcher.addURI(CONTENT_AUTHORITY, PATH_EVENT, CODE_EVENT);
        //EVENT: querying a certain row from the table i.e. adding weight and reps info
        matcher.addURI(CONTENT_AUTHORITY, PATH_EVENT + "/#", CODE_EVENT_ID);
    }


    @Override
    public boolean onCreate() {
        dbHelper = new DataDBhelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = null;  //bring back the result of the query

        int match = matcher.match(uri);

        switch (match) {

            //query entire calendar table
            case CODE_CALENDAR:
               cursor = database.query(
                        CalendarEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null);
                break;
            //query specific in calendar table
            case CODE_CALENDAR_ID:
                // Example URI: content://etchee.com.weightlifty/calendar/3 ←wildcard, "?"
                //since I only have one question mark, I only need one element in the String array

                selection = CalendarEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(
                        CalendarEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null);
                break;

            //query the entire event table
            case CODE_EVENT:
                cursor = database.query(
                        EventEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null);
                break;

            //query specific in event table
            case CODE_EVENT_ID:
                selection = EventEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(
                        EventEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null);
                break;

            //query entire event_type table
            case CODE_EVENT_TYPE:
                cursor = database.query(
                        EventEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null);
                break;

            //query specific in event_type table
            case CODE_EVENT_TYPE_ID:
                selection = EventTypeEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(
                        EventTypeEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null);
                break;

            //if URI matches to none of the above, return an exception
            default: throw new IllegalArgumentException("Query method cannot handle " +
                    "unsupported URI: " + uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        int match = matcher.match(uri);
        switch (match) {

            //query entire calendar table
            case CODE_CALENDAR:

                break;
            //query specific in calendar table
            case CODE_CALENDAR_ID:
                // Example URI: content://etchee.com.weightlifty/calendar/3 ←wildcard, "?"
                //since I only have one question mark, I only need one element in the String array


                break;

            //query the entire event table
            case CODE_EVENT:

                break;

            //query specific in event table
            case CODE_EVENT_ID:

                break;

            //query entire event_type table
            case CODE_EVENT_TYPE:

                break;

            //query specific in event_type table
            case CODE_EVENT_TYPE_ID:

                break;

            //if URI matches to none of the above, return an exception
            default: throw new IllegalArgumentException("Query method cannot handle " +
                    "unsupported URI: " + uri);
        }
        return null;
    }

    private Uri insertInCalendar(Uri uri, ContentValues contentValues) {
        
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }
}
