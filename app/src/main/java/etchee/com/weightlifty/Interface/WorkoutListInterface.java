package etchee.com.weightlifty.Interface;

import android.database.Cursor;
import android.support.v7.widget.SearchView;

/**
 * Created by rikutoechigoya on 2017/05/13.
 */

public interface WorkoutListInterface {
    void onSearchCursorReceived(Cursor cursor);
    void onSearchViewImplemented(SearchView searchView);
}
