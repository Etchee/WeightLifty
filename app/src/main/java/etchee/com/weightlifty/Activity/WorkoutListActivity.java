package etchee.com.weightlifty.Activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;

import java.util.Calendar;
import java.util.Random;

import etchee.com.weightlifty.Adapter.ListViewPagerAdapter;
import etchee.com.weightlifty.DataMethods.subIDfixHelper;
import etchee.com.weightlifty.Fragment.SearchFragment;
import etchee.com.weightlifty.R;
import etchee.com.weightlifty.data.DataContract.EventEntry;

/**
 * Created by etchee.
 *  From MainActivity -> Loads today's data, display as a list.
 *  When SearchView is initiated -> Performs a search, then pass the resulting cursor to SearchAdapter
 */

public class WorkoutListActivity extends AppCompatActivity {

    private SearchFragment fragmentActivity;
    private Activity activity;
    private FloatingActionButton fab;

    //To make sure that there is only one instance because OpenHelper will serialize requests anyways
    private ContentResolver contentResolver;
    private int eventID;
    private final int CREATE_LOADER_ID = 1;
    private final String TAG = getClass().getSimpleName();
    private Toolbar toolbar;
    private Context context;
    private ViewPager viewPager;
    private ListViewPagerAdapter viewPagerAdapter;
    private TabLayout tabLayout;
    private SearchView searchView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        context = getApplicationContext();
        toolbar = (Toolbar) findViewById(R.id.list_toolbar);
        setSupportActionBar(toolbar);
        contentResolver = getContentResolver();
        activity = this;

        //viewpager initialization
        viewPager = (ViewPager)findViewById(R.id.viewPager_fragment_listActivity);
        viewPagerAdapter = new ListViewPagerAdapter(getSupportFragmentManager(), context);
        viewPager.setAdapter(viewPagerAdapter);

        //hook 'em up with the Tabs
        tabLayout = (TabLayout)findViewById(R.id.list_tab);
        tabLayout.setupWithViewPager(viewPager);

        //fab setup
        fab = (FloatingActionButton) findViewById(R.id.listactivity_fab);

        /*
         *  on FAB click, enter chooseEventMode
         */
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterSearchMode();
            }
        });
    }


    private void enterSearchMode() {
        //hide the tab
        tabLayout.setVisibility(View.GONE);
        //and the viewPager
        viewPager.setVisibility(View.GONE);
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_from_left,
                        R.anim.slide_out_to_right,
                        R.anim.slide_in_from_right,
                        R.anim.slide_out_to_left)
                .add(R.id.container_fragment_list, new SearchFragment())
                .commit();
    }

    /**
     *  When creating the option, define the searchView. (Pretty sure this is done right)
     * @param menu menu layout
     * @return  true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_search_button:
                break;

            case R.id.menu_insert_event:
                event_insertDummyValues();
                break;

            case R.id.delete_today_workout:
                //get tab show date format
                //if tabLayout is shown (data is there), perform deletion.
                String lastDate = null;
                if (tabLayout.getSelectedTabPosition() != -1) {
                    Cursor cursor = null;

                    try {
                        cursor = getContentResolver().query(
                            EventEntry.CONTENT_URI,
                                new String[]{EventEntry.COLUMN_FORMATTED_DATE},
                                null,
                                null,
                                null
                        );
                        if (cursor.moveToLast()) {
                            lastDate = cursor.getString(cursor.getColumnIndex(EventEntry.COLUMN_FORMATTED_DATE));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        cursor.close();
                    }
                }else Toast.makeText(activity, "No Event Records.", Toast.LENGTH_SHORT).show();


                int numOfDeletedRows = getContentResolver().delete(
                        EventEntry.CONTENT_URI,
                        EventEntry.COLUMN_FORMATTED_DATE + "=?",
                        new String[]{lastDate}
                );

                Toast.makeText(activity, String.valueOf(numOfDeletedRows) +
                        " events deleted.", Toast.LENGTH_SHORT).show();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private int deleteEventTableDatabase() {
        int numberOfDeletedRows = getContentResolver().delete(
                EventEntry.CONTENT_URI,
                null,
                null
        );
        return numberOfDeletedRows;
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

    private void event_insertDummyValues() {

        ContentValues values = new ContentValues();

        int rep_count = new Random().nextInt(10);
        int set_count = new Random().nextInt(20);
        int date = getDateAsInt();
        int weight_count = 70;
        int sub_ID = getNextSub_id();
        int eventID = new Random().nextInt(900);
        String formattedDate = getFormattedDate();

        values.put(EventEntry.COLUMN_SUB_ID, sub_ID);
        values.put(EventEntry.COLUMN_EVENT_ID, eventID);
        values.put(EventEntry.COLUMN_REP_COUNT, rep_count);
        values.put(EventEntry.COLUMN_SET_COUNT, set_count);
        values.put(EventEntry.COLUMN_WEIGHT_COUNT, weight_count);
        values.put(EventEntry.COLUMN_FORMATTED_DATE, formattedDate);

        Uri uri = getContentResolver().insert(EventEntry.CONTENT_URI, values);

        if (uri == null) throw new IllegalArgumentException("Calendar table (insert dummy)" +
                "failed to insert data. check the MainActivity method and the table.");

    }

    private int getNextSub_id() {
        int sub_id;
        String date = getFormattedDate();

        String projection[] = new String[]{EventEntry.COLUMN_FORMATTED_DATE, EventEntry.COLUMN_SUB_ID};
        String selection = EventEntry.COLUMN_FORMATTED_DATE + "=?";
        String selectionArgs[] = new String[]{date};
        Cursor cursor = null;

        try {
            cursor = getContentResolver().query(
                    EventEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
            );

            //if cursor comes back, there is already some rows
            if (cursor.moveToLast()) {
                int index = cursor.getColumnIndex(EventEntry.COLUMN_SUB_ID);
                sub_id = cursor.getInt(index) + 1;

            } else {
                sub_id = 0;
            }
        } finally {
            cursor.close();
        }


        return sub_id;
    }

    private int getDateAsInt() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;   //month starts from zero
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String concatenated = String.valueOf(year) + String.valueOf(month) + String.valueOf(day);

        return Integer.parseInt(concatenated);
    }

    @Override
    public void onBackPressed() {
        //if in search mode AND the tab is gone, bring it back!
        if (tabLayout.getVisibility() == View.GONE) {
            tabLayout.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE);
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentById(R.id.container_fragment_list))
                    .commit();
        }else super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new subIDfixHelper(getApplicationContext()).execute(getFormattedDate());
    }

}

