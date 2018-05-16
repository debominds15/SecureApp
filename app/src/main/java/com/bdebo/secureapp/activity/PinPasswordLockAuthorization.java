package com.bdebo.secureapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bdebo.secureapp.provider.DBHelper;
import com.bdebo.secureapp.R;
import com.bdebo.secureapp.model.AppAlreadyAuthorised;
import com.bdebo.secureapp.model.AppLockScreen;
import com.bdebo.secureapp.util.AnimUtil;
import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.util.SecureUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * This class is used to show the App Pin Lock Screen
 */
public class PinPasswordLockAuthorization extends AppCompatActivity {
    private String TAG=PinPasswordLockAuthorization.class.getSimpleName();
    private Button cancelPIN,continuePIN,authenticatePIN;
    private EditText edtAppPIN;
    private TextInputLayout edtTextInputLayout;
    private String appPatternType="";
    private TextView txtWrongPinPasswordLock;
    private LinearLayout linearLayout;
    private ImageView imageAppIcon;
    private String appPackName = "";
    private DBHelper dbHandler;
    private int LOCK_SECUREAPP_VERIFICATION = 2001;
    private static int ALL_THEME_IMAGES=1004;
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
        setContentView(R.layout.pin_password_lock_authorization);
        fullScreenCall();
        init();
        continuePIN.setEnabled(false);
        final Intent intent=getIntent();
        continuePIN.setVisibility(View.GONE);
        authenticatePIN.setVisibility(View.VISIBLE);
        edtAppPIN.setTextColor(Color.WHITE);
        if(intent!=null) {
            appPatternType = intent.getStringExtra(AppConstant.PIN_OR_PASSWORD);
            appPackName = intent.getStringExtra(AppConstant.APP_PACKAGE_NAME);
        }

        int position = util.getAppLockScreenDetails().getPos();
        Drawable themeImage = getResources().getDrawable(themeImages[position]);
        linearLayout.setBackground(themeImage);

        Drawable imageAppIconDrawable = getAppImageIcon(appPackName);
        if (imageAppIconDrawable != null) {
            imageAppIcon.setImageDrawable(imageAppIconDrawable);
        }

