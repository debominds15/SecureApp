package com.bdebo.secureapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.amnix.materiallockview.MaterialLockView;
import com.bdebo.secureapp.R;
import com.bdebo.secureapp.model.AppAlreadyAuthorised;
import com.bdebo.secureapp.model.AppLockScreen;
import com.bdebo.secureapp.util.AnimUtil;
import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.util.SecureUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to set the App Pattern Lock Screen
 */
public class PatternLockAuthorization extends AppCompatActivity {
    private String TAG = PatternLockAuthorization.class.getSimpleName();
    private MaterialLockView materialLockView;
    private ImageView imageAppIcon;
    private LinearLayout linearLayout;
    private final Handler handler = new Handler();
    private long waitTimeMillis = 200;
    private String appPassword = "";
    private String appPackName = "";
    private int LOCK_SECUREAPP_VERIFICATION = 2001;
    private static int ALL_THEME_IMAGES=1004;
    private SharedPreferences prefs;
    private boolean isMenuItemSelected = false;
    private SecureUtil util;
    private RelativeLayout menuRelativeLayout;

    private Integer[] themeImages = {
            R.drawable.my_wall,
            R.drawable.my_wall_1,
            R.drawable.my_wall2,
            R.drawable.my_wall3,
            R.drawable.my_wall4,
            R.drawable.my_wall5,
            R.drawable.my_wall6,
            R.drawable.my_wall7,
            R.drawable.my_wall8,
            R.drawable.my_wall9,
            R.drawable.my_wall10,
            R.drawable.my_wall11,
            R.drawable.my_wall12,
            R.drawable.my_wall13,
            R.drawable.my_wall14,
            R.drawable.my_wall15,
            R.drawable.my_wall16,
            R.drawable.my_wall17,
            R.drawable.my_wall18,
            R.drawable.my_wall19,
            R.drawable.my_wall20,
            R.drawable.my_wall21,
            R.drawable.my_wall22,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "condition4 is done!!! ");
        PatternLockAuthorization.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.pattern_lock_authorization);
        linearLayout = (LinearLayout) findViewById(R.id.appLockScreenTheme);
        prefs= PreferenceManager.getDefaultSharedPreferences(this);
        fullScreenCall();
        Intent intent = getIntent();
        if (intent != null) {
            appPassword = intent.getStringExtra(AppConstant.LOCK_DATA);
            appPackName = intent.getStringExtra(AppConstant.APP_PACKAGE_NAME);
        }

        util = new SecureUtil(this);
        materialLockView = (MaterialLockView) findViewById(R.id.pattern);
        imageAppIcon = (ImageView) findViewById(R.id.imgAppIcon);
        menuRelativeLayout = (RelativeLayout) findViewById(R.id.relativeMenuIcon);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG,"less than lollipop");
            menuRelativeLayout.setVisibility(View.GONE);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AnimUtil.setFadeAnimation(this);
        }

        int position = util.getAppLockScreenDetails().getPos();
        Drawable themeImage = getResources().getDrawable(themeImages[position]);
        linearLayout.setBackground(themeImage);


        Drawable imageAppIconDrawable = getAppImageIcon(appPackName);
        if (imageAppIconDrawable != null) {
            imageAppIcon.setImageDrawable(imageAppIconDrawable);
        }
        Log.i(TAG, "condition5 is done!!! ");
        boolean isPatternVisible = prefs.getBoolean(AppConstant.IS_PATTERN_INVISIBLE,false);
        if(!isPatternVisible)
        materialLockView.setPatternInvisible(this,false);
        materialLockView.setOnPatternListener(new MaterialLockView.OnPatternListener() {
            @Override
            public void onPatternDetected(List<MaterialLockView.Cell> pattern, String SimplePattern) {
                if (!SimplePattern.equals(appPassword)) {
                    materialLockView.setDisplayMode(com.amnix.materiallockview.MaterialLockView.DisplayMode.Wrong);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 100ms
                            materialLockView.clearPattern(); // clear the drawn pattern
                        }
                    }, waitTimeMillis);
                } else {
                    patternAuthenticated();
                }

                super.onPatternDetected(pattern, SimplePattern);
            }
        });

    }

    /**
     * This method is used to update the authorization details of app in DB
     * and finish the activity
     */
    private void patternAuthenticated(){
        updateAuthorizationInDB(appPackName);
        callToastMessage("Correct Pattern");
        finish();
        //PatternLockAuthorization.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }

    /**
     * This method is used to update the authorization details in DB
     * @param appPackName
     */
    private void updateAuthorizationInDB(String appPackName) {

        SecureUtil util = new SecureUtil(this);
        ArrayList<AppAlreadyAuthorised> getAllAppsForAuthorization = util.getAllAppsAuthorisationInformation();

        for (int j = 0; j < getAllAppsForAuthorization.size(); j++) {
            if (appPackName.equals(getAllAppsForAuthorization.get(j).getAppPackName()) && !getAllAppsForAuthorization.get(j).isAppAuthorised()) {
                getAllAppsForAuthorization.get(j).setIsAppAuthorised(true);
                util.updateAppAuthorisationRecords(getAllAppsForAuthorization.get(j), getAllAppsForAuthorization.get(j).getId());
            }
        }

    }

    /**
     * Method to get the drawable image icon based on package name
     * @param appPackName
     * @return
     */
    private Drawable getAppImageIcon(String appPackName) {
        Drawable icon = null;
        final List<ApplicationInfo> apps = getPackageManager().getInstalledApplications(0);
        for (int i = 0; i < apps.size(); i++) {
            if ((apps.get(i).flags & ApplicationInfo.FLAG_SYSTEM) != 1 && apps.get(i).packageName.equals(appPackName)) {
                //System app
                try {
                    icon = getPackageManager().getApplicationIcon(apps.get(i).packageName);
                    break;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

            } else {
                if ((apps.get(i).flags & ApplicationInfo.FLAG_SYSTEM) == 1 && apps.get(i).packageName.equals(appPackName)) {
                    try {

                        icon = getPackageManager().getApplicationIcon(apps.get(i).packageName);
                    }
                    catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return icon;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.app_lock_menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null) {
            if (requestCode == ALL_THEME_IMAGES) {
                Bundle extras = data.getExtras();
                int position = extras.getInt(AppConstant.IMAGE_NO);
                Drawable themeImage = getResources().getDrawable(themeImages[position]);
                linearLayout.setBackground(themeImage);

                AppLockScreen appLockScreen = new AppLockScreen();
                appLockScreen.setId(1);
                appLockScreen.setPos(position);
                util.updateParticularThemeImageForAppLockScreen(appLockScreen,1);
            }

            else if(requestCode == LOCK_SECUREAPP_VERIFICATION){
                Bundle extras = data.getExtras();
                boolean  isPasswordCorrect = extras.getBoolean(AppConstant.IS_SECURE_APP_PASS_CORRECT);
                if(isPasswordCorrect)
                    patternAuthenticated();
                else {
                    boolean  isCancel = extras.getBoolean(AppConstant.IS_CANCEL,false);
                    if (!isCancel)
                        callToastMessage("Incorrect Password");
                }
            }


        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(!isMenuItemSelected) {
            //updateAppAlreadyOpened();
            finish();
        }
    }

    /**
     * Update the authorization details
     * if app lock screen is already opened
     * and update the appOpenAlready as false
     * so that next time it should not launch app lock screen again
     */
    private void updateAppAlreadyOpened(){
        ArrayList<AppAlreadyAuthorised> appAlreadyAuthorisedArrayList = util.getAllAppsAuthorisationInformation();
        for (int i=0;i<appAlreadyAuthorisedArrayList.size();i++){

            if(appAlreadyAuthorisedArrayList.get(i).getAppPackName().equals(appPackName)){
                AppAlreadyAuthorised appAlreadyAuthorised = appAlreadyAuthorisedArrayList.get(i);
                util.updateAppAuthorisationRecords(appAlreadyAuthorised, appAlreadyAuthorisedArrayList.get(i).getId());
            }
        }
    }

    /**
     * This method is used to set the full screen for App Lock Screen
     */
    public void fullScreenCall() {

        getWindow().getDecorView().setSystemUiVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /**
     * This method will be invoked if any action is triggered from menu
     * of App Lock Screen
     * @param v
     */
    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.app_lock_menu, popup.getMenu());
        popup.setGravity(Gravity.TOP | Gravity.RIGHT);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Menu m = popup.getMenu();
            m.findItem(R.id.item_action_lock_change_theme).setVisible(false);
            m.findItem(R.id.item_action_lock_invisible_pattern).setVisible(false);
            m.findItem(R.id.item_action_lock_forgot_pattern).setVisible(false);
        }
        boolean isPatternVisible = prefs.getBoolean(AppConstant.IS_PATTERN_INVISIBLE,true);
        materialLockView.setPatternInvisible(PatternLockAuthorization.this,!isPatternVisible);
        popup.getMenu().findItem(R.id.item_action_lock_invisible_pattern).setChecked(isPatternVisible);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                             public boolean onMenuItemClick(MenuItem item) {

                                                 int id = item.getItemId();
                                                 Log.d(TAG, "id: "+id);
                                                 switch (id) {

                                                     case R.id.item_action_lock_change_theme:
                                                         isMenuItemSelected = true;
                                                         Intent intent=new Intent(PatternLockAuthorization.this,AllThemesLockScreenActivity.class);
                                                         startActivityForResult(intent,ALL_THEME_IMAGES);
                                                         return true;
                                                     case R.id.item_action_lock_forgot_pattern:
                                                         isMenuItemSelected = true;
                                                         Intent forgotPassIntent = new Intent(PatternLockAuthorization.this,ForgotPasswordLockActivity.class);
                                                         startActivityForResult(forgotPassIntent,LOCK_SECUREAPP_VERIFICATION);
                                                         return true;
                                                     case R.id.item_action_lock_invisible_pattern:
                                                         item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                                                         item.setActionView(new View(PatternLockAuthorization.this));
                                                         if(item.isChecked()){
                                                             callToastMessage("Invisible Pattern");
                                                             prefs.edit().putBoolean(AppConstant.IS_PATTERN_INVISIBLE,false).commit();
                                                             materialLockView.setPatternInvisible(PatternLockAuthorization.this,true);
                                                             item.setChecked(true);
                                                         }
                                                         else{
                                                             callToastMessage("Visible Pattern");
                                                             prefs.edit().putBoolean(AppConstant.IS_PATTERN_INVISIBLE,true).commit();
                                                             materialLockView.setPatternInvisible(PatternLockAuthorization.this,false);
                                                             item.setChecked(false);
                                                         }
                                                         return true;

                                                     default:
                                                         return false;
                                                 }

                                             }
                                         });
        popup.show();
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
