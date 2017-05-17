package etchee.com.weightlifty.Fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Calendar;

import etchee.com.weightlifty.Activity.EditEventActivity;
import etchee.com.weightlifty.Adapter.SearchAdapter;
import etchee.com.weightlifty.R;
import etchee.com.weightlifty.data.DataContract;
import etchee.com.weightlifty.data.DataContract.EventType_FTSEntry;
import etchee.com.weightlifty.data.DataDbHelper;

/**
 * Launched when searchView is pressed.
 * Created by rikutoechigoya on 2017/05/12.
 */

public class SearchFragment extends android.support.v4.app.Fragment
        implements SearchView.OnCloseListener, SearchView.OnQueryTextListener{

    private ListView listview;
    //To make sure that there is only one instance because OpenHelper will serialize requests anyways
    private final String TAG = getClass().getSimpleName();
    private Context context;
    private SearchAdapter adapter;
    private ContentResolver contentResolver;
    private SearchView searchView;


    //TODO this fragment has to receive inputs from the search bar in the parent Activity
    /*
         So create a interface in the activity and implement in here
     */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();
        contentResolver = context.getContentResolver();
        //pass this activity to the parent Activity
        //TODO do this here
    }


    //assign view components
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new SearchAdapter(context, initEventTypeCursor());
        listview = (ListView)view.findViewById(R.id.listview_fragment_search);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                launchEditEventActivityWithNewEvent(String.valueOf(adapter.getItem(position)));
            }
        });
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_search_layout, container, false);
    }

    //Send contentValues
    private void launchEditEventActivityWithNewEvent(String eventName) {
        Intent intent = new Intent(getActivity(), EditEventActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(DataContract.GlobalConstants.LAUNCH_EDIT_CODE, DataContract.GlobalConstants.LAUNCH_EDIT_NEW);
        bundle.putString(DataContract.GlobalConstants.PASS_EVENT_STRING, eventName);
        bundle.putString(DataContract.GlobalConstants.PASS_SELECTED_DATE, getFormattedDate());
        intent.putExtras(bundle);
        startActivity(intent);
    }


    private String getFormattedDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);

        int month = calendar.get(Calendar.MONTH) + 1;   //month starts from zero
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String concatenated = String.valueOf(year) + "/" + String.valueOf(month) + "/" + String.valueOf(day);

        return concatenated;
    }

    private Cursor initEventTypeCursor() {
        //do a rawQuery because I want to specify the hidden docid column
        Cursor cursor;
        SQLiteDatabase db = new DataDbHelper(context).getReadableDatabase();
        String query = "SELECT " + " * " + " FROM "
                + DataContract.EventType_FTSEntry.TABLE_NAME;
        cursor = db.rawQuery(query, null);

        if (cursor != null) return cursor;
        else throw new NullPointerException(TAG + ": FTS table cursor initialization has failed");
    }

    /**
     * called when this fragment is attached to the ListActivity.
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search_fragment, menu);

        MenuItem item = menu.findItem(R.id.action_search_button);
        MenuItemCompat.expandActionView(item);
        item.setIcon(R.drawable.ic_search_black_24dp);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setQueryHint(getString(R.string.hint_search_events));
        searchView.setOnQueryTextListener(this);
        searchView.setIconified(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_search_button:
                Toast.makeText(context, "Search View tapped.", Toast.LENGTH_SHORT).show();
                break;

            default: throw new IllegalArgumentException("Menu ID was invalid.");

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onClose() {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        query = query + "*";
        Cursor cursor = queryWorkout(query);
        adapter.swapCursor(cursor);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        newText = newText + "*";
        Cursor cursor = queryWorkout(newText);
        adapter.swapCursor(cursor);
        return false;
    }



    /**
     * Search method that returns a cursor.
     *
     * @param input User input text sent from the UI
     * @return cursor that contains the results of the search
     * @throws SQLException if search fails.
     */
    public Cursor queryWorkout(String input) {
        Log.w(TAG, input);
        /*
                Original sample code output:
                SELECT docid as _id,customer,name,(address1||(case when address2> '' then '
               ' || address2 else '' end)) as address,address1,address2,city,state,zipCode from
               CustomerInfo where searchData MATCH 'Piz*';

               My output in here (success):
               SELECT docid as _id,name_event,type_event FROM table_eventType_FTS WHERE name_event MATCH 'dumb';

                From queryEventIDFromName class:
               SELECT docid as _id,* FROM table_eventType_FTS WHERE name_event MATCH 'press sit-up';

               TODO I should make another column that joints all other columns (key_search) to search EVERYTHING
         */
        String query = "SELECT docid as _id," +
                EventType_FTSEntry.COLUMN_EVENT_NAME + "," +
                EventType_FTSEntry.COLUMN_EVENT_TYPE +
                " FROM " + EventType_FTSEntry.TABLE_NAME +
                " WHERE " + EventType_FTSEntry.COLUMN_EVENT_NAME + " MATCH '" + input + "';";

        Cursor cursor = new DataDbHelper(context).getReadableDatabase().rawQuery(query, null);
        return cursor;
    }
}
