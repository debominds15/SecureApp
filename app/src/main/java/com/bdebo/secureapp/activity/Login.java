package com.bdebo.secureapp.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.receiver.MyAdmin;
import com.bdebo.secureapp.R;
import com.bdebo.secureapp.model.User;
import com.bdebo.secureapp.util.SecureUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class is used to show the Login Screen for the SecureApp
 */
public class Login extends AppCompatActivity {

    private String TAG = Login.class.getSimpleName();
    private Button login;
    private Button signup, forgotPassword;
    private TextView wrong, firstUsage;
    private EditText user, pass;
    private Toolbar toolbar;
    private ArrayList<User> userlist;
    private SharedPreferences prefs;
    private RelativeLayout layoutLogin;
    private CheckBox check;
    private boolean userPresent = false;
    private boolean savedlogin, isSignUpDone;
    private int cnt = 0, inc = 0;
    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mComponentName;
    private static final int ADMIN_INTENT = 15;
    private ImageView imageTouchId;
    private SecureUtil util;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 5;
    public final static int PERM_REQUEST_CODE_DRAW_OVERLAYS = 1234;
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    private boolean isDenySelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //startBackgroundService();
        setContentView(R.layout.activity_login);
        init();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int id = getIntent().getIntExtra(NOTIFICATION_ID, -1);
        manager.cancel(id);

        boolean isPasswordRemembered = prefs.getBoolean(AppConstant.IS_REMEMBER_PASSWORD_CHECKED, false);
        Log.d(TAG, "onCreate():: isPasswordRemembered::" + isPasswordRemembered + " notificationId::" + id);
        if (isFingerPrintSupported(this))
            imageTouchId.setVisibility(View.VISIBLE);
        if (!isSignUpDone) {
            login.setEnabled(false);
            forgotPassword.setEnabled(false);
            firstUsage.setVisibility(View.VISIBLE);
            firstUsage.setText(R.string.label_text_first_time);
            firstUsage.setSelected(true);
        } else {
            login.setEnabled(true);
            forgotPassword.setEnabled(true);
            signup.setVisibility(View.GONE);
            firstUsage.setVisibility(View.GONE);
        }

