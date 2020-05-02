package com.nearcompany.db;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Emre on 4/26/2020.
 */

public class Preferences {

    private SharedPreferences pref;

    public Preferences(Context pContext, String userID) {
        pref = pContext.getSharedPreferences(userID, 1);
    }

    public String getName() {
        String name = pref.getString("name", "");
        return name;
    }

    public void setName(String name) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("name", name);
        editor.apply();
        editor.commit();
    }

    public String getLanguage() {
        String language = pref.getString("language", "");
        return language;
    }

    public void setLanguage(String language) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("language", language);
        editor.apply();
        editor.commit();
    }
}
