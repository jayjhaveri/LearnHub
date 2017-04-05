package com.jayjhaveri.learnhub.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.jayjhaveri.learnhub.CategoryActivity;
import com.jayjhaveri.learnhub.UserVideosActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class MostRecentFragment extends VideoListFragment {

    boolean isCategoryActivity = false;
    private boolean isUserVideosActivity = false;

    public MostRecentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public Query getQuery(DatabaseReference databaseReference) {
        // [START recent_posts_query]
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys
        Query recentPostsQuery;
        if (isCategoryActivity){
            recentPostsQuery = databaseReference.child("categories").child(CategoryActivity.categoryName).limitToFirst(100);

        } else if (isUserVideosActivity) {
            recentPostsQuery = databaseReference.child("user-videos").child(UserVideosActivity.uid).limitToFirst(100);
        } else {
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
        } else if (context instanceof UserVideosActivity) {
            isUserVideosActivity = true;
        }
    }
}
