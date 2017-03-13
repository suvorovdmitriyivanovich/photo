package com.photo;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

public class Photo extends Application {
    private static Photo mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized Photo getInstance() {
        return mInstance;
    }

    public static void exit(Activity sender) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sender.startActivity(intent);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}