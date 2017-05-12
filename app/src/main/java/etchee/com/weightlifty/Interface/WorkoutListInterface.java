package etchee.com.weightlifty.Interface;

import android.database.Cursor;

/**
 * Created by rikutoechigoya on 2017/05/13.
 */

public interface WorkoutListInterface {
    void onSearchCursorReceived(Cursor cursor);
}
