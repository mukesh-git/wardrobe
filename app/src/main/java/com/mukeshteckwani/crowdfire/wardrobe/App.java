package com.mukeshteckwani.crowdfire.wardrobe;

import android.app.Application;

/**
 * Created by mukeshteckwani on 31/01/18.
 */

public class App extends Application {
    private static App mThis;

    @Override public void onCreate() {
        super.onCreate();
        mThis = this;
    }

    public static App getInstance() {
        return mThis;
    }
}
