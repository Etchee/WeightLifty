package etchee.com.weightlifty.Activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ListView;

import etchee.com.weightlifty.Adapter.SearchResultsAdapter;
import etchee.com.weightlifty.R;
import etchee.com.weightlifty.data.InstantEventTypeQuery;

/**
 * Created by rikutoechigoya on 2017/04/21.
 */

public class SearchResultsActivity extends ListActivity {

    private String query;
    private InstantEventTypeQuery db;
    private Context context;
    private ListView listview;
    private SearchResultsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = SearchResultsActivity.this;
        listview = (ListView)findViewById(R.id.view_search_list);
        adapter = new SearchResultsAdapter(context, handleIntent() , 0);
        listview.setAdapter();

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
}