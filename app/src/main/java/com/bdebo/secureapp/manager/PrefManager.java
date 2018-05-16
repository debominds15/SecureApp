package com.bdebo.secureapp.manager;

/**
 * Created by M1032607 on 6/30/2017.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Lincoln on 05/05/16.
 */
public class PrefManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // shared pref mode
    int PRIVATE_MODE = 0;
    private String TAG = PrefManager.class.getSimpleName();

    // Shared preferences file name
    private static final String PREF_NAME = "androidhive-welcome";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        Log.d(TAG,"setFirstTimeLaunch():: "+isFirstTime);
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, false);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        Log.d(TAG,"setFirstTimeLaunch():: "+pref.getBoolean(IS_FIRST_TIME_LAUNCH, true));
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

}