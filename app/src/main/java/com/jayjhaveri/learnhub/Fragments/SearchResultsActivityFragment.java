package com.jayjhaveri.learnhub.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.jayjhaveri.learnhub.R;
import com.jayjhaveri.learnhub.adapter.SearchAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class SearchResultsActivityFragment extends Fragment implements SearchAdapter.SearchEmptyListener {

    @BindView(R.id.rv_video_list)
    RecyclerView rv_video_list;
    @BindView(R.id.tv_empty_recyclerView)
    TextView tv_empty;
    @BindView(R.id.pb_video_list)
    ProgressBar pb_video_list;
    //Layoutmanager
    LinearLayoutManager linearLayoutManager;
    private String searchQuery;
    private SearchAdapter searchAdapter;
    private DatabaseReference databaseReference;
    private FirebaseStorage firebaseStorage;

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
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_all_video, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        linearLayoutManager = new LinearLayoutManager(getActivity());
        rv_video_list.setLayoutManager(linearLayoutManager);

        DatabaseReference videosRef = databaseReference.child("videos");
        if (searchQuery != null) {
            searchAdapter = new SearchAdapter(getActivity(), videosRef, firebaseStorage, searchQuery, this);
        }
        rv_video_list.setAdapter(searchAdapter);
    }


    @Override
    public void searchEmpty(int size) {
        if (size == 0) {
            tv_empty.setVisibility(View.VISIBLE);
            pb_video_list.setVisibility(View.GONE);
        } else {
            tv_empty.setVisibility(View.GONE);
            pb_video_list.setVisibility(View.GONE);
        }
    }
}