        inc = prefs.getInt("count", 0);
        savedlogin = prefs.getBoolean(AppConstant.CHECK_SAVED_LOGIN, false);
        if (savedlogin == true) {
            user.setText(prefs.getString(AppConstant.USER_NAME, ""));
            if (isPasswordRemembered)
                pass.setText(prefs.getString(AppConstant.PASSWORD, ""));
        }
        imageTouchId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFingerPrintEnrolled(Login.this)) {

                    if (isSignUpDone) {
                        Intent intent = new Intent(getApplicationContext(), WelcomeTouchIdActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    } else {
                        callToastMessage("Please create your account first");
                    }
                } else {
                    showAddFingerprintAlertDialog(getResources().getString(R.string.dialog_add_fingerprint_title), getResources().getString(R.string.dialog_add_fingerprint_message), Login.this, true);
                }

            }
        });
        signup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
            }
        });

        layoutLogin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                SecureUtil.hideSoftKeyboard(Login.this);
                return false;
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityOptions options = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    options = ActivityOptions.makeSceneTransitionAnimation(Login.this);
                }
                Intent i = new Intent(Login.this, ForgotPassword.class);
                startActivity(i, options.toBundle());
               /* Intent intent = new Intent(Login.this, ForgotPassword.class);
                startActivity(intent);*/
                //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (user.getText().toString().equals(""))
                    user.setError("Username cannot be empty");
                else if (pass.getText().toString().equals(""))
                    pass.setError("Password cannot be empty");
                else {
                    if (check.isChecked()) {
                        prefs.edit().putBoolean(AppConstant.CHECK_SAVED_LOGIN, true).commit();
                        prefs.edit().putString(AppConstant.USER_NAME, user.getText().toString()).commit();
                        boolean isPasswordRemembered = prefs.getBoolean(AppConstant.IS_REMEMBER_PASSWORD_CHECKED, false);
                        if (isPasswordRemembered) {
                            prefs.edit().putString(AppConstant.PASSWORD, pass.getText().toString()).commit();
                        }
                    }

                    Iterator<User> itr = userlist.iterator();
                    while (itr.hasNext()) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(pass.getWindowToken(), 0);
                        User u = itr.next();

                        if (user.getText().toString().equals(u.getUsername()) && pass.getText().toString().equals(u.getPass())) {
                            userPresent = true;
                            Gson gson = new Gson();
                            String json = gson.toJson(u); // myObject - instance of MyObject
                            prefs.edit().putString(AppConstant.USER_OBJ, json).commit();
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            prefs.edit().putString(AppConstant.USER_NAME, user.getText().toString()).commit();
                            prefs.edit().putString(AppConstant.NAME, u.getName()).commit();
                            prefs.edit().putInt(AppConstant.USER_ID, u.getId()).commit();
                            startActivity(i);
                            overridePendingTransition(R.anim.right_enter, R.anim.left_out);

                            LayoutInflater inflater = getLayoutInflater();
                            View toastLayout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.custom_toast_layout));
                            SecureUtil.setToastMessage(Login.this, "Successfully Login", toastLayout);

                            onRestart();
                            break;
                        }
                    }
                    if (userPresent == false) {
                        wrong.setVisibility(View.VISIBLE);
                        cnt++;
                        int attemptsLeft = 3 - cnt;
                        if (attemptsLeft > 1 && attemptsLeft < 3)
                            wrong.setText(attemptsLeft + " Attempts left");
                        else if (attemptsLeft == 1) {
                            wrong.setText("Last Attempt left");
                        } else if (attemptsLeft == 0) {
                            lockDevice();
                        }
                    }
                }
            }

        });
    }

    /**
     * Setting up Animation while returning
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupWindowReturnAnimations() {

        // Re-enter transition is executed when returning back to this activity
        Slide slideTransition = new Slide();
        slideTransition.setSlideEdge(Gravity.LEFT); // Use START if using right - to - left locale
        slideTransition.setDuration(1000);

        getWindow().setReenterTransition(slideTransition);  // When MainActivity Re-enter the Screen
        getWindow().setExitTransition(slideTransition);     // When MainActivity Exits the Screen

        // For overlap of Re Entering Activity - MainActivity.java and Exiting TransitionActivity.java
        getWindow().setAllowReturnTransitionOverlap(false);
    }

    /**
     * Initialize the views
     */
    private void init() {
        util = new SecureUtil(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isSignUpDone = prefs.getBoolean(AppConstant.signUpDonePref, false);
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        imageTouchId = (ImageView) findViewById(R.id.touchIdImage);
        mComponentName = new ComponentName(this, MyAdmin.class);
        userlist = util.getAllUsers();
        signup = (Button) findViewById(R.id.btnSignup);
        login = (Button) findViewById(R.id.Login);
        forgotPassword = (Button) findViewById(R.id.btnForgotPassword);
        user = (EditText) findViewById(R.id.editUser);
        check = (CheckBox) findViewById(R.id.checkBox1);
        pass = (EditText) findViewById(R.id.editPass);
        wrong = (TextView) findViewById(R.id.textIncorrectAttempt);
        firstUsage = (TextView) findViewById(R.id.textFirstUsage);
        layoutLogin = (RelativeLayout) findViewById(R.id.layout_login);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isSignUpDone = prefs.getBoolean(AppConstant.signUpDonePref, false);
        util = new SecureUtil(this);
        userlist = util.getAllUsers();
        if (isSignUpDone) {
            login.setEnabled(true);
            signup.setVisibility(View.GONE);
            firstUsage.setVisibility(View.GONE);
        }

       /* if(isDenySelected){
            checkPermissionREAD_EXTERNAL_STORAGE(this);

        }*/

        hideKeyBoard();
        if (wrong != null && wrong.getVisibility() == View.VISIBLE) {
            wrong.setVisibility(View.GONE);
        }
    }


    /*
     * This method is used to hide the keyboard
     */
    public void hideKeyBoard() {
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    }

    /**
     * This method is used to lock the device if admin permission is given
     */
    public void lockDevice() {
        boolean isAdmin = mDevicePolicyManager.isAdminActive(mComponentName);
        if (isAdmin) {
            mDevicePolicyManager.lockNow();
            finish();
        } else {
            showAlert("Would you like to enable advance security ? Device will lock automatically if someone enters three wrong credentials.");
        }
    }

    /**
     * This method is used to enable the admin permission
     */
    public void enableAdmin() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "SecureApp needs to lock the device if more than 3 unauthorised attempts are made while login.");
        startActivityForResult(intent, ADMIN_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ADMIN_INTENT) {
                callToastMessage("Registered As Admin");
                Intent intent = new Intent(this, Login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
         /*   else  if (requestCode == PERM_REQUEST_CODE_DRAW_OVERLAYS) {
                if (android.os.Build.VERSION.SDK_INT >= 23) {   //Android M Or Over
                    if (!Settings.canDrawOverlays(this)) {
                        // ADD UI FOR USER TO KNOW THAT UI for SYSTEM_ALERT_WINDOW permission was not granted earlier...
                        Log.d(TAG,"onActivityResult():: PERM_REQUEST_CODE_DRAW_OVERLAYS");
                    }
                }
            }*/

            else {
                finish();
                callToastMessage("Failed to register as Admin");
            }
        } else {
            finish();
        }
    }

    /**
     * This method is used to start the background service required for App Lock screen
     *//*
    private void startBackgroundService(){
        final Intent newIntent = new Intent(this, LockMonitorService.class);
        newIntent.setAction(LockMonitorService.ACTION_CHECK_LOCK);
        startService(newIntent);
    }


    /**
     * This method is used to show the alert for Enabling Advance Security
     * @param instr
     */
    public void showAlert(String instr) {

        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(R.string.label_advanced_security).setMessage(instr);
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.cancel();
                finish();
            }
        });
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                callToastMessage("Please activate device administrator permission for SecureApp");
                enableAdmin();
            }
        });
        // Create the AlertDialog object and return it
        alertDialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    finish();
                }
                return true;
            }
        });
        alertDialog.show();
    }

    /**
     * This method is used to check if the fingerprint is supported
     *
     * @param context
     * @return
     */
    public static boolean isFingerPrintSupported(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Fingerprint API only available on from Android 6.0 (M)
            FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED) {
                if (fingerprintManager.isHardwareDetected()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method is used to check if the fingerprint is enrolled in the device
     *
     * @param context
     * @return
     */
    private static boolean isFingerPrintEnrolled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Fingerprint API only available on from Android 6.0 (M)
            FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED) {
                if (fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method is used to show the add fingerprint dialog
     *
     * @param title
     * @param message
     * @param context
     * @param isCallFromLogin
     */
    private void showAddFingerprintAlertDialog(String title, String message, final Context context, final boolean isCallFromLogin) {
        int positiveButtonString;
        if (!isCallFromLogin)
            positiveButtonString = R.string.action_continue;
        else
            positiveButtonString = R.string.action_exit;

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton(positiveButtonString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        if (!isCallFromLogin) {
                            Intent i = new Intent(context, Login.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            context.startActivity(i);
                            finish();
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_action_add_fp, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                        context.startActivity(intent);
                        finish();
                    }
                })
        ;

        // Create the AlertDialog object and return it
        builder.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    finish();
                }
                return true;
            }
        });

        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login_menu, menu);
        boolean isPasswordRemembered = prefs.getBoolean(AppConstant.IS_REMEMBER_PASSWORD_CHECKED, false);
        MenuItem item = menu.findItem(R.id.item_action_login_remember_password);
        if (isPasswordRemembered)
            item.setChecked(isPasswordRemembered);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.item_action_login_remember_password:
                if (item.isChecked()) {
                    prefs.edit().putBoolean(AppConstant.IS_REMEMBER_PASSWORD_CHECKED, false).commit();
                    item.setChecked(false);
                } else {
                    prefs.edit().putBoolean(AppConstant.IS_REMEMBER_PASSWORD_CHECKED, true).commit();
                    item.setChecked(true);
                }
                return true;
            default:
                return false;
        }
    }

    /**
     * This method is used to check the permission for external storage
     *
     * @param context
     * @return
     */
    public boolean checkPermissionREAD_EXTERNAL_STORAGE(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            //permissionToDrawOverlays();
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    /*showDialog("External storage",context,
                            Manifest.permission.READ_EXTERNAL_STORAGE);
*/
                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }

                isDenySelected = true;
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                  /*  Toast.makeText(Login.this, "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();*/
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    /**
     * Permission to draw Overlays/On Other Apps, related to 'android.permission.SYSTEM_ALERT_WINDOW' in Manifest
     * Resolves issue of popup in Android M and above "Screen overlay detected- To change this permission setting you first have to turn off the screen overlay from Settings > Apps"
     * If app has not been granted permission to draw on the screen, create an Intent &
     * set its destination to Settings.ACTION_MANAGE_OVERLAY_PERMISSION &
     * add a URI in the form of "package:<package name>" to send users directly to your app's page.
     * Note: Alternative Ignore URI to send user to the full list of apps.
     */
    public void permissionToDrawOverlays() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {   //Android M Or Over
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, PERM_REQUEST_CODE_DRAW_OVERLAYS);
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
}