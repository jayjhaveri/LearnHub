package com.jayjhaveri.learnhub.Fragments;


import android.content.Context;
import android.support.v4.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.jayjhaveri.learnhub.CategoryActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class MostPopularFragment extends VideoListFragment {

    boolean isCategoryActivity = false;

    public MostPopularFragment() {
        // Required empty public constructor
    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        Query myTopPostsQuery;
        if (isCategoryActivity) {
            myTopPostsQuery = databaseReference.child("categories").child(CategoryActivity.categoryName).orderByChild("views");
        } else {
            myTopPostsQuery = databaseReference.child("videos").orderByChild("views");
        }
        return myTopPostsQuery;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof CategoryActivity) {
            isCategoryActivity = true;
        }
    }


}
