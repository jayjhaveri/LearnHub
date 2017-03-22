package com.jayjhaveri.learnhub.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jayjhaveri.learnhub.CategoryActivity;
import com.jayjhaveri.learnhub.R;
import com.jayjhaveri.learnhub.adapter.CategoryAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.jayjhaveri.learnhub.MainActivity.categoryList;

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


        mAdapter = new CategoryAdapter(categoryList,getActivity(),this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        rv_categories.setLayoutManager(mLayoutManager);
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
        intent.putExtra("name",categoryName);
        startActivity(intent);
    }
}
