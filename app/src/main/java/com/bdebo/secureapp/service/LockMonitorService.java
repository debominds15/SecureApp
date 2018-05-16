/*
package com.bdebo.secureapp.service;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bdebo.secureapp.activity.PatternLockAuthorization;
import com.bdebo.secureapp.activity.PinPasswordLockAuthorization;
import com.bdebo.secureapp.model.App;
import com.bdebo.secureapp.model.AppAlreadyAuthorised;
import com.bdebo.secureapp.receiver.StateReceiver;
import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.util.SecureUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


*/
/**
 * This service class is used to check the current app running in background
 * if the app is secured through SecureApp
 * then lock screen would be shown
 * and update the authorization details in DB
 *//*

public class LockMonitorService extends Service {

    private static final String TAG = LockMonitorService.class.getSimpleName();

    public static final String ACTION_CHECK_LOCK = "com.dwx331409.secureapp.service.LockMonitorService.ACTION_CHECK_LOCK";
    public static final String EXTRA_CHECK_LOCK_DELAY_INDEX = "com.dwx331409.secureapp.service.LockMonitorService.EXTRA_CHECK_LOCK_DELAY_INDEX";
    public static final String EXTRA_STATE = "com.dwx331409.secureapp.service.LockMonitorService.EXTRA_STATE";

    private ScheduledExecutorService scheduler;
    private BroadcastReceiver receiver = null;
    private static final Timer timer = new Timer();
    private CheckLockTask checkLockTask = null;
    private Context mContext;
    private ArrayList<AppAlreadyAuthorised> getAllAppsForAuthorization;
    private ArrayList<App> getAllApps;
    boolean isShuttingDown = false;
    private AppAlreadyAuthorised appAlreadyAuthorised = new AppAlreadyAuthorised();
    private SecureUtil util;
    private SharedPreferences mPrefs;
    private boolean isThirdPartyAppOpened;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mContext= getBaseContext();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        util = new SecureUtil(this);
        checkLock();

        */
/*if (intent != null && intent.getAction() == ACTION_CHECK_LOCK) {
            Log.d(TAG, "if condition is met action_check_lock");

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                checkLock();
            else {
                scheduleMethod();
            }
        }
        else{
            checkLock();
            Log.d(TAG, "intent: "+intent);
        }
        *//*


        if (receiver == null) {
            // Unlike other broad casted intents, for these you CANNOT declare them in the Android Manifest;
            // instead they must be registered in an IntentFilter.
            receiver = new StateReceiver();
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_USER_PRESENT);
            registerReceiver(receiver, filter);
        }

        return START_STICKY;
    }

    */
/**
     * Check the lock status and accordingly take action
     *//*

    private void checkLock(){
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        final boolean isProtected = keyguardManager.isKeyguardSecure();
        final boolean isLocked = keyguardManager.inKeyguardRestrictedInputMode();
        if (checkLockTask != null) {
            checkLockTask.cancel();
        }

        int delayIndex = 0;
        final boolean isInteractive;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT_WATCH) {
            isInteractive = powerManager.isInteractive();
            if (isProtected && !isLocked && !isInteractive) {
                checkLockTask = new CheckLockTask(this, delayIndex);
                timer.schedule(checkLockTask, checkLockDelays[delayIndex]);

            } else {
                isShuttingDown = false;
               // scheduleMethod();
                if (isProtected && isLocked) {
                    isShuttingDown = true;
                    assignAllAppsAuthorisationFalse();
                    util.close();
                    scheduler.shutdown();
                    Log.d(TAG, "locked device");
                }
            }
        } else {

            if (isLocked) {
                checkLockTask = new CheckLockTask(this, delayIndex);
                timer.schedule(checkLockTask, checkLockDelays[delayIndex]);
                    isShuttingDown = true;
                    assignAllAppsAuthorisationFalse();
                    util.close();
                    scheduler.shutdown();
                    Log.d(TAG, "locked device");

            } else {
                Log.d(TAG, "abt to go to scheduleMethod()...");
                isShuttingDown = false;
                scheduleMethod();
            }
        }
    }

    static final int SECOND = 1000;
    static final int MINUTE = 60 * SECOND;
    // This tracks the deltas between the actual options of 5s, 15s, 30s, 1m, 2m, 5m, 10m
    // It also includes an initial offset and some extra times (for safety)
    static final int[] checkLockDelays = new int[] { 1*SECOND, 5*SECOND, 10*SECOND, 20*SECOND, 30*SECOND, 1*MINUTE, 3*MINUTE, 5*MINUTE, 10*MINUTE, 30*MINUTE };
    static int getSafeCheckLockDelay(final int delayIndex) {
        final int safeDelayIndex;
        if (delayIndex >= checkLockDelays.length) {
            safeDelayIndex = checkLockDelays.length - 1;
        } else if (delayIndex < 0) {
            safeDelayIndex = 0;
        } else {
            safeDelayIndex = delayIndex;
        }
        return safeDelayIndex;
    }

    */
