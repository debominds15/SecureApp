package com.bdebo.secureapp;



import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.bdebo.secureapp.activity.Login;

/**
 * This application class is used to hold all application related settings.
 */

public class SecureAppApplication extends Application implements Application.ActivityLifecycleCallbacks{

    private static int started;
    private static int stopped;
    private static String TAG = SecureAppApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    /**
     * checks whether application is visible to user
     *
     * @return boolean
     */
   public static boolean isApplicationVisible() {
       return started > stopped;
   }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        ++started;
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        ++stopped;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public static void logout(Context context){
        Intent intent = new Intent(context,Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
