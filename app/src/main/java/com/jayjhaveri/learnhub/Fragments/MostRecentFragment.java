package com.jayjhaveri.learnhub.Fragments;


import android.content.Context;
import android.support.v4.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.jayjhaveri.learnhub.CategoryActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class MostRecentFragment extends VideoListFragment {

    boolean isCategoryActivity = false;

    public MostRecentFragment() {
        // Required empty public constructor
    }

    public Query getQuery(DatabaseReference databaseReference) {
        // [START recent_posts_query]
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys
        Query recentPostsQuery;
        if (isCategoryActivity){
            recentPostsQuery = databaseReference.child("categories").child(CategoryActivity.categoryName).limitToFirst(100);
        }else {
            recentPostsQuery = databaseReference.child("videos")
                    .limitToFirst(100);
        }


        // [END recent_posts_query]

        return recentPostsQuery;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof CategoryActivity){
            isCategoryActivity = true;
        }
    }
}