/**
     * Scheduler to check current app running in foreground
     * and show App lock screen if required
     *//*

    private void scheduleMethod() {
        scheduler = Executors
                .newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                if(!isShuttingDown)
                showAppLockScreen();

            }
        }, 0, 100, TimeUnit.NANOSECONDS);
    }

    */
/**
     * TimerTask class
     *//*

    class CheckLockTask extends TimerTask {
        final int delayIndex;
        final Context context;
        CheckLockTask(final Context context, final int delayIndex) {
            this.context = context;
            this.delayIndex = delayIndex;
        }
        @Override
        public void run() {
            Log.i(TAG, String.format("CLT.run [%x]: redirect intent to LockMonitorService", System.identityHashCode(this)));
            final Intent newIntent = new Intent(context, LockMonitorService.class);
            newIntent.setAction(ACTION_CHECK_LOCK);
            newIntent.putExtra(EXTRA_CHECK_LOCK_DELAY_INDEX, getSafeCheckLockDelay(delayIndex + 1));
            context.startService(newIntent);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    */
/**
     * Check foreground app and show lock screen
     *//*

    private  void showAppLockScreen(){
        getAllAppsForAuthorization = util.getAllAppsAuthorisationInformation();
        getAllApps = util.getAllAppsFromDB();
        Log.d(TAG,"showAppLockScreen():: getAllAppsForAuthorization::"+getAllAppsForAuthorization.get(0).isAppOpenThroughSecureApp()+" "+getAllAppsForAuthorization.get(0).getAppPackName());
        if(getAllAppsForAuthorization != null) {
            getForegroundAppOnDevice(getAllAppsForAuthorization);
        }
    }

    */
