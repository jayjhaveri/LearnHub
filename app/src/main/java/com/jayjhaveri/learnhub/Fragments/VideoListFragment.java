package com.jayjhaveri.learnhub.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

    public static final String EXTRA_VIDEO_UID = "video_uid";
    private static final String TAG = "VideoListFragment";
    @BindView(R.id.rv_video_list)
    RecyclerView rv_video_list;
    // [END define_database_reference]
    // [START define_database_reference]
    private DatabaseReference databaseReference;
    private FirebaseStorage firebaseStorage;
    private StorageReference imageReference;
    private StorageReference mProfileImageReference;
    private FirebaseRecyclerAdapter<VideoDetail, VideoViewHolder> firebaseRecyclerAdapter;
    private LinearLayoutManager linearLayoutManager;


    public VideoListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
         super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_video, container, false);
        ButterKnife.bind(this,rootView);
        //Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference();
        rv_video_list.setHasFixedSize(true);

        firebaseStorage = FirebaseStorage.getInstance();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        rv_video_list.setLayoutManager(linearLayoutManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getQuery(databaseReference);

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<VideoDetail, VideoViewHolder>(VideoDetail.class, R.layout.list_video,
                VideoViewHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(VideoViewHolder viewHolder, final VideoDetail model, final int position) {
                final DatabaseReference videoRef = getRef(position);


                imageReference = firebaseStorage.getReferenceFromUrl(model.imageUrl);
                final String videoKey = videoRef.getKey();

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), VideoDetailActivity.class);
                        intent.putExtra(VideoDetailActivity.EXTRA_POST_KEY,videoKey);
                        intent.putExtra(VideoDetailActivity.EXTRA_VIDEO_DETAIL, model);
                        startActivity(intent);
                    }
                });

                viewHolder.bindToPost(getActivity(), model, imageReference);
            }
        };

        rv_video_list.setAdapter(firebaseRecyclerAdapter);

    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);
}
