package etchee.com.weightlifty.Activity;

import android.app.ListFragment;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.github.clans.fab.FloatingActionButton;

import etchee.com.weightlifty.Adapter.ListAdapter;
import etchee.com.weightlifty.Adapter.SearchAdapter;
import etchee.com.weightlifty.R;
import etchee.com.weightlifty.data.DataContract;

/**
 * Created by rikutoechigoya on 2017/05/12.
 */

public class CurrentListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private ListView listview;
    private FloatingActionButton fab;

    //To make sure that there is only one instance because OpenHelper will serialize requests anyways
    private ContentResolver contentResolver;
    private int eventID;
    private final int CREATE_LOADER_ID = 1;
    private final String TAG = getClass().getSimpleName();
    private SearchManager searchManager;
    private Toolbar toolbar;
    private Context context;
    private ListAdapter listAdapter;
    private SearchAdapter searchAdapter;

    //do non-graphical assignments
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();

        contentResolver = context.getContentResolver();
        //cursor init
        Cursor listCursor = initListCursor();
        Cursor eventTypeCursor = initEventTypeCursor();

        //adapter initialization
        searchAdapter = new SearchAdapter(context, eventTypeCursor);
        listAdapter = new ListAdapter(context, listCursor, 0);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //fab setup
        fab = (FloatingActionButton)findViewById(R.id.list_fab);

        /**
         *  on FAB click, enter chooseEventMode
         */
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterChooseEventMode();
            }
        });


        //for the empty view
        View emptyView = findViewById(R.id.view_empty);
        listview = (ListView)findViewById(R.id.listview_workout);
        listview.setEmptyView(emptyView);

        listview.setAdapter(listAdapter);

        //listView setup
        listview.setOnItemClickListener(listViewOnItemClickSetup());

        //only display today's data for now
        Bundle bundle = new Bundle();
        bundle.putInt(DataContract.GlobalConstants.PASS_CREATE_LOADER_DATE, getDateAsInt());
        getLoaderManager().initLoader(CREATE_LOADER_ID, bundle, this);


    }

    //do view-realted assignments
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_current_fragment, container, false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