/**
     * Check foreground app running
     *//*

    private void getForegroundAppOnDevice(ArrayList<AppAlreadyAuthorised> authoriseAppsList){
        ActivityManager mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    String recentPkg="";
                    long timeGap = 0;
                    UsageStatsManager usm = (UsageStatsManager)this.getSystemService(Context.USAGE_STATS_SERVICE);
                    long time = System.currentTimeMillis();
                    List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
                    if (appList != null && appList.size() > 0) {
                        SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                        for (UsageStats usageStats : appList) {
                            mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                        }
                        if (mySortedMap != null && !mySortedMap.isEmpty()) {
                            recentPkg = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                            timeGap = System.currentTimeMillis() - mySortedMap.get(mySortedMap.lastKey()).getLastTimeUsed();
                        }
                    }
                   Log.i(TAG, "Foreground App is: "+recentPkg+" time gap::"+String.valueOf(timeGap));

                    for (int j = 0; j < authoriseAppsList.size(); j++) {
                        //      Log.i(TAG, "Foreground App is: " + recentPkg + " to be authorized " + authoriseAppsList.get(j).getAppPackName() + " isAuthorized::" + authoriseAppsList.get(j).isAppAuthorised() + " authoriseAppsList.get(j).isAppOpenThroughSecureApp()::" + authoriseAppsList.get(j).isAppOpenThroughSecureApp() + " authoriseAppsList.get(j).isAppAlreadyOpen()::" + authoriseAppsList.get(j).isAppOpenAlready());
                        //Log.i(TAG, "Foreground App is: "+recentPkg+" isThirdPartyAppOpened::"+isThirdPartyAppOpened);
                        if (recentPkg.equals(authoriseAppsList.get(j).getAppPackName()) && !authoriseAppsList.get(j).isAppAuthorised() && !authoriseAppsList.get(j).isAppOpenThroughSecureApp() && !authoriseAppsList.get(j).isAppOpenAlready()) {
                            //        Log.i(TAG, "Foreground App is: " + recentPkg + " to be authorized " + authoriseAppsList.get(j).getAppName() + " isAuthorized::" + authoriseAppsList.get(j).isAppAuthorised());
                            getAppDetailsFromPackName(recentPkg);
                            updateAppAlreadyOpened(recentPkg, true);
                            break;
                        } else if (recentPkg.equals(authoriseAppsList.get(j).getAppPackName()) && authoriseAppsList.get(j).isAppOpenThroughSecureApp()) {
                            appAlreadyAuthorised = authoriseAppsList.get(j);
                            break;
                        }
                    }
                    //Update the isAppOpenThroughSecureApp as false right after the current app goes from foreground
                    //to launch Lock screen next time if not authenticated before
                    if (appAlreadyAuthorised != null && !recentPkg.equals(appAlreadyAuthorised.getAppPackName()) && appAlreadyAuthorised.isAppOpenThroughSecureApp()) {
                        mPrefs.edit().putBoolean(AppConstant.IS_THIRD_PARTY_APP_OPENED_SECURELY, false).commit();
                        updateAppAuthorizedAppOpenThroughSecureApp(appAlreadyAuthorised.getAppPackName(), false);
                    }

            }
            else {
                    String recentPkg = mActivityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
                    //Log.i(TAG, "Foreground App is: "+recentPkg);
                    for(int j=0;j<authoriseAppsList.size();j++) {

                        isThirdPartyAppOpened = mPrefs.getBoolean(AppConstant.IS_THIRD_PARTY_APP_OPENED_SECURELY,false);

                        if ((recentPkg.equals(authoriseAppsList.get(j).getAppPackName())) && !authoriseAppsList.get(j).isAppAuthorised() && !authoriseAppsList.get(j).isAppOpenThroughSecureApp() && !isThirdPartyAppOpened){
                            getAppDetailsFromPackName(recentPkg);
                            appAlreadyAuthorised = authoriseAppsList.get(j);
                        }
                        else if((recentPkg.equals(authoriseAppsList.get(j).getAppPackName()) ) && authoriseAppsList.get(j).isAppOpenThroughSecureApp() && isThirdPartyAppOpened){
                            appAlreadyAuthorised = authoriseAppsList.get(j);
                            break;
                        }
                    }
                    //Update the isAppOpenThroughSecureApp as false right after the current app goes from foreground
                    //to launch Lock screen next time if not authenticated before
                    if(appAlreadyAuthorised != null && !recentPkg.equals(appAlreadyAuthorised.getAppPackName()) && appAlreadyAuthorised.isAppOpenThroughSecureApp()){
                        mPrefs.edit().putBoolean(AppConstant.IS_THIRD_PARTY_APP_OPENED_SECURELY,false).commit();
                        updateAppAuthorizedAppOpenThroughSecureApp(appAlreadyAuthorised.getAppPackName(),false);
                    }

            }
        } catch (Exception e) {
            Log.e(TAG,e.getLocalizedMessage());
        }
    }

    */
