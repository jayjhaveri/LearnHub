package com.jayjhaveri.learnhub.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jayjhaveri.learnhub.R;
import com.jayjhaveri.learnhub.VideoDetailActivity;
import com.jayjhaveri.learnhub.model.VideoDetail;
import com.jayjhaveri.learnhub.viewholder.VideoViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ADMIN-PC on 21-03-2017.
 */

public abstract class VideoListFragment extends Fragment {

    private static final String TAG = "VideoListFragment";

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]

    private FirebaseStorage mStorage;
    private StorageReference mImageReference;
    private StorageReference mProfileImageReference;

    private FirebaseRecyclerAdapter<VideoDetail, VideoViewHolder> mAdapter;

    @BindView(R.id.rv_video_list)
     RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    public VideoListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
         super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_video, container, false);
        ButterKnife.bind(this,rootView);
        //Firebase database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mRecycler.setHasFixedSize(true);

        mStorage = FirebaseStorage.getInstance();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);

        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getQuery(mDatabase);

        mAdapter = new FirebaseRecyclerAdapter<VideoDetail, VideoViewHolder>(VideoDetail.class, R.layout.list_video,
                VideoViewHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(VideoViewHolder viewHolder, final VideoDetail model, final int position) {
                final DatabaseReference videoRef = getRef(position);

                Log.d("Model",model.body);
                mImageReference = mStorage.getReferenceFromUrl(model.imageUrl);
                final String videoKey = videoRef.getKey();

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), VideoDetailActivity.class);
                        intent.putExtra(VideoDetailActivity.EXTRA_POST_KEY,videoKey);
                        intent.putExtra(VideoDetailActivity.EXTRA_VIDEO_URL, model.videoUrl);
                        startActivity(intent);
                    }
                });

                viewHolder.bindToPost(getActivity(),model, mImageReference);
            }
        };

        mRecycler.setAdapter(mAdapter);

    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);
}