        if(appPatternType.equals("Password")) {
            edtTextInputLayout.setHint("Enter Password");
            edtAppPIN.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            edtAppPIN.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
        edtAppPIN.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(edtAppPIN.getText().toString().length()>=4)
                    authenticatePIN.setEnabled(true);
                else
                    authenticatePIN.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        edtAppPIN.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() > 0)
                txtWrongPinPasswordLock.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        authenticatePIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String appPassword = "";
                if(intent != null){
                    appPassword  = intent.getStringExtra(AppConstant.LOCK_DATA);
                }
                if(!appPassword.equals(edtAppPIN.getText().toString())){
                    txtWrongPinPasswordLock.setVisibility(View.VISIBLE);
                    edtAppPIN.setText("");
                    if(appPatternType.equals("Password")){
                        txtWrongPinPasswordLock.setText("Wrong Password");
                        txtWrongPinPasswordLock.postDelayed(new Runnable() {
                            public void run() {
                                txtWrongPinPasswordLock.setVisibility(View.GONE);
                            }
                        }, 3000);
                    }
                }
                else{
                    txtWrongPinPasswordLock.setVisibility(View.GONE);
                    patternAuthenticated();
                }

            }
        });

        cancelPIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        linearLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                SecureUtil.hideSoftKeyboard(PinPasswordLockAuthorization.this);
                return false;
            }
        });

    }

    /**
     * Initializes the views
     */
    private void init(){
        cancelPIN=(Button) this.findViewById(R.id.btnCancelAppPIN);
        continuePIN=(Button) this.findViewById(R.id.btnAppPINContinue);
        authenticatePIN=(Button) this.findViewById(R.id.btnAppPINAuthenticate);
        edtAppPIN=(EditText) this.findViewById(R.id.edtAppPIN);
        edtTextInputLayout = (TextInputLayout) findViewById(R.id.txtInputLayoutAppPinOrPassword);
        linearLayout = (LinearLayout) findViewById(R.id.appLockScreenTheme);
        menuRelativeLayout = (RelativeLayout) findViewById(R.id.relativeMenuIcon);
        dbHandler = new DBHelper(getApplicationContext());
        txtWrongPinPasswordLock = (TextView) findViewById(R.id.txtWrongPinPasswordLock);
        imageAppIcon = (ImageView) findViewById(R.id.imgAppIcon);
        util = new SecureUtil(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG,"less than lollipop");
            menuRelativeLayout.setVisibility(View.GONE);
        }
    }

    /**
     * This method is used to update authorization for App in DB
     * @param appPackName
     */
    private void updateAuthorizationInDB(String appPackName){
        SecureUtil util = new SecureUtil(this);
        ArrayList<AppAlreadyAuthorised> getAllAppsForAuthorization = util.getAllAppsAuthorisationInformation();
        for(int j=0;j<getAllAppsForAuthorization.size();j++) {
            if (appPackName.equals(getAllAppsForAuthorization.get(j).getAppPackName()) && !getAllAppsForAuthorization.get(j).isAppAuthorised()) {
                getAllAppsForAuthorization.get(j).setIsAppAuthorised(true);
                util.updateAppAuthorisationRecords(getAllAppsForAuthorization.get(j), getAllAppsForAuthorization.get(j).getId());
            }
        }
        dbHandler.close();
    }

    /**
     * This method is used to update and finish the current activity after authorization
     */
    private void patternAuthenticated(){
        updateAuthorizationInDB(appPackName);
        callToastMessage("Correct Password");
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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
    protected void onPause() {
        super.onPause();
        if(isMenuItemSelected){
            cancelPIN.setVisibility(View.GONE);
            authenticatePIN.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isMenuItemSelected){
            cancelPIN.setVisibility(View.VISIBLE);
            authenticatePIN.setVisibility(View.VISIBLE);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AnimUtil.setFadeAnimation(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null) {
            if (requestCode == ALL_THEME_IMAGES) {
                Log.d(TAG,"onActivityResult pos:");
                Bundle extras = data.getExtras();
                int position = extras.getInt(AppConstant.IMAGE_NO);
                Log.d(TAG,"onActivityResult pos:"+position);
                Drawable themeImage = getResources().getDrawable(themeImages[position]);
                linearLayout.setBackground(themeImage);

                AppLockScreen appLockScreen = new AppLockScreen();
                appLockScreen.setId(1);
                appLockScreen.setPos(position);
                util.updateParticularThemeImageForAppLockScreen(appLockScreen,1);

                finish();
            }

            else if(requestCode == LOCK_SECUREAPP_VERIFICATION){
                Bundle extras = data.getExtras();
                boolean  isPasswordCorrect = extras.getBoolean(AppConstant.IS_SECURE_APP_PASS_CORRECT);
                if(isPasswordCorrect)
                    patternAuthenticated();
                else {
                    boolean isCancelled = extras.getBoolean(AppConstant.IS_CANCEL);
                    if (isCancelled){
                        edtAppPIN.setEnabled(true);
                    }
                    else {
                        callToastMessage("Incorrect Password");
                        edtAppPIN.setEnabled(true);

                    }
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!isMenuItemSelected) {
            Log.d(TAG,"onStop() going to finish...");
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
     * Display Toast message
     * @param msg
     */
    private void callToastMessage(String msg){
        LayoutInflater inflater = getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.custom_toast_layout));
        SecureUtil.setToastMessage(this,msg,toastLayout);
    }


    /**
     * This method is used to set full screen for App Lock Screen
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
            popup.getMenu().findItem(R.id.item_action_lock_invisible_pattern).setVisible(false);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {

                    int id = item.getItemId();
                    switch (id) {

                        case R.id.item_action_lock_change_theme:
                            Log.d(TAG, "item_action_lock_change_theme");
                            isMenuItemSelected = true;
                            Intent intent = new Intent(PinPasswordLockAuthorization.this, AllThemesLockScreenActivity.class);
                            startActivityForResult(intent, ALL_THEME_IMAGES);
                            return true;

                        case R.id.item_action_lock_forgot_pattern:
                            isMenuItemSelected = true;
                            Intent forgotPassIntent = new Intent(PinPasswordLockAuthorization.this, ForgotPasswordLockActivity.class);
                            startActivityForResult(forgotPassIntent, LOCK_SECUREAPP_VERIFICATION);
                            return true;

                        default:
                            return false;
                    }
                }
            });
            popup.show();
        }
}