/**
     * Assign all the apps authorization details as false
     * if the app is locked
     *//*

    private void assignAllAppsAuthorisationFalse(){

        if(getAllAppsForAuthorization != null) {
            for (int i = 0; i < getAllAppsForAuthorization.size(); i++) {
                AppAlreadyAuthorised appAlreadyAuthorised = new AppAlreadyAuthorised();
                appAlreadyAuthorised.setAppName(getAllAppsForAuthorization.get(i).getAppName());
                appAlreadyAuthorised.setAppPackName(getAllAppsForAuthorization.get(i).getAppPackName());
                appAlreadyAuthorised.setId(getAllAppsForAuthorization.get(i).getId());
                appAlreadyAuthorised.setIsAppAuthorised(false);
                appAlreadyAuthorised.setAppOpenThroughSecureApp(false);
                appAlreadyAuthorised.setAppOpenAlready(false);
                util.updateAppAuthorisationRecords(appAlreadyAuthorised, getAllAppsForAuthorization.get(i).getId());
            }
        }
    }
    */
/**
     * Update the authorization details
     * if app is opened through SecureApp then
     * open App securely without asking pin/password
     * and update the details as false
     * so that next time if the app is open from outside
     * the lock screen appears
     * @param appPackName
     * @param isAppOpenedThroughSecureApp
     *//*

    private void updateAppAuthorizedAppOpenThroughSecureApp(String appPackName,boolean isAppOpenedThroughSecureApp){
        ArrayList<AppAlreadyAuthorised> appAlreadyAuthorisedArrayList = getAllAppsForAuthorization;
        for (int i=0;i<appAlreadyAuthorisedArrayList.size();i++){

            if(appAlreadyAuthorisedArrayList.get(i).getAppPackName().equals(appPackName) && !isAppOpenedThroughSecureApp){
                AppAlreadyAuthorised appAlreadyAuthorised = appAlreadyAuthorisedArrayList.get(i);
                appAlreadyAuthorised.setAppOpenThroughSecureApp(false);
                appAlreadyAuthorised.setAppOpenAlready(false);
                util.updateAppAuthorisationRecords(appAlreadyAuthorised, appAlreadyAuthorisedArrayList.get(i).getId());
            }
        }
        appAlreadyAuthorised = null;
    }


    */
/**
     * Fetch App details from package name
     * and launch Lock screen accordingly.
     * @param appPackName
     *//*

    private void getAppDetailsFromPackName(String appPackName) {
        App app;
        if (appPackName != null) {
            app = getAppDetailsFromAppPackageName(appPackName);
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (receiver != null) {
            unregisterReceiver(receiver);
            util.close();
            receiver = null;
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

    */
/**
     * Update the authorization details
     * if app lock screen is already opened
     * and update the appOpenAlready as false
     * so that next time it should not launch app lock screen again
     * @param appPackName
     *//*

    private void updateAppAlreadyOpened(String appPackName, boolean isAppOpenAlready){
        ArrayList<AppAlreadyAuthorised> appAlreadyAuthorisedArrayList = getAllAppsForAuthorization;
        for (int i=0;i<appAlreadyAuthorisedArrayList.size();i++){

            if(appAlreadyAuthorisedArrayList.get(i).getAppPackName().equals(appPackName)){
                AppAlreadyAuthorised appAlreadyAuthorised = appAlreadyAuthorisedArrayList.get(i);
                appAlreadyAuthorised.setAppOpenAlready(isAppOpenAlready);
                util.updateAppAuthorisationRecords(appAlreadyAuthorised, appAlreadyAuthorisedArrayList.get(i).getId());
            }
        }
        appAlreadyAuthorised = null;
    }

    private boolean isAppOnForeground(String appPackname) {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            Log.d(TAG,"isAppOnForeground():: null");
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                Log.d(TAG,"isAppOnForeground():: processName::"+appProcess.processName+" appPackname::"+appPackname);
                if(appPackname.equals(appProcess.processName))
                return true;
            }
        }
        return false;
    }

}
*/
