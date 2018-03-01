package com.mukeshteckwani.crowdfire.wardrobe.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mukeshteckwani.crowdfire.wardrobe.App;

/**
 * Created by mukeshteckwani on 31/01/18.
 */

class PreferenceHelper {
    static final String CAMERA_FILNAME = "CAMERA_FILENAME";

    static void addString(String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(key, value);
        editor.apply();
    }

    private static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance());
    }

    public static String getString(String key) {
        return getSharedPreferences().getString(key, null);
    }
}
