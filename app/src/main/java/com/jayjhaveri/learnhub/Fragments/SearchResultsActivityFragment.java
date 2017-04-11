package com.jayjhaveri.learnhub.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * A placeholder fragment containing a simple view.
 */
public class SearchResultsActivityFragment extends VideoListFragment {

    private Query search;
    private String searchQuery;

    public SearchResultsActivityFragment() {
    }

    public static SearchResultsActivityFragment newInstance(String searchQuery) {

        Bundle bundle = new Bundle();
        bundle.putString("search", searchQuery);

        SearchResultsActivityFragment searchResultsActivityFragment = new SearchResultsActivityFragment();
        searchResultsActivityFragment.setArguments(bundle);

        return searchResultsActivityFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchQuery = getArguments().getString("search");
    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {

        Log.d("Query", searchQuery);

        search = databaseReference.child("videos").orderByChild("title").startAt(searchQuery)
                .endAt(searchQuery + "\uf8ff");


        return databaseReference.child("videos");

    }
}
