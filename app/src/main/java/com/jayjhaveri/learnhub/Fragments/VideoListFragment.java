package com.jayjhaveri.learnhub.Fragments;

import android.content.Context;
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
import com.jayjhaveri.learnhub.BookmarkActivity;
import com.jayjhaveri.learnhub.LikeVideosActivity;
import com.jayjhaveri.learnhub.R;
import com.jayjhaveri.learnhub.UserVideosActivity;
import com.jayjhaveri.learnhub.VideoDetailActivity;
import com.jayjhaveri.learnhub.adapter.LikedAndBookmarkAdapter;
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

    public final String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @BindView(R.id.rv_video_list)
    RecyclerView rv_video_list;
    //Ternary operator
    String likes = null;
    // [END define_database_reference]
    // [START define_database_reference]
    private DatabaseReference databaseReference;
    private FirebaseStorage firebaseStorage;
    private StorageReference imageReference;
    private StorageReference mProfileImageReference;
    private FirebaseRecyclerAdapter<VideoDetail, VideoViewHolder> firebaseRecyclerAdapter;
    private LinearLayoutManager linearLayoutManager;
    private LikedAndBookmarkAdapter likedAndBookmarkAdapter;
    //Check isUserVideoActivity
    private boolean isUserVideoActivity = false;
    private boolean isLikedVideoActivity = false;
    private boolean isBookMarkActivity = false;

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

        if (isLikedVideoActivity || isBookMarkActivity) {
            DatabaseReference videosRef = databaseReference.child("videos");
            if (isBookMarkActivity) {
                likes = "bookmarks";
            } else {
                likes = "likes";
            }
            DatabaseReference sortRef = databaseReference.child("users").
                    child(UID).child(likes);
            likedAndBookmarkAdapter = new LikedAndBookmarkAdapter(getActivity(), videosRef, sortRef, firebaseStorage);
            rv_video_list.setAdapter(likedAndBookmarkAdapter);
        } else {

            firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<VideoDetail, VideoViewHolder>(VideoDetail.class,
                    R.layout.list_video,
                    VideoViewHolder.class,
                    postsQuery) {
                @Override
                protected void populateViewHolder(VideoViewHolder viewHolder, final VideoDetail model, final int position) {
                    DatabaseReference videoRef = getRef(position);


                    imageReference = firebaseStorage.getReferenceFromUrl(model.imageUrl);
                    final String videoKey = videoRef.getKey();


                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), VideoDetailActivity.class);
                            intent.putExtra(VideoDetailActivity.EXTRA_POST_KEY, videoKey);
                            intent.putExtra(VideoDetailActivity.EXTRA_VIDEO_DETAIL, model);
                            startActivity(intent);
                        }
                    });

                    viewHolder.bt_item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), VideoDetailActivity.class);
                            intent.putExtra(VideoDetailActivity.EXTRA_POST_KEY, videoKey);
                            intent.putExtra(VideoDetailActivity.EXTRA_VIDEO_DETAIL, model);
                            startActivity(intent);
                        }
                    });


                    viewHolder.bindToPost(getActivity(), model, imageReference);
                }
            };

            rv_video_list.setAdapter(firebaseRecyclerAdapter);
        }


    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);

    @Override
    public void onAttach(Context context) {
        if (context instanceof UserVideosActivity) {
            isUserVideoActivity = true;
        } else if (context instanceof BookmarkActivity) {
            isBookMarkActivity = true;
        } else if (context instanceof LikeVideosActivity) {
            isLikedVideoActivity = true;
        }
        super.onAttach(context);
    }

}
