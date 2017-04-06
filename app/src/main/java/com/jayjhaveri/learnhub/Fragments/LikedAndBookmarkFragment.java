package com.jayjhaveri.learnhub.Fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * A placeholder fragment containing a simple view.
 */
public class LikedAndBookmarkFragment extends VideoListFragment {


    public LikedAndBookmarkFragment() {

    }


    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        return databaseReference.child("videos")
                .limitToFirst(100);
    }
}
