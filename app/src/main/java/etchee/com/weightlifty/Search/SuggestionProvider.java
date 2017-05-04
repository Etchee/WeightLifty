package etchee.com.weightlifty.Search;

import android.content.SearchRecentSuggestionsProvider;

import etchee.com.weightlifty.data.DataContract;

/**
 * Created by rikutoechigoya on 2017/05/04.
 */

public class SuggestionProvider extends SearchRecentSuggestionsProvider {
    public SuggestionProvider() {
        setupSuggestions("suggestionProviderAuthority", this.DATABASE_MODE_QUERIES);
    }
}
