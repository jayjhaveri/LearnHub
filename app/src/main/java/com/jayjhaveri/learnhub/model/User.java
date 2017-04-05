package com.jayjhaveri.learnhub.model;

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
    public Map<String, Boolean> bookmarks = new HashMap<>();
    public Map<String, Boolean> likes = new HashMap<>();
    public Map<String, Boolean> dislikes = new HashMap<>();

    public User() {
        //Default constructor for firebase
    }

    public User(String email, String name, String profileImage, String uid) {
        this.email = email;
        this.name = name;
        this.profileImage = profileImage;
        this.uid = uid;
    }
}
