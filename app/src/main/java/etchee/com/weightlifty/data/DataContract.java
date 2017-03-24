package etchee.com.weightlifty.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by rikutoechigoya on 2017/03/24.
 */

public final class DataContract {

    //private, contract cannot be instantiated
    private DataContract() {

    }

    public static final class EventEntry implements BaseColumns {
        public final static String TABLE_NAME = "workout_log";
        public final static String _ID = BaseColumns._ID;


        public final static String COLUMN_EXAMPLE = "event_example";


        public static final String CONTENT_AUTHORITY = "etchee.com.weightlifty";
        public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
        public static final String PATH_LOG = "pets";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_LOG);
    }
}
