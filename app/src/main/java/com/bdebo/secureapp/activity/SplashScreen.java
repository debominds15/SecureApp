package com.bdebo.secureapp.activity;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bdebo.secureapp.receiver.MyAdmin;
import com.bdebo.secureapp.R;
import com.bdebo.secureapp.util.SecureUtil;

/**
 * This class is used to show the splash screen
 * and decides which activity to be launched next
 */
public class SplashScreen extends Activity {

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mComponentName;
    private static final int ADMIN_INTENT = 15;
    private int progressStatus = 0;
    boolean adminPermission = false;
    private Handler handler = new Handler();
    private SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mComponentName = new ComponentName(this, MyAdmin.class);
        setContentView(R.layout.splash_screen);
        showProgressDialog();
    }

    /**
     * This method is used to show the progress dialog
     */
    public void showProgressDialog()
    {
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.splash_screen_progressbar);

        new Thread(new Runnable() {
            public void run() {
                while (progressStatus < 100) {
                    progressStatus += 1;
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus);
                        }
                    });
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (progressStatus == 100) {
                    prefs= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    boolean isFirstTimeLaunch = prefs.getBoolean("isFirstTimeLaunch",true);
                       if(!isFirstTimeLaunch){
                           launchLoginScreen();
                        finish();
                    }
                    else{
                        prefs.edit().putBoolean("isFirstTimeLaunch",false).commit();
                        startActivity(new Intent(SplashScreen.this, WelcomeActivity.class));
                        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                        finish();
                    }
                }
            }
        }).start();
    }

    /**
     * Launch Login Screen
     */
    private void launchLoginScreen() {
        startActivity(new Intent(SplashScreen.this, Login.class));
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RESULT_OK){
            adminPermission=true;
            callToastMessage("Registered As Admin");
        }else{
            callToastMessage("Failed to register as Admin");
        }
    }
    /**
     * Display Toast message
     * @param msg
     */
    private void callToastMessage(String msg){
        LayoutInflater inflater = getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.custom_toast_layout));
        SecureUtil.setToastMessage(this,msg,toastLayout);
    }

}