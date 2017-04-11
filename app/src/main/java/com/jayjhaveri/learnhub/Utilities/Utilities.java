package com.jayjhaveri.learnhub.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jayjhaveri.learnhub.R;
import com.jayjhaveri.learnhub.model.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ADMIN-PC on 18-03-2017.
 */

public class Utilities {

    public static void writeStringPreference(Context context ,String key, String value){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.utility) ,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String readStringPreference(Context context, String key){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.utility),
                Context.MODE_PRIVATE);
        return sharedPref.getString(key,"no_uri");
    }

    public static void deleteStringPreference(Context context, String key){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.utility),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(key);
        editor.commit();
    }

    public static void putDataToUsers(final User user) {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(user.uid)) {
                    Map<String, Object> userValues = user.toMap();

                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/users/" + user.uid, userValues);

                    databaseReference.updateChildren(childUpdates);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public static User getLoginUser(FirebaseAuth auth) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        User user = new User(firebaseUser.getEmail(),
                firebaseUser.getDisplayName(),
                firebaseUser.getPhotoUrl().toString(),
                firebaseUser.getUid());

        return user;
    }

    public static DatabaseReference getUserVideosRef() {
        return FirebaseDatabase.getInstance().getReference().child("user-videos");
    }

    public static DatabaseReference getCategoryVideosRef() {
        return FirebaseDatabase.getInstance().getReference().child("categories");
    }

    public static DatabaseReference getVideosRef() {
        return FirebaseDatabase.getInstance().getReference().child("videos");
    }

    public interface RemoveItemList {
        public void removeItem(int position);
    }
}
