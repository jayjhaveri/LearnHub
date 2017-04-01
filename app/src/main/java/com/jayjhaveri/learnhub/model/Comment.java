package com.jayjhaveri.learnhub.model;

import com.google.firebase.database.IgnoreExtraProperties;

// [START comment_class]
@IgnoreExtraProperties
public class Comment {

    public String uid;
    public String author;
    public String text;
    public String imageUrl;

    public Comment() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    public Comment(String uid, String author, String text, String imageUrl) {
        this.uid = uid;
        this.author = author;
        this.text = text;
        this.imageUrl = imageUrl;
    }

}
// [END comment_class]
