package com.bdebo.secureapp.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.bdebo.secureapp.activity.PatternLockAuthorization;
import com.bdebo.secureapp.activity.PinPasswordLockAuthorization;
import com.bdebo.secureapp.model.App;
import com.bdebo.secureapp.model.AppAlreadyAuthorised;
import com.bdebo.secureapp.receiver.StateReceiver;
import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.util.SecureUtil;

import java.util.ArrayList;

public class WindowChangeDetectingService extends AccessibilityService {

    private static final String TAG = WindowChangeDetectingService.class.getSimpleName();
    private SecureUtil util;
    private ArrayList<AppAlreadyAuthorised> getAllAppsForAuthorization;
    private ArrayList<App> getAllApps;
    private AppAlreadyAuthorised appAlreadyAuthorised = new AppAlreadyAuthorised();
    private StateReceiver mScreenStateReceiver;
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        mScreenStateReceiver = new StateReceiver();
        util = new SecureUtil(this);
        //Configure these here for compatibility with API 13 and below.
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        if (Build.VERSION.SDK_INT >= 16)
            //Just in case this helps
            config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        setServiceInfo(config);

        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenStateReceiver, screenStateFilter);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        showAppLockScreen();
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null && event.getClassName() != null) {
                ComponentName componentName = new ComponentName(
                        event.getPackageName().toString(),
                        event.getClassName().toString()
                );
                ActivityInfo activityInfo = tryGetActivity(componentName);
                boolean isActivity = activityInfo != null;
                if (isActivity && getAllAppsForAuthorization != null)
                {
                    String recentPkg = event.getPackageName().toString();
                    for (int j = 0; j < getAllAppsForAuthorization.size(); j++) {
                        Log.i(TAG, "condition0 is done!!! ");
                           //  Log.i(TAG, "Foreground App is: " + recentPkg + " to be authorized " + getAllAppsForAuthorization.get(j).getAppPackName() + " isAuthorized::" + authoriseAppsList.get(j).isAppAuthorised() + " authoriseAppsList.get(j).isAppOpenThroughSecureApp()::" + authoriseAppsList.get(j).isAppOpenThroughSecureApp() + " authoriseAppsList.get(j).isAppAlreadyOpen()::" + authoriseAppsList.get(j).isAppOpenAlready());
                        if (recentPkg.equals(getAllAppsForAuthorization.get(j).getAppPackName()) && !getAllAppsForAuthorization.get(j).isAppAuthorised() && !getAllAppsForAuthorization.get(j).isAppOpenThroughSecureApp()) {
                            Log.i(TAG, "condition1 is done!!! ");
                            getAppDetailsFromPackName(recentPkg);
                            break;
                        } else if (recentPkg.equals(getAllAppsForAuthorization.get(j).getAppPackName()) && getAllAppsForAuthorization.get(j).isAppOpenThroughSecureApp()) {
                            appAlreadyAuthorised = getAllAppsForAuthorization.get(j);
                            break;
                        }
                    }
                    //Update the isAppOpenThroughSecureApp as false right after the current app goes from foreground
                    //to launch Lock screen next time if not authenticated before
                    if (appAlreadyAuthorised != null && !recentPkg.equals(appAlreadyAuthorised.getAppPackName()) && appAlreadyAuthorised.isAppOpenThroughSecureApp()) {
                        updateAppAuthorizedAppOpenThroughSecureApp(appAlreadyAuthorised.getAppPackName(), false);
                    }
                    Log.i("CurrentActivity", componentName.flattenToShortString()+" package name: "+event.getPackageName());
                }

            }
        }
    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Override
    public void onInterrupt() {}

    /**
     * Check foreground app and show lock screen
     */
    private  void showAppLockScreen(){
        getAllAppsForAuthorization = util.getAllAppsAuthorisationInformation();
        getAllApps = util.getAllAppsFromDB();
    }

    /**
     * Update the authorization details
     * if app is opened through SecureApp then
     * open App securely without asking pin/password
     * and update the details as false
     * so that next time if the app is open from outside
     * the lock screen appears
     * @param appPackName
     * @param isAppOpenedThroughSecureApp
     */
    private void updateAppAuthorizedAppOpenThroughSecureApp(String appPackName,boolean isAppOpenedThroughSecureApp){
        ArrayList<AppAlreadyAuthorised> appAlreadyAuthorisedArrayList = getAllAppsForAuthorization;
        for (int i=0;i<appAlreadyAuthorisedArrayList.size();i++){

            if(appAlreadyAuthorisedArrayList.get(i).getAppPackName().equals(appPackName) && !isAppOpenedThroughSecureApp){
                AppAlreadyAuthorised appAlreadyAuthorised = appAlreadyAuthorisedArrayList.get(i);
                appAlreadyAuthorised.setAppOpenThroughSecureApp(false);
                util.updateAppAuthorisationRecords(appAlreadyAuthorised, appAlreadyAuthorisedArrayList.get(i).getId());
            }
        }
        appAlreadyAuthorised = null;
    }


    /**
     * Fetch App details from package name
     * and launch Lock screen accordingly.
     * @param appPackName
     */
    private void getAppDetailsFromPackName(String appPackName) {
        App app;
        if (appPackName != null) {
            app = getAppDetailsFromAppPackageName(appPackName);
            Log.i(TAG, "condition2 is done!!! ");
            Intent intent;
            if (app.getAppLockType() != null) {
                switch (app.getAppLockType()) {

                    case "PIN":
                        intent = new Intent(this, PinPasswordLockAuthorization.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(AppConstant.APP_PACKAGE_NAME, appPackName);
                        intent.putExtra(AppConstant.PIN_OR_PASSWORD, app.getAppLockType());
                        intent.putExtra(AppConstant.LOCK_DATA, app.getPass());
                        startActivity(intent);
                        break;

                    case "Password":
                        intent = new Intent(this, PinPasswordLockAuthorization.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(AppConstant.APP_PACKAGE_NAME, appPackName);
                        intent.putExtra(AppConstant.PIN_OR_PASSWORD, app.getAppLockType());
                        intent.putExtra(AppConstant.LOCK_DATA, app.getPass());
                        startActivity(intent);
                        break;

                    case "Pattern":
                        intent = new Intent(this, PatternLockAuthorization.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(AppConstant.APP_PACKAGE_NAME, appPackName);
                        intent.putExtra(AppConstant.LOCK_DATA, app.getPass());
                        startActivity(intent);
                        break;
                }
            }
        }
    }

    private App getAppDetailsFromAppPackageName(String packageName){
        App app = new App();
        if(getAllApps != null) {
            for (int i = 0; i < getAllApps.size(); i++) {
                if (packageName.equals(getAllApps.get(i).getAppPackName())) {
                    app = getAllApps.get(i);
                    break;
                }
            }
        }
        return app;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mScreenStateReceiver);
    }
}