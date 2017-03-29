package etchee.com.weightlifty.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.CalendarContract;
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
        Uri uri_new;
        switch (match) {

            //query entire calendar table
            case CODE_CALENDAR:
                uri_new = insertInCalendarTable(uri, contentValues);
                break;

            //query the entire event table
            case CODE_EVENT:
                uri_new = insertInEventTable(uri, contentValues);
                break;

            //query entire event_type table
            case CODE_EVENT_TYPE:
                uri_new = insertInEventTypeTable(uri, contentValues);
                break;

            //if URI matches to none of the above, return an exception
            default: throw new IllegalArgumentException("Query method cannot handle " +
                    "unsupported URI: " + uri);
        }

        //if URI is returned with null value, then there's something wrong.
        if (uri == null) throw new IllegalArgumentException("ContentProvider;s insert method is " +
                "returning a null URI.");

        return uri_new;
    }

    //to be used for the actual insert implementation
    private Uri insertInCalendarTable(Uri uri, ContentValues contentValues) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        //id is the ID of the newly inserted row. Returns -1 in case of an error with insertion.
        long id = database.insert(CalendarEntry.TABLE_NAME, null, contentValues);

        String testString = contentValues.getAsString(CalendarEntry.COLUMN_DATE);
        if (testString == null) {
            throw new IllegalArgumentException("Content provider's insert method of " +
                    "the calendar table has received" +
                    "null for the date value. Check what is passed into the insert method.");
        }

        if (id < 0) {
            throw new IllegalArgumentException("Content provider's insertion has failed.");
        }

        //append the id of the newly inserted row, append at the end of the CONTENT_AUTHORITY,
        //then return it.
        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertInEventTable(Uri uri, ContentValues contentValues) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        long id = database.insert(EventEntry.TABLE_NAME, null, contentValues);

        String testString = contentValues.getAsString(EventEntry.COLUMN_REP_SEQUENCE);
        if (testString == null) throw new IllegalArgumentException("Content provider's insert " +
                "method for Event Table has received" +
                "null for the date value. Check what is passed into the insert method");
        if (id < 0) throw new IllegalArgumentException("Content provider's insertion (" +
                "Event Table) has failed.");

        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertInEventTypeTable(Uri uri, ContentValues contentValues) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        long id = database.insert(EventTypeEntry.TABLE_NAME, null, contentValues);

        String testString = contentValues.getAsString(EventTypeEntry.COLUMN_EVENT_NAME);
        if (testString == null) throw new IllegalArgumentException("Content provider's insert" +
                "method for event type table has received null for the event name value.");
        if (id < 0) throw new IllegalArgumentException("Content provider's insertion (Event" +
                "type table) has failed.");

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int numOfRowsDeleted;
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int match = matcher.match(uri);

        switch (match) {

            case CODE_CALENDAR:
                numOfRowsDeleted = database.delete(CalendarEntry.TABLE_NAME, selection, selectionArgs);
                break;

            //query specific in calendar table
            case CODE_CALENDAR_ID:
                // Example URI: content://etchee.com.weightlifty/calendar/3 ←wildcard, "?"
                //since I only have one question mark, I only need one element in the String array
                selection = CalendarEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                numOfRowsDeleted = database.delete(CalendarEntry.TABLE_NAME, selection, selectionArgs);
                break;

            //query the entire event table
            case CODE_EVENT:
                numOfRowsDeleted = database.delete(EventEntry.TABLE_NAME, selection, selectionArgs);
                break;

            //query specific in event table
            case CODE_EVENT_ID:
                selection = EventEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                numOfRowsDeleted = database.delete(EventEntry.TABLE_NAME, selection, selectionArgs);
                break;

            //query entire event_type table
            case CODE_EVENT_TYPE:
                numOfRowsDeleted = database.delete(EventTypeEntry.TABLE_NAME, selection, selectionArgs);
                break;

            //query specific in event_type table
            case CODE_EVENT_TYPE_ID:
                selection = EventTypeEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                numOfRowsDeleted = database.delete(EventTypeEntry.TABLE_NAME, selection, selectionArgs);
                break;

            //if URI matches to none of the above, return an exception
            default: throw new IllegalArgumentException("Query method cannot handle " +
                    "unsupported URI: " + uri);
        }

        if (numOfRowsDeleted < 0) throw new IllegalArgumentException("Content Provider (delete" +
                "method) gave an error. Number of deleted row was 0 or less.");

        return numOfRowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int match = matcher.match(uri);

        switch (match) {

            case CODE_CALENDAR:
                return updateCalendar(uri, contentValues, selection, selectionArgs);

            case CODE_CALENDAR_ID:
                selection = CalendarEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateCalendar(uri, contentValues, selection, selectionArgs);

            default: throw new IllegalArgumentException("ContentProvider (update) cannot" +
                    "handle unsupported URI" + uri);
        }
    }

    private int updateCalendar(Uri uri, ContentValues contentValues,
                               String selection, String[] selectionArgs) {
        int numOfRowsUpdated;
        SQLiteDatabase database= dbHelper.getWritableDatabase();

        //sanity check: don't need to proceed if contentValues is empty
        if (contentValues.size() == 0) throw new IllegalArgumentException("Content Provider" +
                "(Update method) received null value for contentValues");

        //sanity check: if any value comes empty with a key, then there's something wrong.
        if (contentValues.containsKey(CalendarEntry.COLUMN_DATE)) {
            String date = contentValues.getAsString(CalendarEntry.COLUMN_DATE);
            if (date == null) throw new IllegalArgumentException("ContentProvider" +
                    "(update method, calendarEntry) has received null for date value");
        }

        if (contentValues.containsKey(CalendarEntry.COLUMN_EVENT_IDs)) {
            String eventIDs = contentValues.getAsString(CalendarEntry.COLUMN_EVENT_IDs);
            if (eventIDs == null) throw new IllegalArgumentException("ContentProvider" +
                    "(Update method, calendarEntry) has received null for the EventIDs");
        }

        if (contentValues.containsKey(CalendarEntry.COLUMN_DAY_TAG)) {
            String dayTag = contentValues.getAsString(CalendarEntry.COLUMN_DAY_TAG);
            if (dayTag == null) throw new IllegalArgumentException("ContentProvider " +
                    "(Update method, calendarEntry) has received null for day tag value." );
        }

        numOfRowsUpdated = database.update(CalendarEntry.TABLE_NAME, contentValues, selection,
                selectionArgs);
        if (numOfRowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numOfRowsUpdated;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }
}
