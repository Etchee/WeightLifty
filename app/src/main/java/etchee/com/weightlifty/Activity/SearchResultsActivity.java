package etchee.com.weightlifty.Activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;
import android.widget.ListView;

import etchee.com.weightlifty.Adapter.SearchResultsAdapter;
import etchee.com.weightlifty.R;
import etchee.com.weightlifty.data.InstantEventTypeQuery;

/**
 * Created by rikutoechigoya on 2017/04/21.
 */

public class SearchResultsActivity extends Activity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private String query;
    private InstantEventTypeQuery db;
    private Context context;
    private ListView listview;
    private SearchResultsAdapter adapter;
    private SearchView searchView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //component setting
        context = SearchResultsActivity.this;
        searchView = (SearchView) findViewById(R.id.search);
        listview = (ListView)findViewById(R.id.view_search_list);

        //ListView setting
        adapter = new SearchResultsAdapter(context, handleIntent() , 0);
        listview.setAdapter(adapter);

        db = new InstantEventTypeQuery(context);

        /** Query string is bundled in the intent **/
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
        }
    }

    private Cursor handleIntent(Intent intent) {

        Cursor cursor = null;

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            cursor = db.getWordMatches(query, null);
            //process Cursor and display results
        }

        return cursor;
    }

    private void showResults(String query) {

    }


    @Override
    public boolean onClose() {

        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}