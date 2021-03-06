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
    public static final String PATH_FTS = "EventType_FTS";

    //private, contract cannot be instantiated
    private DataContract() {

    }


    public static final class GlobalConstants {

        //When an item in listActivity is clicked, pass the item ID
        public static final String SUB_ID = "item_sub_id";
        //From ChooseEventActivity, contentValues will be passed.
        public static final String CONTENT_VALUES = "contentValues";

        public static final String VIEWPAGER_POSITION_AS_DATE = "viewpager_position";

        //in the inner class in EditEventActivity, "QueryEventName", id for making a loader.
        public static final int QUERY_EVENT_NAME = 0;
        public static final int QUERY_EVENT_TYPE = 2;
        public static final int QUERY_EVENT_ID = 4;

        public static final int QUERY_SETS_NUMBER = 8;
        public static final int QUERY_REPS_COUNT = 16;
        public static final int QUERY_WEIGHT_COUNT = 32;

        public static final int LAUNCH_EDIT_NEW = 0;
        public static final int LAUNCH_EDIT_EXISTING = 1;
        public static final String LAUNCH_EDIT_CODE = "LAUNCHEDIT";

        public static final String PASS_SUB_ID = "subIDpassed";
        public static final String PASS_SELECTED_DATE = "Date";

        public static final String PASS_CREATE_LOADER_DATE = "passdate";


        public static final String PASS_EVENT_ID = "eventIDpassed";
        public static final String PASS_EVENT_STRING = "eventStringPassed";

        public static final String ORDER_DESCENDING = " DESC";
        public static final String ORDER_ASCENDING = " ASC";

        public static final String NAME_PREFERENCE_FILE = "etchee.com.PREFERENCE_KEY";
        public static final String UNIT_METRIC = "unit_metric";
        public static final String UNIT_IMPEREIAL = "unit_imperial";
    }

    public static final class EventSuggestion_Provider {
        public static final String AUTHORITY = "provider-authority";
    }

    public static final class EventType_FTSEntry {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FTS).build();
        public static final String TABLE_NAME = "table_eventType_FTS";
        public static final String COLUMN_EVENT_NAME = "name_event";
        public static final String COLUMN_EVENT_TYPE = "type_event";
        public static final String COLUMN_ROW_ID = "docid";
        public static final String TABLE_NAME_CONTENT = TABLE_NAME + "_content";
    }

    public static final class CalendarEntry implements BaseColumns {

        //content URI for this table
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CALENDAR).build();
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CALENDAR;

        public static final String TABLE_NAME = "table_calendar";
        public static final String _ID = BaseColumns._ID;

        //and the columns
        public static final String COLUMN_DATE = "table_calendar_date";
        public static final String COLUMN_EVENT_IDs = "table_calendar_event_id";
        public static final String COLUMN_DAY_TAG = "table_calendar_day_note";
    }

    public static final class EventEntry implements BaseColumns {

        //content URI for this table
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENT).build();

        public static final String TABLE_NAME = "table_event";
        public static final String _ID = BaseColumns._ID;

        //and the columns
        public static final String COLUMN_SUB_ID = "table_event_sub_id";
        public static final String COLUMN_EVENT_ID = "table_event_event_id";
        public static final String COLUMN_SET_COUNT = "table_event_set_count";
        public static final String COLUMN_REP_COUNT = "table_event_rep_count";
        public static final String COLUMN_WEIGHT_COUNT = "table_event_weight_count";
        public static final String COLUMN_FORMATTED_DATE = "formatted_date";

    }


    public static final class EventTypeEntry implements BaseColumns {

        //content URI for this table
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENT_TYPE).build();

        public static final String TABLE_NAME = "table_eventType";
        public static final String _ID = BaseColumns._ID;

        //and the columns
        public static final String COLUMN_EVENT_NAME = "table_eventType_event_name";
        public static final String COLUMN_EVENT_TYPE = "table_eventType_event_type";
    }
}
