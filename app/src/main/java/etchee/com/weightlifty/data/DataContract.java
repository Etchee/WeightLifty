package etchee.com.weightlifty.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by rikutoechigoya on 2017/03/24.
 */

public final class DataContract {

    //universal declarations
    public static final String CONTENT_AUTHORITY = "etchee.com.weightlifty";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Specific URI addresses
    public static final String PATH_CALENDAR = "calendar";
    public static final String PATH_EVENT_TYPE = "event_type";
    public static final String PATH_EVENT = "event";

    //private, contract cannot be instantiated
    private DataContract() {

    }

    public static final class CalendarEntry implements BaseColumns {

        //content URI for this table
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CALENDAR).build();
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CALENDAR;

        public static final String TABLE_NAME = "calendar";
        public static final String _ID = BaseColumns._ID;

        //and the columns
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_EVENT_IDs = "event_id";
        public static final String COLUMN_DAY_TAG = "day_note";
    }

    public static final class EventEntry implements BaseColumns {

        //content URI for this table
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENT).build();

        public static final String TABLE_NAME = "event";
        public static final String _ID = BaseColumns._ID;

        //and the columns
        public static final String COLUMN_SUB_ID = "sub_ID";
        public static final String COLUMN_EVENT_ID = "event_ID";
        public static final String COLUMN_SET_COUNT = "set_count";
        public static final String COLUMN_REP_SEQUENCE = "rep_sequence";
        public static final String COLUMN_WEIGHT_SEQUENCE = "weight_sequence";

    }


    public static final class EventTypeEntry implements BaseColumns {

        //content URI for this table
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENT_TYPE).build();

        public static final String TABLE_NAME = "event_type";
        public static final String _ID = BaseColumns._ID;

        //and the columns
        public static final String COLUMN_EVENT_NAME = "event_name";
    }
}
