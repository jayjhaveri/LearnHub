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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jayjhaveri.learnhub.R;
import com.jayjhaveri.learnhub.VideoDetailActivity;
import com.jayjhaveri.learnhub.model.VideoDetail;
import com.jayjhaveri.learnhub.viewholder.VideoViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ADMIN-PC on 09-04-2017.
 */

public class SearchAdapter extends RecyclerView.Adapter<VideoViewHolder> {

    FirebaseStorage firebaseStorage;
    List<VideoDetail> videoLists = new ArrayList<>();
    List<String> videoListKeys = new ArrayList<>();
    DatabaseReference videoRef;
    Context context;

    ChildEventListener childEventListener;

    public SearchAdapter(Context context, DatabaseReference videoRef, FirebaseStorage firebaseStorage, final String searchQuery) {
        this.videoRef = videoRef;
        this.context = context;
        this.firebaseStorage = firebaseStorage;
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                VideoDetail videoDetail = dataSnapshot.getValue(VideoDetail.class);
                String key = dataSnapshot.getKey();
                String searchLower = searchQuery.toLowerCase();
                if (videoDetail.category.toLowerCase().contains(searchLower)
                        || videoDetail.body.toLowerCase().contains(searchLower)
                        || videoDetail.author.toLowerCase().contains(searchLower)
                        || videoDetail.body.toLowerCase().contains(searchLower)
                        || videoDetail.title.toLowerCase().contains(searchLower)) {

                    notifyItemInserted(videoLists.size() - 1);
                    videoListKeys.add(key);
                    videoLists.add(videoDetail);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();

                // [START_EXCLUDE]
                int sortIndex = videoListKeys.indexOf(key);
                if (sortIndex > -1) {
                    // Remove data from the list
                    videoListKeys.remove(sortIndex);
                    videoLists.remove(sortIndex);
                    // Update the RecyclerView
                    notifyItemRemoved(sortIndex);
                } else {
                    Log.w("Adapter", "onChildRemoved:unknown_child:" + key);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        videoRef.addChildEventListener(childEventListener);
        this.childEventListener = childEventListener;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final VideoViewHolder holder, int position) {
        final VideoDetail videoDetail = videoLists.get(position);
        StorageReference imageReference = firebaseStorage.getReferenceFromUrl(videoDetail.imageUrl);
        holder.bindToPost(context, videoDetail, imageReference);

        holder.bt_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, VideoDetailActivity.class);
                intent.putExtra(VideoDetailActivity.EXTRA_POST_KEY, videoListKeys.get(holder.getAdapterPosition()));
                intent.putExtra(VideoDetailActivity.EXTRA_VIDEO_DETAIL, videoDetail);
                context.startActivity(intent);
            }
        });

        holder.iv_popUp_menu.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return videoLists.size();
    }

    public void cleanUpAdapter() {
        videoRef.removeEventListener(childEventListener);
    }
}
