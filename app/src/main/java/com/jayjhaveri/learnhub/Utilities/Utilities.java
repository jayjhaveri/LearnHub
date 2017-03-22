package com.jayjhaveri.learnhub.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.jayjhaveri.learnhub.R;

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
}
