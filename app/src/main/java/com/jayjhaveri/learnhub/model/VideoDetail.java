package com.jayjhaveri.learnhub.model;

import android.os.Parcel;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ADMIN-PC on 15-03-2017.
 */

public class VideoDetail implements Serializable {

    public String uid;
    public String author;
    public String category;
    public String title;
    public String body;
    public String profileImage;
    public int likeCount = 0;
    public int disLikeCount = 0;
    public long views = 0;
    public String videoUrl;
    public String imageUrl;
    public String fileUrl;
    public String duration;
    public Map<String, Object> timestamp;
    public Map<String, Boolean> likes = new HashMap<>();
    public Map<String, Boolean> disLikes = new HashMap<>();

    public VideoDetail() {
    }

    public VideoDetail(String uid, String author,
                       String category,
                       String title, String body,
                       String profileImage,
                       String videoUrl, String imageUrl,
                       String fileUrl,
                       String duration) {
        this.uid = uid;
        this.author = author;
        this.category = category;
        this.title = title;
        this.body = body;
        this.profileImage = profileImage;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.fileUrl = fileUrl;
        this.duration = duration;

        //Date last changed will always be set to ServerValue.TIMESTAMP
        HashMap<String, Object> timestamp = new HashMap<String, Object>();
        timestamp.put("date", ServerValue.TIMESTAMP);

        this.timestamp = timestamp;
    }


    protected VideoDetail(Parcel in) {
        uid = in.readString();
        author = in.readString();
        category = in.readString();
        title = in.readString();
        body = in.readString();
        profileImage = in.readString();
        likeCount = in.readInt();
        disLikeCount = in.readInt();
        views = in.readInt();
        videoUrl = in.readString();
        imageUrl = in.readString();
        fileUrl = in.readString();
    }



    @Exclude
    public Map<String,Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("category", category);
        result.put("title", title);
        result.put("body", body);
        result.put("profileImage", profileImage);
        result.put("likeCount", likeCount);
        result.put("disLikeCount", disLikeCount);
        result.put("likes", likes);
        result.put("disLikes", disLikes);
        result.put("views", views);
        result.put("videoUrl", videoUrl);
        result.put("imageUrl", imageUrl);
        result.put("fileUrl", fileUrl);
        result.put("duration", duration);
        result.put("timestamp", timestamp);
        return result;
    }

    @Exclude
    public Map<String, Object> toEditMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("category", category);
        result.put("title", title);
        result.put("body", body);
        return result;
    }

    @Exclude
    public Long getTimestamp() {
        if (timestamp.containsKey("date")) {
            return (Long) timestamp.get("date");
        }

        return null;
    }


}
