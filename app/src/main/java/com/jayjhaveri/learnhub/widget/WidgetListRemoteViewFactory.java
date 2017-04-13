package com.jayjhaveri.learnhub.widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jayjhaveri.learnhub.R;
import com.jayjhaveri.learnhub.VideoDetailActivity;
import com.jayjhaveri.learnhub.model.VideoDetail;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Created by ADMIN-PC on 09-04-2017.
 */

public class WidgetListRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {

    private ArrayList<VideoDetail> videoDetailList = new ArrayList<>();
    private ArrayList<String> videoKeys = new ArrayList<>();

    private Context context;
    private Intent intent;

    WidgetListRemoteViewFactory(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;
    }

    private void populateVideoList() {
        Query databaseReference = FirebaseDatabase.getInstance().getReference().child("videos").limitToFirst(10);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                videoDetailList.clear();
                videoKeys.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    videoDetailList.add(snapshot.getValue(VideoDetail.class));
                    videoKeys.add(snapshot.getKey());
                }

                Collections.reverse(videoDetailList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        databaseReference.addValueEventListener(valueEventListener);
        if (!videoDetailList.isEmpty()) {
            databaseReference.removeEventListener(valueEventListener);
        }
    }

    @Override
    public void onCreate() {
        populateVideoList();
    }

    @Override
    public void onDataSetChanged() {
        populateVideoList();
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        return videoDetailList.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {

        final RemoteViews remoteView = new RemoteViews(
                context.getPackageName(), R.layout.widget_layout);

        String videoTitle = videoDetailList.get(i).title;
        String author = videoDetailList.get(i).author;
        long views = videoDetailList.get(i).views;

        PrettyTime p = new PrettyTime();
        long timestamp = 0;
        if (videoDetailList.get(i).getTimestamp() != 0) {
            timestamp = videoDetailList.get(i).getTimestamp();
        }

        remoteView.setTextViewText(R.id.tv_title, videoTitle);
        remoteView.setTextViewText(R.id.tv_author, author);
        remoteView.setTextViewText(R.id.tv_views, String.valueOf(views) + context.getString(R.string.total_views));
        remoteView.setTextViewText(R.id.tv_upload_time, p.format(new Date(timestamp)) + " • ");

        Intent intent = new Intent();
        intent.putExtra(VideoDetailActivity.EXTRA_VIDEO_DETAIL, videoDetailList.get(i));
        intent.putExtra(VideoDetailActivity.EXTRA_POST_KEY, videoKeys.get(i));
        remoteView.setOnClickFillInIntent(R.id.item_main_layout, intent);

        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


}
