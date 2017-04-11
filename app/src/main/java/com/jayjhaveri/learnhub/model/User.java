package com.jayjhaveri.learnhub.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ADMIN-PC on 04-04-2017.
 */

public class User {

    public String email;
    public String name;
    public String profileImage;
    public String uid;
    public Map<String, Long> bookmarks = new HashMap<>();
    public Map<String, Long> likes = new HashMap<>();
    public Map<String, Long> dislikes = new HashMap<>();

    public User() {
        //Default constructor for firebase
    }

    public User(String email, String name, String profileImage, String uid) {
        this.email = email;
        this.name = name;
        this.profileImage = profileImage;
        this.uid = uid;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("email", email);
        result.put("name", name);
        result.put("profileImage", profileImage);
        result.put("uid", uid);
        result.put("bookmarks", bookmarks);
        result.put("likes", likes);
        result.put("dislikes", dislikes);

        return result;
    }
}
