package com.bdebo.secureapp.activity;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.amnix.materiallockview.MaterialLockView;
import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import com.bdebo.secureapp.provider.DBHelper;
import com.bdebo.secureapp.R;
import com.bdebo.secureapp.model.User;
import com.bdebo.secureapp.model.Device;
import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.util.SecureUtil;
import com.google.gson.Gson;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.List;

/**
 * This class is used to show the Device Lock Screen
 */
public class DeviceLockScreenActivity extends AppCompatActivity {

    private static final String TAG = DeviceLockScreenActivity.class.getSimpleName();
    LinearLayout linearLayoutDevicePIN,layoutBtnsPasswordVerification;
    private MaterialLockView materialLockView;
    private DBHelper dbHelper;
    private SharedPreferences prefs;
    private User user;
    private String patternData;
    final Handler handler = new Handler();
    private long waitTimeMillis = 200;
    private Device device;
    private LinearLayout layout;
    private PinLockView mPinLockView;
    private IndicatorDots mIndicatorDots;
    private SecureUtil util;
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_lock_authorisation);
        fullScreenCall();
        init();
        Gson gson = new Gson();
        String json=prefs.getString(AppConstant.USER_OBJ,null);
        util = new SecureUtil(this);
        user = gson.fromJson(json, User.class);
        device = util.getDeviceLockDetails(user.getId());
        int imagePosition = device.getImagePosition();
        Drawable themeImage = getResources().getDrawable(themeImages[imagePosition]);
        layout.setBackground(themeImage);
        SQLiteDatabase.loadLibs(DeviceLockScreenActivity.this);
        dbHelper.getWritableDatabase("mypass123");
        checkPatternOrPassword();
    }

    /**
     * This method is used to return the index of button
     * @param intermediatePin
     * @return
     */
    private int getIndexOfButton(String intermediatePin) {
        String lastIndexChar = intermediatePin.substring(intermediatePin.length() - 1);
        int index = Integer.parseInt(lastIndexChar);
        if(index == 0) {
            index = 11;
        }
        return index;
    }

    /**
     * This method is used to show the Pattern Screen
     */
    private void showPatternScreen(){
        boolean isInvisiblePatternSet = prefs.getBoolean(AppConstant.IS_INVISIBLE_PATTERN_SET,true);
        materialLockView.setPatternInvisible(DeviceLockScreenActivity.this,isInvisiblePatternSet);
        materialLockView.setOnPatternListener(new MaterialLockView.OnPatternListener() {
            @Override
            public void onPatternDetected(List<MaterialLockView.Cell> pattern, String SimplePattern) {
                patternData = SimplePattern;
                String password = device.getPassword();
                if (!SimplePattern.equals(password)) {
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
     * This method is used to show the Pin Lock Screen
     */
    private void showPinLockScreen(){
        //attach lock view with dot indicator
        mPinLockView.attachIndicatorDots(mIndicatorDots);
        Drawable d = getResources().getDrawable(R.drawable.water_drop);
        mPinLockView.setButtonBackgroundDrawable(d);
        //set lock code length
        mPinLockView.setPinLength(device.getPassword().length());
        //set listener for lock code change
        mPinLockView.setPinLockListener(new PinLockListener() {
            @Override
            public void onComplete(String pin) {
                String password = device.getPassword();
                int index = getIndexOfButton(pin);
                didTapButton(mPinLockView.getChildAt(index-1));
             /*   MediaPlayer mp = MediaPlayer.create(DeviceLockScreenActivity.this,R.raw.water_sound);
                mp.start();
             */   if (pin.equals(password)) {
                    finish();
                } else {
                    mPinLockView.resetPinLockView();
                    callToastMessage("Failed , try again!");
                }
            }

            @Override
            public void onEmpty() {
            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {
                int index = getIndexOfButton(intermediatePin);
                didTapButton(mPinLockView.getChildAt(index-1));
                /*MediaPlayer mp = MediaPlayer.create(DeviceLockScreenActivity.this,R.raw.water_sound);
                mp.start();*/
            }
        });
    }

    /**
     * This method id used to check lock type Pattern or Password and show the screen accordingly
     */
    private void checkPatternOrPassword() {

            if(device.getLockType().equals("Pattern")){
                materialLockView.setVisibility(View.VISIBLE);
                linearLayoutDevicePIN.setVisibility(View.GONE);
                layoutBtnsPasswordVerification.setVisibility(View.GONE);
                showPatternScreen();
            }
            else{
                materialLockView.setVisibility(View.GONE);
                linearLayoutDevicePIN.setVisibility(View.VISIBLE);
                showPinLockScreen();
            }
    }

    /**
     * Initialize the views
     */
    private void init(){
        materialLockView = (MaterialLockView) findViewById(R.id.patternDeviceAuthentication);
        linearLayoutDevicePIN = (LinearLayout) findViewById(R.id.linearLayoutDevicePIN);
        layoutBtnsPasswordVerification = (LinearLayout) findViewById(R.id.layoutBtnsPasswordVerification);
        layout = (LinearLayout) findViewById(R.id.appLockScreenTheme);
        mPinLockView = (PinLockView) findViewById(R.id.pin_lock_view);
        mIndicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);
        //set lock code length
        mPinLockView.setPinLength(6);
        prefs= PreferenceManager.getDefaultSharedPreferences(this);
        dbHelper= new DBHelper(this);
    }

    /**
     * This method is used to show the animation and finish the current activity once the pattern is authenticated
     */
    private void patternAuthenticated(){
        callToastMessage("Correct Pattern");
        finish();
        DeviceLockScreenActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
     * This method is used to show the full lock screen and hide navigation
     */
    public void fullScreenCall() {

        getWindow().getDecorView().setSystemUiVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    /**
     * This method is used to show animation of bounce after each button is pressed in lock screen
     * @param view
     */
    public void didTapButton(View view) {
        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        view.startAnimation(myAnim);
    }

}
