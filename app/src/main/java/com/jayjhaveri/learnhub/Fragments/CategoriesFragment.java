package com.jayjhaveri.learnhub.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jayjhaveri.learnhub.BaseApplication;
import com.jayjhaveri.learnhub.CategoryActivity;
import com.jayjhaveri.learnhub.R;
import com.jayjhaveri.learnhub.adapter.CategoryAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class CategoriesFragment extends Fragment implements CategoryAdapter.CategoryClickListener {

    @BindView(R.id.rv_categories)
    RecyclerView rv_categories;

    //CategoryAdapter
    CategoryAdapter mAdapter;



    public CategoriesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_categories, container, false);
        ButterKnife.bind(this,rootView);


        mAdapter = new CategoryAdapter(BaseApplication.categoryList, getActivity(), this);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        rv_categories.setLayoutManager(gridLayoutManager);
        rv_categories.setHasFixedSize(true);
        rv_categories.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onCategoryClick(String categoryName) {
        Intent intent = new Intent(getActivity(), CategoryActivity.class);
        intent.putExtra(getString(R.string.category_name), categoryName);
        startActivity(intent);
    }
}
