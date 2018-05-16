package com.bdebo.secureapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bdebo.secureapp.activity.DeviceLockScreenActivity;
import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.util.SecureUtil;

/**
 * This is Broadcast Receiver class
 * used to trigger whenever the device is unlocked
 */
public class StateReceiver extends BroadcastReceiver {

    private static final String TAG = StateReceiver.class.getSimpleName();

    /**
     * Shared preference instance
     */
    private SharedPreferences prefs;
    @Override
    public void onReceive(Context context, Intent intent) {
        prefs= PreferenceManager.getDefaultSharedPreferences(context);
        Log.d(TAG, "onReceive()");
        if(intent.getAction().equals(Intent.ACTION_SCREEN_ON))
        {
            Log.d(TAG, "screen_on");
            prefs.edit().putBoolean("is_screen_on",true).commit();
            boolean isDeviceLockPasswordEnabled = prefs.getBoolean(AppConstant.IS_DEVICE_LOCK_ENABLED, false);

            //  Handle resuming events
            if(isDeviceLockPasswordEnabled) {
                Log.d(TAG,"isDeviceLockPasswordEnabled::"+isDeviceLockPasswordEnabled);
                Intent intent1 = new Intent(context, DeviceLockScreenActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent1);
            }
        }
        else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
            Log.d(TAG, "screen_off");
            prefs.edit().putBoolean("is_screen_on",false).commit();
            SecureUtil secureUtil = new SecureUtil(context);
            secureUtil.assignAllAppsAuthorisationFalse();
            Log.d(TAG, "Successfully updated!! while screen off");
        }
        /*else {
            final Intent newIntent = new Intent(context, LockMonitorService.class);
            newIntent.setAction(LockMonitorService.ACTION_CHECK_LOCK);
            newIntent.putExtra(LockMonitorService.EXTRA_STATE, intent.getAction());
            context.startService(newIntent);
            if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
                prefs.edit().putBoolean("is_screen_on",false).commit();
            }
        }*/
    }
}