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
import android.util.Log;

import java.sql.Date;
import java.util.Arrays;

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

                //TODO When "event" column is updated, insert corresponding rows in the Event Table


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

    //TODO Each time app is up, Insert rows of days up to the current date.

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

        Log.v("Content provider", "ID: " + String.valueOf(id));
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
        int rowsUpdated;

        switch (match) {

            case CODE_CALENDAR:
                rowsUpdated = updateCalendar(uri, contentValues, selection, selectionArgs);
                break;

            case CODE_CALENDAR_ID:
                selection = CalendarEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsUpdated = updateCalendar(uri, contentValues, selection, selectionArgs);
                break;

            case CODE_EVENT_TYPE:
                rowsUpdated = updateEventType(uri, contentValues, selection, selectionArgs);
                break;

            case CODE_EVENT_TYPE_ID:
                selection = EventTypeEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsUpdated = updateEventType(uri, contentValues, selection, selectionArgs);
                break;

            case CODE_EVENT:
                rowsUpdated = updateEvent(uri, contentValues, selection, selectionArgs);
                break;

            case CODE_EVENT_ID:
                selection = EventTypeEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsUpdated = updateEvent(uri, contentValues, selection, selectionArgs);
                break;

            default: throw new IllegalArgumentException("ContentProvider (update) cannot" +
                    "handle unsupported URI" + uri);
        }
            if (rowsUpdated != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }

            return rowsUpdated;
    }

    //TODO: when this method is called, check the # of events, then insert that # of rows to event table.
    private int updateCalendar(Uri uri, ContentValues contentValues,
                               String selection, String[] selectionArgs) {
        int numOfRowsUpdated;
        SQLiteDatabase database= dbHelper.getWritableDatabase();

        //get the current day's date to insert as the new calendar row
        java.util.Date today = new java.util.Date();
        java.sql.Date sqlDate = new java.sql.Date(today.getTime());
        String currentDate = sqlDate.toString();
        Log.v("Date example", "date is set as: " + currentDate);

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

        //sanity check
        if (contentValues.containsKey(CalendarEntry.COLUMN_DAY_TAG)) {
            String dayTag = contentValues.getAsString(CalendarEntry.COLUMN_DAY_TAG);
            if (dayTag == null) throw new IllegalArgumentException("ContentProvider " +
                    "(Update method, calendarEntry) has received null for day tag value." );
        }

        //finally, update the specified row.
        numOfRowsUpdated = database.update(CalendarEntry.TABLE_NAME, contentValues, selection,
                selectionArgs);
        if (numOfRowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        //check the number of items in event column
//        String events[] = getEventColumnAsArray();

        return numOfRowsUpdated;
    }




    private String[] getEventColumnAsArray(String date) {

        SQLiteDatabase database = dbHelper.getReadableDatabase();

        String projection[] = new String[]{CalendarEntry.COLUMN_EVENT_IDs};

        //convert the day into ID here.　→ pass to selection.

        Cursor rawEvent = database.query(
                CalendarEntry.TABLE_NAME,
                projection,
                date,
                null,
                null,
                null,
                null
        );

        int columnIndex_eventIDs = rawEvent.getColumnIndex(EventEntry.COLUMN_SUB_ID);
        String events = rawEvent.getString(columnIndex_eventIDs);
        // これが　→ [5,5,5,5]みたいになってるので
        String eventsArray[] = events.split(",");

        return eventsArray;
    }

    private int updateEventType(Uri uri, ContentValues contentValues, String selection,
                                String[] selectionArgs) {
        int numOfRowsUpdated;
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        //sanity checks
        if (contentValues.size() == 0) throw new IllegalArgumentException("Content Provider " +
                "(Update method, event type table) received null value for contentValues");

        ///sanity check: if any value comes empty with a key, then tehre's something wrong.
        if (contentValues.containsKey(EventTypeEntry.COLUMN_EVENT_NAME)) {
            String sample_type = contentValues.getAsString(EventTypeEntry.COLUMN_EVENT_NAME);
            if (sample_type == null) throw new IllegalArgumentException("ContentProvider " +
                    "(Update method, event type table) has received null for the new event type.");
        }

        numOfRowsUpdated = database.update(EventTypeEntry.TABLE_NAME, contentValues, selection,
                selectionArgs);

        return numOfRowsUpdated;
    }

    private int updateEvent (Uri uri, ContentValues contentValues, String selection,
                             String[] selectionArgs) {
        int numOfRowsUpdated;
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        //sanity checks
        if (contentValues.size() == 0) throw new IllegalArgumentException("Content Provider " +
                "(Update method, event table) received null value for contentValues");

        //sanity check: if any value comes empty with a key, then there's something wrong

        if (contentValues.containsKey(EventEntry.COLUMN_SUB_ID)) {
            String sample_ID = contentValues.getAsString(EventEntry.COLUMN_SUB_ID);
            if (sample_ID == null) throw new IllegalArgumentException("ContentProvider " +
                    "(Update method, event table) received null value for contentValues");
        }

        if (contentValues.containsKey(EventEntry.COLUMN_WEIGHT_SEQUENCE)) {
            String sample_weight_sequence = contentValues.getAsString(EventEntry.COLUMN_REP_SEQUENCE);
            if (sample_weight_sequence == null) throw new IllegalArgumentException("ContentProvider" +
                    "(Update method, event table) has received null value for contentValues");
        }

        numOfRowsUpdated = database.update(EventEntry.TABLE_NAME, contentValues, selection,
                selectionArgs);

        return numOfRowsUpdated;
    }

    // Not planning to export this content provider to any other applications--
    // No need to implement this as of now.
    @Nullable
    @Override
    public String getType(Uri uri) {
        /*int match = matcher.match(uri);

        switch (match) {

            case CODE_CALENDAR:
                break;
            case CODE_CALENDAR_ID:
                break;

            case CODE_EVENT:
                break;

            case CODE_EVENT_ID:
                break;

            case CODE_EVENT_TYPE:
                break;
            case CODE_EVENT_TYPE_ID:
                break;

        }*/

        return null;
    }
}
