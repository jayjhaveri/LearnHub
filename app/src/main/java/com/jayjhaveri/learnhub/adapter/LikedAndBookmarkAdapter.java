package com.jayjhaveri.learnhub.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jayjhaveri.learnhub.R;
import com.jayjhaveri.learnhub.VideoDetailActivity;
import com.jayjhaveri.learnhub.model.VideoDetail;
import com.jayjhaveri.learnhub.viewholder.VideoViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ADMIN-PC on 06-04-2017.
 */

public class LikedAndBookmarkAdapter extends RecyclerView.Adapter<VideoViewHolder> {

    List<String> sortKeys = new ArrayList<>();
    List<VideoDetail> videoDetailList = new ArrayList<>();
    FirebaseStorage firebaseStorage;
    DatabaseReference databaseReference;
    Context context;
    private ChildEventListener childEventListener;


    public LikedAndBookmarkAdapter(Context context, final DatabaseReference videoRef,
                                   final DatabaseReference sortRef, FirebaseStorage firebaseStorage) {

        this.context = context;
        this.firebaseStorage = firebaseStorage;
        this.databaseReference = sortRef;

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final String key = dataSnapshot.getKey();

                sortKeys.add(key);
                videoRef.child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        VideoDetail videoDetail = dataSnapshot.getValue(VideoDetail.class);
                        if (videoDetail != null) {
                            videoDetailList.add(videoDetail);
                            notifyItemInserted(videoDetailList.size() - 1);
                        } else {
                            sortRef.child(key).removeValue();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();

                // [START_EXCLUDE]
                int sortIndex = sortKeys.indexOf(key);
                if (sortIndex > -1) {
                    // Remove data from the list
                    sortKeys.remove(sortIndex);
                    // Update the RecyclerView
                    notifyItemRemoved(sortIndex);
                } else {
                    Log.w("Adapter", "onChildRemoved:unknown_child:" + key);
                }
                // [END_EXCLUDE]
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        this.childEventListener = childEventListener;
        sortRef.addChildEventListener(childEventListener);
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final VideoViewHolder holder, int position) {
        final VideoDetail videoDetail = videoDetailList.get(position);
        StorageReference imageReference = firebaseStorage.getReferenceFromUrl(videoDetail.imageUrl);
        holder.bindToPost(context, videoDetail, imageReference);

        holder.bt_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, VideoDetailActivity.class);
                intent.putExtra(VideoDetailActivity.EXTRA_POST_KEY, sortKeys.get(holder.getAdapterPosition()));
                intent.putExtra(VideoDetailActivity.EXTRA_VIDEO_DETAIL, videoDetail);
                context.startActivity(intent);
            }
        });

        holder.iv_popUp_menu.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return sortKeys.size();
    }

    public void clearAdapter() {
        if (childEventListener != null) {
            databaseReference.removeEventListener(childEventListener);
        }
    }

}
